package com.cca.ia.rag.document;

import com.cca.ia.rag.document.dto.DocumentActionsDto;
import com.cca.ia.rag.document.dto.DocumentChunkContentDto;
import com.cca.ia.rag.document.dto.DocumentChunkSaveDto;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentFileEntity;

import java.util.List;

public interface DocumentService {

    List<DocumentFileEntity> getDocuments(Long collectionId);

    List<DocumentChunkEntity> getDocumentChunksByDocumentId(Long documentId, DocumentChunkEntity.DocumentChunkType type);

    void saveDocumentChunks(Long documentId, DocumentChunkSaveDto dto) throws Exception;

    DocumentChunkContentDto getChunkContentByDocumentAndChunkId(Long documentId, Long chunkId) throws Exception;

    List<DocumentFileEntity> getDocumentsById(List<Long> documentsId);

    void executeActions(Long documentId, DocumentActionsDto actions);

    void executeActionsAsync(DocumentFileEntity document, DocumentActionsDto actions) throws Exception;

    void deleteAllFromSourceDocument(Long documentId) throws Exception;
}
