package com.cca.ia.rag.document.model;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface DocumentRepository extends CrudRepository<DocumentEntity, Long> {

    List<DocumentEntity> findByCollectionId(Long collectionId);

    List<DocumentEntity> findByIdIn(Collection<Long> ids);

}
