package com.cca.ia.rag.document.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentChunkRepository extends CrudRepository<DocumentChunkEntity, Long> {

    List<DocumentChunkEntity> findByDocumentIdAndTypeOrderByOrderDesc(Long documentId, DocumentChunkEntity.DocumentChunkType type);

    List<DocumentChunkEntity> findByDocumentId(Long id);
}
