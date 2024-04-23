package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ChatEntity;
import org.springframework.data.repository.CrudRepository;

public interface ChatRepository extends CrudRepository<ChatEntity, Long> {

}
