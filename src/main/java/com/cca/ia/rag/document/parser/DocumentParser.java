package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.model.DocumentEntity;

public interface DocumentParser {

    void parseAndPersist(DocumentEntity documentEntity, String collectionName, String filename) throws Exception;

}
