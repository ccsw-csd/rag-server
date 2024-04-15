package com.cca.ia.rag.document;

import com.cca.ia.rag.document.model.*;

import java.util.List;

public interface DocumentService {

    List<DocumentEntity> getDocuments(Long collectionId);

    List<DocumentChunkEntity> getDocumentChunksByDocumentId(Long documentId, DocumentChunkEntity.DocumentChunkType type);

    void saveDocumentChunks(Long documentId, DocumentChunkSaveDto dto) throws Exception;

    DocumentChunkContentDto getChunkContentByDocumentAndChunkId(Long documentId, Long chunkId) throws Exception;

    List<DocumentEntity> getDocumentsById(List<Long> documentsId);

    void executeActions(Long documentId, DocumentActionsDto actions);

    void executeActionsAsync(DocumentEntity document, DocumentActionsDto actions) throws Exception;
}
