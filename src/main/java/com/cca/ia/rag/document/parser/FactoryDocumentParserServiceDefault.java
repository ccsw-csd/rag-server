package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.dto.DocumentChunkConfigDto;
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

        String extensionFile = filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : null;
        ParserConfigDto parserConfigDto = new ParserConfigDto();

        List<String> filesAcceptedTextParser = Arrays.asList(".java", ".xml", ".properties", ".yaml", ".sql");

        if (extensionFile.equals(".pdf")) {
            parserConfigDto.setMaxTokens(config.getChunkSizeDocumentation());
            return pdfDocumentParser.parseAndExtractChunks(filename, stream, parserConfigDto);
        } else if (filesAcceptedTextParser.contains(extensionFile)) {
            parserConfigDto.setMaxTokens(config.getChunkSizeCode());
            return textDocumentParser.parseAndExtractChunks(filename, stream, parserConfigDto);
        }

        throw new Exception("Unsupported file type");
    }

}
