package com.cca.ia.rag.chat.assistant;

import com.cca.ia.rag.chat.assistant.database.DynamicRepository;
import com.cca.ia.rag.chat.assistant.model.RequestContextDto;
import com.cca.ia.rag.chat.assistant.model.ResponseContextDto;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentChunkRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service("sqlAssistantService")
public class SQLAssistantService implements AssistantService {

    public static final int RETRIES = 3;
    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private DynamicRepository dynamicRepository;

    @Override
    public ResponseContextDto process(RequestContextDto request) {

        ResponseContextDto response = new ResponseContextDto();

        generateDocumentContext(request);

        generatePrompt(request);

        launchAIQuestion(request, response, RETRIES);

        return response;
    }

    private void generatePrompt(RequestContextDto request) {
        String promptText = request.getPrompt("sqlSystemPrompt");
        if (promptText == null)
            throw new IllegalStateException("Prompt not found");

        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptText);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("data", request.getContext()));

        UserMessage userMessage = new UserMessage(request.getQuestion());

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage), OpenAiChatOptions.builder().withModel("gpt-3.5-turbo-0125").withTemperature(0.7f).build());

        request.setPrompt(prompt);
    }

    private void generateDocumentContext(RequestContextDto request) {
        List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentDocumentCollectionIdAndDocumentFilenameLike(request.getCollection().getId(), "%database%");

        for (DocumentChunkEntity documentChunk : documentChunks) {
            request.addDocument(documentChunk, documentChunk.getContent());
        }
    }

    private void launchAIQuestion(RequestContextDto request, ResponseContextDto response, int retries) {

        String databaseURL = request.getProperty("databaseURL");
        String databaseUsername = request.getProperty("databaseUsername");
        String databasePassword = request.getProperty("databasePassword");

        if (StringUtils.hasText(databaseURL) == false || StringUtils.hasText(databaseUsername) == false || StringUtils.hasText(databasePassword) == false) {
            String responseMessage = "No se han encontrado las credenciales de la base de datos";
            response.setResponse(responseMessage);
            return;
        }

        ChatResponse chatResponse = getChatClient(request).call(request.getPrompt());
        String responseText = chatResponse.getResult().getOutput().getContent();
        response.addTokens(chatResponse.getMetadata().getUsage().getTotalTokens());

        try {
            responseText = dynamicRepository.launchQuery(responseText, databaseURL, databaseUsername, databasePassword);
            response.setResponse(responseText);

        } catch (Exception e) {

            if (retries <= 0) {
                String responseMessage = "No se ha podido obtener una respuesta para la query: \n<br />\n```sql\n\n" + responseText + "\n\n```\n\n<br />";
                response.setResponse(responseMessage);
            } else {
                launchAIQuestion(request, response, retries - 1);

            }
        }

    }

    private ChatModel getChatClient(RequestContextDto request) {
        String apiKey = request.getProperty("apiKey");
        return new OpenAiChatModel(new OpenAiApi(apiKey));
    }

}
