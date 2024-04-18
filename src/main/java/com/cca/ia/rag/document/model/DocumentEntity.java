package com.cca.ia.rag.document.model;

import com.cca.ia.rag.collection.model.CollectionEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "document")
public class DocumentEntity {

    public static enum DocumentType {
        DOCUMENT(0), CODE(1);

        private final int value;

        DocumentType(int value) {
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
    @JoinColumn(name = "collection_id", nullable = false)
    private CollectionEntity collection;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private DocumentType type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CollectionEntity getCollection() {
        return collection;
    }

    public void setCollection(CollectionEntity collection) {
        this.collection = collection;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
    
    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

}
