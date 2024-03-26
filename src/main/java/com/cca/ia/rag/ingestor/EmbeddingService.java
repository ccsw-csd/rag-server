package com.cca.ia.rag.ingestor;

import org.springframework.ai.document.Document;

import java.util.List;

public interface EmbeddingService {

    void add(String filename, List<Document> documents);


}
