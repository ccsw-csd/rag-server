package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.dto.DocumentChunkConfigDto;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Service
public class FactoryDocumentParserServiceDefault implements FactoryDocumentParserService {

    @Autowired
    @Qualifier("pdfDocumentParser")
    private DocumentParser pdfDocumentParser;

    @Autowired
    @Qualifier("textDocumentParser")
    private DocumentParser textDocumentParser;

    @Override
    public List<Document> parseAndExtractChunks(String filename, InputStream stream, DocumentChunkConfigDto config) throws Exception {

        ParserConfigDto parserConfigDto = new ParserConfigDto();
        DocumentChunkEntity.DocumentChunkType chunkType = getChunkType(filename);

        if (chunkType.equals(DocumentChunkEntity.DocumentChunkType.DOCUMENT)) {
            parserConfigDto.setMaxTokens(config.getChunkSizeDocumentation());
            return pdfDocumentParser.parseAndExtractChunks(filename, stream, parserConfigDto);
        } else if (chunkType.equals(DocumentChunkEntity.DocumentChunkType.CODE)) {
            parserConfigDto.setMaxTokens(config.getChunkSizeCode());
            return textDocumentParser.parseAndExtractChunks(filename, stream, parserConfigDto);
        }

        throw new Exception("Unsupported file type");
    }

    @Override
    public DocumentChunkEntity.DocumentChunkType getChunkType(String filename) {

        String extensionFile = filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : null;
        List<String> filesAcceptedTextParser = Arrays.asList(".java", ".xml", ".properties", ".yaml", ".sql");

        if (extensionFile.equals(".pdf")) {
            return DocumentChunkEntity.DocumentChunkType.DOCUMENT;
        } else if (filesAcceptedTextParser.contains(extensionFile)) {
            return DocumentChunkEntity.DocumentChunkType.CODE;
        }

        return null;
    }

}
