package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ChatEntity;
import com.cca.ia.rag.chat.model.ConversationEntity;
import com.cca.ia.rag.chat.model.EmbeddingMessage;
import com.cca.ia.rag.chat.model.MessageDto;
import com.cca.ia.rag.collection.CollectionRepository;
import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.document.embedding.EmbeddingService;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentChunkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatServiceDefault implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Value("classpath:/prompts/system-qa.st")
    private Resource qaSystemPromptResource;

    @Value("classpath:/prompts/system-chatbot.st")
    private Resource chatbotSystemPromptResource;

    private final ChatClient chatClient;

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
    public ChatServiceDefault(ChatClient chatClient) {
        this.chatClient = chatClient;
        //this.vectorStore = vectorStore;
    }

    public String generate(String message, boolean stuffit) {
        Message systemMessage = getSystemMessage(message, stuffit);
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        logger.info("Asking AI model to reply to question.");
        ChatResponse chatResponse = chatClient.call(prompt);
        logger.info("AI responded.");
        return chatResponse.getResult().getOutput().getContent();
    }

    private Message getSystemMessage(String query, boolean stuffit) {
        if (stuffit) {
            logger.info("Retrieving relevant documents");
            List<Document> similarDocuments = null; //vectorStore.similaritySearch(query);
            logger.info(String.format("Found %s relevant documents.", similarDocuments.size()));
            String documents = similarDocuments.stream().map(entry -> entry.getContent()).collect(Collectors.joining("\n"));
            SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.qaSystemPromptResource);
            return systemPromptTemplate.createMessage(Map.of("documents", documents));
        } else {
            logger.info("Not stuffing the prompt, using generic prompt");
            return new SystemPromptTemplate(this.chatbotSystemPromptResource).createMessage();
        }
    }

    @Override
    public ConversationEntity sendQuestion(Long collectionId, String question) {
        long start = System.currentTimeMillis();
        CollectionEntity collection = collectionRepository.findById(collectionId).orElseThrow();

        ChatEntity chat = chatRepository.findById(1L).orElseThrow();

        ConversationEntity conversation = new ConversationEntity();
        conversation.setChat(chat);
        conversation.setAuthor("Pablo Jiménez Martínez"); //TODO cambiar
        conversation.setUser(true);
        conversation.setContent(question);
        conversation.setDate(LocalDateTime.now());
        conversationRepository.save(conversation);

        logger.info("Recibimos pregunta: " + question);

        logger.info("Buscamos documentos similares");
        List<Document> similarDocuments = embeddingService.findSimilarity(collection.getName(), question);
        String documentContent = similarDocuments.stream().map(entry -> entry.getContent()).collect(Collectors.joining("\n"));
        logger.info("Encontrados " + similarDocuments.size() + " documentos relevantes.");

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(this.qaSystemPromptResource);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("documents", documentContent));
        UserMessage userMessage = new UserMessage(question);

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), OpenAiChatOptions.builder().withModel("gpt-3.5-turbo-0125").withTemperature(0.7f).build());
        logger.info("Generamos prompt para IA.");

        logger.info("Preguntamos al modelo de IA");

        ChatResponse chatResponse = chatClient.call(prompt);
        long end = System.currentTimeMillis();

        logger.info("La IA ha respondido: " + chatResponse.getResult().getOutput().getContent());

        MessageDto messageDto = new MessageDto();

        List<String> embeddings = similarDocuments.stream().map(entry -> entry.getId()).collect(Collectors.toList());

        String response = chatResponse.getResult().getOutput().getContent();

        ConversationEntity conversationResponse = new ConversationEntity();
        conversationResponse.setChat(chat);
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
            embeddingMessage.setType(chunk.getType());
            embeddingMessage.setOrder(chunk.getOrder());
            embeddingMessage.setDocument(chunk.getDocument().getFilename());

            embeddings.add(embeddingMessage);
        }

        return embeddings;
    }
}
