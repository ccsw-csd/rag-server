package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.DocumentChunkRepository;
import com.cca.ia.rag.document.DocumentRepository;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentEntity;
import com.cca.ia.rag.document.model.DocumentParserDto;
import com.cca.ia.rag.s3.RemoteFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DocumentParserServiceDefault implements DocumentParserService {

    @Autowired
    private DocumentParser pdfDocumentParser;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private RemoteFileService remoteFileService;

    @Override
    @Transactional(readOnly = false)
    public void parse(DocumentParserDto dto) throws Exception {

        if (dto.isOverwrite())
            deleteDocumentsAndChunks(dto.getCollectionId(), dto.getFilename());

        String filename = dto.getFilename();
        if (filename.endsWith(".pdf")) {
            parsePdfDocument(dto);
            return;
        }

        throw new Exception("Unsupported file type");

    }

    private void deleteDocumentsAndChunks(Long collectionId, String filename) throws Exception {

        //TODO Buscamos la collection y sacamos su nombre
        //String collectionName = document.getCollection().getName();
        String collectionName = "mentconnect";

        List<DocumentEntity> documents = documentRepository.findByCollectionIdAndFilename(collectionId, filename);

        for (DocumentEntity document : documents) {

            List<DocumentChunkEntity> chunks = documentChunkRepository.findByDocumentId(document.getId());

            for (DocumentChunkEntity chunk : chunks) {
                remoteFileService.deleteObject(collectionName, "chunk/" + chunk.getFilename());
                documentChunkRepository.delete(chunk);
            }

            //remoteFileService.deleteObject(collectionName, "storage/" + document.getFilename());
            //documentRepository.delete(document);
        }

    }

    private void parsePdfDocument(DocumentParserDto dto) throws Exception {

        Long collectionId = dto.getCollectionId();
        //TODO: Buscamos la collection y sacamos su nombre
        String collectionName = "mentconnect";

        String filename = dto.getFilename();

        String parseType = dto.getParseType();

        DocumentEntity documentEntity = null;
        if (parseType.contains("load"))
            documentEntity = loadDocument(dto);
        else
            documentEntity = documentRepository.findByCollectionIdAndFilename(collectionId, filename).get(0);

        documentEntity.setStatus(DocumentEntity.DocumentStatus.PROCESING);
        documentRepository.save(documentEntity);

        if (parseType.contains("chunk"))
            pdfDocumentParser.parseAndPersist(documentEntity, collectionName, filename);
        
    }

    private DocumentEntity loadDocument(DocumentParserDto dto) throws Exception {

        //TODO: Cambiar por llamada a CollectionId.id
        Long collectionId = dto.getCollectionId();
        String collectionName = "mentconnect";
        String filename = dto.getFilename();

        DocumentEntity documentEntity = new DocumentEntity();
        documentEntity.setCollectionId(collectionId);
        documentEntity.setFilename(filename);
        documentEntity.setSource(filename);
        documentEntity.setStatus(DocumentEntity.DocumentStatus.PROCESING);
        documentRepository.save(documentEntity);

        Resource resource = new InputStreamResource(remoteFileService.getObject(collectionName, filename));
        remoteFileService.putObject(resource.getInputStream(), collectionName, "storage/" + filename);

        return documentEntity;
    }

}
