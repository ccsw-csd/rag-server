package com.cca.ia.rag.document.model;

public class DocumentChunkDto {

    private Long id;
    private Long order;
    private String splitFile;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public String getSplitFile() {
        return splitFile;
    }

    public void setSplitFile(String splitFile) {
        this.splitFile = splitFile;
    }
}