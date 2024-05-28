package com.cca.ia.rag.chat.model;

import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentFileEntity;

public class ConversationContextDto {

    private Long id;

    private String filename;

    private String path;

    private Long tokens;

    private Long chunkNumber;

    private DocumentChunkEntity.DocumentChunkModifyType modifyType;

    private DocumentChunkEntity.DocumentChunkType type;

    private String embeddingId;

    private DocumentFileEntity.DocumentStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getTokens() {
        return tokens;
    }

    public void setTokens(Long tokens) {
        this.tokens = tokens;
    }

    public Long getChunkNumber() {
        return chunkNumber;
    }

    public void setChunkNumber(Long chunkNumber) {
        this.chunkNumber = chunkNumber;
    }

    public DocumentChunkEntity.DocumentChunkModifyType getModifyType() {
        return modifyType;
    }

    public void setModifyType(DocumentChunkEntity.DocumentChunkModifyType modifyType) {
        this.modifyType = modifyType;
    }

    public DocumentChunkEntity.DocumentChunkType getType() {
        return type;
    }

    public void setType(DocumentChunkEntity.DocumentChunkType type) {
        this.type = type;
    }

    public String getEmbeddingId() {
        return embeddingId;
    }

    public void setEmbeddingId(String embeddingId) {
        this.embeddingId = embeddingId;
    }

    public DocumentFileEntity.DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentFileEntity.DocumentStatus status) {
        this.status = status;
    }
}
