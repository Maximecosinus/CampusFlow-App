package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Evenement;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.ChefClubValidationService;
import com.universite.UniClubs.services.ChefClubValidationService.ValidationResult;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.EvenementService;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Autowired
    private ChefClubValidationService chefClubValidationService;
    
    @Autowired
    private EvenementService evenementService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

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
            System.out.println("=== DEBUG CREATION CLUB ===");
            System.out.println("Nom du club: " + club.getNom());
            System.out.println("ChefId reçu: " + chefId);
            
            // Créer le club
            Club savedClub = clubService.createClub(club);
            System.out.println("Club créé avec ID: " + savedClub.getId());
            
            // Si un chef a été sélectionné, valider et l'assigner
            if (chefId != null) {
                System.out.println("Tentative d'assignation du chef: " + chefId);
                ValidationResult validation = chefClubValidationService.validateChefClubAssignmentById(chefId);
                
                if (!validation.isValid()) {
                    System.out.println("Validation échouée: " + validation.getMessage());
                    // Supprimer le club créé car la validation a échoué
                    clubService.deleteClub(savedClub.getId());
                    redirectAttributes.addFlashAttribute("error", validation.getMessage());
                    return "redirect:/admin/clubs/create";
                }
                
                Utilisateur chef = validation.getUser();
                System.out.println("Chef trouvé: " + chef.getPrenom() + " " + chef.getNom());
                
                // Assigner le rôle de chef de club
                chef.setRole(com.universite.UniClubs.entities.Role.CHEF_DE_CLUB);
                utilisateurService.updateUser(chef);
                System.out.println("Rôle mis à jour vers CHEF_DE_CLUB");
                
                // Assigner le club au chef
                savedClub.setChefClub(chef);
                clubService.updateClub(savedClub);
                System.out.println("Chef assigné au club");
                
                redirectAttributes.addFlashAttribute("success", 
                    "Club créé avec succès ! Chef de club assigné : " + chef.getPrenom() + " " + chef.getNom());
            } else {
                System.out.println("Aucun chef sélectionné");
                redirectAttributes.addFlashAttribute("success", 
                    "Club créé avec succès ! Vous pourrez assigner un chef de club plus tard.");
            }
            
            return "redirect:/admin/clubs";
            
        } catch (Exception e) {
            System.out.println("Erreur lors de la création: " + e.getMessage());
            e.printStackTrace();
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
     * Page pour assigner un chef de club à un club existant
     */
    @GetMapping("/clubs/{clubId}/assign-chef")
    public String showAssignChefForm(@PathVariable UUID clubId, Model model) {
        // Utiliser la nouvelle méthode qui charge les inscriptions de manière eager
        Club club = clubService.findClubByIdWithInscriptions(clubId)
            .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        
        model.addAttribute("club", club);
        return "admin/assign-chef-simple"; // Template simplifié temporairement
    }

    /**
     * Traitement de l'assignation d'un chef de club
     */
    @PostMapping("/clubs/{clubId}/assign-chef")
    public String assignChefClub(@PathVariable UUID clubId,
                               @RequestParam("chefEmail") String chefEmail,
                               RedirectAttributes redirectAttributes) {
        try {
            Club club = clubService.findClubById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));

            // Valider l'assignation
            ValidationResult validation = chefClubValidationService.validateChefClubAssignment(chefEmail);
            
            if (!validation.isValid()) {
                redirectAttributes.addFlashAttribute("error", validation.getMessage());
                return "redirect:/admin/clubs/" + clubId + "/assign-chef";
            }

            Utilisateur chef = validation.getUser();

            // Vérifier que le club n'a pas déjà un chef
            if (club.getChefClub() != null) {
                redirectAttributes.addFlashAttribute("error", 
                    "Ce club a déjà un chef de club : " + club.getChefClub().getPrenom() + " " + club.getChefClub().getNom());
                return "redirect:/admin/clubs/" + clubId + "/assign-chef";
            }

            // Assigner le rôle de chef de club
            chef.setRole(com.universite.UniClubs.entities.Role.CHEF_DE_CLUB);
            utilisateurService.updateUser(chef);

            // Assigner le club au chef
            club.setChefClub(chef);
            clubService.updateClub(club);

            redirectAttributes.addFlashAttribute("success", 
                "Chef de club assigné avec succès ! " + chef.getPrenom() + " " + chef.getNom() + " dirige maintenant le club " + club.getNom());
            
            return "redirect:/admin/clubs";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de l'assignation du chef de club : " + e.getMessage());
            return "redirect:/admin/clubs/" + clubId + "/assign-chef";
        }
    }

    /**
     * API pour valider un email de chef de club
     */
    @GetMapping("/clubs/validate-chef-email")
    @ResponseBody
    public ValidationResponseDto validateChefEmail(@RequestParam("email") String email) {
        System.out.println("=== DEBUG VALIDATION EMAIL ===");
        System.out.println("Email reçu: " + email);
        
        ValidationResult result = chefClubValidationService.validateChefClubAssignment(email);
        
        System.out.println("Résultat validation:");
        System.out.println("- Valid: " + result.isValid());
        System.out.println("- Message: " + result.getMessage());
        System.out.println("- User: " + (result.getUser() != null ? result.getUser().getEmail() : "null"));
        
        // Créer un DTO simple pour éviter les problèmes de sérialisation JSON
        ValidationResponseDto response = new ValidationResponseDto();
        response.setValid(result.isValid());
        response.setMessage(result.getMessage());
        
        if (result.getUser() != null) {
            response.setUserEmail(result.getUser().getEmail());
            response.setUserNom(result.getUser().getNom());
            response.setUserPrenom(result.getUser().getPrenom());
            response.setUserId(result.getUser().getId());
        }
        
        return response;
    }

    /**
     * Section Événements - Liste de tous les événements
     */
    @GetMapping("/events")
    public String adminEvents(Model model) {
        try {
            System.out.println("=== ADMIN EVENTS PAGE LOADED ===");
            Utilisateur admin = getCurrentUser();
            model.addAttribute("admin", admin);
            System.out.println("Admin connecté: " + admin.getPrenom() + " " + admin.getNom());
            
            // Récupérer tous les événements
            List<Evenement> events = evenementService.findAllEvents();
            System.out.println("Nombre d'événements trouvés: " + events.size());
            model.addAttribute("events", events);
            
            // Calculer les statistiques
            long totalEvents = events.size();
            long publishedEvents = events.stream()
                .filter(e -> e.getStatut() == com.universite.UniClubs.entities.StatutEvenement.PUBLIE)
                .count();
            long upcomingEvents = events.stream()
                .filter(e -> e.getDateHeureDebut().isAfter(LocalDateTime.now()))
                .count();
            long universityEvents = events.stream()
                .filter(e -> e.getClub() == null)
                .count();
            
            System.out.println("Statistiques - Total: " + totalEvents + ", Publiés: " + publishedEvents + ", À venir: " + upcomingEvents + ", Universitaires: " + universityEvents);
            
            model.addAttribute("totalEvents", totalEvents);
            model.addAttribute("publishedEvents", publishedEvents);
            model.addAttribute("upcomingEvents", upcomingEvents);
            model.addAttribute("universityEvents", universityEvents);
            
            System.out.println("=== RENDU DE LA PAGE ADMIN/EVENTS ===");
            return "admin/events";
        } catch (Exception e) {
            System.out.println("ERREUR lors du chargement des événements: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Erreur lors du chargement des événements : " + e.getMessage());
            return "admin/events";
        }
    }
    
    /**
     * Créer un événement universitaire
     */
    @PostMapping("/events/create")
    public String createUniversityEvent(@RequestParam("titre") String titre,
                                      @RequestParam("description") String description,
                                      @RequestParam("lieu") String lieu,
                                      @RequestParam("dateDebut") String dateDebut,
                                      @RequestParam("heureDebut") String heureDebut,
                                      @RequestParam(value = "dateFin", required = false) String dateFin,
                                      @RequestParam(value = "heureFin", required = false) String heureFin,
                                      @RequestParam(value = "capaciteMax", required = false) Integer capaciteMax,
                                      @RequestParam("statut") String statut,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Créer l'événement
            Evenement event = new Evenement();
            event.setTitre(titre);
            event.setDescription(description);
            event.setLieu(lieu);
            
            // Parser les dates
            LocalDate debutDate = LocalDate.parse(dateDebut);
            LocalTime debutTime = LocalTime.parse(heureDebut);
            event.setDateHeureDebut(LocalDateTime.of(debutDate, debutTime));
            
            // Note: L'entité Evenement n'a pas de champ dateHeureFin pour l'instant
            // if (dateFin != null && !dateFin.isEmpty() && heureFin != null && !heureFin.isEmpty()) {
            //     LocalDate finDate = LocalDate.parse(dateFin);
            //     LocalTime finTime = LocalTime.parse(heureFin);
            //     event.setDateHeureFin(LocalDateTime.of(finDate, finTime));
            // }
            
            if (capaciteMax != null) {
                event.setCapaciteMax(capaciteMax);
            }
            
            // Définir le statut
            event.setStatut(com.universite.UniClubs.entities.StatutEvenement.valueOf(statut));
            
            // Les événements universitaires n'ont pas de club associé
            event.setClub(null);
            
            // Sauvegarder
            evenementService.saveEvent(event);
            
            redirectAttributes.addFlashAttribute("success", 
                "Événement universitaire créé avec succès !");
            
            return "redirect:/admin/events";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la création de l'événement : " + e.getMessage());
            return "redirect:/admin/events";
        }
    }
    
    /**
     * Voir les détails d'un événement
     */
    @GetMapping("/events/{eventId}")
    public String viewEvent(@PathVariable UUID eventId, Model model) {
        try {
            System.out.println("=== BOUTON VOIR CLIQUE - ID: " + eventId + " ===");
            Evenement event = evenementService.findByIdWithClub(eventId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
            
            System.out.println("Événement trouvé: " + event.getTitre());
            model.addAttribute("event", event);
            return "admin/event-details";
        } catch (Exception e) {
            System.out.println("ERREUR lors de la vue de l'événement: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/events?error=" + e.getMessage();
        }
    }
    
    /**
     * Formulaire de modification d'un événement
     */
    @GetMapping("/events/{eventId}/edit")
    public String editEventForm(@PathVariable UUID eventId, Model model) {
        try {
            System.out.println("=== BOUTON MODIFIER CLIQUE - ID: " + eventId + " ===");
            Evenement event = evenementService.findByIdWithClub(eventId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
            
            System.out.println("Événement trouvé pour modification: " + event.getTitre());
            model.addAttribute("event", event);
            return "admin/edit-event-form";
        } catch (Exception e) {
            System.out.println("ERREUR lors de l'édition de l'événement: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/events?error=" + e.getMessage();
        }
    }
    
    /**
     * Mettre à jour un événement
     */
    @PostMapping("/events/{eventId}/update")
    public String updateEvent(@PathVariable UUID eventId,
                            @RequestParam("titre") String titre,
                            @RequestParam("description") String description,
                            @RequestParam("lieu") String lieu,
                            @RequestParam("dateDebut") String dateDebut,
                            @RequestParam("heureDebut") String heureDebut,
                            @RequestParam(value = "dateFin", required = false) String dateFin,
                            @RequestParam(value = "heureFin", required = false) String heureFin,
                            @RequestParam(value = "capaciteMax", required = false) Integer capaciteMax,
                            @RequestParam("statut") String statut,
                            RedirectAttributes redirectAttributes) {
        try {
            Evenement event = evenementService.findByIdWithClub(eventId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
            
            // Mettre à jour les propriétés
            event.setTitre(titre);
            event.setDescription(description);
            event.setLieu(lieu);
            
            // Parser les dates
            LocalDate debutDate = LocalDate.parse(dateDebut);
            LocalTime debutTime = LocalTime.parse(heureDebut);
            event.setDateHeureDebut(LocalDateTime.of(debutDate, debutTime));
            
            // Note: L'entité Evenement n'a pas de champ dateHeureFin pour l'instant
            // if (dateFin != null && !dateFin.isEmpty() && heureFin != null && !heureFin.isEmpty()) {
            //     LocalDate finDate = LocalDate.parse(dateFin);
            //     LocalTime finTime = LocalTime.parse(heureFin);
            //     event.setDateHeureFin(LocalDateTime.of(finDate, finTime));
            // } else {
            //     event.setDateHeureFin(null);
            // }
            
            if (capaciteMax != null) {
                event.setCapaciteMax(capaciteMax);
            } else {
                event.setCapaciteMax(null);
            }
            
            // Définir le statut
            event.setStatut(com.universite.UniClubs.entities.StatutEvenement.valueOf(statut));
            
            // Sauvegarder
            evenementService.saveEvent(event);
            
            redirectAttributes.addFlashAttribute("success", 
                "Événement modifié avec succès !");
            
            return "redirect:/admin/events";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la modification de l'événement : " + e.getMessage());
            return "redirect:/admin/events";
        }
    }
    
    /**
     * Supprimer un événement
     */
    @PostMapping("/events/{eventId}/delete")
    public String deleteEvent(@PathVariable UUID eventId, RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== BOUTON SUPPRIMER CLIQUE - ID: " + eventId + " ===");
            evenementService.deleteEvent(eventId);
            
            System.out.println("Événement supprimé avec succès: " + eventId);
            redirectAttributes.addFlashAttribute("success", 
                "Événement supprimé avec succès !");
            
            return "redirect:/admin/events";
            
        } catch (Exception e) {
            System.out.println("ERREUR lors de la suppression de l'événement: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", 
                "Erreur lors de la suppression de l'événement : " + e.getMessage());
            return "redirect:/admin/events";
        }
    }

    /**
     * Test simple pour vérifier si le JavaScript fonctionne
     */
    @GetMapping("/test")
    public String test() {
        return "test";
    }

    /**
     * Endpoint temporaire pour créer des événements de test
     */
    @GetMapping("/create-test-events")
    public String createTestEvents(RedirectAttributes redirectAttributes) {
        try {
            // Événement 1: Conférence sur l'IA
            Evenement event1 = new Evenement();
            event1.setTitre("Conférence sur l'Intelligence Artificielle");
            event1.setDescription("Une conférence passionnante sur les dernières avancées en intelligence artificielle et leur impact sur notre société.");
            event1.setLieu("Amphithéâtre A - Campus Principal");
            event1.setDateHeureDebut(LocalDateTime.of(2024, 12, 15, 14, 0));
            event1.setCapaciteMax(150);
            event1.setStatut(com.universite.UniClubs.entities.StatutEvenement.PUBLIE);
            event1.setClub(null); // Événement universitaire
            evenementService.saveEvent(event1);
            
            // Événement 2: Tournoi de Football
            Evenement event2 = new Evenement();
            event2.setTitre("Tournoi de Football Inter-Clubs");
            event2.setDescription("Tournoi de football entre les différents clubs de l'université. Inscriptions ouvertes à tous les étudiants.");
            event2.setLieu("Stade Universitaire");
            event2.setDateHeureDebut(LocalDateTime.of(2024, 12, 20, 10, 0));
            event2.setCapaciteMax(200);
            event2.setStatut(com.universite.UniClubs.entities.StatutEvenement.PUBLIE);
            event2.setClub(null); // Événement universitaire
            evenementService.saveEvent(event2);
            
            // Événement 3: Atelier de Programmation
            Evenement event3 = new Evenement();
            event3.setTitre("Atelier de Programmation Web");
            event3.setDescription("Apprenez les bases du développement web avec HTML, CSS et JavaScript. Matériel fourni.");
            event3.setLieu("Salle Informatique B-205");
            event3.setDateHeureDebut(LocalDateTime.of(2024, 12, 18, 16, 0));
            event3.setCapaciteMax(30);
            event3.setStatut(com.universite.UniClubs.entities.StatutEvenement.BROUILLON);
            event3.setClub(null); // Événement universitaire
            evenementService.saveEvent(event3);
            
            redirectAttributes.addFlashAttribute("success", "3 événements de test créés avec succès !");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la création des événements de test : " + e.getMessage());
        }
        
        return "redirect:/admin/events";
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
        if (authentication != null && authentication.getPrincipal() instanceof com.universite.UniClubs.services.CustomUserDetails) {
            com.universite.UniClubs.services.CustomUserDetails userDetails = (com.universite.UniClubs.services.CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUtilisateur();
        }
        return null;
    }

    /**
     * Affiche la page de profil de l'administrateur
     */
    @GetMapping("/profile")
    public String adminProfile(Model model) {
        Utilisateur admin = getCurrentUser();
        model.addAttribute("admin", admin);
        return "admin/profile";
    }

    /**
     * Met à jour le profil de l'administrateur
     */
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String prenom, 
                               @RequestParam String nom, 
                               @RequestParam(required = false) String bio,
                               RedirectAttributes redirectAttributes) {
        try {
            Utilisateur admin = getCurrentUser();
            admin.setPrenom(prenom);
            admin.setNom(nom);
            if (bio != null) {
                admin.setBio(bio);
            }
            
               utilisateurService.updateUser(admin);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour du profil");
        }
        
        return "redirect:/admin/profile";
    }

    /**
     * Change le mot de passe de l'administrateur
     */
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        try {
            Utilisateur admin = getCurrentUser();
            
            // Vérifier que le mot de passe actuel est correct
            if (!passwordEncoder.matches(currentPassword, admin.getMotDePasse())) {
                redirectAttributes.addFlashAttribute("error", "Mot de passe actuel incorrect");
                return "redirect:/admin/profile";
            }
            
            // Vérifier que les nouveaux mots de passe correspondent
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Les nouveaux mots de passe ne correspondent pas");
                return "redirect:/admin/profile";
            }
            
            // Encoder et sauvegarder le nouveau mot de passe
            admin.setMotDePasse(passwordEncoder.encode(newPassword));
               utilisateurService.updateUser(admin);
            
            redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du changement de mot de passe");
        }
        
        return "redirect:/admin/profile";
    }

    /**
     * DTO simple pour la réponse de validation (évite les problèmes de sérialisation JSON)
     */
    public static class ValidationResponseDto {
        private boolean valid;
        private String message;
        private String userEmail;
        private String userNom;
        private String userPrenom;
        private java.util.UUID userId;

        // Constructeurs
        public ValidationResponseDto() {}

        public ValidationResponseDto(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        // Getters et setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getUserNom() {
            return userNom;
        }

        public void setUserNom(String userNom) {
            this.userNom = userNom;
        }

        public String getUserPrenom() {
            return userPrenom;
        }

        public void setUserPrenom(String userPrenom) {
            this.userPrenom = userPrenom;
        }

        public java.util.UUID getUserId() {
            return userId;
        }

        public void setUserId(java.util.UUID userId) {
            this.userId = userId;
        }
    }
}
