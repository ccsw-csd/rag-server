package com.cca.ia.rag.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentActionsDto {

    private boolean deleteEmbeddings = false;

    private boolean deleteEnhacedChunks = false;

    private boolean deleteChunks = false;

    private boolean deleteDocument = false;

    private boolean createDocument = false;

    private boolean createChunks = false;

    private boolean createEnhacedChunks = false;

    private boolean createEmbeddings = false;

    private DocumentChunkConfigDto chunkConfig = new DocumentChunkConfigDto();

    public DocumentChunkConfigDto getChunkConfig() {
        return chunkConfig;
    }

    public void setChunkConfig(DocumentChunkConfigDto chunkConfig) {
        this.chunkConfig = chunkConfig;
    }

    public boolean isDeleteEmbeddings() {
        return deleteEmbeddings;
    }

    public void setDeleteEmbeddings(boolean deleteEmbeddings) {
        this.deleteEmbeddings = deleteEmbeddings;
    }

    public boolean isDeleteEnhacedChunks() {
        return deleteEnhacedChunks;
    }

    public void setDeleteEnhacedChunks(boolean deleteEnhacedChunks) {
        this.deleteEnhacedChunks = deleteEnhacedChunks;
    }

    public boolean isDeleteChunks() {
        return deleteChunks;
    }

    public void setDeleteChunks(boolean deleteChunks) {
        this.deleteChunks = deleteChunks;
    }

    public boolean isDeleteDocument() {
        return deleteDocument;
    }

    public void setDeleteDocument(boolean deleteDocument) {
        this.deleteDocument = deleteDocument;
    }

    public boolean isCreateDocument() {
        return createDocument;
    }

    public void setCreateDocument(boolean createDocument) {
        this.createDocument = createDocument;
    }

    public boolean isCreateChunks() {
        return createChunks;
    }

    public void setCreateChunks(boolean createChunks) {
        this.createChunks = createChunks;
    }

    public boolean isCreateEnhacedChunks() {
        return createEnhacedChunks;
    }

    public void setCreateEnhacedChunks(boolean createEnhacedChunks) {
        this.createEnhacedChunks = createEnhacedChunks;
    }

    public boolean isCreateEmbeddings() {
        return createEmbeddings;
    }

    public void setCreateEmbeddings(boolean createEmbeddings) {
        this.createEmbeddings = createEmbeddings;
    }
}
