package com.signature.repository;

import com.signature.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByIsSignedTrue();
    
    List<Document> findByIsSignedFalse();
    
    @Query("SELECT d FROM Document d ORDER BY d.uploadedAt DESC")
    List<Document> findAllOrderByUploadedAtDesc();
    
    @Query("SELECT d FROM Document d WHERE d.isSigned = true ORDER BY d.signedAt DESC")
    List<Document> findSignedDocumentsOrderBySignedAtDesc();
    
    List<Document> findByUploadedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Document> findBySignerNameContainingIgnoreCase(String signerName);
}
