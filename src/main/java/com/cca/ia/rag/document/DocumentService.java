package com.cca.ia.rag.document;

import com.cca.ia.rag.document.model.DocumentChunkContentDto;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentEntity;

import java.util.List;

public interface DocumentService {

    List<DocumentEntity> getDocuments(Long collectionId);

    List<DocumentChunkEntity> getDocumentChunksByDocumentId(Long documentId);

    DocumentChunkContentDto getChunkContentByDocumentAndChunkId(Long documentId, Long chunkId) throws Exception;

    List<DocumentEntity> getDocumentsById(List<Long> documentsId);
}
