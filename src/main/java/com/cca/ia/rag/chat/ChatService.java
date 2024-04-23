package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ConversationEntity;
import com.cca.ia.rag.chat.model.EmbeddingMessage;

import java.util.List;

public interface ChatService {

    ConversationEntity sendQuestion(Long collectionId, String question);

    List<ConversationEntity> findByChatId(Long chatId);

    List<EmbeddingMessage> getEmbeddingsFromMessageId(Long messageId);
}
