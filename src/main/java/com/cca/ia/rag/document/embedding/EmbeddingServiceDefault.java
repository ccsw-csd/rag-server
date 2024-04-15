package com.cca.ia.rag.document.embedding;

import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentEntity;
import com.cca.ia.rag.s3.RemoteFileService;
import org.springframework.ai.chroma.ChromaApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorsore.ChromaVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingServiceDefault implements EmbeddingService {

    @Autowired
    private EmbeddingClient embeddingClient;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private ChromaApi chromaApi;

    @Override
    public void deleteEmbeddings(CollectionEntity collection, List<DocumentChunkEntity> chunks) {

        List<String> ids = new ArrayList<>();

        for (DocumentChunkEntity chunk : chunks) {
            if (chunk.getEmbedding())
                ids.add(chunk.getFilename());
        }

        if (ids.size() > 0) {
            VectorStore vectorStore = getVectorStore(collection.getName());
            vectorStore.delete(ids);
        }
    }

    @Override
    public void createEmbeddings(CollectionEntity collection, DocumentEntity document, List<DocumentChunkEntity> chunks) throws Exception {

        List<Document> documents = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("type", "text");
        metadata.put("source", document.getFilename());

        for (DocumentChunkEntity chunk : chunks) {

            String content = remoteFileService.getContent(collection.getName(), "chunk/" + chunk.getFilename());
            String id = chunk.getFilename();

            documents.add(new Document(id, content, metadata));

        }

        VectorStore vectorStore = getVectorStore(collection.getName());
        vectorStore.add(documents);
    }

    @Override
    public List<Document> findSimilarity(String collectionName, String question) {
        VectorStore vectorStore = getVectorStore(collectionName);

        return vectorStore.similaritySearch(SearchRequest.defaults().withQuery(question).withTopK(10).withSimilarityThreshold(0.8d));
    }

    private VectorStore getVectorStore(String collectionName) {

        ChromaVectorStore vectorStore = new ChromaVectorStore(embeddingClient, chromaApi, collectionName);

        try {
            vectorStore.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return vectorStore;

    }

}
