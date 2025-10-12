package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

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
    
    @Autowired
    private ClubService clubService;

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
     * Section Clubs - Liste de tous les clubs
     */
    @GetMapping("/clubs")
    public String adminClubs(Model model) {
        Utilisateur admin = getCurrentUser();
        model.addAttribute("admin", admin);
        
        // Récupérer tous les clubs avec leurs inscriptions
        List<Club> clubs = clubService.findAllClubsWithInscriptions();
        model.addAttribute("clubs", clubs);
        
        return "admin/clubs";
    }

    /**
     * Formulaire de création d'un nouveau club
     */
    @GetMapping("/clubs/create")
    public String showCreateClubForm(Model model) {
        Utilisateur admin = getCurrentUser();
        model.addAttribute("admin", admin);
        
        // Ajouter un objet Club vide pour le formulaire
        model.addAttribute("club", new Club());
        
        return "admin/create-club";
    }

    /**
     * Traitement de la création d'un nouveau club
     */
    @PostMapping("/clubs/create")
    public String createClub(@ModelAttribute("club") Club club,
                           @RequestParam(value = "chefId", required = false) UUID chefId,
                           RedirectAttributes redirectAttributes) {
        try {
            // Créer le club
            Club savedClub = clubService.createClub(club);
            
            // Si un chef a été sélectionné, l'assigner
            if (chefId != null) {
                Utilisateur chef = utilisateurService.findById(chefId)
                    .orElseThrow(() -> new RuntimeException("Étudiant non trouvé"));
                
                // Assigner le rôle de chef de club
                chef.setRole(com.universite.UniClubs.entities.Role.CHEF_DE_CLUB);
                utilisateurService.updateUser(chef);
                
                // Assigner le club au chef
                savedClub.setChefClub(chef);
                clubService.updateClub(savedClub);
                
                redirectAttributes.addFlashAttribute("success", 
                    "Club créé avec succès ! Chef de club assigné : " + chef.getPrenom() + " " + chef.getNom());
            } else {
                redirectAttributes.addFlashAttribute("success", 
                    "Club créé avec succès ! Vous pourrez assigner un chef de club plus tard.");
            }
            
            return "redirect:/admin/clubs";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la création du club : " + e.getMessage());
            return "redirect:/admin/clubs/create";
        }
    }

    /**
     * API pour rechercher des étudiants par nom ou email
     */
    @GetMapping("/clubs/search-students")
    @ResponseBody
    public List<Utilisateur> searchStudents(@RequestParam("query") String query) {
        return utilisateurService.searchStudentsByNameOrEmail(query);
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
