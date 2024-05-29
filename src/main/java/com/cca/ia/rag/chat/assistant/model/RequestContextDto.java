package com.cca.ia.rag.chat.assistant.model;

import com.cca.ia.rag.collection.model.CollectionEntity;
import com.cca.ia.rag.collection.model.CollectionPropertyDto;
import com.cca.ia.rag.collection.model.CollectionPropertyEntity;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RequestContextDto {
    private CollectionEntity collection;
    private String originalQuery;
    private QuestionParsed questionParsed;

    private Prompt prompt;

    private List<CollectionPropertyDto> properties;

    private List<CollectionPropertyEntity> prompts;

    private List<DocumentChunkEntity> documents = new ArrayList<>();

    private StringBuilder context = new StringBuilder();

    public RequestContextDto(CollectionEntity collection, String query) {
        this.collection = collection;
        this.originalQuery = query;
    }

    public void addDocument(DocumentChunkEntity document, String content) {
        this.documents.add(document);
        this.context.append(content + "\n");
    }

    public void setCollectionProperties(List<CollectionPropertyDto> properties) {
        this.properties = properties;
    }

    public void setCollectionPrompts(List<CollectionPropertyEntity> prompts) {
        this.prompts = prompts;
    }

    public CollectionEntity getCollection() {
        return collection;
    }

    public void parseQuery() {

        questionParsed = new QuestionParsed(originalQuery);

        if (questionParsed.getNoAutocontext() == null) {
            questionParsed.setNoAutocontext("true".equals(getProperty("noAutocontext")));
        }

        if (questionParsed.getOnlyDoc() == null) {
            questionParsed.setOnlyDoc("true".equals(getProperty("onlyDoc")));
        }

        if (questionParsed.getOnlyCode() == null) {
            questionParsed.setOnlyCode("true".equals(getProperty("onlyCode")));
        }

        if (questionParsed.getChat() == null) {
            questionParsed.setChat("true".equals(getProperty("chat")));
        }

        if (questionParsed.getQuery() == null) {
            questionParsed.setQuery("true".equals(getProperty("query")));
        }

        if (questionParsed.getChat() && questionParsed.getQuery()) {
            questionParsed.setChat(true);
            questionParsed.setQuery(false);
        }

    }

    public boolean isQuery() {
        return questionParsed.getQuery();
    }

    public String getProperty(String key) {
        Optional<CollectionPropertyDto> property = this.properties.stream().filter(e -> e.getKey().toLowerCase().equals(key.toLowerCase())).findFirst();

        if (property.isPresent())
            return property.get().getValue();
        else
            return null;
    }

    public String getPrompt(String key) {
        Optional<CollectionPropertyEntity> property = this.prompts.stream().filter(e -> e.getKey().toLowerCase().equals(key.toLowerCase())).findFirst();

        if (property.isPresent())
            return property.get().getValue();
        else
            return null;
    }

    public String getContext() {
        return context.toString();
    }

    public String getQuestion() {
        if (questionParsed == null)
            return originalQuery;

        return questionParsed.getQuestion();
    }

    public void setPrompt(Prompt prompt) {
        this.prompt = prompt;
    }

    public Prompt getPrompt() {
        return prompt;
    }

    public List<DocumentChunkEntity> getDocuments() {
        return documents;
    }

    public List<String> getFiles() {
        return questionParsed.getFiles();
    }

    public boolean getNoAutocontext() {
        return questionParsed.getNoAutocontext();
    }

    public boolean getOnlyDoc() {
        return questionParsed.getOnlyDoc();
    }

    public boolean getOnlyCode() {
        return questionParsed.getOnlyCode();
    }
}
