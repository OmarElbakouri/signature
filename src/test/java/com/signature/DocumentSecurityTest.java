package com.signature;

import com.signature.entity.Document;
import com.signature.entity.User;
import com.signature.repository.DocumentRepository;
import com.signature.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DocumentSecurityTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    private User user1;
    private User user2;
    private Document doc1User1;
    private Document doc2User1;
    private Document doc1User2;

    @BeforeEach
    public void setup() {
        // Configuration des utilisateurs de test
        user1 = new User("user1", "user1@example.com", "password", "User", "One");
        user1.setId(1L);

        user2 = new User("user2", "user2@example.com", "password", "User", "Two");
        user2.setId(2L);

        // Configuration des documents de test
        doc1User1 = new Document();
        doc1User1.setId(1L);
        doc1User1.setUser(user1);
        doc1User1.setOriginalName("doc1-user1.pdf");

        doc2User1 = new Document();
        doc2User1.setId(2L);
        doc2User1.setUser(user1);
        doc2User1.setOriginalName("doc2-user1.pdf");

        doc1User2 = new Document();
        doc1User2.setId(3L);
        doc1User2.setUser(user2);
        doc1User2.setOriginalName("doc1-user2.pdf");

        // Configuration des mocks
        // Par défaut, tous les documents sont retournés sans filtrage
        when(documentRepository.findAllOrderByUploadedAtDesc())
                .thenReturn(Arrays.asList(doc1User1, doc2User1, doc1User2));
        
        // Avec filtrage utilisateur
        when(documentRepository.findAllByUserOrderByUploadedAtDesc(user1))
                .thenReturn(Arrays.asList(doc1User1, doc2User1));
        when(documentRepository.findAllByUserOrderByUploadedAtDesc(user2))
                .thenReturn(List.of(doc1User2));

        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc1User1));
        when(documentRepository.findById(2L)).thenReturn(Optional.of(doc2User1));
        when(documentRepository.findById(3L)).thenReturn(Optional.of(doc1User2));
    }

    private void authenticateAs(User user) {
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetAllDocuments_whenAuthenticated_returnsOnlyUserDocuments() {
        // Authentification en tant que user1
        authenticateAs(user1);

        // Exécution du test
        List<Document> result = documentService.getAllDocuments();

        // Vérifications
        assertEquals(2, result.size());
        assertTrue(result.contains(doc1User1));
        assertTrue(result.contains(doc2User1));
        assertFalse(result.contains(doc1User2));
    }

    @Test
    public void testGetDocumentById_whenOwnedByUser_returnsDocument() {
        // Authentification en tant que user1
        authenticateAs(user1);

        // Exécution du test - demande d'un document appartenant à user1
        Optional<Document> result = documentService.getDocumentById(1L);

        // Vérifications
        assertTrue(result.isPresent());
        assertEquals(doc1User1, result.get());
    }

    @Test
    public void testGetDocumentById_whenNotOwnedByUser_returnsEmpty() {
        // Authentification en tant que user1
        authenticateAs(user1);

        // Exécution du test - demande d'un document appartenant à user2
        Optional<Document> result = documentService.getDocumentById(3L);

        // Vérifications
        assertFalse(result.isPresent());
    }

    @Test
    public void testDeleteDocument_whenOwnedByUser_deletesDocument() {
        // Authentification en tant que user1
        authenticateAs(user1);

        // Exécution du test - suppression d'un document appartenant à user1
        boolean result = documentService.deleteDocument(1L);

        // Vérifications
        assertTrue(result);
        verify(documentRepository).deleteById(1L);
    }

    @Test
    public void testDeleteDocument_whenNotOwnedByUser_doesNotDelete() {
        // Authentification en tant que user1
        authenticateAs(user1);

        // Exécution du test - tentative de suppression d'un document appartenant à user2
        boolean result = documentService.deleteDocument(3L);

        // Vérifications
        assertFalse(result);
        verify(documentRepository, never()).deleteById(3L);
    }

    @Test
    public void testGetAllDocuments_whenNotAuthenticated_returnsAllDocuments() {
        // Pas d'authentification
        clearAuthentication();

        // Exécution du test
        List<Document> result = documentService.getAllDocuments();

        // Vérifications - en mode non-authentifié, comportement de fallback
        assertEquals(3, result.size());
    }
}
