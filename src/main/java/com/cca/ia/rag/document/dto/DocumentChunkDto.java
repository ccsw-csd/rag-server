package com.cca.ia.rag.document.dto;

import com.cca.ia.rag.document.model.DocumentChunkEntity;

public class DocumentChunkDto {

    private Long id;
    private Long order;

    private String content;

    private DocumentChunkEntity.DocumentChunkModifyType type;

    public DocumentChunkEntity.DocumentChunkModifyType getType() {
        return type;
    }

    public void setType(DocumentChunkEntity.DocumentChunkModifyType type) {
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
