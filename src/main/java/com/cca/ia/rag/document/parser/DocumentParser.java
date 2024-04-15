package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.model.DocumentChunkConfigDto;
import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DocumentParser {

    List<Document> parseAndExtractChunks(Resource resource, DocumentChunkConfigDto config) throws Exception;

}
