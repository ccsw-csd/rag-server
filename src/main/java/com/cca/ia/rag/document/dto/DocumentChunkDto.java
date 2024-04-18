package com.cca.ia.rag.document.dto;

import com.cca.ia.rag.document.model.DocumentChunkEntity;

public class DocumentChunkDto {

    private Long id;
    private Long order;
    private String filename;

    private DocumentChunkEntity.DocumentChunkType type;

    public DocumentChunkEntity.DocumentChunkType getType() {
        return type;
    }

    public void setType(DocumentChunkEntity.DocumentChunkType type) {
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
