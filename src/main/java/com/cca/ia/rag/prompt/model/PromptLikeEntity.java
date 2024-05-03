package com.cca.ia.rag.prompt.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "prompt_like")
public class PromptLikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "prompt_id", nullable = false)
    private long promptId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getPromptId() {
        return promptId;
    }

    public void setPromptId(long promptId) {
        this.promptId = promptId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
