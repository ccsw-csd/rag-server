package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.DocumentChunkRepository;
import com.cca.ia.rag.document.DocumentRepository;
import com.cca.ia.rag.document.DocumentService;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentEntity;
import com.cca.ia.rag.s3.RemoteFileService;
import org.apache.commons.io.IOUtils;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PdfDocumentParser implements DocumentParser {

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentChunkRepository documentChunkRepository;

    @Override
    @Transactional(readOnly = false)
    @Async
    public void parseAndPersist(DocumentEntity documentEntity, String collectionName, String filename) throws Exception {

        Resource resource = new InputStreamResource(remoteFileService.getObject(collectionName, "storage/" + filename));

        ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(resource, PdfDocumentReaderConfig.builder().build());
        //TikaDocumentReader pdfReader = new TikaDocumentReader(resource, ExtractedTextFormatter.builder().withNumberOfBottomTextLinesToDelete(3).withNumberOfTopPagesToSkipBeforeDelete(1).build());

        var tokenTextSplitter = new TokenTextSplitter(2000, 300, 5, 3000, true);

        List<Document> chunks = tokenTextSplitter.apply(pdfReader.get());

        long order = 1;
        for (Document chunk : chunks) {

            DocumentChunkEntity documentSplitEntity = new DocumentChunkEntity();

            documentSplitEntity.setDocument(documentEntity);
            documentSplitEntity.setFilename(chunk.getId());
            documentSplitEntity.setOrder(order++);

            documentChunkRepository.save(documentSplitEntity);
            uploadChunk(collectionName, chunk);
        }

        documentEntity.setStatus(DocumentEntity.DocumentStatus.CHUNK);
        documentRepository.save(documentEntity);
    }

    private void uploadChunk(String collectionName, Document document) throws Exception {

        //System.out.println("Metadata");
        //document.getMetadata().forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));

        remoteFileService.putObject(IOUtils.toInputStream(normalizeText(document.getContent())), collectionName, "chunk/" + document.getId());

    }

    private String normalizeText(String text) {

        return text.replaceAll("(?m)^[ \t]*\r?\n", "");

    }

}
