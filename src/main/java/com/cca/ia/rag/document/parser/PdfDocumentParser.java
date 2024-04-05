package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.DocumentChunkRepository;
import com.cca.ia.rag.document.DocumentRepository;
import com.cca.ia.rag.document.DocumentService;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentEntity;
import com.cca.ia.rag.s3.RemoteFileService;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<String> paragraphs = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(resource.getContentAsByteArray())) {

            PDFTextStripper tStripper = new PDFTextStripper();
            int page = 1;

            String pdfFileInText = tStripper.getText(document);

            StringBuilder sb = new StringBuilder();

            // split by whitespace
            String lines[] = pdfFileInText.split("\\r?\\n");
            for (String line : lines) {

                if (line == null || line.trim().length() == 0)
                    continue;

                line = line.trim();
                if (line.equals(String.valueOf(page))) {
                    page++;
                    continue;
                }

                if (line.endsWith(".")) {
                    sb.append(line);
                    paragraphs.add(sb.toString());
                    sb = new StringBuilder();
                } else {
                    sb.append(line + " ");
                }

            }

            paragraphs.add(sb.toString());
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", filename);
        metadata.put("content_type", "original");

        List<Document> chunks = new ArrayList<>();
        int actualSize = 0;
        int maxTokens = 1500;
        int maxSizeInWords = (int) (maxTokens * 0.75d);
        StringBuilder sb = new StringBuilder();

        for (String paragraph : paragraphs) {

            int paragraphWords = paragraph.split("\\s+").length;
            if (actualSize + paragraphWords < maxSizeInWords) {
                sb.append(paragraph + "\n\n");

                actualSize += paragraphWords;
            } else {
                Document document = new Document(sb.toString(), metadata);
                chunks.add(document);

                sb = new StringBuilder();
                actualSize = 0;
            }
        }

        Document document = new Document(sb.toString(), metadata);
        chunks.add(document);


        /*
        ParagraphPdfDocumentReader pdfReader = new ParagraphPdfDocumentReader(resource, PdfDocumentReaderConfig.builder().build());
        //TikaDocumentReader pdfReader = new TikaDocumentReader(resource, ExtractedTextFormatter.builder().withNumberOfBottomTextLinesToDelete(3).withNumberOfTopPagesToSkipBeforeDelete(1).build());

        var tokenTextSplitter = new TokenTextSplitter(2000, 300, 5, 3000, true);

        List<Document> chunks = tokenTextSplitter.apply(pdfReader.get());
        */

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

        remoteFileService.putObject(IOUtils.toInputStream(document.getContent()), collectionName, "chunk/" + document.getId());

    }

    private String normalizeText(String text) {

        return text.replaceAll("(?m)^[ \t]*\r?\n", "");

    }

}
