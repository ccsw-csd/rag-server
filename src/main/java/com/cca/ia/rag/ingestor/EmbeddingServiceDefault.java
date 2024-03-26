package com.cca.ia.rag.ingestor;

import com.cca.ia.rag.ingestor.model.EmbeddingEntity;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingServiceDefault implements EmbeddingService {

    @Autowired
    EmbeddingRepository embeddingRepository;

    @Override
    public void add(String filename, List<Document> documents) {

        long order = 1;

        for (Document document : documents) {
            EmbeddingEntity embeddingEntity = new EmbeddingEntity();

            String metadata = "type=text,source=document";

            embeddingEntity.setCollectionId("TestCollection");
            embeddingEntity.setFilename(filename);
            embeddingEntity.setDocumentId(document.getId());
            embeddingEntity.setDocumentOrder(order);
            embeddingEntity.setDocumentContent(document.getContent());
            embeddingEntity.setMetadata(metadata);

            embeddingRepository.save(embeddingEntity);
            order++;
        }

    }
}
