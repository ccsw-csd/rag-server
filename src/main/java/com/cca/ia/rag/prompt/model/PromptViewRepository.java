package com.cca.ia.rag.prompt.model;

import org.springframework.data.repository.CrudRepository;

public interface PromptViewRepository extends CrudRepository<PromptViewEntity, Long> {

    int countByPromptId(long promptId);

}
