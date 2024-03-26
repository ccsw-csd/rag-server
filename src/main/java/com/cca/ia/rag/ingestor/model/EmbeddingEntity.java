package com.cca.ia.rag.ingestor.model;

import jakarta.persistence.*;

@Entity
@Table(name = "embedding")
public class EmbeddingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "collection_id", nullable = false)
    private String collectionId;

    @Column(name = "file_name", nullable = false)
    private String filename;

    @Column(name = "document_id", nullable = false)
    private String documentId;

    @Column(name = "document_order", nullable = false)
    private Long documentOrder;

    @Column(name = "document_content", nullable = false)
    private String documentContent;

    @Column(name = "metadata", nullable = true)
    private String metadata;

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

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Long getDocumentOrder() {
        return documentOrder;
    }

    public void setDocumentOrder(Long documentOrder) {
        this.documentOrder = documentOrder;
    }

    public String getDocumentContent() {
        return documentContent;
    }

    public void setDocumentContent(String documentContent) {
        this.documentContent = documentContent;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }
}
