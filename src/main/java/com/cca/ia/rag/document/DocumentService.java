package com.cca.ia.rag.document;

import com.cca.ia.rag.document.dto.*;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentFileEntity;

import java.util.List;

public interface DocumentService {

    List<DocumentFileEntity> getDocuments(Long collectionId);

    List<DocumentChunkEntity> getDocumentChunksByDocumentId(Long documentId, DocumentChunkEntity.DocumentChunkModifyType type);

    void saveDocumentChunks(Long documentId, DocumentChunkSaveDto dto) throws Exception;

    DocumentChunkContentDto getChunkContentByDocumentAndChunkId(Long documentId, Long chunkId) throws Exception;

    List<DocumentFileEntity> getDocumentsById(List<Long> documentsId);

    void generateEmbeddings(Long parentDocumentId, GenerateEmbeddingsDto data);

    void generateEmbeddings(Long parentDocumentId, List<DocumentFileEntity> documentFiles);

    void deleteAllFromSourceDocument(Long documentId) throws Exception;

    void uploadDocuments(Long collectionId, DocumentUploadFormDataDto data) throws Exception;

    void createDocument(Long collectionId, CreateDocumentDto dto);
}
