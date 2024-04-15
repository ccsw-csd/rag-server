package com.cca.ia.rag.document;

import com.cca.ia.rag.document.model.DocumentEntity;
import org.springframework.ai.document.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentUtils {

    public static List<Document> createChunksFromArrayString(DocumentEntity document, String[] contents) throws Exception {

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

    private static String trimEmptyLines(String content) {
        return content.replaceAll("\\n+$", "").replaceFirst("^\\n+", "");
    }

}
