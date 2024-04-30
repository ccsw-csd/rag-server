package com.cca.ia.rag.document.parser;

import org.apache.commons.io.IOUtils;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("textDocumentParser")
public class TextDocumentParser implements DocumentParser {

    @Override
    @Transactional(readOnly = false)
    public List<Document> parseAndExtractChunks(String filename, InputStream stream, ParserConfigDto config) throws Exception {

        String content = IOUtils.toString(stream, StandardCharsets.UTF_8);

        content = content.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");

        String lines[] = content.split("\\r?\\n");

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", filename);
        metadata.put("content_type", "original");

        List<Document> chunks = new ArrayList<>();
        int actualSize = 0;
        int maxTokens = config.getMaxTokens();
        int maxSizeInWords = (int) (maxTokens * 0.75d);
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {

            int paragraphWords = line.split("\\s+").length;
            if (actualSize + paragraphWords < maxSizeInWords) {
                sb.append(line + "\n");

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

        return chunks;
    }

}
