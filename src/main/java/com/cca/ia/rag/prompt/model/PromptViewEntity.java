package com.cca.ia.rag.prompt.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prompt_view")
public class PromptViewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "prompt_id", nullable = false)
    private long promptId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
