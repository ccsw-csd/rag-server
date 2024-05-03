package com.cca.ia.rag.prompt.model;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PromptPostRepository extends CrudRepository<PromptPostEntity, Long> {

    @Modifying
    void deleteByPromptId(Long id);

    List<PromptPostEntity> findByPromptIdOrderByOrderAsc(long id);
}
