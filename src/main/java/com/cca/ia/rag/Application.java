package com.cca.ia.rag;

import org.springframework.ai.chroma.ChromaApi;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.ai.vectorsore.ChromaVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ChromaApi chromaApi(RestTemplate restTemplate) {
        String chromaUrl = "http://frparccsw:8000";
        ChromaApi chromaApi = new ChromaApi(chromaUrl, restTemplate);
        return chromaApi;
    }

    @Bean
    public VectorStore chromaVectorStore(EmbeddingClient embeddingClient, ChromaApi chromaApi) {
        return new ChromaVectorStore(embeddingClient, chromaApi, "TestCollection");
    }
}
