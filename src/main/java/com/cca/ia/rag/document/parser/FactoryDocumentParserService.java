package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.model.DocumentChunkConfigDto;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface FactoryDocumentParserService {

    List<Document> parseAndExtractChunks(String filename, Resource resource, DocumentChunkConfigDto config) throws Exception;

    /*
    void parse(DocumentParserDto documentParserDto) throws Exception;

    void deleteDocumentChunks(DocumentEntity document) throws Exception;

    void saveDocumentChunks(DocumentEntity document, String[] contents) throws Exception;

     */
}
