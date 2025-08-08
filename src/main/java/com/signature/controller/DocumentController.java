package com.signature.controller;

import com.signature.entity.Document;
import com.signature.service.DocumentService;
import com.signature.service.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    @Autowired
    private SignatureService signatureService;

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            if (!file.getContentType().equals("application/pdf")) {
                return ResponseEntity.badRequest().body("Only PDF files are supported");
            }

            Document document = signatureService.uploadDocument(file);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error uploading document: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/sign")
    public ResponseEntity<?> signDocument(@PathVariable Long id, @RequestParam String signerName) {
        try {
            Document signedDocument = signatureService.signDocument(id, signerName);
            return ResponseEntity.ok(signedDocument);
        } catch (Exception e) {
            // Return error in JSON format for proper client-side parsing
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\": \"Error signing document: " + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/signed")
    public ResponseEntity<List<Document>> getSignedDocuments() {
        List<Document> documents = documentService.getSignedDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/unsigned")
    public ResponseEntity<List<Document>> getUnsignedDocuments() {
        List<Document> documents = documentService.getUnsignedDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        Optional<Document> document = documentService.getDocumentById(id);
        if (document.isPresent()) {
            return ResponseEntity.ok(document.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id, 
                                                  @RequestParam(defaultValue = "false") boolean signed) {
        try {
            Optional<Document> documentOpt = documentService.getDocumentById(id);
            if (documentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Document document = documentOpt.get();
            byte[] content = signatureService.getDocumentContent(id, signed);

            String filename = signed ? 
                document.getOriginalName().replace(".pdf", "_signed.pdf") : 
                document.getOriginalName();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                .headers(headers)
                .body(content);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error deleting document: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        long totalDocs = documentService.getTotalDocuments();
        long signedDocs = documentService.getSignedDocumentsCount();
        long unsignedDocs = totalDocs - signedDocs;
        
        return ResponseEntity.ok(new Object() {
            public final long totalDocuments = totalDocs;
            public final long signedDocuments = signedDocs;
            public final long unsignedDocuments = unsignedDocs;
        });
    }
    
    @GetMapping("/test-certificate")
    public ResponseEntity<?> testCertificate() {
        try {
            String result = signatureService.testCertificateLoading();
            return ResponseEntity.ok("{\"status\": \"success\", \"message\": \"" + result + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"status\": \"error\", \"message\": \"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
