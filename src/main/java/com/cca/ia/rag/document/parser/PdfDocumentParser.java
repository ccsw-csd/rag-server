package com.cca.ia.rag.document.parser;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("pdfDocumentParser")
public class PdfDocumentParser implements DocumentParser {

    @Override
    @Transactional(readOnly = false)
    public List<Document> parseAndExtractChunks(String filename, InputStream stream, ParserConfigDto config) throws Exception {

        List<String> paragraphs = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(IOUtils.toByteArray(stream))) {

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
                    sb.append(line + "\n");
                }

            }

            paragraphs.add(sb.toString());
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", filename);
        metadata.put("content_type", "original");

        List<Document> chunks = new ArrayList<>();
        int actualSize = 0;
        int maxTokens = config.getMaxTokens();
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

        return chunks;
    }

    private String normalizeText(String text) {

        return text.replaceAll("(?m)^[ \t]*\r?\n", "");

    }

}
