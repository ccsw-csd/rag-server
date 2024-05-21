package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ChatDto;
import com.cca.ia.rag.chat.model.ChatEntity;
import com.cca.ia.rag.chat.model.ConversationEntity;
import com.cca.ia.rag.chat.model.EmbeddingMessage;

import java.util.List;

public interface ChatService {

    ConversationEntity sendQuestion(Long chatId, String question);

    List<ConversationEntity> findByChatId(Long chatId);

    List<EmbeddingMessage> getEmbeddingsFromMessageId(Long messageId);

    List<ChatEntity> findChatsByCollectionId(Long collectionId);

    ChatEntity createChatByCollectionId(Long collectionId, ChatDto data);
}
