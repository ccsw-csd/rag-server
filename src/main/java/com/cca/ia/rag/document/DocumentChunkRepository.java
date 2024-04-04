package com.cca.ia.rag.document;

import com.cca.ia.rag.document.model.DocumentChunkEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DocumentChunkRepository extends CrudRepository<DocumentChunkEntity, Long> {

    List<DocumentChunkEntity> findByDocumentId(Long documentId);

}
