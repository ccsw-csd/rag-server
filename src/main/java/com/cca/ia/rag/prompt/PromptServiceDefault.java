package com.cca.ia.rag.prompt;

import com.cca.ia.rag.config.security.UserUtils;
import com.cca.ia.rag.prompt.dto.PromptEditDto;
import com.cca.ia.rag.prompt.dto.PromptPostDto;
import com.cca.ia.rag.prompt.dto.PromptStatsDto;
import com.cca.ia.rag.prompt.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromptServiceDefault implements PromptService {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private PromptRepository promptRepository;

    @Autowired
    private PromptPostRepository promptPostRepository;

    @Autowired
    private PromptLikeRepository promptLikeRepository;

    @Autowired
    private PromptViewRepository promptViewRepository;

    @Autowired
    private PersonRepository personRepository;

    @Override
    public List<PromptEntity> findAll() {
        return (List<PromptEntity>) promptRepository.findAll();
    }

    @Override
    public List<String> getTags(String query) {
        return promptRepository.findTags(query);
    }

    @Override
    @Transactional(readOnly = false)
    public long save(PromptEditDto data) {

        PromptEntity prompt = null;

        PersonEntity person = personRepository.findByUsernameAndActiveTrue(UserUtils.getUserDetails().getUsername());

        if (data.getId() != null) {
            prompt = promptRepository.findById(data.getId()).orElseThrow(() -> new RuntimeException("Prompt not found"));
        } else {
            prompt = new PromptEntity();
            prompt.setPerson(person);
        }

        prompt.setDate(LocalDateTime.now());
        prompt.setTitle(data.getTitle());
        prompt.setDescription(data.getDescription());
        prompt.setTags(data.getTags());

        promptRepository.save(prompt);

        List<PromptPostEntity> promptPosts = new ArrayList<>();
        promptPostRepository.deleteByPromptId(prompt.getId());
        int order = 1;

        for (PromptPostDto postDto : data.getPosts()) {
            PromptPostEntity promptPost = new PromptPostEntity();
            promptPost.setPrompt(prompt);
            promptPost.setOrder(order++);
            promptPost.setContent(postDto.getContent());
            promptPost.setType(postDto.getType());
            promptPosts.add(promptPost);

        }

        promptPostRepository.saveAll(promptPosts);

        return prompt.getId();
    }

    @Override
    public PromptEditDto getForEdit(long promptId) {

        PromptEntity prompt = promptRepository.findById(promptId).orElseThrow(() -> new RuntimeException("Prompt not found"));

        PromptEditDto promptEdit = mapper.map(prompt, PromptEditDto.class);

        List<PromptPostEntity> prompPosts = promptPostRepository.findByPromptIdOrderByOrderAsc(promptId);
        promptEdit.setPosts(prompPosts.stream().map(e -> mapper.map(e, PromptPostDto.class)).collect(Collectors.toList()));

        PromptLikeEntity promptLike = promptLikeRepository.getByPromptIdAndUsername(promptId, UserUtils.getUserDetails().getUsername());
        promptEdit.setUserLiked(promptLike != null);

        return promptEdit;
    }

    private PromptStatsDto generatePromptStats(long promptId) {
        PromptLikeEntity promptLike = promptLikeRepository.getByPromptIdAndUsername(promptId, UserUtils.getUserDetails().getUsername());

        PromptStatsDto promptStats = new PromptStatsDto();
        promptStats.setLikes(promptLikeRepository.countByPromptId(promptId));
        promptStats.setViews(promptViewRepository.countByPromptId(promptId));
        promptStats.setUserLiked(promptLike != null);

        return promptStats;
    }

    @Override
    public PromptStatsDto like(long promptId) {

        PromptLikeEntity promptLike = promptLikeRepository.getByPromptIdAndUsername(promptId, UserUtils.getUserDetails().getUsername());

        if (promptLike != null) {
            promptLikeRepository.delete(promptLike);
        } else {
            promptLike = new PromptLikeEntity();
            promptLike.setPromptId(promptId);
            promptLike.setUsername(UserUtils.getUserDetails().getUsername());
            promptLikeRepository.save(promptLike);
        }

        return generatePromptStats(promptId);
    }

    @Override
    public PromptStatsDto view(long promptId) {

        PromptViewEntity promptView = new PromptViewEntity();
        promptView.setPromptId(promptId);
        promptView.setUsername(UserUtils.getUserDetails().getUsername());
        promptView.setDate(LocalDateTime.now());
        promptViewRepository.save(promptView);

        return generatePromptStats(promptId);
    }

    @Value("${app.code}")
    private String appCode;

    @Override
    public void delete(long promptId) {

        PromptEntity prompt = promptRepository.findById(promptId).orElseThrow(() -> new RuntimeException("Prompt not found"));

        String username = UserUtils.getUserDetails().getUsername();
        if (!username.equals(prompt.getPerson().getUsername())) {

            boolean isGranted = UserUtils.isGranted(appCode, "ADMIN");
            if (!isGranted) {
                throw new RuntimeException("You can't delete this prompt");
            }
        }

        promptRepository.delete(prompt);
    }

}
