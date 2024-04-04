package com.cca.ia.rag.document;

import com.cca.ia.rag.document.model.*;
import com.cca.ia.rag.document.parser.DocumentParserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private DocumentParserService documentParserService;

    @Autowired
    private ModelMapper mapper;

    @PostMapping("/parse")
    public void parseDocument(@RequestBody DocumentParserDto dto) {

        try {
            documentParserService.parse(dto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @PostMapping("/list-by-ids")
    public List<DocumentDto> getDocumentsById(@RequestParam("ids") List<Long> documentsId) {

        List<DocumentEntity> documents = documentService.getDocumentsById(documentsId);

        return documents.stream().map(e -> mapper.map(e, DocumentDto.class)).collect(Collectors.toList());
    }

    @GetMapping("/by-collection/{collectionId}")
    public List<DocumentDto> getDocuments(@PathVariable Long collectionId) {

        List<DocumentEntity> documents = documentService.getDocuments(collectionId);

        return documents.stream().map(e -> mapper.map(e, DocumentDto.class)).collect(Collectors.toList());
    }

    @GetMapping("/{documentId}/chunks")
    public List<DocumentChunkDto> getDocumentChunks(@PathVariable Long documentId) {

        List<DocumentChunkEntity> documents = documentService.getDocumentChunksByDocumentId(documentId);

        return documents.stream().map(e -> mapper.map(e, DocumentChunkDto.class)).collect(Collectors.toList());
    }

    @GetMapping("/{documentId}/chunk/{chunkId}/content")
    public DocumentChunkContentDto getDocuments(@PathVariable Long documentId, @PathVariable Long chunkId) {

        try {
            return documentService.getChunkContentByDocumentAndChunkId(documentId, chunkId);
        } catch (Exception e) {
            return null;
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred in the controller: " + e.getMessage());
    }
}
