package com.signature.controller;

import com.signature.entity.User;
import com.signature.service.DocumentService;
import com.signature.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DocumentService documentService;
    
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                           @RequestParam(value = "logout", required = false) String logout,
                           Model model) {
        
        if (error != null) {
            model.addAttribute("errorMessage", "Nom d'utilisateur ou mot de passe incorrect");
        }
        
        if (logout != null) {
            model.addAttribute("successMessage", "Vous avez été déconnecté avec succès");
        }
        
        return "auth/login";
    }
    
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
    
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                              BindingResult bindingResult,
                              @RequestParam("confirmPassword") String confirmPassword,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        
        // Vérifier si les mots de passe correspondent
        if (!user.getPassword().equals(confirmPassword)) {
            bindingResult.rejectValue("password", "error.user", "Les mots de passe ne correspondent pas");
        }
        
        // Vérifier les erreurs de validation
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        
        try {
            userService.registerUser(user);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            User user = (User) auth.getPrincipal();
            model.addAttribute("user", user);
            
            // Ajout des statistiques de documents pour l'utilisateur connecté
            model.addAttribute("signedDocumentsCount", documentService.getSignedDocumentsCount());
            model.addAttribute("unsignedDocumentsCount", documentService.getUnsignedDocumentsCount());
            model.addAttribute("totalDocuments", documentService.getTotalDocuments());
        }
        return "dashboard";
    }
    
    @GetMapping("/")
    public String home() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }
}
