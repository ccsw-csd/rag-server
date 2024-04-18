package com.cca.ia.rag.document.dto;

import com.cca.ia.rag.document.model.DocumentFileEntity;

public class DocumentFileDto {

    private Long id;
    private DocumentDto document;
    private String filename;

    private String path;

    private DocumentFileEntity.DocumentStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentDto getDocument() {
        return document;
    }

    public void setDocument(DocumentDto document) {
        this.document = document;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public DocumentFileEntity.DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentFileEntity.DocumentStatus status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
