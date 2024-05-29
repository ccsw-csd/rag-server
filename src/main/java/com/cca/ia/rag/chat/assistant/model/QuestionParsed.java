package com.cca.ia.rag.chat.assistant.model;

import java.util.ArrayList;
import java.util.List;

public class QuestionParsed {
    private String question;

    private Boolean noAutocontext;

    private Boolean onlyDoc;

    private Boolean onlyCode;

    private List<String> files;

    private Boolean query;

    private Boolean chat;

    public QuestionParsed(String question) {
        this.question = question;
        this.noAutocontext = extractTag("@noAutocontext");
        this.onlyDoc = extractTag("@onlyDoc");
        this.onlyCode = extractTag("@onlyCode");
        this.query = extractTag("@query");
        this.chat = extractTag("@chat");
        this.files = extractFiles();
    }

    private List<String> extractFiles() {
        List<String> files = new ArrayList<>();
        String tag = "@file(";
        int startIndex = 0;

        do {
            startIndex = question.toLowerCase().indexOf(tag.toLowerCase());

            if (startIndex >= 0) {

                int endIndex = question.indexOf(")", startIndex);
                if (endIndex < 0) {
                    endIndex = question.length() - 1;
                }

                files.add(question.substring(startIndex + tag.length(), endIndex));
                question = question.substring(0, startIndex) + question.substring(endIndex + 1);
            }
        } while (startIndex >= 0);

        return files;
    }

    private Boolean extractTag(String tag) {

        int index = 0;
        Boolean foundTag = null;

        do {
            index = question.toLowerCase().indexOf(tag.toLowerCase());

            if (index >= 0) {
                foundTag = true;
                question = question.substring(0, index) + question.substring(index + tag.length() + 1);
            }
        } while (index >= 0);

        return foundTag;
    }

    public String getQuestion() {
        return question;
    }

    public Boolean getNoAutocontext() {
        return noAutocontext;
    }

    public Boolean getOnlyDoc() {
        return onlyDoc;
    }

    public Boolean getOnlyCode() {
        return onlyCode;
    }

    public List<String> getFiles() {
        return files;
    }

    public Boolean getQuery() {
        return query;
    }

    public Boolean getChat() {
        return chat;
    }

    public void setNoAutocontext(Boolean noAutocontext) {
        this.noAutocontext = noAutocontext;
    }

    public void setOnlyDoc(Boolean onlyDoc) {
        this.onlyDoc = onlyDoc;
    }

    public void setOnlyCode(Boolean onlyCode) {
        this.onlyCode = onlyCode;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public void setQuery(Boolean query) {
        this.query = query;
    }

    public void setChat(Boolean chat) {
        this.chat = chat;
    }
}