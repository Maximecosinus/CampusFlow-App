package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.services.UtilisateurService;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
        
        // Ajouter tous les rôles disponibles pour les formulaires
        model.addAttribute("allRoles", Arrays.asList(Role.values()));
        
        return "super-admin/users";
    }
    
    /**
     * Formulaire de création d'utilisateur
     */
    @GetMapping("/users/create")
    public String createUserForm(Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        model.addAttribute("allRoles", Arrays.asList(Role.values()));
        model.addAttribute("user", new Utilisateur());
        
        return "super-admin/create-user";
    }
    
    /**
     * Création d'un nouvel utilisateur
     */
    @PostMapping("/users/create")
    public String createUser(@ModelAttribute Utilisateur user,
                            @RequestParam(required = false) List<String> roles,
                            RedirectAttributes redirectAttributes) {
        try {
            // Vérifier si l'email existe déjà
            try {
                utilisateurService.findByEmail(user.getEmail());
                redirectAttributes.addFlashAttribute("error", "Un utilisateur avec cet email existe déjà.");
                return "redirect:/super-admin/users/create";
            } catch (Exception e) {
                // Email n'existe pas, on peut continuer
            }
            
            // Encoder le mot de passe
            user.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
            
            // Assigner les rôles (pour l'instant, on garde un seul rôle principal)
            // TODO: Implémenter la gestion des rôles multiples
            if (roles != null && !roles.isEmpty()) {
                user.setRole(Role.valueOf(roles.get(0)));
            } else {
                user.setRole(Role.ETUDIANT); // Rôle par défaut
            }
            
            utilisateurService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Utilisateur créé avec succès !");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création de l'utilisateur : " + e.getMessage());
        }
        
        return "redirect:/super-admin/users";
    }
    
    /**
     * Formulaire de modification d'utilisateur
     */
    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable UUID id, Model model) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        var user = utilisateurService.findById(id);
        if (user.isEmpty()) {
            return "redirect:/super-admin/users";
        }
        
        model.addAttribute("user", user.get());
        model.addAttribute("allRoles", Arrays.asList(Role.values()));
        
        return "super-admin/edit-user";
    }
    
    /**
     * Modification d'un utilisateur
     */
    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable UUID id,
                           @ModelAttribute Utilisateur user,
                           @RequestParam(required = false) List<String> roles,
                           RedirectAttributes redirectAttributes) {
        try {
            var existingUser = utilisateurService.findById(id);
            if (existingUser.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé.");
                return "redirect:/super-admin/users";
            }
            
            Utilisateur userToUpdate = existingUser.get();
            
            // Mettre à jour les informations de base
            userToUpdate.setPrenom(user.getPrenom());
            userToUpdate.setNom(user.getNom());
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setBio(user.getBio());
            
            // Mettre à jour le rôle
            if (roles != null && !roles.isEmpty()) {
                userToUpdate.setRole(Role.valueOf(roles.get(0)));
            }
            
            // Si un nouveau mot de passe est fourni, l'encoder
            if (user.getMotDePasse() != null && !user.getMotDePasse().trim().isEmpty()) {
                userToUpdate.setMotDePasse(passwordEncoder.encode(user.getMotDePasse()));
            }
            
            utilisateurService.saveUser(userToUpdate);
            redirectAttributes.addFlashAttribute("success", "Utilisateur modifié avec succès !");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification de l'utilisateur : " + e.getMessage());
        }
        
        return "redirect:/super-admin/users";
    }
    
    /**
     * Suppression d'un utilisateur
     */
    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable UUID id, RedirectAttributes redirectAttributes) {
        try {
            var user = utilisateurService.findById(id);
            if (user.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé.");
                return "redirect:/super-admin/users";
            }
            
            // Empêcher la suppression du Super Admin actuel
            Utilisateur currentUser = getCurrentUser();
            if (currentUser != null && currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Vous ne pouvez pas supprimer votre propre compte.");
                return "redirect:/super-admin/users";
            }
            
            utilisateurService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé avec succès !");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
        }
        
        return "redirect:/super-admin/users";
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
