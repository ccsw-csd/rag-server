package com.cca.ia.rag.document;

import com.cca.ia.rag.collection.CollectionService;
import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.document.model.DocumentChunkContentDto;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentEntity;
import com.cca.ia.rag.s3.RemoteFileService;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class DocumentServiceDefault implements DocumentService {

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private ModelMapper mapper;

    @Override
    public List<DocumentEntity> getDocuments(Long collectionId) {

        return documentRepository.findByCollectionId(collectionId);
    }

    @Override
    public List<DocumentChunkEntity> getDocumentChunksByDocumentId(Long documentId) {
        return documentChunkRepository.findByDocumentId(documentId);
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
