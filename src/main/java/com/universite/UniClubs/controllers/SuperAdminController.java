package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.services.UtilisateurService;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.EvenementService;
import com.universite.UniClubs.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
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
    
    @Autowired
    private AuditLogService auditLogService;

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
        
        // Calculer les statistiques côté contrôleur
        long totalUsers = allUsers.size();
        long totalStudents = allUsers.stream().filter(u -> u.getRole().name().equals("ETUDIANT")).count();
        long totalAdmins = allUsers.stream().filter(u -> u.getRole().name().equals("ADMIN")).count();
        long totalSuperAdmins = allUsers.stream().filter(u -> u.getRole().name().equals("SUPER_ADMIN")).count();
        
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalAdmins", totalAdmins);
        model.addAttribute("totalSuperAdmins", totalSuperAdmins);
        
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
                            RedirectAttributes redirectAttributes,
                            HttpServletRequest request) {
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
            
            // Enregistrer l'action dans le journal d'audit
            Utilisateur currentUser = getCurrentUser();
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN) {
                auditLogService.logAdminCreation(currentUser.getEmail(), user.getEmail(), user.getId().toString(), request);
            } else {
                auditLogService.logAction(currentUser.getEmail(), 
                    String.format("Création de l'utilisateur '%s' avec le rôle %s", user.getEmail(), user.getRole().name()),
                    com.universite.UniClubs.entities.AuditLog.ActionType.CREATE, 
                    com.universite.UniClubs.entities.AuditLog.EntityType.USER, 
                    user.getId().toString(), request);
            }
            
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
    public String deleteUser(@PathVariable UUID id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
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
            
            Utilisateur userToDelete = user.get();
            
            // Enregistrer l'action dans le journal d'audit
            if (userToDelete.getRole() == Role.ADMIN || userToDelete.getRole() == Role.SUPER_ADMIN) {
                auditLogService.logAdminDeletion(currentUser.getEmail(), userToDelete.getEmail(), userToDelete.getId().toString(), request);
            } else {
                auditLogService.logAction(currentUser.getEmail(), 
                    String.format("Suppression de l'utilisateur '%s'", userToDelete.getEmail()),
                    com.universite.UniClubs.entities.AuditLog.ActionType.DELETE, 
                    com.universite.UniClubs.entities.AuditLog.EntityType.USER, 
                    userToDelete.getId().toString(), request);
            }
            
            utilisateurService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprimé avec succès !");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression de l'utilisateur : " + e.getMessage());
        }
        
        return "redirect:/super-admin/users";
    }


    /**
     * Journal d'audit - Consultation des logs
     */
    @GetMapping("/audit-logs")
    public String auditLogs(Model model, 
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           @RequestParam(value = "size", defaultValue = "20") int size,
                           @RequestParam(value = "search", required = false) String search) {
        Utilisateur superAdmin = getCurrentUser();
        model.addAttribute("superAdmin", superAdmin);
        
        // Calculer les statistiques
        long totalLogs = auditLogService.getTotalLogCount();
        long recentLogs = auditLogService.getRecentLogs().size();
        
        // Récupérer les logs avec pagination
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<com.universite.UniClubs.entities.AuditLog> logsPage;
        
        if (search != null && !search.trim().isEmpty()) {
            logsPage = auditLogService.searchLogs(search.trim(), pageable);
            model.addAttribute("searchTerm", search.trim());
        } else {
            logsPage = auditLogService.getAllLogs(pageable);
        }
        
        model.addAttribute("logs", logsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logsPage.getTotalPages());
        model.addAttribute("totalElements", logsPage.getTotalElements());
        model.addAttribute("totalLogs", totalLogs);
        model.addAttribute("recentLogs", recentLogs);
        
        return "super-admin/audit-logs";
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
        
        // Récupérer tous les clubs avec leurs inscriptions chargées
        var clubs = clubService.findAllClubsWithInscriptions();
        
        // Calculer les statistiques côté contrôleur pour éviter les erreurs LazyInitializationException
        long totalClubs = clubs.size();
        long clubsWithChef = clubs.stream().filter(c -> c.getChefClub() != null).count();
        long clubsWithoutChef = totalClubs - clubsWithChef;
        long totalMembers = clubs.stream().mapToInt(c -> c.getInscriptions() != null ? c.getInscriptions().size() : 0).sum();
        
        model.addAttribute("clubs", clubs);
        model.addAttribute("totalClubs", totalClubs);
        model.addAttribute("clubsWithChef", clubsWithChef);
        model.addAttribute("clubsWithoutChef", clubsWithoutChef);
        model.addAttribute("totalMembers", totalMembers);
        
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
        
        // Calculer les statistiques côté contrôleur pour éviter les erreurs SpEL
        long totalEvents = events.size();
        long publishedEvents = events.stream().filter(e -> e.getStatut().name().equals("PUBLIE")).count();
        long upcomingEvents = events.stream().filter(e -> e.getDateHeureDebut().isAfter(java.time.LocalDateTime.now())).count();
        long universityEvents = events.stream().filter(e -> e.getClub() == null).count();
        
        model.addAttribute("events", events);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("publishedEvents", publishedEvents);
        model.addAttribute("upcomingEvents", upcomingEvents);
        model.addAttribute("universityEvents", universityEvents);
        
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
