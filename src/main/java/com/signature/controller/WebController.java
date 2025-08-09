package com.signature.controller;

import com.signature.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class WebController {

    @Autowired
    private DocumentService documentService;

    // Root mapping removed - handled by AuthController for authentication flow

    @GetMapping("/upload")
    public String uploadPage() {
        return "upload";
    }

    @GetMapping("/documents")
    public String documentsPage(Model model) {
        model.addAttribute("documents", documentService.getAllDocuments());
        return "documents";
    }

    @GetMapping("/documents/signed")
    public String signedDocumentsPage(Model model) {
        model.addAttribute("documents", documentService.getSignedDocuments());
        model.addAttribute("pageTitle", "Documents Signés");
        return "documents";
    }

    @GetMapping("/documents/unsigned")
    public String unsignedDocumentsPage(Model model) {
        model.addAttribute("documents", documentService.getUnsignedDocuments());
        model.addAttribute("pageTitle", "Documents Non Signés");
        return "documents";
    }

    @GetMapping("/history")
    public String historyPage(Model model) {
        model.addAttribute("signedDocuments", documentService.getSignedDocuments());
        return "history";
    }
}
