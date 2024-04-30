package com.cca.ia.rag.document;

import com.cca.ia.rag.document.model.DocumentFileEntity;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentUtils {

    public static List<Document> createChunksFromArrayString(DocumentFileEntity document, String[] contents) throws Exception {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("filename", document.getFilename());
        metadata.put("content_type", "original_modified");

        List<Document> chunks = new ArrayList<>();

        for (String chunkContent : contents) {
            chunkContent = trimEmptyLines(chunkContent);
            chunks.add(new Document(chunkContent, metadata));
        }

        return chunks;
    }

    public static long countTokens(String content) {

        long tokensByWords = (long) (content.split("\\s+").length / 0.6d);
        long tokensByChars = (long) (content.replaceAll(" ", "").replaceAll("\n", "").length() / 3.25d);

        return Math.max(tokensByWords, tokensByChars);
    }

    private static String trimEmptyLines(String content) {
        return content.replaceAll("\\n+$", "").replaceFirst("^\\n+", "");
    }

}
