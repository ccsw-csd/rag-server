package com.cca.ia.rag.document;

import com.cca.ia.rag.collection.CollectionService;
import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.document.embedding.EmbeddingService;
import com.cca.ia.rag.document.model.*;
import com.cca.ia.rag.document.parser.FactoryDocumentParserService;
import com.cca.ia.rag.s3.RemoteFileService;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentServiceDefault implements DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private FactoryDocumentParserService documentParserService;

    @Autowired
    @Lazy
    private DocumentService documentService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private ModelMapper mapper;

    @Override
    public void executeActions(Long documentId, DocumentActionsDto actions) {

        DocumentEntity document = documentRepository.findById(documentId).orElse(null);
        document.setStatus(DocumentEntity.DocumentStatus.PROCESSING);
        documentRepository.save(document);

        try {
            documentService.executeActionsAsync(document, actions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Async
    public void executeActionsAsync(DocumentEntity document, DocumentActionsDto actions) throws Exception {

        CollectionEntity collection = collectionService.findById(document.getCollectionId());
        String collectionName = collection.getName();
        List<DocumentChunkEntity> chunks = null;

        // *************** DELETE ACTIONS ***************
        if (actions.isDeleteEmbeddings()) {
            if (chunks == null)
                chunks = documentChunkRepository.findByDocumentId(document.getId());

            embeddingService.deleteEmbeddings(collection, chunks);
        }

        if (actions.isDeleteEnhacedChunks()) {
            List<String> files = deleteChunksDocumentByType(document, DocumentChunkEntity.DocumentChunkType.ENHANCED);
            deleteFiles(collectionName, files);
        }

        if (actions.isDeleteChunks()) {
            List<String> files = deleteChunksDocumentByType(document, DocumentChunkEntity.DocumentChunkType.ORIGINAL);
            deleteFiles(collectionName, files);

            files = deleteChunksDocumentByType(document, DocumentChunkEntity.DocumentChunkType.ORIGINAL_MODIFIED);
            deleteFiles(collectionName, files);
        }

        if (actions.isDeleteDocument()) {
            LOG.error("Not implemented: isDeleteDocument");
        }

        // *************** CREATE ACTIONS ***************

        if (actions.isCreateDocument()) {
            document.setStatus(DocumentEntity.DocumentStatus.PROCESSING);
            LOG.error("Not implemented: isCreateDocument");
        }

        if (actions.isCreateChunks()) {

            Resource resource = new InputStreamResource(remoteFileService.getObject(collectionName, document.getFilename()));
            List<Document> documents = documentParserService.parseAndExtractChunks(document.getFilename(), resource, actions.getChunkConfig());
            saveChunkDocuments(document, documents, DocumentChunkEntity.DocumentChunkType.ORIGINAL);
            uploadFiles(collectionName, documents);

            document.setStatus(DocumentEntity.DocumentStatus.CHUNK);
        }

        if (actions.isCreateEnhacedChunks()) {
            document.setStatus(DocumentEntity.DocumentStatus.ENHANCED);
            LOG.error("Not implemented: isCreateEnhacedChunks");
        }

        if (actions.isCreateEmbeddings()) {
            if (chunks == null)
                chunks = documentChunkRepository.findByDocumentId(document.getId());

            embeddingService.createEmbeddings(collection, document, chunks);

            for (DocumentChunkEntity chunk : chunks) {
                chunk.setEmbedding(Boolean.TRUE);
            }
            documentChunkRepository.saveAll(chunks);

            document.setStatus(DocumentEntity.DocumentStatus.EMBEDDINGS);
        }

        documentRepository.save(document);
    }

    @Override
    public List<DocumentEntity> getDocuments(Long collectionId) {

        return documentRepository.findByCollectionId(collectionId);
    }

    @Override
    @Transactional(readOnly = false)
    public void saveDocumentChunks(Long documentId, DocumentChunkSaveDto dto) throws Exception {

        DocumentEntity document = documentRepository.findById(documentId).orElse(null);
        CollectionEntity collection = collectionService.findById(document.getCollectionId());

        List<String> files = deleteChunksDocumentByType(document, DocumentChunkEntity.DocumentChunkType.ORIGINAL);
        deleteFiles(collection.getName(), files);

        files = deleteChunksDocumentByType(document, DocumentChunkEntity.DocumentChunkType.ORIGINAL_MODIFIED);
        deleteFiles(collection.getName(), files);

        List<Document> chunks = DocumentUtils.createChunksFromArrayString(document, dto.getContents());
        saveChunkDocuments(document, chunks, DocumentChunkEntity.DocumentChunkType.ORIGINAL_MODIFIED);
        uploadFiles(collection.getName(), chunks);
    }

    private void saveChunkDocuments(DocumentEntity documentEntity, List<Document> documents, DocumentChunkEntity.DocumentChunkType type) throws Exception {

        List<DocumentChunkEntity> chunks = new ArrayList<>();

        long order = 1;
        for (Document chunk : documents) {

            DocumentChunkEntity documentChunkEntity = new DocumentChunkEntity();

            documentChunkEntity.setDocument(documentEntity);
            documentChunkEntity.setFilename(chunk.getId());
            documentChunkEntity.setOrder(order++);
            documentChunkEntity.setType(type);

            documentChunkRepository.save(documentChunkEntity);
            chunks.add(documentChunkEntity);
        }
    }

    private void uploadFiles(String collectionName, List<Document> documents) throws Exception {

        for (Document document : documents) {
            remoteFileService.putObject(IOUtils.toInputStream(document.getContent()), collectionName, "chunk/" + document.getId());
        }

    }

    private List<String> deleteChunksDocumentByType(DocumentEntity document, DocumentChunkEntity.DocumentChunkType type) throws Exception {
        List<String> files = new ArrayList<>();
        List<DocumentChunkEntity> chunks = documentChunkRepository.findByDocumentIdAndTypeOrderByOrderDesc(document.getId(), type);

        for (DocumentChunkEntity chunk : chunks) {
            files.add(chunk.getFilename());
            documentChunkRepository.delete(chunk);
        }

        return files;
    }

    private void deleteFiles(String collectionName, List<String> files) throws Exception {
        for (String file : files) {
            remoteFileService.deleteObject(collectionName, "chunk/" + file);
        }
    }

    @Override
    public List<DocumentChunkEntity> getDocumentChunksByDocumentId(Long documentId, DocumentChunkEntity.DocumentChunkType type) {

        List<DocumentChunkEntity> documents = documentChunkRepository.findByDocumentIdAndTypeOrderByOrderDesc(documentId, type);

        if (type == DocumentChunkEntity.DocumentChunkType.ORIGINAL && (documents == null || documents.isEmpty())) {
            documents = documentChunkRepository.findByDocumentIdAndTypeOrderByOrderDesc(documentId, DocumentChunkEntity.DocumentChunkType.ORIGINAL_MODIFIED);
        }

        return documents;
    }

    @Override
    public DocumentChunkContentDto getChunkContentByDocumentAndChunkId(Long documentId, Long chunkId) throws Exception {

        DocumentChunkEntity chunk = documentChunkRepository.findById(chunkId).orElse(null);

        if (chunk == null || chunk.getDocument().getId().equals(documentId) == Boolean.FALSE) {
            return null;
        }

        CollectionEntity collection = collectionService.findById(chunk.getDocument().getCollectionId());
        InputStream stream = remoteFileService.getObject(collection.getName(), "chunk/" + chunk.getFilename());

        String content = IOUtils.toString(stream, StandardCharsets.UTF_8);

        DocumentChunkContentDto result = mapper.map(chunk, DocumentChunkContentDto.class);
        result.setContent(content);

        return result;
    }

    @Override
    public List<DocumentEntity> getDocumentsById(List<Long> documentsId) {
        return documentRepository.findByIdIn(documentsId);
    }

}
