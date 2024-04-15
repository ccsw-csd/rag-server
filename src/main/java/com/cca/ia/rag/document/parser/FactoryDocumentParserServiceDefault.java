package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.model.DocumentChunkConfigDto;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FactoryDocumentParserServiceDefault implements FactoryDocumentParserService {

    @Autowired
    private DocumentParser pdfDocumentParser;

    /*
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private CollectionService collectionService;
    */

    @Override
    public List<Document> parseAndExtractChunks(String filename, Resource resource, DocumentChunkConfigDto config) throws Exception {

        if (filename.endsWith(".pdf")) {
            return pdfDocumentParser.parseAndExtractChunks(resource, config);
        }

        throw new Exception("Unsupported file type");
    }

/*

    @Override
    public void saveDocumentChunks(DocumentEntity document, String[] contents) throws Exception {

        CollectionEntity collection = collectionService.findById(document.getCollectionId());

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", document.getFilename());
        metadata.put("content_type", "original_modified");

        List<Document> chunks = new ArrayList<>();

        for (String chunkContent : contents) {
            chunkContent = trimEmptyLines(chunkContent);
            chunks.add(new Document(chunkContent, metadata));
        }

        uploadChunkDocuments(collection, document, chunks);
    }

 */

    private String trimEmptyLines(String content) {
        return content.replaceAll("\\n+$", "").replaceFirst("^\\n+", "");
    }

    /*
    private void deleteDocumentsAndChunks(Long collectionId, String filename) throws Exception {

        CollectionEntity collection = collectionService.findById(collectionId);

        List<DocumentEntity> documents = documentRepository.findByCollectionIdAndFilename(collectionId, filename);

        for (DocumentEntity document : documents) {
            deleteDocumentChunks(document);
        }

    }

     */

    private List<Document> parsePdfDocument(Resource resource) throws Exception {

        /*
        Long collectionId = dto.getCollectionId();
        CollectionEntity collection = collectionService.findById(collectionId);

        String filename = dto.getFilename();

        String parseType = dto.getParseType();

        DocumentEntity documentEntity = null;
        if (parseType.contains("load"))
            documentEntity = loadDocument(dto);
        else
            documentEntity = documentRepository.findByCollectionIdAndFilename(collectionId, filename).get(0);

        documentEntity.setStatus(DocumentEntity.DocumentStatus.PROCESSING);
        documentRepository.save(documentEntity);

        if (parseType.contains("chunk")) {

         */
        // return pdfDocumentParser.parseAndExtractChunks(resource);

            /*
            uploadChunkDocuments(collection, documentEntity, chunkDocuments);

            documentEntity.setStatus(DocumentEntity.DocumentStatus.CHUNK);
            documentRepository.save(documentEntity);
        }
        */
        return null;
    }

    /*
    private void uploadChunkDocuments(CollectionEntity collection, DocumentEntity documentEntity, List<Document> documents) throws Exception {

        long order = 1;
        for (Document chunk : documents) {

            DocumentChunkEntity documentSplitEntity = new DocumentChunkEntity();

            documentSplitEntity.setDocument(documentEntity);
            documentSplitEntity.setFilename(chunk.getId());
            documentSplitEntity.setOrder(order++);

            documentChunkRepository.save(documentSplitEntity);
            uploadChunk(collection.getName(), chunk);
        }

    }

    private void uploadChunk(String collectionName, Document document) throws Exception {
        remoteFileService.putObject(IOUtils.toInputStream(document.getContent()), collectionName, "chunk/" + document.getId());
    }

    private DocumentEntity loadDocument(DocumentParserDto dto) throws Exception {

        Long collectionId = dto.getCollectionId();
        CollectionEntity collection = collectionService.findById(collectionId);
        String filename = dto.getFilename();

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setCollectionId(collectionId);
        documentEntity.setFilename(filename);
        documentEntity.setSource(filename);
        documentEntity.setStatus(DocumentEntity.DocumentStatus.PROCESSING);
        documentRepository.save(documentEntity);

        Resource resource = new InputStreamResource(remoteFileService.getObject(collection.getName(), filename));
        remoteFileService.putObject(resource.getInputStream(), collection.getName(), "storage/" + filename);

        return documentEntity;
    }
    */
}
