package com.cca.ia.rag.chat;

import com.cca.ia.rag.chat.model.ChatEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChatRepository extends CrudRepository<ChatEntity, Long> {

    List<ChatEntity> findByCollectionIdOrderByUpdateDateDesc(Long collectionId);
}
