package com.cca.ia.rag.document;

import com.cca.ia.rag.document.model.*;
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
    private ModelMapper mapper;

    @PostMapping("/{documentId}/action")
    public void documentActions(@PathVariable Long documentId, @RequestBody DocumentActionsDto actions) throws Exception {

        documentService.executeActions(documentId, actions);

    }

    @PostMapping("/list-by-ids")
    public List<DocumentDto> getDocumentsById(@RequestParam("ids") List<Long> documentsId) {

        List<DocumentEntity> documents = documentService.getDocumentsById(documentsId);

        return documents.stream().map(e -> mapper.map(e, DocumentDto.class)).collect(Collectors.toList());
    }

    @PostMapping("/{documentId}/chunks")
    public void saveDocumentChunks(@PathVariable Long documentId, @RequestBody DocumentChunkSaveDto dto) throws Exception {

        documentService.saveDocumentChunks(documentId, dto);
    }

    @GetMapping("/by-collection/{collectionId}")
    public List<DocumentDto> getDocuments(@PathVariable Long collectionId) {

        List<DocumentEntity> documents = documentService.getDocuments(collectionId);

        return documents.stream().map(e -> mapper.map(e, DocumentDto.class)).collect(Collectors.toList());
    }

    @GetMapping("/{documentId}/chunks/{type}")
    public List<DocumentChunkDto> getDocumentChunks(@PathVariable Long documentId, @PathVariable Integer type) {

        List<DocumentChunkEntity> documents = documentService.getDocumentChunksByDocumentId(documentId, DocumentChunkEntity.DocumentChunkType.fromInt(type));

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
