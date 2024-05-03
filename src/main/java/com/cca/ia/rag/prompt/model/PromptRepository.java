package com.cca.ia.rag.prompt.model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PromptRepository extends CrudRepository<PromptEntity, Long> {

    @Query(value = "SELECT DISTINCT tag FROM prompt_tag WHERE tag LIKE %:query% order by tag asc", nativeQuery = true)
    List<String> findTags(@Param("query") String query);
}
