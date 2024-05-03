package com.cca.ia.rag.prompt.dto;

import java.util.ArrayList;
import java.util.List;

public class PromptEditDto {

    private Long id;

    private PersonDto person;

    private String title;

    private String description;

    private long views;

    private long likes;

    private boolean userLiked;

    private List<String> tags = new ArrayList<>();

    private List<PromptPostDto> posts = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonDto getPerson() {
        return person;
    }

    public void setPerson(PersonDto person) {
        this.person = person;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<PromptPostDto> getPosts() {
        return posts;
    }

    public void setPosts(List<PromptPostDto> posts) {
        this.posts = posts;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public long getLikes() {
        return likes;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public boolean getUserLiked() {
        return userLiked;
    }

    public void setUserLiked(boolean userLiked) {
        this.userLiked = userLiked;
    }
}
