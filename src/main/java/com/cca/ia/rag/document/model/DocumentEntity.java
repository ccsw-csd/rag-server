package com.cca.ia.rag.document.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document")
public class DocumentEntity {

    public static enum DocumentStatus {
        PROCESING(0), CHUNK(1), ENHACED(2), EMBEDDINGS(3);

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

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "source", nullable = false)
    private String source;

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

    public Long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Long collectionId) {
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
