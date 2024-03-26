package com.cca.ia.rag.ingestor;

import com.cca.ia.rag.ingestor.model.EmbeddingEntity;
import org.springframework.data.repository.CrudRepository;

public interface EmbeddingRepository extends CrudRepository<EmbeddingEntity, Long> {
}
