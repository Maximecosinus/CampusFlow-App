package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur pour le tableau de bord administrateur
 * Toutes les routes sont sécurisées avec @PreAuthorize("hasRole('ADMIN')")
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UtilisateurService utilisateurService;

    /**
     * Page d'accueil du tableau de bord administrateur
     */
    @GetMapping("")
    public String adminDashboard(Model model) {
        // Récupérer l'utilisateur connecté
        Utilisateur admin = getCurrentUser();
        model.addAttribute("admin", admin);
        
        // Statistiques générales (à implémenter plus tard)
        model.addAttribute("totalUsers", utilisateurService.countAllUsers());
        model.addAttribute("totalClubs", 0); // À implémenter
        model.addAttribute("totalEvents", 0); // À implémenter
        
        return "admin/dashboard";
    }

    /**
     * Section Clubs (à implémenter)
     */
    @GetMapping("/clubs")
    public String adminClubs(Model model) {
        Utilisateur admin = getCurrentUser();
        model.addAttribute("admin", admin);
        return "admin/clubs";
    }

    /**
     * Section Événements (à implémenter)
     */
    @GetMapping("/events")
    public String adminEvents(Model model) {
        Utilisateur admin = getCurrentUser();
        model.addAttribute("admin", admin);
        return "admin/events";
    }

    /**
     * Section Communication (à implémenter)
     */
    @GetMapping("/communication")
    public String adminCommunication(Model model) {
        Utilisateur admin = getCurrentUser();
        model.addAttribute("admin", admin);
        return "admin/communication";
    }

    /**
     * Section Utilisateurs (à implémenter)
     */
    @GetMapping("/users")
    public String adminUsers(Model model) {
        Utilisateur admin = getCurrentUser();
        model.addAttribute("admin", admin);
        return "admin/users";
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            String email = authentication.getName();
            return utilisateurService.findByEmail(email);
        }
        return null;
    }
}
