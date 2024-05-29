package com.cca.ia.rag.chat.assistant.model;

import com.cca.ia.rag.document.model.DocumentChunkEntity;

import java.util.List;

public class ResponseContextDto {

    private long spentTokens = 0;

    private long spentTime = 0;

    private String request = "";

    private String response = "";

    private List<DocumentChunkEntity> documents;

    public void setResponse(String response) {
        this.response = response;
    }

    public void addTokens(long tokens) {
        this.spentTokens += tokens;
    }

    public void setSpentTime(long spentTime) {
        this.spentTime = spentTime;
    }

    public long getSpentTokens() {
        return spentTokens;
    }

    public long getSpentTime() {
        return spentTime;
    }

    public String getResponse() {
        return response;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setDocuments(List<DocumentChunkEntity> documents) {
        this.documents = documents;
    }

    public List<DocumentChunkEntity> getDocuments() {
        return documents;
    }

}
