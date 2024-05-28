package com.cca.ia.rag.chat.database;

import com.cca.ia.rag.chat.model.ConversationPropertiesEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ConversationContextRepository extends CrudRepository<ConversationPropertiesEntity, Long> {

    List<ConversationPropertiesEntity> findByConversationId(Long conversationId);
}
