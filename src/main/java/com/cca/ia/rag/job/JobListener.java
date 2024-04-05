package com.cca.ia.rag.job;

import com.cca.ia.rag.job.model.JobContent;
import com.cca.ia.rag.job.model.JobEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class JobListener {

    private static final Logger logger = LoggerFactory.getLogger(JobListener.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private JobRepository repository;

    @Autowired
    private JobExecutor jobReaderData;

    //@Scheduled(fixedRate = 60000)
    @Transactional(readOnly = false)
    public void listener() {

        repository.findByStatus(JobEntity.JobStatus.PENDING).forEach(job -> {

            logger.debug("Se ha encontrado el job: " + job.getId() + " y contenido: " + job.getContent());
            job.setStartDate(LocalDateTime.now());
            //job.setStatus(JobEntity.JobStatus.RUNNING);

            try {
                JobContent jobContent = mapper.readValue(job.getContent(), JobContent.class);

                if (StringUtils.hasText(jobContent.getType()) && jobContent.getType().startsWith("read_")) {
                    jobReaderData.execute(job, jobContent);
                    return;
                }

                throw new RuntimeException("No se ha encontrado un procesador para este tipo");
            } catch (Exception e) {
                logger.error("Error al procesar el job: " + job.getId() + " y contenido: " + job.getContent(), e);
                registerErrorJob(job, e.getMessage());
            }
        });

    }

    private void registerErrorJob(JobEntity job, String errorContent) {
        //job.setStatus(JobEntity.JobStatus.FAILED);
        job.setErrorContent(errorContent);
        job.setEndDate(LocalDateTime.now());
        repository.save(job);
    }

}
