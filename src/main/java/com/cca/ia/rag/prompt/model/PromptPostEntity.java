package com.cca.ia.rag.prompt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "prompt_post")
public class PromptPostEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prompt_id", nullable = false)
    private PromptEntity prompt;

    @Column(name = "`order`", nullable = false)
    private long order;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "type", nullable = false)
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PromptEntity getPrompt() {
        return prompt;
    }

    public void setPrompt(PromptEntity prompt) {
        this.prompt = prompt;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
