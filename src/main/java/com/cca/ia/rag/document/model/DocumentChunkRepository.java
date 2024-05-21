package com.cca.ia.rag.document.model;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentChunkRepository extends CrudRepository<DocumentChunkEntity, Long> {

    List<DocumentChunkEntity> findByDocumentIdAndModifyTypeOrderByOrderDesc(Long documentId, DocumentChunkEntity.DocumentChunkModifyType type);

    List<DocumentChunkEntity> findByDocumentDocumentCollectionIdAndDocumentFilenameLike(Long id, String filename);

    List<DocumentChunkEntity> findByDocumentId(Long id);

    List<DocumentChunkEntity> findByDocumentDocumentId(Long id);

    List<DocumentChunkEntity> findByDocumentDocumentCollectionIdAndEmbeddingIn(Long id, List<String> embeddings);
}
