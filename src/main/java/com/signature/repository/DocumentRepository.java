package com.signature.repository;

import com.signature.entity.Document;
import com.signature.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    // Méthodes existantes améliorées pour filtrer par utilisateur
    List<Document> findByIsSignedTrueAndUser(User user);
    
    List<Document> findByIsSignedFalseAndUser(User user);
    
    @Query("SELECT d FROM Document d WHERE d.user = :user ORDER BY d.uploadedAt DESC")
    List<Document> findAllByUserOrderByUploadedAtDesc(@Param("user") User user);
    
    @Query("SELECT d FROM Document d WHERE d.isSigned = true AND d.user = :user ORDER BY d.signedAt DESC")
    List<Document> findSignedDocumentsByUserOrderBySignedAtDesc(@Param("user") User user);
    
    List<Document> findByUploadedAtBetweenAndUser(LocalDateTime start, LocalDateTime end, User user);
    
    List<Document> findBySignerNameContainingIgnoreCaseAndUser(String signerName, User user);
    
    // Méthodes originales conservées pour la compatibilité et l'administration
    List<Document> findByIsSignedTrue();
    
    List<Document> findByIsSignedFalse();
    
    @Query("SELECT d FROM Document d ORDER BY d.uploadedAt DESC")
    List<Document> findAllOrderByUploadedAtDesc();
    
    @Query("SELECT d FROM Document d WHERE d.isSigned = true ORDER BY d.signedAt DESC")
    List<Document> findSignedDocumentsOrderBySignedAtDesc();
    
    List<Document> findByUploadedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Document> findBySignerNameContainingIgnoreCase(String signerName);
}
