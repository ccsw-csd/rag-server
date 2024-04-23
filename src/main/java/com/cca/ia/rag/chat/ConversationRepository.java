package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ConversationEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ConversationRepository extends CrudRepository<ConversationEntity, Long> {

    List<ConversationEntity> findByChatIdOrderByDateAsc(Long chatId);

}
