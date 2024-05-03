package com.cca.ia.rag.prompt.model;

import org.springframework.data.repository.CrudRepository;

public interface PromptLikeRepository extends CrudRepository<PromptLikeEntity, Long> {

    PromptLikeEntity getByPromptIdAndUsername(long promptId, String username);

    int countByPromptId(long promptId);

}
