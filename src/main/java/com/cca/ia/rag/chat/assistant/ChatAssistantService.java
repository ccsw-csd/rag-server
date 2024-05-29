package com.cca.ia.rag.chat.assistant;

import com.cca.ia.rag.chat.assistant.model.RequestContextDto;
import com.cca.ia.rag.chat.assistant.model.ResponseContextDto;
import com.cca.ia.rag.document.DocumentUtils;
import com.cca.ia.rag.document.embedding.EmbeddingService;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentChunkRepository;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("chatAssistantService")
public class ChatAssistantService implements AssistantService {

    public static final String CODE_PERCENTAGE_PROMPT = "Eres un Agente Decisor que decide si una pregunta de un usuario hace referencia con el código fuente del proyecto. "
            + "Debes tener en cuenta que código fuente del proyecto está escrito en Java, Spring boot o Angular, así que busca palabras clave en la pregunta del usuario. "
            + "La respuesta debe ser el porcentaje de fiabilidad (entre 0 y 100) de que la pregunta se refiere a código fuente. Siendo 100 una fiabilidad del 100% y 0 una fiabilidad del 0%. Responde solamente con un número entero. \n";
    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Override
    public ResponseContextDto process(RequestContextDto request) {

        ResponseContextDto response = new ResponseContextDto();

        generateDocumentContext(request, response);

        generatePrompt(request);

        launchAIQuestion(request, response);

        return response;
    }

    private void launchAIQuestion(RequestContextDto request, ResponseContextDto response) {

        ChatResponse chatResponse = getChatClient(request).call(request.getPrompt());
        String responseText = chatResponse.getResult().getOutput().getContent();

        response.setResponse(responseText);
        response.addTokens(chatResponse.getMetadata().getUsage().getTotalTokens());
    }

    private void generatePrompt(RequestContextDto request) {
        String promptText = request.getPrompt("documentSystemPrompt");
        if (promptText == null)
            throw new IllegalStateException("Prompt not found");

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("data", request.getContext()));

        UserMessage userMessage = new UserMessage(request.getQuestion());

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), OpenAiChatOptions.builder().withModel("gpt-3.5-turbo-0125").withTemperature(0.7f).build());

        request.setPrompt(prompt);
    }

    private long generateDocumentContextWithFiles(RequestContextDto request, long maxTokensForSearch) {

        if (request.getFiles() == null && request.getFiles().size() == 0)
            return maxTokensForSearch;

        for (String filename : request.getFiles()) {

            filename = filename.replaceAll("\\*", "%");

            List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentDocumentCollectionIdAndDocumentFilenameLike(request.getCollection().getId(), filename);

            for (DocumentChunkEntity documentChunk : documentChunks) {

                long tokens = DocumentUtils.countTokens(documentChunk.getContent());
                maxTokensForSearch -= tokens;

                if (maxTokensForSearch < 0)
                    return maxTokensForSearch;

                request.addDocument(documentChunk, documentChunk.getContent());
            }
        }

        return maxTokensForSearch;
    }

    private void generateDocumentContext(RequestContextDto request, ResponseContextDto response) {

        String promptText = request.getPrompt("documentSystemPrompt");
        if (promptText == null)
            throw new IllegalStateException("Prompt not found");

        String baseText = promptText + request.getQuestion();
        long spentTokens = DocumentUtils.countTokens(baseText);

        long maxTokens = 10000;
        long maxTokensForSearch = maxTokens - spentTokens;

        maxTokensForSearch = generateDocumentContextWithFiles(request, maxTokensForSearch);
        if (maxTokensForSearch <= 0) {
            return;
        }

        if (request.getNoAutocontext()) {
            return;
        }

        generateDocumentContextWithVectorDB(request, response, maxTokensForSearch);

    }

    private void generateDocumentContextWithVectorDB(RequestContextDto request, ResponseContextDto response, long maxTokensForSearch) {
        int codeQuestion = getCodePercentage(request, response);

        List<Document> documentVectorDB = embeddingService.findSimilarity(request.getCollection(), request.getQuestion(), codeQuestion, maxTokensForSearch);
        List<String> embeddingIds = documentVectorDB.stream().map(entry -> entry.getId()).collect(Collectors.toList());

        List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentDocumentCollectionIdAndEmbeddingIn(request.getCollection().getId(), embeddingIds);

        for (DocumentChunkEntity documentChunk : documentChunks) {
            request.addDocument(documentChunk, documentChunk.getContent());
        }
    }

    private int getCodePercentage(RequestContextDto request, ResponseContextDto response) {
        int codePercentage = 0;

        if (request.getOnlyDoc()) {
            codePercentage = 0;
        } else if (request.getOnlyCode()) {
            codePercentage = 100;
        } else {
            codePercentage = calculateCodePercentage(request, response);
        }
        return codePercentage;
    }

    private int calculateCodePercentage(RequestContextDto request, ResponseContextDto response) {

        Message assistantMessage = new AssistantMessage(CODE_PERCENTAGE_PROMPT);
        UserMessage userMessage = new UserMessage("La pregunta de usuario es la siguiente: " + request.getQuestion());

        Prompt prompt = new Prompt(List.of(assistantMessage, userMessage), OpenAiChatOptions.builder().withModel("gpt-3.5-turbo-0125").withTemperature(0.1f).build());

        ChatResponse chatResponse = getChatClient(request).call(prompt);
        String responseText = chatResponse.getResult().getOutput().getContent().trim();
        response.addTokens(chatResponse.getMetadata().getUsage().getTotalTokens());

        try {
            return Integer.parseInt(responseText);
        } catch (Exception e) {
            return 50;
        }

    }

    private ChatClient getChatClient(RequestContextDto request) {
        String apiKey = request.getProperty("apiKey");
        return new OpenAiChatClient(new OpenAiApi(apiKey));
    }

}
