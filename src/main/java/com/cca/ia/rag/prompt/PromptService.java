package com.cca.ia.rag.prompt;

import com.cca.ia.rag.prompt.dto.PromptEditDto;
import com.cca.ia.rag.prompt.dto.PromptStatsDto;
import com.cca.ia.rag.prompt.model.PromptEntity;

import java.util.List;

public interface PromptService {

    List<PromptEntity> findAll();

    List<String> getTags(String query);

    void save(PromptEditDto data);

    PromptEditDto getForEdit(long id);

    PromptStatsDto like(long promptId);

    PromptStatsDto view(long promptId);

    void delete(long promptId);
}
