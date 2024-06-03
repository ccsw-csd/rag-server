package com.cca.ia.rag.document.embedding;

import com.cca.ia.rag.collection.CollectionService;
import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.collection.model.CollectionPropertyDto;
import com.cca.ia.rag.document.DocumentUtils;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentFileEntity;
import org.springframework.ai.chroma.ChromaApi;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.id.IdGenerator;
import org.springframework.ai.document.id.RandomIdGenerator;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.ChromaVectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingServiceDefault implements EmbeddingService {

    @Autowired
    private ChromaApi chromaApi;

    @Autowired
    private CollectionService collectionService;

    private IdGenerator idGenerator = new RandomIdGenerator();

    private EmbeddingModel getEmbeddingClient(Long collectionId) {

        List<CollectionPropertyDto> data = collectionService.findProperties(collectionId);

        String apiKey = data.stream().filter(e -> e.getKey().equals("apiKey")).findFirst().orElseThrow().getValue();

        OpenAiApi openAiApi = new OpenAiApi(apiKey);
        OpenAiEmbeddingModel embeddingClient = new OpenAiEmbeddingModel(openAiApi);

        return embeddingClient;
    }

    @Override
    public void deleteEmbeddings(CollectionEntity collection, List<DocumentChunkEntity> chunks) {

        List<String> ids = new ArrayList<>();

        for (DocumentChunkEntity chunk : chunks) {
            if (StringUtils.hasText(chunk.getEmbedding()))
                ids.add(chunk.getEmbedding());
        }

        if (ids.size() > 0) {
            VectorStore vectorStore = getVectorStore(collection);
            vectorStore.delete(ids);
        }
    }

    @Override
    public List<DocumentChunkEntity> createEmbeddings(CollectionEntity collection, DocumentFileEntity document, List<DocumentChunkEntity> chunks) {

        List<Document> documents = new ArrayList<>();
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("type", "text");
        metadata.put("source", document.getFilename());

        for (DocumentChunkEntity chunk : chunks) {

            metadata.put("chunk-order", chunk.getOrder());
            metadata.put("chunk-modify-type", chunk.getModifyType());
            metadata.put("chunk-type", chunk.getType());

            String content = chunk.getContent();
            String id = idGenerator.generateId(content, metadata);

            chunk.setEmbedding(id);

            documents.add(new Document(id, content, metadata));
        }

        VectorStore vectorStore = getVectorStore(collection);
        vectorStore.add(documents);

        return chunks;
    }

    private List<Document> findSimilarityCodes(VectorStore vectorStore, String question, long maxTokens) {
        if (maxTokens == 0) {
            return new ArrayList<>();
        }

        List<Document> codesFound = vectorStore.similaritySearch(SearchRequest.defaults().withQuery(question).withTopK(100).withSimilarityThreshold(0.5d).withFilterExpression("'chunk-type' == 'CODE'"));

        long actualTokens = 0;

        List<Document> documentList = new ArrayList<>();
        for (Document document : codesFound) {

            long tokens = DocumentUtils.countTokens(document.getContent());
            actualTokens += tokens;

            if (actualTokens < maxTokens) {
                documentList.add(document);
            } else {
                break;
            }

        }

        return documentList;
    }

    private List<Document> findSimilarityDocumentations(VectorStore vectorStore, String question, long maxTokens) {
        if (maxTokens == 0) {
            return new ArrayList<>();
        }

        List<Document> documentsFound = vectorStore.similaritySearch(SearchRequest.defaults().withQuery(question).withTopK(100).withSimilarityThreshold(0.7d));

        long actualTokens = 0;

        List<Document> documentList = new ArrayList<>();
        for (Document document : documentsFound) {

            long tokens = DocumentUtils.countTokens(document.getContent());
            actualTokens += tokens;

            if (actualTokens < maxTokens) {
                documentList.add(document);
            } else {
                break;
            }

        }

        return documentList;
    }

    @Override
    public List<Document> findSimilarity(CollectionEntity collection, String question, int codeQuestion, long maxTokens) {
        VectorStore vectorStore = getVectorStore(collection);

        long maxTokensDocumentation = (long) (maxTokens * (100 - codeQuestion) / 100.0d);
        long maxTokensCode = (long) (maxTokens * codeQuestion / 100.0d);

        List<Document> documents = new ArrayList<>();

        documents.addAll(findSimilarityDocumentations(vectorStore, question, maxTokensDocumentation));
        documents.addAll(findSimilarityCodes(vectorStore, question, maxTokensCode));

        return documents;
    }

    private VectorStore getVectorStore(CollectionEntity collection) {

        EmbeddingModel embeddingClient = getEmbeddingClient(collection.getId());
        ChromaVectorStore vectorStore = new ChromaVectorStore(embeddingClient, chromaApi, collection.getName(), true);

        try {
            vectorStore.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return vectorStore;

    }

}
