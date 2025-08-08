package com.signature.service;

import com.signature.entity.Document;
import com.signature.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    public List<Document> getAllDocuments() {
        return documentRepository.findAllOrderByUploadedAtDesc();
    }

    public List<Document> getSignedDocuments() {
        return documentRepository.findSignedDocumentsOrderBySignedAtDesc();
    }

    public List<Document> getUnsignedDocuments() {
        return documentRepository.findByIsSignedFalse();
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getDocumentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return documentRepository.findByUploadedAtBetween(start, end);
    }

    public List<Document> searchBySignerName(String signerName) {
        return documentRepository.findBySignerNameContainingIgnoreCase(signerName);
    }

    public void deleteDocument(Long id) {
        documentRepository.deleteById(id);
    }

    public long getTotalDocuments() {
        return documentRepository.count();
    }

    public long getSignedDocumentsCount() {
        return documentRepository.findByIsSignedTrue().size();
    }

    public long getUnsignedDocumentsCount() {
        return documentRepository.findByIsSignedFalse().size();
    }
}
