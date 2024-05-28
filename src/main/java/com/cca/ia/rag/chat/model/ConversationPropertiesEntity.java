package com.cca.ia.rag.chat.model;

import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentFileEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "chat_context")
public class ConversationPropertiesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "conversation_id", nullable = false)
    private ConversationEntity conversation;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "path", nullable = true)
    private String path;

    @Column(name = "tokens", nullable = true)
    private Long tokens;

    @Column(name = "chunk_number", nullable = true)
    private Long chunkNumber;

    @Column(name = "modify_type", nullable = true)
    @Enumerated(EnumType.ORDINAL)
    private DocumentChunkEntity.DocumentChunkModifyType modifyType;

    @Column(name = "type", nullable = true)
    @Enumerated(EnumType.ORDINAL)
    private DocumentChunkEntity.DocumentChunkType type;

    @Column(name = "embedding_id", nullable = true)
    private String embeddingId;

    @Column(name = "status", nullable = true)
    @Enumerated(EnumType.ORDINAL)
    private DocumentFileEntity.DocumentStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ConversationEntity getConversation() {
        return conversation;
    }

    public void setConversation(ConversationEntity conversation) {
        this.conversation = conversation;
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
