package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ChatDto;
import com.cca.ia.rag.chat.model.ChatEntity;
import com.cca.ia.rag.chat.model.ConversationEntity;
import com.cca.ia.rag.chat.model.ConversationPropertiesEntity;

import java.util.List;

public interface ChatService {

    ConversationEntity sendQuestion(Long chatId, String question);

    List<ConversationEntity> findByChatId(Long chatId);

    List<ConversationPropertiesEntity> getEmbeddingsFromMessageId(Long conversationId);

    List<ChatEntity> findChatsByCollectionId(Long collectionId);

    ChatEntity createChatByCollectionId(Long collectionId, ChatDto data);

    void deleteChat(Long chatId);

    void renameChat(Long chatId, String title);
}
