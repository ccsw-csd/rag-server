package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ChatDto;
import com.cca.ia.rag.chat.model.ChatEntity;
import com.cca.ia.rag.chat.model.ConversationEntity;
import com.cca.ia.rag.chat.model.EmbeddingMessage;
import com.cca.ia.rag.collection.CollectionRepository;
import com.cca.ia.rag.collection.model.CollectionEntity;
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
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ChatServiceDefault implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Value("classpath:/prompts/system-qa.st")
    private Resource qaSystemPromptResource;

    @Autowired
    private ChatClient chatClient;

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

    private int isCodeQuestion(String question) {

        Message assistantMessage = new AssistantMessage("Eres un Agente Decisor que decide si una pregunta de un usuario hace referencia con el código fuente del proyecto. "
                + "Debes tener en cuenta que código fuente del proyecto está escrito en Java, Spring boot o Angular, así que busca palabras clave en la pregunta del usuario. "
                + "La respuesta debe ser el porcentaje de fiabilidad (entre 0 y 100) de que la pregunta se refiere a código fuente. Siendo 100 una fiabilidad del 100% y 0 una fiabilidad del 0%. Responde solamente con un número entero. \n");
        UserMessage userMessage = new UserMessage("La pregunta de usuario es la siguiente: " + question);

        Prompt prompt = new Prompt(List.of(assistantMessage, userMessage), OpenAiChatOptions.builder().withModel("gpt-3.5-turbo-0125").withTemperature(0.1f).build());

        ChatResponse chatResponse = chatClient.call(prompt);
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

    private List<Document> generateSystemMessage(CollectionEntity collection, QuestionParsed question, long spentTokens) {
        List<Document> similarDocuments = new ArrayList<>();
        long maxTokens = 10000;
        long maxTokensForSearch = maxTokens - spentTokens;

        if (question.files != null && question.files.size() > 0) {

            for (String filename : question.files) {

                filename = filename.replaceAll("\\*", "%");

                Map<String, Object> metadata = Map.of("file", filename, "type", "manual-type");
                List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentDocumentCollectionIdAndDocumentFilenameLike(collection.getId(), filename);

                for (DocumentChunkEntity documentChunk : documentChunks) {

                    long tokens = DocumentUtils.countTokens(documentChunk.getContent());
                    maxTokensForSearch -= tokens;

                    if (maxTokensForSearch < 0)
                        return similarDocuments;

                    similarDocuments.add(new Document(documentChunk.getEmbedding(), documentChunk.getContent(), metadata));
                }
            }
        }

        if (question.noAutocontext) {
            return similarDocuments;
        }

        if (maxTokensForSearch > 0) {
            int codeQuestion = 0;

            if (question.onlyDoc) {
                codeQuestion = 0;
            } else if (question.onlyCode) {
                codeQuestion = 100;
            } else {
                codeQuestion = isCodeQuestion(question.question);
            }

            similarDocuments.addAll(embeddingService.findSimilarity(collection.getName(), question.question, codeQuestion, maxTokensForSearch));
        }

        return similarDocuments;

    }

    @Override
    @Transactional(readOnly = false)
    public ConversationEntity sendQuestion(Long chatId, String question) {

        long start = System.currentTimeMillis();

        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow();
        CollectionEntity collection = chatEntity.getCollection();

        QuestionParsed questionParsed = new QuestionParsed(question);

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.qaSystemPromptResource);
        long spentTokens = DocumentUtils.countTokens(systemPromptTemplate.getTemplate() + questionParsed.question);
        List<Document> similarDocuments = generateSystemMessage(collection, questionParsed, spentTokens);
        String documentContent = similarDocuments.stream().map(entry -> entry.getContent()).collect(Collectors.joining("\n"));

        Message systemMessage = systemPromptTemplate.createMessage(Map.of("documents", documentContent));

        UserMessage userMessage = new UserMessage(questionParsed.question);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), OpenAiChatOptions.builder().withModel("gpt-3.5-turbo-0125").withTemperature(0.7f).build());

        ChatResponse chatResponse = chatClient.call(prompt);
        long end = System.currentTimeMillis();

        ConversationEntity conversation = new ConversationEntity();
        conversation.setChat(chatEntity);
        conversation.setAuthor(UserUtils.getUserDetails().getDisplayName());
        conversation.setUser(true);
        conversation.setContent(questionParsed.question);
        conversation.setDate(LocalDateTime.now());
        conversationRepository.save(conversation);

        List<String> embeddings = similarDocuments.stream().map(entry -> entry.getId()).collect(Collectors.toList());

        String response = chatResponse.getResult().getOutput().getContent();

        ConversationEntity conversationResponse = new ConversationEntity();
        conversationResponse.setChat(chatEntity);
        conversationResponse.setAuthor("Assistant"); //TODO cambiar
        conversationResponse.setUser(false);
        conversationResponse.setContent(response);
        conversationResponse.setTokens(chatResponse.getMetadata().getUsage().getTotalTokens());
        conversationResponse.setSpentTime(end - start);
        conversationResponse.setDate(LocalDateTime.now());
        conversationResponse.setEmbeddings(embeddings);
        conversationRepository.save(conversationResponse);

        return conversationResponse;
    }

    @Override
    public List<ConversationEntity> findByChatId(Long chatId) {

        return conversationRepository.findByChatIdOrderByDateAsc(chatId);

    }

    @Override
    public List<EmbeddingMessage> getEmbeddingsFromMessageId(Long messageId) {

        ConversationEntity message = conversationRepository.findById(messageId).orElseThrow();
        CollectionEntity collection = message.getChat().getCollection();

        List<DocumentChunkEntity> chunks = documentChunkRepository.findByDocumentDocumentCollectionIdAndEmbeddingIn(collection.getId(), message.getEmbeddings());

        List<EmbeddingMessage> embeddings = new ArrayList<>();

        for (DocumentChunkEntity chunk : chunks) {

            EmbeddingMessage embeddingMessage = new EmbeddingMessage();

            embeddingMessage.setId(chunk.getId());
            embeddingMessage.setEmbeddingId(chunk.getEmbedding());
            embeddingMessage.setContent(chunk.getContent());
            embeddingMessage.setType(chunk.getModifyType());
            embeddingMessage.setOrder(chunk.getOrder());
            embeddingMessage.setDocument(chunk.getDocument().getFilename());

            embeddings.add(embeddingMessage);
        }

        return embeddings;
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
}
