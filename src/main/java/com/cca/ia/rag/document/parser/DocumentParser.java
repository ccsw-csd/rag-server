package com.cca.ia.rag.document.parser;

import org.springframework.ai.document.Document;

import java.io.InputStream;
import java.util.List;

public interface DocumentParser {

    List<Document> parseAndExtractChunks(String filename, InputStream stream, ParserConfigDto config) throws Exception;

}
