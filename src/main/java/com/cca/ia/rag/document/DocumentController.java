package com.cca.ia.rag.document;

import com.cca.ia.rag.document.dto.*;
import com.cca.ia.rag.document.model.DocumentChunkEntity;
import com.cca.ia.rag.document.model.DocumentFileEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @DeleteMapping("/delete-from-source/{documentId}")
    public void deleteAllFromSourceDocument(@PathVariable Long documentId) throws Exception {

        try {
            documentService.deleteAllFromSourceDocument(documentId);
        } catch (Exception e) {

        }

    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @PostMapping("/generate-embeddings/{parentDocumentId}")
    public void generateEmbeddings(@PathVariable Long parentDocumentId, @RequestBody GenerateEmbeddingsDto data) throws Exception {

        documentService.generateEmbeddings(parentDocumentId, data);

    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @PostMapping("/list-by-ids")
    public List<DocumentFileDto> getDocumentsById(@RequestParam("ids") List<Long> documentsId) {

        List<DocumentFileEntity> documents = documentService.getDocumentsById(documentsId);

        return documents.stream().map(e -> mapper.map(e, DocumentFileDto.class)).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @PostMapping("/{documentId}/chunks")
    public void saveDocumentChunks(@PathVariable Long documentId, @RequestBody DocumentChunkSaveDto dto) throws Exception {

        documentService.saveDocumentChunks(documentId, dto);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @PutMapping("/create-in-collection/{collectionId}")
    public void createDocument(@PathVariable Long collectionId, @RequestBody CreateDocumentDto dto) {

        documentService.createDocument(collectionId, dto);
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @GetMapping("/by-collection/{collectionId}")
    public List<DocumentFileDto> getDocuments(@PathVariable Long collectionId) {

        List<DocumentFileEntity> documents = documentService.getDocuments(collectionId);

        return documents.stream().map(e -> mapper.map(e, DocumentFileDto.class)).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @GetMapping("/{documentId}/chunks/{type}")
    public List<DocumentChunkDto> getDocumentChunks(@PathVariable Long documentId, @PathVariable Integer type) {

        List<DocumentChunkEntity> documents = documentService.getDocumentChunksByDocumentId(documentId, DocumentChunkEntity.DocumentChunkModifyType.fromInt(type));

        return documents.stream().map(e -> mapper.map(e, DocumentChunkDto.class)).collect(Collectors.toList());
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @GetMapping("/{documentId}/chunk/{chunkId}/content")
    public DocumentChunkContentDto getDocuments(@PathVariable Long documentId, @PathVariable Long chunkId) {

        try {
            return documentService.getChunkContentByDocumentAndChunkId(documentId, chunkId);
        } catch (Exception e) {
            return null;
        }
    }

    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CHAT')")
    @RequestMapping(path = "/upload-in-collection/{collectionId}", method = RequestMethod.POST)
    public void uploadDocuments(@PathVariable Long collectionId, @ModelAttribute DocumentUploadFormDataDto data) {

        try {
            documentService.uploadDocuments(collectionId, data);
        } catch (Exception e) {
        }
    }

}
