package com.cca.ia.rag.chat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDto {

    private Long id;
    private String author;

    private boolean user = false;
    private String content;

    private long tokens;

    private LocalDateTime date;

    private Long spentTime;

    private List<String> embeddings = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public boolean isUser() {
        return user;
    }

    public void setUser(boolean user) {
        this.user = user;
    }

    public long getTokens() {
        return tokens;
    }

    public void setTokens(long tokens) {
        this.tokens = tokens;
    }

    public Long getSpentTime() {
        return spentTime;
    }

    public void setSpentTime(Long spentTime) {
        this.spentTime = spentTime;
    }

    public List<String> getEmbeddings() {
        return embeddings;
    }

    public void setEmbeddings(List<String> embeddings) {
        this.embeddings = embeddings;
    }
}
