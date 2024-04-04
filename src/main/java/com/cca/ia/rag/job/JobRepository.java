package com.cca.ia.rag.job;

import com.cca.ia.rag.ingestor.model.EmbeddingEntity;
import com.cca.ia.rag.job.model.JobEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JobRepository extends CrudRepository<JobEntity, Long> {

    List<JobEntity> findByStatus(JobEntity.JobStatus status);

}
