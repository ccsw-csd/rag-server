package com.cca.ia.rag.document.model;

import org.springframework.data.repository.CrudRepository;

import java.util.Collection;
import java.util.List;

public interface DocumentFileRepository extends CrudRepository<DocumentFileEntity, Long> {

    List<DocumentFileEntity> findByDocumentCollectionIdOrderByDocumentFilenameAsc(Long collectionId);

    List<DocumentFileEntity> findByIdIn(Collection<Long> ids);

    List<DocumentFileEntity> findByDocumentId(Long documentId);

    List<DocumentFileEntity> findByDocumentIdAndPathStartsWith(Long id, String path);
}
