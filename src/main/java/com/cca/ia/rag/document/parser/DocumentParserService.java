package com.cca.ia.rag.document.parser;

import com.cca.ia.rag.document.model.DocumentParserDto;

public interface DocumentParserService {

    void parse(DocumentParserDto documentParserDto) throws Exception;
    
}
