package com.cca.ia.rag.document.dto;

import com.cca.ia.rag.document.model.DocumentEntity;

public class DocumentDto {

    private Long id;
    private String collectionId;
    private String filename;
    private DocumentEntity.DocumentType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public DocumentEntity.DocumentType getType() {
        return type;
    }

    public void setType(DocumentEntity.DocumentType type) {
        this.type = type;
    }
}
