package com.cca.ia.rag.document.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentChunkConfigDto {

    private int chunkSizeDocumentation = 1500;

    private int chunkSizeCode = 4000;

    public int getChunkSizeDocumentation() {
        return chunkSizeDocumentation;
    }

    public void setChunkSizeDocumentation(int chunkSizeDocumentation) {
        this.chunkSizeDocumentation = chunkSizeDocumentation;
    }

    public int getChunkSizeCode() {
        return chunkSizeCode;
    }

    public void setChunkSizeCode(int chunkSizeCode) {
        this.chunkSizeCode = chunkSizeCode;
    }
}
