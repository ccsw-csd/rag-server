package com.cca.ia.rag.document.model;

public class DocumentDto {

    private Long id;
    private String collectionId;
    private String filename;
    private String source;

    private DocumentEntity.DocumentStatus status;

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public DocumentEntity.DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentEntity.DocumentStatus status) {
        this.status = status;
    }
}
