package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.database.ChatRepository;
import com.cca.ia.rag.chat.database.ConversationContextRepository;
import com.cca.ia.rag.chat.database.ConversationRepository;
import com.cca.ia.rag.chat.database.DynamicRepository;
import com.cca.ia.rag.chat.model.ChatDto;
import com.cca.ia.rag.chat.model.ChatEntity;
import com.cca.ia.rag.chat.model.ConversationEntity;
import com.cca.ia.rag.chat.model.ConversationPropertiesEntity;
import com.cca.ia.rag.collection.CollectionService;
import com.cca.ia.rag.collection.database.CollectionRepository;
import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.collection.model.CollectionPropertyDto;
import com.cca.ia.rag.collection.model.CollectionPropertyEntity;
import com.cca.ia.rag.config.security.UserUtils;
import com.cca.ia.rag.document.DocumentUtils;
import com.cca.ia.rag.document.embedding.EmbeddingService;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChatServiceDefault implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private DynamicRepository dynamicRepository;

    @Autowired
    private ConversationContextRepository conversationContextRepository;

    @Autowired
    private CollectionService collectionService;

    private ChatClient getChatClient(Long collectionId) {

        List<CollectionPropertyDto> data = collectionService.findProperties(collectionId);

        String apiKey = data.stream().filter(e -> e.getKey().equals("apiKey")).findFirst().orElseThrow().getValue();

        return new OpenAiChatClient(new OpenAiApi(apiKey));

    }

    private int isCodeQuestion(Long collectionId, String question) {

        Message assistantMessage = new AssistantMessage("Eres un Agente Decisor que decide si una pregunta de un usuario hace referencia con el código fuente del proyecto. "
                + "Debes tener en cuenta que código fuente del proyecto está escrito en Java, Spring boot o Angular, así que busca palabras clave en la pregunta del usuario. "
                + "La respuesta debe ser el porcentaje de fiabilidad (entre 0 y 100) de que la pregunta se refiere a código fuente. Siendo 100 una fiabilidad del 100% y 0 una fiabilidad del 0%. Responde solamente con un número entero. \n");
        UserMessage userMessage = new UserMessage("La pregunta de usuario es la siguiente: " + question);

        Prompt prompt = new Prompt(List.of(assistantMessage, userMessage), OpenAiChatOptions.builder().withModel("gpt-3.5-turbo-0125").withTemperature(0.1f).build());

        ChatResponse chatResponse = getChatClient(collectionId).call(prompt);
        String response = chatResponse.getResult().getOutput().getContent().trim();

        try {
            return Integer.parseInt(response);
        } catch (Exception e) {
            return 50;
        }
    }

    class QuestionParsed {
        private String question;
        private boolean noAutocontext;

        private boolean onlyDoc;

        private boolean onlyCode;

        private List<String> files;

        private boolean query;

        public QuestionParsed(String question) {
            this.question = question;
            this.noAutocontext = extractTag("@noAutocontext");
            this.onlyDoc = extractTag("@onlyDoc");
            this.onlyCode = extractTag("@onlyCode");
            this.query = extractTag("@query");
            this.files = extractFiles();
        }

        private List<String> extractFiles() {
            List<String> files = new ArrayList<>();
            String tag = "@file(";
            int startIndex = 0;

            do {
                startIndex = question.toLowerCase().indexOf(tag.toLowerCase());

                if (startIndex >= 0) {

                    int endIndex = question.indexOf(")", startIndex);
                    if (endIndex < 0) {
                        endIndex = question.length() - 1;
                    }

                    files.add(question.substring(startIndex + tag.length(), endIndex));
                    question = question.substring(0, startIndex) + question.substring(endIndex + 1);
                }
            } while (startIndex >= 0);

            return files;
        }

        private boolean extractTag(String tag) {

            int index = 0;
            boolean foundTag = false;

            do {
                index = question.toLowerCase().indexOf(tag.toLowerCase());

                if (index >= 0) {
                    foundTag = true;
                    question = question.substring(0, index) + question.substring(index + tag.length() + 1);
                }
            } while (index >= 0);

            return foundTag;
        }

    }

    private void generateSystemMessage(CollectionEntity collection, QuestionParsed question, long spentTokens, MessageWithDocuments messageWithDocuments) {
        long maxTokens = 10000;
        long maxTokensForSearch = maxTokens - spentTokens;

        if (question.files != null && question.files.size() > 0) {

            for (String filename : question.files) {

                filename = filename.replaceAll("\\*", "%");

                List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentDocumentCollectionIdAndDocumentFilenameLike(collection.getId(), filename);

                for (DocumentChunkEntity documentChunk : documentChunks) {

                    long tokens = DocumentUtils.countTokens(documentChunk.getContent());
                    maxTokensForSearch -= tokens;

                    if (maxTokensForSearch < 0)
                        return;

                    messageWithDocuments.addDocument(documentChunk, documentChunk.getContent());
                }
            }
        }

        if (question.noAutocontext) {
            return;
        }

        if (maxTokensForSearch > 0) {
            int codeQuestion = 0;

            if (question.onlyDoc) {
                codeQuestion = 0;
            } else if (question.onlyCode) {
                codeQuestion = 100;
            } else {
                codeQuestion = isCodeQuestion(collection.getId(), question.question);
            }

            List<Document> documentVectorDB = embeddingService.findSimilarity(collection, question.question, codeQuestion, maxTokensForSearch);
            List<String> embeddings = documentVectorDB.stream().map(entry -> entry.getId()).collect(Collectors.toList());

            List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentDocumentCollectionIdAndEmbeddingIn(collection.getId(), embeddings);

            for (DocumentChunkEntity documentChunk : documentChunks) {
                messageWithDocuments.addDocument(documentChunk, documentChunk.getContent());
            }

        }
    }

    class MessageWithDocuments {
        private Message message;

        private List<DocumentChunkEntity> documents = new ArrayList<>();

        private StringBuilder content = new StringBuilder();

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }

        public List<DocumentChunkEntity> getDocuments() {
            return documents;
        }

        public String getContent() {
            return content.toString();
        }

        public void addDocument(DocumentChunkEntity document, String content) {
            this.documents.add(document);
            this.content.append(content + "\n");
        }

    }

    private MessageWithDocuments generateSystemMessage(CollectionEntity collection, QuestionParsed questionParsed) {

        MessageWithDocuments messageWithDocuments = new MessageWithDocuments();

        List<CollectionPropertyEntity> prompts = collectionService.findPrompts(collection.getId());

        if (questionParsed.query) {
            List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentDocumentCollectionIdAndDocumentFilenameLike(collection.getId(), "%database%");

            for (DocumentChunkEntity documentChunk : documentChunks) {
                messageWithDocuments.addDocument(documentChunk, documentChunk.getContent());
            }

            String promptText = prompts.stream().filter(e -> e.getKey().toLowerCase().contains("sqlsystemprompt")).findFirst().orElseThrow().getValue();

            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptText);
            Message systemMessage = systemPromptTemplate.createMessage(Map.of("data", messageWithDocuments.getContent()));
            messageWithDocuments.setMessage(systemMessage);

        } else {
            String promptText = prompts.stream().filter(e -> e.getKey().toLowerCase().contains("documentsystemprompt")).findFirst().orElseThrow().getValue();

            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptText);
            long spentTokens = DocumentUtils.countTokens(promptText + questionParsed.question);
            generateSystemMessage(collection, questionParsed, spentTokens, messageWithDocuments);

            Message systemMessage = systemPromptTemplate.createMessage(Map.of("data", messageWithDocuments.getContent()));
            messageWithDocuments.setMessage(systemMessage);
        }

        return messageWithDocuments;

    }

    class ParsedResponse {
        ChatResponse chatResponse;
        String response;

        public ParsedResponse(ChatResponse chatResponse, String response) {
            this.chatResponse = chatResponse;
            this.response = response;
        }
    }

    private ParsedResponse launchIAQuestion(Long collectionId, QuestionParsed questionParsed, Prompt prompt, int retries) {

        ChatResponse chatResponse = getChatClient(collectionId).call(prompt);
        String response = chatResponse.getResult().getOutput().getContent();

        if (questionParsed.query) {
            List<CollectionPropertyDto> properties = this.collectionService.findProperties(collectionId);
            String databaseURL = properties.stream().filter(e -> e.getKey().equals("databaseURL")).findFirst().orElseThrow().getValue();
            String databaseUsername = properties.stream().filter(e -> e.getKey().equals("databaseUsername")).findFirst().orElseThrow().getValue();
            String databasePassword = properties.stream().filter(e -> e.getKey().equals("databasePassword")).findFirst().orElseThrow().getValue();

            if (StringUtils.hasText(databaseURL) == false || StringUtils.hasText(databaseUsername) == false || StringUtils.hasText(databasePassword) == false) {
                return new ParsedResponse(chatResponse, "No se han configurado las credenciales de la base de datos");
            }

            try {
                response = dynamicRepository.launchQuery(response, databaseURL, databaseUsername, databasePassword);
            } catch (Exception e) {

                if (retries <= 0) {
                    return new ParsedResponse(chatResponse, "No se ha podido obtener una respuesta para la query: \n" + response);
                } else {
                    return launchIAQuestion(collectionId, questionParsed, prompt, retries - 1);
                }
            }

        }

        return new ParsedResponse(chatResponse, response);
    }

    private ParsedResponse launchIAQuestion(Long collectionId, QuestionParsed questionParsed, Prompt prompt) {
        return launchIAQuestion(collectionId, questionParsed, prompt, 2);
    }

    @Override
    @Transactional(readOnly = false)
    public ConversationEntity sendQuestion(Long chatId, String question) {

        long start = System.currentTimeMillis();

        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow();
        CollectionEntity collection = chatEntity.getCollection();

        QuestionParsed questionParsed = new QuestionParsed(question);

        MessageWithDocuments systemMessage = generateSystemMessage(collection, questionParsed);

        UserMessage userMessage = new UserMessage(questionParsed.question);

        Prompt prompt = new Prompt(List.of(systemMessage.getMessage(), userMessage), OpenAiChatOptions.builder().withModel("gpt-3.5-turbo-0125").withTemperature(0.7f).build());

        ParsedResponse launchIAQuestion = launchIAQuestion(collection.getId(), questionParsed, prompt);

        long end = System.currentTimeMillis();

        ConversationEntity conversation = new ConversationEntity();
        conversation.setChat(chatEntity);
        conversation.setAuthor(UserUtils.getUserDetails().getDisplayName());
        conversation.setUser(true);
        conversation.setContent(questionParsed.question);
        conversation.setDate(LocalDateTime.now());
        conversationRepository.save(conversation);

        //List<String> embeddings = systemMessage.getDocuments().stream().map(entry -> entry.getId()).collect(Collectors.toList());

        ConversationEntity conversationResponse = new ConversationEntity();
        conversationResponse.setChat(chatEntity);
        conversationResponse.setAuthor("Assistant"); //TODO cambiar
        conversationResponse.setUser(false);
        conversationResponse.setContent(launchIAQuestion.response);
        if (launchIAQuestion.chatResponse != null && launchIAQuestion.chatResponse.getMetadata() != null)
            conversationResponse.setTokens(launchIAQuestion.chatResponse.getMetadata().getUsage().getTotalTokens());
        conversationResponse.setSpentTime(end - start);
        conversationResponse.setDate(LocalDateTime.now());
        conversationRepository.save(conversationResponse);

        saveConversationContext(systemMessage, conversationResponse);

        return conversationResponse;

    }

    private void saveConversationContext(MessageWithDocuments messageWithDocuments, ConversationEntity conversation) {

        List<ConversationPropertiesEntity> context = new ArrayList<>();

        for (DocumentChunkEntity document : messageWithDocuments.getDocuments()) {
            ConversationPropertiesEntity contextEntity = new ConversationPropertiesEntity();

            contextEntity.setChunkNumber(document.getOrder());
            contextEntity.setFilename(document.getDocument().getFilename());
            contextEntity.setPath(document.getDocument().getPath());
            contextEntity.setTokens(DocumentUtils.countTokens(document.getContent()));
            contextEntity.setType(document.getType());
            contextEntity.setStatus(document.getDocument().getStatus());
            contextEntity.setModifyType(document.getModifyType());
            contextEntity.setEmbeddingId(document.getEmbedding());
            contextEntity.setConversation(conversation);

            context.add(contextEntity);
        }

        conversationContextRepository.saveAll(context);

    }

    @Override
    public List<ConversationEntity> findByChatId(Long chatId) {

        return conversationRepository.findByChatIdOrderByDateAsc(chatId);

    }

    @Override
    public List<ConversationPropertiesEntity> getEmbeddingsFromMessageId(Long conversationId) {

        return conversationContextRepository.findByConversationId(conversationId);
    }

    @Override
    public List<ChatEntity> findChatsByCollectionId(Long collectionId) {
        return chatRepository.findByCollectionIdOrderByUpdateDateDesc(collectionId);
    }

    @Override
    @Transactional(readOnly = false)
    public ChatEntity createChatByCollectionId(Long collectionId, ChatDto data) {
        CollectionEntity collection = collectionRepository.findById(collectionId).orElseThrow();

        ChatEntity chat = new ChatEntity();

        String title = data.getTitle();

        QuestionParsed questionParsed = new QuestionParsed(title);

        if (questionParsed.question.length() > 300)
            questionParsed.question = questionParsed.question.substring(0, 300);

        chat.setTitle(questionParsed.question);
        chat.setCollection(collection);
        chat.setUsername(UserUtils.getUserDetails().getUsername());
        chat.setUpdateDate(LocalDateTime.now());

        return chatRepository.save(chat);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteChat(Long chatId) {

        conversationRepository.deleteByChatId(chatId);

        chatRepository.deleteById(chatId);
    }

    @Override
    @Transactional(readOnly = false)
    public void renameChat(Long chatId, String title) {
        ChatEntity chat = chatRepository.findById(chatId).orElseThrow();
        chat.setTitle(title);
        chatRepository.save(chat);
    }
}
