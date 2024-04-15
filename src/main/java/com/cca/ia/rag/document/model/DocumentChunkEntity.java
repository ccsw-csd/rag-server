package com.cca.ia.rag.document.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document_chunk")
public class DocumentChunkEntity {

    public static enum DocumentChunkType {
        ORIGINAL(0), ORIGINAL_MODIFIED(1), ENHANCED(2), PERSONAL(3);

        private final int value;

        DocumentChunkType(int value) {
            this.value = value;
        }

        public static DocumentChunkType fromInt(int value) {
            if (value == 0)
                return ORIGINAL;
            if (value == 1)
                return ORIGINAL_MODIFIED;
            if (value == 2)
                return ENHANCED;
            if (value == 3)
                return PERSONAL;
            throw new RuntimeException("Type not exists");
        }

        public int getValue() {
            return value;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "`order`", nullable = false)
    private Long order;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private DocumentChunkType type;

    @Column(name = "embedding", nullable = false)
    private Boolean embedding;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DocumentEntity getDocument() {
        return document;
    }

    public void setDocument(DocumentEntity document) {
        this.document = document;
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

    public DocumentChunkType getType() {
        return type;
    }

    public void setType(DocumentChunkType type) {
        this.type = type;
    }

    public Boolean getEmbedding() {
        return embedding;
    }

    public void setEmbedding(Boolean embedding) {
        this.embedding = embedding;
    }
}
