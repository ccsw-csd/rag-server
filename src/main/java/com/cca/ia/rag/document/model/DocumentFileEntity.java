package com.cca.ia.rag.document.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document_file")
public class DocumentFileEntity {

    public static enum DocumentStatus {
        PROCESSING(0), CHUNK(1), ENHANCED(2), EMBEDDINGS(3);

        private final int value;

        DocumentStatus(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    ;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id", nullable = false)
    private DocumentEntity document;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private DocumentStatus status;

    @Column(name = "path", nullable = true)
    private String path;

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
