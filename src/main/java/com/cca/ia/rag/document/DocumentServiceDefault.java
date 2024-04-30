package com.cca.ia.rag.document;

import com.cca.ia.rag.collection.CollectionRepository;
import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.document.dto.*;
import com.cca.ia.rag.document.embedding.EmbeddingService;
import com.cca.ia.rag.document.model.*;
import com.cca.ia.rag.document.parser.FactoryDocumentParserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class DocumentServiceDefault implements DocumentService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private DocumentFileRepository documentFileRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private CollectionRepository collectionRepository;

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
    public void generateEmbeddings(Long parentDocumentId, GenerateEmbeddingsDto data) {

        List<DocumentFileEntity> listDocumentFiles = new ArrayList<>();

        if (data.getId() != null) {
            DocumentFileEntity documentFile = documentFileRepository.findById(data.getId()).orElse(null);
            documentFile.setStatus(DocumentFileEntity.DocumentStatus.PROCESSING);
            documentFileRepository.save(documentFile);

            listDocumentFiles.add(documentFile);

        } else {

            List<DocumentFileEntity> documentFilesEntity = documentFileRepository.findByDocumentIdAndPathStartsWith(parentDocumentId, data.getPath());

            for (DocumentFileEntity documentFile : documentFilesEntity) {
                documentFile.setStatus(DocumentFileEntity.DocumentStatus.PROCESSING);
                documentFileRepository.save(documentFile);

                listDocumentFiles.add(documentFile);
            }
        }

        documentService.generateEmbeddings(parentDocumentId, listDocumentFiles);
    }

    @Async
    public void generateEmbeddings(Long parentDocumentId, List<DocumentFileEntity> documentFiles) {

        DocumentEntity document = documentRepository.findById(parentDocumentId).orElse(null);

        for (DocumentFileEntity documentFile : documentFiles) {

            List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentId(documentFile.getId());
            try {
                embeddingService.deleteEmbeddings(document.getCollection(), documentChunks);
                for (DocumentChunkEntity documentChunk : documentChunks) {
                    documentChunk.setEmbedding(null);
                }
                documentChunkRepository.saveAll(documentChunks);

                documentChunks = embeddingService.createEmbeddings(document.getCollection(), documentFile, documentChunks);
                documentChunkRepository.saveAll(documentChunks);

                documentFile.setStatus(DocumentFileEntity.DocumentStatus.EMBEDDINGS);
                documentFileRepository.save(documentFile);
            } catch (Exception e) {
                LOG.error("Error generating embeddings", e);
            }
        }

    }

    private void createDocumentChunkFiles(DocumentFileEntity documentFile, InputStream stream, DocumentChunkConfigDto config) throws Exception {

        List<Document> documents = documentParserService.parseAndExtractChunks(documentFile.getFilename(), stream, config);

        persistDocumentChunkFiles(documentFile, documents, DocumentChunkEntity.DocumentChunkModifyType.ORIGINAL);
    }

    private void persistDocumentChunkFiles(DocumentFileEntity documentFile, List<Document> documents, DocumentChunkEntity.DocumentChunkModifyType modifyType) {

        DocumentChunkEntity.DocumentChunkType chunkType = documentParserService.getChunkType(documentFile.getFilename());

        long order = 1;

        for (Document document : documents) {
            DocumentChunkEntity documentChunkEntity = new DocumentChunkEntity();

            documentChunkEntity.setDocument(documentFile);
            documentChunkEntity.setContent(document.getContent());
            documentChunkEntity.setOrder(order++);
            documentChunkEntity.setModifyType(modifyType);
            documentChunkEntity.setType(chunkType);

            documentChunkRepository.save(documentChunkEntity);
        }
    }

    private void createDocumentFiles(DocumentEntity document, MultipartFile file, DocumentChunkConfigDto config) throws Exception {

        String filename = document.getFilename();

        if (filename.endsWith(".pdf")) {
            DocumentFileEntity documentFile = new DocumentFileEntity();

            documentFile.setDocument(document);
            documentFile.setFilename(filename);
            documentFile.setStatus(DocumentFileEntity.DocumentStatus.PROCESSING);
            documentFileRepository.save(documentFile);

            createDocumentChunkFiles(documentFile, file.getInputStream(), config);

            documentFile.setStatus(DocumentFileEntity.DocumentStatus.CHUNK);
            documentFileRepository.save(documentFile);

        } else if (filename.endsWith(".zip")) {

            List<String> filesAccepted = Arrays.asList(".pdf", ".java", ".xml", ".properties", ".yaml", ".sql");
            List<String> ignorePaths = Arrays.asList("src/test/", "/target/");

            InputStream stream = file.getInputStream();
            try (ZipInputStream zis = new ZipInputStream(stream)) {

                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {

                    String zipEntryName = zipEntry.getName();

                    if (ignorePaths.stream().anyMatch(zipEntryName::contains)) {
                        zipEntry = zis.getNextEntry();
                        continue;
                    }

                    String extensionFile = zipEntryName.contains(".") ? zipEntryName.substring(zipEntryName.lastIndexOf(".")) : null;

                    if (filesAccepted.contains(extensionFile)) {

                        String zipLastname = zipEntryName.substring(zipEntryName.lastIndexOf("/") + 1);

                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                        InputStream extractedStream = new ByteArrayInputStream(out.toByteArray());

                        DocumentFileEntity documentFile = new DocumentFileEntity();

                        documentFile.setDocument(document);
                        documentFile.setFilename(zipLastname);
                        documentFile.setStatus(DocumentFileEntity.DocumentStatus.PROCESSING);
                        documentFile.setPath(filename + "/" + zipEntryName.replace("/" + zipLastname, ""));
                        documentFileRepository.save(documentFile);

                        createDocumentChunkFiles(documentFile, extractedStream, config);

                        documentFile.setStatus(DocumentFileEntity.DocumentStatus.CHUNK);
                        documentFileRepository.save(documentFile);

                        extractedStream.close();
                        out.close();
                    }

                    zipEntry = zis.getNextEntry();
                    // ...
                }

                zis.closeEntry();

            }
        }

    }

    @Override
    public void uploadDocuments(Long collectionId, DocumentUploadFormDataDto data) throws Exception {

        DocumentChunkConfigDto config = new DocumentChunkConfigDto();
        config.setChunkSizeDocumentation(data.getTokensDocumentation());
        config.setChunkSizeCode(data.getTokensCode());

        List<DocumentEntity> documents = new ArrayList<>();
        CollectionEntity collection = collectionRepository.findById(collectionId).orElseThrow(() -> new RuntimeException("Collection not found"));

        for (MultipartFile file : data.getFile()) {
            DocumentEntity document = new DocumentEntity();

            document.setType(DocumentEntity.DocumentType.DOCUMENT);
            document.setCollection(collection);
            document.setFilename(file.getOriginalFilename());

            documents.add(documentRepository.save(document));

            createDocumentFiles(document, file, config);
        }

    }

    @Override
    public void deleteAllFromSourceDocument(Long documentId) throws Exception {

        DocumentEntity document = documentRepository.findById(documentId).orElse(null);
        List<DocumentFileEntity> documentFiles = documentFileRepository.findByDocumentId(documentId);
        List<DocumentChunkEntity> documentChunks = documentChunkRepository.findByDocumentDocumentId(documentId);

        embeddingService.deleteEmbeddings(document.getCollection(), documentChunks);

        documentChunkRepository.deleteAll(documentChunks);
        documentFileRepository.deleteAll(documentFiles);
        documentRepository.delete(document);
    }

    private void executeActionsAsync(DocumentEntity documentSource, DocumentFileEntity document, GenerateEmbeddingsDto actions) throws Exception {
/*
        CollectionEntity collection = null;

        if (documentSource != null)
            collection = documentSource.getCollection();
        else if (document != null)
            collection = document.getDocument().getCollection();

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
            //deleteChunkFiles(collectionName, files);
            //TODO rehacer
        }

        if (actions.isDeleteChunks()) {
            List<String> files = deleteChunksDocumentByType(document, DocumentChunkEntity.DocumentChunkType.ORIGINAL);
            //deleteChunkFiles(collectionName, files);
            //TODO rehacer

            files = deleteChunksDocumentByType(document, DocumentChunkEntity.DocumentChunkType.ORIGINAL_MODIFIED);
            //deleteChunkFiles(collectionName, files);
            //TODO rehacer

            files = deleteChunksDocumentByType(document, DocumentChunkEntity.DocumentChunkType.PERSONAL);
            //deleteChunkFiles(collectionName, files);
            //TODO rehacer
        }

        // *************** CREATE ACTIONS ***************

        if (actions.isCreateEnhacedChunks()) {
            document.setStatus(DocumentFileEntity.DocumentStatus.ENHANCED);
            LOG.error("Not implemented: isCreateEnhacedChunks");
        }

        if (actions.isCreateEmbeddings()) {
            if (chunks == null)
                chunks = documentChunkRepository.findByDocumentId(document.getId());

            embeddingService.createEmbeddings(collection, document, chunks);

            for (DocumentChunkEntity chunk : chunks) {
                //chunk.setEmbedding(Boolean.TRUE);
            }
            documentChunkRepository.saveAll(chunks);

            document.setStatus(DocumentFileEntity.DocumentStatus.EMBEDDINGS);
        }

        documentFileRepository.save(document);

 */
    }

    @Override
    public List<DocumentFileEntity> getDocuments(Long collectionId) {

        return documentFileRepository.findByDocumentCollectionIdOrderByDocumentFilenameAsc(collectionId);
    }

    @Override
    @Transactional(readOnly = false)
    public void saveDocumentChunks(Long documentId, DocumentChunkSaveDto dto) throws Exception {

        DocumentFileEntity documentFile = documentFileRepository.findById(documentId).orElse(null);
        CollectionEntity collection = documentFile.getDocument().getCollection();

        List<DocumentChunkEntity> documentChunks = new ArrayList<>();

        documentChunks.addAll(documentChunkRepository.findByDocumentIdAndModifyTypeOrderByOrderDesc(documentId, DocumentChunkEntity.DocumentChunkModifyType.ORIGINAL));
        documentChunks.addAll(documentChunkRepository.findByDocumentIdAndModifyTypeOrderByOrderDesc(documentId, DocumentChunkEntity.DocumentChunkModifyType.ORIGINAL_MODIFIED));

        try {
            embeddingService.deleteEmbeddings(collection, documentChunks);
            for (DocumentChunkEntity documentChunk : documentChunks) {
                documentChunk.setEmbedding(null);
            }
            documentChunkRepository.deleteAll(documentChunks);
        } catch (Exception e) {
            LOG.error("Error remove embeddings", e);
        }

        List<Document> documents = DocumentUtils.createChunksFromArrayString(documentFile, dto.getContents());

        persistDocumentChunkFiles(documentFile, documents, DocumentChunkEntity.DocumentChunkModifyType.ORIGINAL_MODIFIED);

    }

    private String deleteDocumentFileEntity(DocumentFileEntity document) throws Exception {
        String filename = document.getFilename();
        documentFileRepository.delete(document);
        return filename;
    }

    private List<String> deleteChunksDocumentByType(DocumentFileEntity document, DocumentChunkEntity.DocumentChunkModifyType type) throws Exception {
        //TODO rehacer
        List<String> files = new ArrayList<>();
        /*
        List<DocumentChunkEntity> chunks = documentChunkRepository.findByDocumentIdAndTypeOrderByOrderDesc(document.getId(), type);

        for (DocumentChunkEntity chunk : chunks) {

            String remotePath = StringUtils.hasText(document.getPath()) ? document.getPath() + "/" : "";
            files.add(remotePath + chunk.getFilename());

            documentChunkRepository.delete(chunk);
        }
         */

        return files;
    }

    @Override
    public List<DocumentChunkEntity> getDocumentChunksByDocumentId(Long documentId, DocumentChunkEntity.DocumentChunkModifyType type) {

        List<DocumentChunkEntity> documents = documentChunkRepository.findByDocumentIdAndModifyTypeOrderByOrderDesc(documentId, type);

        if (type == DocumentChunkEntity.DocumentChunkModifyType.ORIGINAL && (documents == null || documents.isEmpty())) {
            documents = documentChunkRepository.findByDocumentIdAndModifyTypeOrderByOrderDesc(documentId, DocumentChunkEntity.DocumentChunkModifyType.ORIGINAL_MODIFIED);
        }

        return documents;
    }

    @Override
    public DocumentChunkContentDto getChunkContentByDocumentAndChunkId(Long documentId, Long chunkId) throws Exception {

        DocumentChunkEntity chunk = documentChunkRepository.findById(chunkId).orElse(null);

        if (chunk == null || chunk.getDocument().getId().equals(documentId) == Boolean.FALSE) {
            return null;
        }

        return null;

        //TODO rehacer
        /*

        DocumentFileEntity document = documentFileRepository.findById(documentId).orElse(null);
        CollectionEntity collection = document.getDocument().getCollection();

        InputStream stream = remoteFileService.getObject(collection.getName(), "chunk/" + chunk.getFilename());

        String content = IOUtils.toString(stream, StandardCharsets.UTF_8);

        DocumentChunkContentDto result = mapper.map(chunk, DocumentChunkContentDto.class);
        result.setContent(content);

        return result;

         */
    }

    @Override
    public List<DocumentFileEntity> getDocumentsById(List<Long> documentsId) {
        return documentFileRepository.findByIdIn(documentsId);
    }

}
