package com.signature.controller;

import com.signature.entity.User;
import com.signature.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Contrôleur pour gérer les routes liées au profil utilisateur
 */
@Controller
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Récupère l'utilisateur actuellement connecté à partir du contexte de sécurité
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return (User) auth.getPrincipal();
        }
        return null;
    }

    /**
     * Affiche la page du profil utilisateur
     */
    @GetMapping("/profile")
    public String profilePage(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        return "profile";
    }

    /**
     * Affiche la page des paramètres utilisateur
     */
    @GetMapping("/settings")
    public String settingsPage(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        model.addAttribute("user", currentUser);
        return "settings";
    }

    /**
     * Gère la mise à jour du profil utilisateur
     */
    @PostMapping("/profile/update")
    public String updateProfile(@Valid @ModelAttribute("user") User userForm,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        if (bindingResult.hasErrors()) {
            return "profile";
        }
        
        try {
            // Mise à jour uniquement des champs autorisés (pas le mot de passe ici)
            currentUser.setFirstName(userForm.getFirstName());
            currentUser.setLastName(userForm.getLastName());
            currentUser.setEmail(userForm.getEmail());
            
            userService.updateUser(currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Profil mis à jour avec succès");
            return "redirect:/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "profile";
        }
    }

    /**
     * Gère le changement de mot de passe
     */
    @PostMapping("/settings/password")
    public String updatePassword(@ModelAttribute("currentPassword") String currentPassword,
                                @ModelAttribute("newPassword") String newPassword,
                                @ModelAttribute("confirmPassword") String confirmPassword,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // Vérifier que les nouveaux mots de passe correspondent
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Les nouveaux mots de passe ne correspondent pas");
            return "redirect:/settings";
        }
        
        try {
            boolean updated = userService.updatePassword(currentUser, currentPassword, newPassword);
            if (updated) {
                redirectAttributes.addFlashAttribute("successMessage", "Mot de passe mis à jour avec succès");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Le mot de passe actuel est incorrect");
            }
            return "redirect:/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/settings";
        }
    }
}
