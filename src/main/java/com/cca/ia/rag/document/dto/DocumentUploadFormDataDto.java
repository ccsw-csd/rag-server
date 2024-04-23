package com.cca.ia.rag.document.dto;

import org.springframework.web.multipart.MultipartFile;

public class DocumentUploadFormDataDto {

    private MultipartFile file[];
    private int tokensDocumentation;
    private int tokensCode;

    public MultipartFile[] getFile() {
        return file;
    }

    public void setFile(MultipartFile[] file) {
        this.file = file;
    }

    public int getTokensDocumentation() {
        return tokensDocumentation;
    }

    public void setTokensDocumentation(int tokensDocumentation) {
        this.tokensDocumentation = tokensDocumentation;
    }

    public int getTokensCode() {
        return tokensCode;
    }

    public void setTokensCode(int tokensCode) {
        this.tokensCode = tokensCode;
    }
}
