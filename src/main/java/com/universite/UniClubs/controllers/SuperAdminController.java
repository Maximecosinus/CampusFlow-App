package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.UtilisateurService;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SuperAdminController {

    @Autowired
    private UtilisateurService utilisateurService;
    
    @Autowired
    private ClubService clubService;
    
    @Autowired
    private EvenementService evenementService;

    /**
     * Dashboard Super Admin
     */
    @GetMapping("")
    public String superAdminDashboard(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        // Statistiques générales
        long totalUsers = utilisateurService.countAllUsers();
        long totalAdmins = utilisateurService.countUsersByRole(com.universite.UniClubs.entities.Role.ADMIN);
        long totalStudents = utilisateurService.countUsersByRole(com.universite.UniClubs.entities.Role.ETUDIANT);
        long totalClubLeaders = utilisateurService.countUsersByRole(com.universite.UniClubs.entities.Role.CHEF_DE_CLUB);
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalClubLeaders", totalClubLeaders);
        
        return "super-admin/dashboard";
    }

    /**
     * Gestion des utilisateurs (Super Admin uniquement)
     */
    @GetMapping("/users")
    public String manageUsers(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        // Récupérer tous les utilisateurs
        var allUsers = utilisateurService.findAllUsers();
        model.addAttribute("users", allUsers);
        
        return "super-admin/users";
    }

    /**
     * Gestion des administrateurs
     */
    @GetMapping("/admins")
    public String manageAdmins(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        // Récupérer tous les administrateurs
        var admins = utilisateurService.findUsersByRole(com.universite.UniClubs.entities.Role.ADMIN);
        model.addAttribute("admins", admins);
        
        return "super-admin/admins";
    }

    /**
     * Accès aux fonctionnalités admin normales
     */
    @GetMapping("/admin-features")
    public String adminFeatures(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        return "super-admin/admin-features";
    }

    /**
     * Paramètres système
     */
    @GetMapping("/system-settings")
    public String systemSettings(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        return "super-admin/system-settings";
    }

    /**
     * Profil Super Admin
     */
    @GetMapping("/profile")
    public String superAdminProfile(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        return "super-admin/profile";
    }

    /**
     * Gestion des clubs (version Super Admin)
     */
    @GetMapping("/clubs")
    public String superAdminClubs(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        // Récupérer tous les clubs
        var clubs = clubService.findAllClubs();
        model.addAttribute("clubs", clubs);
        
        return "super-admin/clubs";
    }

    /**
     * Gestion des événements (version Super Admin)
     */
    @GetMapping("/events")
    public String superAdminEvents(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        // Récupérer tous les événements
        var events = evenementService.findAllEvents();
        model.addAttribute("events", events);
        
        return "super-admin/events";
    }

    /**
     * Communication (version Super Admin)
     */
    @GetMapping("/communication")
    public String superAdminCommunication(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        return "super-admin/communication";
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     */
    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.universite.UniClubs.services.CustomUserDetails) {
            com.universite.UniClubs.services.CustomUserDetails userDetails = (com.universite.UniClubs.services.CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUtilisateur();
        }
        return null;
    }
}
