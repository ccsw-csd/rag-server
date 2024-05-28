package com.cca.ia.rag.document.embedding;

import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentFileEntity;
import org.springframework.ai.document.Document;

import java.util.List;

public interface EmbeddingService {

    void deleteEmbeddings(CollectionEntity collection, List<DocumentChunkEntity> chunks);

    List<DocumentChunkEntity> createEmbeddings(CollectionEntity collection, DocumentFileEntity document, List<DocumentChunkEntity> chunks);

    List<Document> findSimilarity(CollectionEntity collection, String question, int codeQuestion, long maxTokens);
}
