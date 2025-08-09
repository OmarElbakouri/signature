package com.signature.service;

import com.signature.entity.Document;
import com.signature.entity.User;
import com.signature.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;
    
    /**
     * Récupère l'utilisateur actuellement connecté
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    /**
     * Récupère tous les documents de l'utilisateur connecté
     */
    public List<Document> getAllDocuments() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return documentRepository.findAllByUserOrderByUploadedAtDesc(currentUser);
        }
        // Fallback pour les requêtes administratives ou de test
        return documentRepository.findAllOrderByUploadedAtDesc();
    }

    /**
     * Récupère les documents signés de l'utilisateur connecté
     */
    public List<Document> getSignedDocuments() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return documentRepository.findSignedDocumentsByUserOrderBySignedAtDesc(currentUser);
        }
        // Fallback pour les requêtes administratives ou de test
        return documentRepository.findSignedDocumentsOrderBySignedAtDesc();
    }

    /**
     * Récupère les documents non signés de l'utilisateur connecté
     */
    public List<Document> getUnsignedDocuments() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return documentRepository.findByIsSignedFalseAndUser(currentUser);
        }
        // Fallback pour les requêtes administratives ou de test
        return documentRepository.findByIsSignedFalse();
    }

    /**
     * Récupère un document par son ID, vérifie qu'il appartient à l'utilisateur connecté
     */
    public Optional<Document> getDocumentById(Long id) {
        Optional<Document> documentOpt = documentRepository.findById(id);
        User currentUser = getCurrentUser();
        
        if (documentOpt.isPresent() && currentUser != null) {
            Document document = documentOpt.get();
            // Vérifier que le document appartient à l'utilisateur connecté
            if (document.getUser() != null && !document.getUser().getId().equals(currentUser.getId())) {
                // Document appartient à un autre utilisateur, accès refusé
                return Optional.empty();
            }
        }
        
        return documentOpt;
    }

    /**
     * Récupère les documents par plage de dates pour l'utilisateur connecté
     */
    public List<Document> getDocumentsByDateRange(LocalDateTime start, LocalDateTime end) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return documentRepository.findByUploadedAtBetweenAndUser(start, end, currentUser);
        }
        // Fallback pour les requêtes administratives ou de test
        return documentRepository.findByUploadedAtBetween(start, end);
    }

    /**
     * Recherche par nom de signataire pour l'utilisateur connecté
     */
    public List<Document> searchBySignerName(String signerName) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return documentRepository.findBySignerNameContainingIgnoreCaseAndUser(signerName, currentUser);
        }
        // Fallback pour les requêtes administratives ou de test
        return documentRepository.findBySignerNameContainingIgnoreCase(signerName);
    }

    /**
     * Supprime un document s'il appartient à l'utilisateur connecté
     * @return true si le document a été supprimé, false sinon
     */
    public boolean deleteDocument(Long id) {
        Optional<Document> documentOpt = getDocumentById(id); // Utilise déjà la vérification d'appartenance
        if (documentOpt.isPresent()) {
            documentRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Compte le nombre total de documents de l'utilisateur connecté
     */
    public long getTotalDocuments() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return documentRepository.findAllByUserOrderByUploadedAtDesc(currentUser).size();
        }
        // Fallback pour les requêtes administratives ou de test
        return documentRepository.count();
    }

    /**
     * Compte le nombre de documents signés de l'utilisateur connecté
     */
    public long getSignedDocumentsCount() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return documentRepository.findByIsSignedTrueAndUser(currentUser).size();
        }
        // Fallback pour les requêtes administratives ou de test
        return documentRepository.findByIsSignedTrue().size();
    }

    /**
     * Compte le nombre de documents non signés de l'utilisateur connecté
     */
    public long getUnsignedDocumentsCount() {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            return documentRepository.findByIsSignedFalseAndUser(currentUser).size();
        }
        // Fallback pour les requêtes administratives ou de test
        return documentRepository.findByIsSignedFalse().size();
    }
}
