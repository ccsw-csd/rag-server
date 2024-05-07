package com.cca.ia.rag.prompt;

import com.cca.ia.rag.prompt.dto.PromptDto;
import com.cca.ia.rag.prompt.dto.PromptEditDto;
import com.cca.ia.rag.prompt.dto.PromptStatsDto;
import com.cca.ia.rag.prompt.model.PromptEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/prompt")
public class PromptController {

    @Autowired
    private PromptService promptService;

    @Autowired
    private ModelMapper mapper;

    @GetMapping("")
    public List<PromptDto> findAll() throws Exception {

        List<PromptEntity> prompts = promptService.findAll();

        return prompts.stream().map(e -> mapper.map(e, PromptDto.class)).collect(Collectors.toList());

    }

    @GetMapping("/{promptId}")
    public PromptEditDto get(@PathVariable long promptId) throws Exception {
        return promptService.getForEdit(promptId);
    }

    @DeleteMapping("/{promptId}")
    public void delete(@PathVariable long promptId) throws Exception {
        promptService.delete(promptId);
    }

    @GetMapping("/tags/{query}")
    public List<String> getTags(@PathVariable String query) {
        return promptService.getTags(query);
    }

    @PutMapping("")
    @ResponseBody
    public long save(@RequestBody PromptEditDto data) {
        return promptService.save(data);
    }

    @PostMapping("/{id}/like")
    public PromptStatsDto like(@PathVariable long id) throws Exception {
        return promptService.like(id);
    }

    @PostMapping("/{id}/view")
    public PromptStatsDto view(@PathVariable long id) throws Exception {
        return promptService.view(id);
    }

}
