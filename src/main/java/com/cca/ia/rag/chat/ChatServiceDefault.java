package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.assistant.AssistantService;
import com.cca.ia.rag.chat.assistant.model.QuestionParsed;
import com.cca.ia.rag.chat.assistant.model.RequestContextDto;
import com.cca.ia.rag.chat.assistant.model.ResponseContextDto;
import com.cca.ia.rag.chat.database.ChatRepository;
import com.cca.ia.rag.chat.database.ConversationContextRepository;
import com.cca.ia.rag.chat.database.ConversationRepository;
import com.cca.ia.rag.chat.model.ChatDto;
import com.cca.ia.rag.chat.model.ChatEntity;
import com.cca.ia.rag.chat.model.ConversationEntity;
import com.cca.ia.rag.chat.model.ConversationPropertiesEntity;
import com.cca.ia.rag.collection.database.CollectionRepository;
import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.config.security.UserUtils;
import com.cca.ia.rag.document.DocumentUtils;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ChatServiceDefault implements ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ConversationContextRepository conversationContextRepository;

    @Autowired
    @Qualifier("masterAssistantService")
    private AssistantService assistantService;

    @Override
    @Transactional(readOnly = false)
    public ConversationEntity sendQuestion(Long chatId, String question) {

        ChatEntity chatEntity = chatRepository.findById(chatId).orElseThrow();
        RequestContextDto requestContextDto = new RequestContextDto(chatEntity.getCollection(), question);

        ResponseContextDto response = null;
        try {
            response = assistantService.process(requestContextDto);
        } catch (Exception e) {
            logger.error("Error processing question: " + e.getMessage(), e);
            response = new ResponseContextDto();
            response.setRequest(question);
            response.setResponse("Ocurrió un problema al procesar la pregunta. Por favor, inténtalo de nuevo. \n<br />\n Error: " + e.getMessage());
        }

        ConversationEntity conversation = new ConversationEntity();
        conversation.setChat(chatEntity);
        conversation.setAuthor(UserUtils.getUserDetails().getDisplayName());
        conversation.setUser(true);
        conversation.setContent(response.getRequest());
        conversation.setDate(LocalDateTime.now());
        conversationRepository.save(conversation);

        ConversationEntity conversationResponse = new ConversationEntity();
        conversationResponse.setChat(chatEntity);
        conversationResponse.setAuthor("Assistant");
        conversationResponse.setUser(false);
        conversationResponse.setContent(response.getResponse());
        conversationResponse.setTokens(response.getSpentTokens());
        conversationResponse.setSpentTime(response.getSpentTime());
        conversationResponse.setDate(LocalDateTime.now());
        conversationRepository.save(conversationResponse);

        saveConversationContext(response.getDocuments(), conversationResponse);

        return conversationResponse;
    }

    private void saveConversationContext(List<DocumentChunkEntity> documents, ConversationEntity conversation) {

        if (documents == null)
            return;

        List<ConversationPropertiesEntity> context = new ArrayList<>();

        for (DocumentChunkEntity document : documents) {
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

        String question = questionParsed.getQuestion();

        if (questionParsed.getQuestion().length() > 300)
            question = question.substring(0, 300);

        chat.setTitle(question);
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
