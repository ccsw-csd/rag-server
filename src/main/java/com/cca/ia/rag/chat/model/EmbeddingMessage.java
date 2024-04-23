package com.cca.ia.rag.chat.model;

import com.cca.ia.rag.document.model.DocumentChunkEntity;

public class EmbeddingMessage {

    private Long id;

    private String embeddingId;

    private String content;

    private DocumentChunkEntity.DocumentChunkType type;

    private Long order;

    private String document;

    public Long getTokens() {

        return (long) (content.split(" ").length / 0.75d);

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmbeddingId() {
        return embeddingId;
    }

    public void setEmbeddingId(String embeddingId) {
        this.embeddingId = embeddingId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DocumentChunkEntity.DocumentChunkType getType() {
        return type;
    }

    public void setType(DocumentChunkEntity.DocumentChunkType type) {
        this.type = type;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }
}
