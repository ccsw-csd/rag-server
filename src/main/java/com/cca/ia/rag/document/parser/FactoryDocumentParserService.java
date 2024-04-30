package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.dto.DocumentChunkConfigDto;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import org.springframework.ai.document.Document;

import java.io.InputStream;
import java.util.List;

public interface FactoryDocumentParserService {

    List<Document> parseAndExtractChunks(String filename, InputStream stream, DocumentChunkConfigDto config) throws Exception;

    DocumentChunkEntity.DocumentChunkType getChunkType(String filename);
}
