package com.cca.ia.rag.job;

import com.cca.ia.rag.job.model.JobContent;
import com.cca.ia.rag.job.model.JobEntity;

public interface JobExecutor {

    void execute(JobEntity job, JobContent jobContent) throws Exception;
}
