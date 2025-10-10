package com.universite.UniClubs.controllers;


import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Evenement;
import com.universite.UniClubs.entities.StatutEvenement;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.EvenementService;
import com.universite.UniClubs.services.EmailNotificationService;
import com.universite.UniClubs.services.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import com.universite.UniClubs.services.InscriptionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/gestion-club")
@PreAuthorize("hasRole('CHEF_DE_CLUB')")
public class ClubManagementController {

    private static final Logger logger = LoggerFactory.getLogger(ClubManagementController.class);

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private InscriptionService inscriptionService; // Injecter le nouveau service

    @Autowired
    private EvenementService evenementService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private CalendarService calendarService;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    // Méthode utilitaire pour récupérer l'utilisateur connecté de manière sécurisée
    private Utilisateur getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("=== GET CURRENT USER DEBUG ===");
        logger.info("Authentication: {}", authentication);
        logger.info("Principal: {}", authentication != null ? authentication.getPrincipal() : "NULL");
        logger.info("Principal class: {}", authentication != null && authentication.getPrincipal() != null ? 
                   authentication.getPrincipal().getClass().getName() : "NULL");
        
        if (authentication != null && authentication.getPrincipal() instanceof com.universite.UniClubs.services.CustomUserDetails) {
            com.universite.UniClubs.services.CustomUserDetails userDetails = 
                (com.universite.UniClubs.services.CustomUserDetails) authentication.getPrincipal();
            logger.info("CustomUserDetails trouvé: {}", userDetails);
            return userDetails.getUtilisateur();
        }
        
        // Si le principal est une String (nom d'utilisateur), on doit récupérer l'utilisateur depuis la base
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String email = (String) authentication.getPrincipal();
            logger.info("Principal est une String (email): {}", email);
            
            // Récupérer l'utilisateur depuis la base de données
            Optional<Utilisateur> utilisateurOptional = utilisateurRepository.findByEmail(email);
            if (utilisateurOptional.isPresent()) {
                logger.info("Utilisateur trouvé par email: {}", email);
                return utilisateurOptional.get();
            } else {
                logger.error("Utilisateur non trouvé pour l'email: {}", email);
                throw new RuntimeException("Utilisateur non trouvé pour l'email: " + email);
            }
        }
        
        logger.error("Type de principal non supporté: {}", 
                    authentication != null && authentication.getPrincipal() != null ? 
                    authentication.getPrincipal().getClass().getName() : "NULL");
        throw new RuntimeException("Utilisateur non authentifié - Type de principal non supporté");
    }

    @GetMapping
    public String showManagementDashboard(Model model, @ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub) {
        // La sécurité @PreAuthorize garantit que chefDeClub n'est pas null et a le bon rôle.

        // On appelle notre nouvelle méthode pour récupérer le club AVEC TOUS SES DÉTAILS
        Optional<Club> clubDirige = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());

        if (clubDirige.isEmpty()){
            model.addAttribute("error", "Vous n'êtes assigné à la direction d'aucun club.");
            return "error-page"; // Assure-toi de créer cette page error-page.html
        }
        model.addAttribute("club", clubDirige.get());
        return "gestion-club/dashboard";
    }

    // NOUVELLE MÉTHODE pour la page de gestion des membres
    @GetMapping("/membres")
    public String showMembersManagementPage(Model model, @ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub) {

        // On récupère le club du chef avec la liste de ses membres déjà chargée
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());

        if (clubOptional.isEmpty()) {
            model.addAttribute("error", "Impossible de trouver le club que vous dirigez.");
            return "error-page";
        }

        // On passe l'objet club complet à la vue
        model.addAttribute("club", clubOptional.get());

        return "gestion-club/membres"; // Renvoie vers une nouvelle vue
    }

    // NOUVELLE MÉTHODE pour approuver
    @PostMapping("/membres/approuver/{inscriptionId}")
    public String approuverMembre(@PathVariable UUID inscriptionId) {
        inscriptionService.approuverInscription(inscriptionId);
        // On redirige vers la même page pour voir le changement
        return "redirect:/gestion-club/membres";
    }

    // NOUVELLE MÉTHODE pour refuser
    @PostMapping("/membres/refuser/{inscriptionId}")
    public String refuserMembre(@PathVariable UUID inscriptionId) {
        // Pour l'instant, on met un motif par défaut.
        // Plus tard, on pourra ajouter une modale pour demander le motif au chef de club.
        inscriptionService.refuserInscription(inscriptionId, "Refus par le chef du club.");
        return "redirect:/gestion-club/membres";
    }

    // NOUVELLE MÉTHODE pour afficher le formulaire d'ajout de membre
    @GetMapping("/membres/ajouter")
    public String showAddMemberForm(Model model, @ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub) {
        // Récupérer le club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        
        if (clubOptional.isEmpty()) {
            model.addAttribute("error", "Impossible de trouver le club que vous dirigez.");
            return "error-page";
        }
        
        model.addAttribute("club", clubOptional.get());
        // TODO: Ajouter une liste d'utilisateurs disponibles pour l'invitation
        return "gestion-club/ajouter-membre";
    }

    // NOUVELLE MÉTHODE pour supprimer un membre du club
    @PostMapping("/membres/supprimer/{membreId}")
    public String supprimerMembre(@PathVariable UUID membreId, @ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub) {
        // Récupérer le club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        
        if (clubOptional.isEmpty()) {
            return "redirect:/gestion-club/membres?error=club-not-found";
        }
        
        Club club = clubOptional.get();
        
        // Trouver l'inscription du membre dans ce club
        inscriptionService.supprimerMembreDuClub(membreId, club.getId());
        
        return "redirect:/gestion-club/membres?success=member-removed";
    }

    // NOUVELLE MÉTHODE pour promouvoir un membre (changer son rôle)
    @PostMapping("/membres/promouvoir/{membreId}")
    public String promouvoirMembre(@PathVariable UUID membreId, @ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub) {
        // Récupérer le club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        
        if (clubOptional.isEmpty()) {
            return "redirect:/gestion-club/membres?error=club-not-found";
        }
        
        // TODO: Implémenter la logique de promotion (par exemple, devenir vice-président)
        // Pour l'instant, on redirige avec un message de succès
        return "redirect:/gestion-club/membres?success=member-promoted";
    }

    // NOUVELLE MÉTHODE pour la page de gestion des événements
    @GetMapping("/evenements")
    public String showEventsManagementPage(Model model, @ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub) {
        // Récupérer le club du chef avec la liste de ses événements déjà chargée
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());

        if (clubOptional.isEmpty()) {
            model.addAttribute("error", "Impossible de trouver le club que vous dirigez.");
            return "error-page";
        }

        // On passe l'objet club complet à la vue
        model.addAttribute("club", clubOptional.get());

        return "gestion-club/gestion-evenements";
    }

    // NOUVELLE MÉTHODE pour afficher le formulaire de création d'événement
    @GetMapping("/evenements/creer")
    public String showCreateEventForm(Model model, @ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub) {
        // Récupérer le club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());

        if (clubOptional.isEmpty()) {
            model.addAttribute("error", "Impossible de trouver le club que vous dirigez.");
            return "error-page";
        }

        model.addAttribute("club", clubOptional.get());
        return "gestion-club/creer-evenement";
    }

    // NOUVELLE MÉTHODE pour traiter la soumission du formulaire de création d'événement
    @PostMapping("/evenements/creer")
    public String createEvent(@RequestParam String titre,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String lieu,
                             @RequestParam String dateEvenement,
                             @RequestParam String heureEvenement,
                             @RequestParam(required = false) Integer capaciteMax,
                             @RequestParam String action,
                             @ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub,
                             Model model) {
        
        // Récupérer le club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        
        if (clubOptional.isEmpty()) {
            model.addAttribute("error", "Impossible de trouver le club que vous dirigez.");
            return "error-page";
        }
        
        Club club = clubOptional.get();
        
        try {
            // Créer l'objet événement
            Evenement evenement = new Evenement();
            evenement.setTitre(titre);
            evenement.setDescription(description);
            evenement.setLieu(lieu);
            evenement.setCapaciteMax(capaciteMax);
            evenement.setClub(club);
            
            // Parser la date et l'heure
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String dateTimeString = dateEvenement + " " + heureEvenement;
            LocalDateTime dateHeureDebut = LocalDateTime.parse(dateTimeString, formatter);
            evenement.setDateHeureDebut(dateHeureDebut);
            
            // Déterminer le statut selon l'action
            if ("draft".equals(action)) {
                evenement.setStatut(StatutEvenement.BROUILLON);
                evenementService.saveEvent(evenement);
                return "redirect:/gestion-club/evenements?success=draft-saved";
            } else if ("publish".equals(action)) {
                evenement.setStatut(StatutEvenement.PUBLIE);
                Evenement savedEvent = evenementService.saveEvent(evenement);
                // Envoyer les emails de notification à tous les membres du club
                emailNotificationService.notifyClubMembersAboutNewEvent(savedEvent, club);
                return "redirect:/gestion-club/evenements?success=event-published";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la création de l'événement: " + e.getMessage());
            model.addAttribute("club", club);
            return "gestion-club/creer-evenement";
        }
        
        return "redirect:/gestion-club/evenements?error=invalid-action";
    }

    // NOUVELLE MÉTHODE pour afficher les détails d'un événement
    @GetMapping("/evenements/{id}")
    public String viewEventDetails(@PathVariable UUID id, Model model) {
        logger.info("=== DÉBOGAGE DÉTAILS ÉVÉNEMENT ===");
        logger.info("ID de l'événement demandé: {}", id);
        
        Utilisateur chefDeClub = getCurrentUser();
        logger.info("Chef de club connecté: {} (ID: {})", chefDeClub.getEmail(), chefDeClub.getId());
        
        Optional<Evenement> evenementOptional = evenementService.findByIdWithClub(id);
        
        if (evenementOptional.isEmpty()) {
            logger.warn("Événement non trouvé: {}", id);
            model.addAttribute("error", "Événement non trouvé.");
            return "error-page";
        }
        
        Evenement evenement = evenementOptional.get();
        logger.info("Événement trouvé: {} (ID: {})", evenement.getTitre(), evenement.getId());
        logger.info("Club de l'événement: {} (ID: {})", 
                   evenement.getClub() != null ? evenement.getClub().getNom() : "NULL",
                   evenement.getClub() != null ? evenement.getClub().getId() : "NULL");
        
        // Vérifier que l'événement appartient au club du chef
        logger.info("Recherche du club pour le chef ID: {}", chefDeClub.getId());
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        
        if (clubOptional.isEmpty()) {
            logger.warn("Club non trouvé pour le chef: {}", chefDeClub.getId());
            model.addAttribute("error", "Impossible de trouver le club que vous dirigez.");
            return "error-page";
        }
        
        Club club = clubOptional.get();
        logger.info("Club trouvé: {} (ID: {})", club.getNom(), club.getId());
        logger.info("Nombre d'événements dans le club: {}", club.getEvenementsOrganises().size());
        
        // Lister tous les événements du club pour débogage
        for (Evenement event : club.getEvenementsOrganises()) {
            logger.info("Événement du club: {} (ID: {})", event.getTitre(), event.getId());
        }
        
        // Vérification directe : l'événement appartient-il au club du chef ?
        boolean eventBelongsToClub = evenement.getClub() != null && 
                                   evenement.getClub().getId().equals(club.getId());
        
        logger.info("Vérification directe - L'événement {} appartient au club {}: {}", 
                   evenement.getId(), club.getId(), eventBelongsToClub);
        
        if (!eventBelongsToClub) {
            logger.warn("Accès non autorisé - L'événement {} n'appartient pas au club {}", evenement.getId(), club.getId());
            model.addAttribute("error", "Vous n'avez pas l'autorisation de voir cet événement.");
            return "error-page";
        }
        
        logger.info("Accès autorisé - Affichage des détails");
        model.addAttribute("evenement", evenement);
        model.addAttribute("club", clubOptional.get());
        return "gestion-club/details-evenement";
    }

    // NOUVELLE MÉTHODE pour afficher le formulaire de modification d'un événement
    @GetMapping("/evenements/{id}/modifier")
    public String showEditEventForm(@PathVariable UUID id, Model model) {
        Utilisateur chefDeClub = getCurrentUser();
        
        Optional<Evenement> evenementOptional = evenementService.findByIdWithClub(id);
        
        if (evenementOptional.isEmpty()) {
            model.addAttribute("error", "Événement non trouvé.");
            return "error-page";
        }
        
        Evenement evenement = evenementOptional.get();
        
        // Vérifier que l'événement appartient au club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        if (clubOptional.isEmpty()) {
            model.addAttribute("error", "Impossible de trouver le club que vous dirigez.");
            return "error-page";
        }
        
        Club club = clubOptional.get();
        boolean eventBelongsToClub = evenement.getClub() != null && 
                                   evenement.getClub().getId().equals(club.getId());
        
        if (!eventBelongsToClub) {
            model.addAttribute("error", "Vous n'avez pas l'autorisation de modifier cet événement.");
            return "error-page";
        }
        
        model.addAttribute("evenement", evenement);
        model.addAttribute("club", clubOptional.get());
        return "gestion-club/modifier-evenement";
    }

    // NOUVELLE MÉTHODE pour traiter la modification d'un événement
    @PostMapping("/evenements/{id}/modifier")
    public String updateEvent(@PathVariable UUID id,
                             @RequestParam String titre,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String lieu,
                             @RequestParam String dateEvenement,
                             @RequestParam String heureEvenement,
                             @RequestParam(required = false) Integer capaciteMax,
                             @RequestParam String action,
                             Model model) {
        
        Utilisateur chefDeClub = getCurrentUser();
        
        Optional<Evenement> evenementOptional = evenementService.findByIdWithClub(id);
        
        if (evenementOptional.isEmpty()) {
            model.addAttribute("error", "Événement non trouvé.");
            return "error-page";
        }
        
        Evenement evenement = evenementOptional.get();
        
        // Vérifier que l'événement appartient au club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        if (clubOptional.isEmpty()) {
            model.addAttribute("error", "Impossible de trouver le club que vous dirigez.");
            return "error-page";
        }
        
        Club club = clubOptional.get();
        boolean eventBelongsToClub = evenement.getClub() != null && 
                                   evenement.getClub().getId().equals(club.getId());
        
        if (!eventBelongsToClub) {
            model.addAttribute("error", "Vous n'avez pas l'autorisation de modifier cet événement.");
            return "error-page";
        }
        
        try {
            // Mettre à jour les informations de l'événement
            evenement.setTitre(titre);
            evenement.setDescription(description);
            evenement.setLieu(lieu);
            evenement.setCapaciteMax(capaciteMax);
            
            // Parser la date et l'heure
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String dateTimeString = dateEvenement + " " + heureEvenement;
            LocalDateTime dateHeureDebut = LocalDateTime.parse(dateTimeString, formatter);
            evenement.setDateHeureDebut(dateHeureDebut);
            
            // Déterminer le statut selon l'action
            if ("draft".equals(action)) {
                evenement.setStatut(StatutEvenement.BROUILLON);
                evenementService.saveEvent(evenement);
                return "redirect:/gestion-club/evenements?success=draft-updated";
            } else if ("publish".equals(action)) {
                evenement.setStatut(StatutEvenement.PUBLIE);
                Evenement savedEvent = evenementService.saveEvent(evenement);
                // Envoyer les emails de notification à tous les membres du club
                emailNotificationService.notifyClubMembersAboutNewEvent(savedEvent, clubOptional.get());
                return "redirect:/gestion-club/evenements?success=event-updated-and-published";
            }
            
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la modification de l'événement: " + e.getMessage());
            model.addAttribute("evenement", evenement);
            model.addAttribute("club", clubOptional.get());
            return "gestion-club/modifier-evenement";
        }
        
        return "redirect:/gestion-club/evenements?error=invalid-action";
    }

    // NOUVELLE MÉTHODE pour publier/dépublier un événement
    @PostMapping("/evenements/{id}/toggle-status")
    public String toggleEventStatus(@PathVariable UUID id) {
        Utilisateur chefDeClub = getCurrentUser();
        
        Optional<Evenement> evenementOptional = evenementService.findByIdWithClub(id);
        
        if (evenementOptional.isEmpty()) {
            return "redirect:/gestion-club/evenements?error=event-not-found";
        }
        
        Evenement evenement = evenementOptional.get();
        
        // Vérifier que l'événement appartient au club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        if (clubOptional.isEmpty()) {
            return "redirect:/gestion-club/evenements?error=club-not-found";
        }
        
        Club club = clubOptional.get();
        boolean eventBelongsToClub = evenement.getClub() != null && 
                                   evenement.getClub().getId().equals(club.getId());
        
        if (!eventBelongsToClub) {
            return "redirect:/gestion-club/evenements?error=unauthorized";
        }
        
        // Changer le statut
        StatutEvenement newStatus = (evenement.getStatut() == StatutEvenement.BROUILLON) 
            ? StatutEvenement.PUBLIE 
            : StatutEvenement.BROUILLON;
        
        evenementService.updateEventStatus(id, newStatus);
        
        // Si on publie, envoyer les emails
        if (newStatus == StatutEvenement.PUBLIE) {
            emailNotificationService.notifyClubMembersAboutNewEvent(evenement, clubOptional.get());
            return "redirect:/gestion-club/evenements?success=event-published";
        } else {
            return "redirect:/gestion-club/evenements?success=event-unpublished";
        }
    }

    // NOUVELLE MÉTHODE pour supprimer un événement
    @PostMapping("/evenements/{id}/supprimer")
    public String deleteEvent(@PathVariable UUID id) {
        Utilisateur chefDeClub = getCurrentUser();
        
        Optional<Evenement> evenementOptional = evenementService.findByIdWithClub(id);
        
        if (evenementOptional.isEmpty()) {
            return "redirect:/gestion-club/evenements?error=event-not-found";
        }
        
        Evenement evenement = evenementOptional.get();
        
        // Vérifier que l'événement appartient au club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        if (clubOptional.isEmpty()) {
            return "redirect:/gestion-club/evenements?error=club-not-found";
        }
        
        Club club = clubOptional.get();
        boolean eventBelongsToClub = evenement.getClub() != null && 
                                   evenement.getClub().getId().equals(club.getId());
        
        if (!eventBelongsToClub) {
            return "redirect:/gestion-club/evenements?error=unauthorized";
        }
        
        evenementService.deleteEvent(id);
        return "redirect:/gestion-club/evenements?success=event-deleted";
    }

    // MÉTHODE DE DÉBOGAGE TEMPORAIRE
    @GetMapping("/debug/evenements")
    public String debugEvents(@ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub, Model model) {
        logger.info("=== DÉBOGAGE ÉVÉNEMENTS ===");
        logger.info("Chef de club: {} (ID: {})", chefDeClub.getEmail(), chefDeClub.getId());
        
        // Récupérer le club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        
        if (clubOptional.isEmpty()) {
            logger.warn("AUCUN CLUB TROUVÉ pour le chef: {}", chefDeClub.getId());
            model.addAttribute("error", "Aucun club trouvé pour ce chef");
            return "error-page";
        }
        
        Club club = clubOptional.get();
        logger.info("Club trouvé: {} (ID: {})", club.getNom(), club.getId());
        logger.info("Nombre d'événements dans le club: {}", club.getEvenementsOrganises().size());
        
        // Lister tous les événements du club
        for (Evenement event : club.getEvenementsOrganises()) {
            logger.info("Événement: {} (ID: {}) - Statut: {} - Club associé: {}", 
                       event.getTitre(), 
                       event.getId(), 
                       event.getStatut(),
                       event.getClub() != null ? event.getClub().getNom() : "NULL");
        }
        
        // Essayer de récupérer tous les événements de la base
        List<Evenement> allEvents = evenementService.findAllUpcomingEvents();
        logger.info("Nombre total d'événements dans la base: {}", allEvents.size());
        
        for (Evenement event : allEvents) {
            logger.info("Événement global: {} (ID: {}) - Club: {}", 
                       event.getTitre(), 
                       event.getId(),
                       event.getClub() != null ? event.getClub().getNom() : "NULL");
        }
        
        model.addAttribute("club", club);
        model.addAttribute("allEvents", allEvents);
        return "gestion-club/debug-evenements";
    }

    // MÉTHODE DE TEST POUR CRÉER UN ÉVÉNEMENT DE TEST
    @PostMapping("/debug/creer-evenement-test")
    public String createTestEvent(@ModelAttribute("utilisateurConnecte") Utilisateur chefDeClub) {
        logger.info("=== CRÉATION D'UN ÉVÉNEMENT DE TEST ===");
        
        // Récupérer le club du chef
        Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
        
        if (clubOptional.isEmpty()) {
            logger.warn("AUCUN CLUB TROUVÉ pour créer l'événement de test");
            return "redirect:/gestion-club/debug/evenements?error=no-club";
        }
        
        Club club = clubOptional.get();
        logger.info("Création d'un événement de test pour le club: {}", club.getNom());
        
        try {
            // Créer un événement de test
            Evenement evenement = new Evenement();
            evenement.setTitre("Événement de Test - " + java.time.LocalDateTime.now().toString());
            evenement.setDescription("Ceci est un événement de test créé automatiquement");
            evenement.setLieu("Salle de test");
            evenement.setDateHeureDebut(java.time.LocalDateTime.now().plusDays(1));
            evenement.setStatut(StatutEvenement.BROUILLON);
            evenement.setClub(club);
            
            // Sauvegarder l'événement
            Evenement savedEvent = evenementService.saveEvent(evenement);
            logger.info("Événement de test créé avec succès: {} (ID: {})", savedEvent.getTitre(), savedEvent.getId());
            
            return "redirect:/gestion-club/debug/evenements?success=test-event-created";
            
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'événement de test: {}", e.getMessage(), e);
            return "redirect:/gestion-club/debug/evenements?error=creation-failed";
        }
    }

    // ===== MÉTHODES POUR LE CALENDRIER =====
    
    /**
     * Endpoint de test pour diagnostiquer les problèmes d'accès
     */
    @GetMapping("/calendrier/test")
    @ResponseBody
    public ResponseEntity<String> testCalendarAccess() {
        logger.info("=== TEST ACCÈS CALENDRIER ===");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("Authentication: {}", auth);
            logger.info("Principal: {}", auth.getPrincipal());
            logger.info("Authorities: {}", auth.getAuthorities());
            
            Utilisateur chefDeClub = getCurrentUser();
            logger.info("Chef de club: {} (ID: {})", chefDeClub.getEmail(), chefDeClub.getId());
            
            return ResponseEntity.ok("Test réussi - Utilisateur: " + chefDeClub.getEmail());
            
        } catch (Exception e) {
            logger.error("Erreur lors du test: {}", e.getMessage(), e);
            return ResponseEntity.ok("Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint de test simple pour diagnostiquer les problèmes
     */
    @GetMapping("/calendrier/debug")
    @ResponseBody
    public ResponseEntity<String> debugCalendarAccess() {
        logger.info("=== DEBUG CALENDRIER SIMPLE ===");
        
        try {
            // Test 1: Authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            logger.info("✓ Authentication: {}", auth != null ? "OK" : "NULL");
            
            if (auth == null) {
                return ResponseEntity.ok("ERREUR: Authentication est null");
            }
            
            // Test 2: Principal
            Object principal = auth.getPrincipal();
            logger.info("✓ Principal: {}", principal != null ? principal.getClass().getSimpleName() : "NULL");
            
            if (!(principal instanceof com.universite.UniClubs.services.CustomUserDetails)) {
                return ResponseEntity.ok("ERREUR: Principal n'est pas CustomUserDetails: " + principal.getClass().getName());
            }
            
            // Test 3: CustomUserDetails
            com.universite.UniClubs.services.CustomUserDetails userDetails = 
                (com.universite.UniClubs.services.CustomUserDetails) principal;
            logger.info("✓ CustomUserDetails: OK");
            
            // Test 4: Utilisateur
            Utilisateur utilisateur = userDetails.getUtilisateur();
            logger.info("✓ Utilisateur: {} (ID: {})", utilisateur.getEmail(), utilisateur.getId());
            
            // Test 5: Repository
            Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(utilisateur.getId());
            logger.info("✓ Repository query: {}", clubOptional.isPresent() ? "Club trouvé" : "Club non trouvé");
            
            if (clubOptional.isPresent()) {
                Club club = clubOptional.get();
                logger.info("✓ Club: {} (ID: {})", club.getNom(), club.getId());
                return ResponseEntity.ok("SUCCESS: " + club.getNom() + " - " + club.getEvenementsOrganises().size() + " événements");
            } else {
                return ResponseEntity.ok("ERREUR: Aucun club trouvé pour l'utilisateur " + utilisateur.getEmail());
            }
            
        } catch (Exception e) {
            logger.error("ERREUR dans debug: {}", e.getMessage(), e);
            return ResponseEntity.ok("ERREUR: " + e.getMessage() + " - " + e.getClass().getSimpleName());
        }
    }
    
    /**
     * Endpoint de test simple pour le calendrier
     */
    @GetMapping("/calendrier/test-simple")
    public String testSimpleCalendar(Model model) {
        logger.info("=== TEST CALENDRIER SIMPLE ===");
        
        try {
            Utilisateur chefDeClub = getCurrentUser();
            logger.info("Chef de club: {} (ID: {})", chefDeClub.getEmail(), chefDeClub.getId());
            
            Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
            
            if (clubOptional.isPresent()) {
                Club club = clubOptional.get();
                logger.info("Club trouvé: {} (ID: {})", club.getNom(), club.getId());
                
                model.addAttribute("club", club);
                model.addAttribute("utilisateurConnecte", chefDeClub);
                
                return "gestion-club/calendrier-test";
            } else {
                logger.error("Club non trouvé pour le chef ID: {}", chefDeClub.getId());
                model.addAttribute("error", "Club non trouvé");
                return "error-page";
            }
            
        } catch (Exception e) {
            logger.error("Erreur dans test simple: {}", e.getMessage(), e);
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "error-page";
        }
    }
    
    /**
     * Affiche la page de gestion du calendrier du club
     */
    @GetMapping("/calendrier")
    public String showCalendarPage(Model model) {
        logger.info("=== AFFICHAGE PAGE CALENDRIER ===");
        
        try {
            Utilisateur chefDeClub = getCurrentUser();
            logger.info("Chef de club connecté: {} (ID: {})", chefDeClub.getEmail(), chefDeClub.getId());
            
            // Récupérer le club avec tous ses détails
            Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
            
            if (clubOptional.isEmpty()) {
                logger.error("Club non trouvé pour le chef ID: {}", chefDeClub.getId());
                return "redirect:/gestion-club?error=club-not-found";
            }
            
            Club club = clubOptional.get();
            logger.info("Club trouvé: {} (ID: {})", club.getNom(), club.getId());
            logger.info("Nombre d'événements dans le club: {}", club.getEvenementsOrganises().size());
            
            model.addAttribute("club", club);
            model.addAttribute("utilisateurConnecte", chefDeClub);
            
            logger.info("Page calendrier affichée avec succès");
            return "gestion-club/calendrier";
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'affichage de la page calendrier: {}", e.getMessage(), e);
            return "redirect:/gestion-club?error=calendar-page-error";
        }
    }

    /**
     * Exporte les événements du club au format ICS pour synchronisation avec les calendriers
     */
    @GetMapping("/calendrier/export/ics")
    public ResponseEntity<String> exportCalendarICS() {
        logger.info("=== EXPORT CALENDRIER ICS ===");
        
        try {
            Utilisateur chefDeClub = getCurrentUser();
            logger.info("Chef de club connecté: {} (ID: {})", chefDeClub.getEmail(), chefDeClub.getId());
            
            // Récupérer le club avec tous ses détails
            Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
            
            if (clubOptional.isEmpty()) {
                logger.error("Club non trouvé pour le chef ID: {}", chefDeClub.getId());
                return ResponseEntity.notFound().build();
            }
            
            Club club = clubOptional.get();
            logger.info("Club trouvé: {} (ID: {})", club.getNom(), club.getId());
            
            // Générer le contenu ICS
            String icsContent = calendarService.generateICSContent(club);
            String fileName = calendarService.generateICSFileName(club);
            
            logger.info("Contenu ICS généré avec succès ({} caractères)", icsContent.length());
            
            // Configurer les headers pour le téléchargement
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/calendar; charset=utf-8"));
            headers.setContentDispositionFormData("attachment", fileName);
            headers.setCacheControl("no-cache, no-store, must-revalidate");
            headers.setPragma("no-cache");
            headers.setExpires(0);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(icsContent);
                
        } catch (Exception e) {
            logger.error("Erreur lors de l'export ICS: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API endpoint pour récupérer les événements au format JSON pour FullCalendar
     */
    @GetMapping("/calendrier/api/events")
    @ResponseBody
    public ResponseEntity<List<CalendarEventDto>> getCalendarEvents(
            @RequestParam(value = "filter", defaultValue = "all") String filter) {
        logger.info("=== API CALENDRIER EVENTS ===");
        
        try {
            Utilisateur chefDeClub = getCurrentUser();
            logger.info("Chef de club connecté: {} (ID: {})", chefDeClub.getEmail(), chefDeClub.getId());
            
            // Récupérer le club avec tous ses détails
            Optional<Club> clubOptional = clubRepository.findClubWithDetailsByChefId(chefDeClub.getId());
            
            if (clubOptional.isEmpty()) {
                logger.error("Club non trouvé pour le chef ID: {}", chefDeClub.getId());
                return ResponseEntity.notFound().build();
            }
            
            Club club = clubOptional.get();
            logger.info("Club trouvé: {} (ID: {})", club.getNom(), club.getId());
            
            // Convertir les événements en DTOs pour FullCalendar
            List<CalendarEventDto> events = new ArrayList<>();
            LocalDateTime maintenant = LocalDateTime.now();
            
            if (club.getEvenementsOrganises() != null) {
                for (Evenement evenement : club.getEvenementsOrganises()) {
                    // Appliquer le filtre selon le paramètre
                    boolean shouldInclude = false;
                    
                    if ("all".equals(filter)) {
                        // Tous les événements publiés
                        shouldInclude = evenement.getStatut() != null && 
                                      evenement.getStatut().name().equals("PUBLIE");
                    } else if ("upcoming".equals(filter)) {
                        // Seulement les événements à venir
                        shouldInclude = evenement.getStatut() != null && 
                                      evenement.getStatut().name().equals("PUBLIE") &&
                                      evenement.getDateHeureDebut().isAfter(maintenant);
                    } else if ("past".equals(filter)) {
                        // Seulement les événements passés
                        shouldInclude = evenement.getStatut() != null && 
                                      evenement.getStatut().name().equals("PUBLIE") &&
                                      evenement.getDateHeureDebut().isBefore(maintenant);
                    }
                    
                    if (shouldInclude) {
                        
                        CalendarEventDto eventDto = new CalendarEventDto();
                        eventDto.setId(evenement.getId().toString());
                        eventDto.setTitle(evenement.getTitre());
                        eventDto.setStart(evenement.getDateHeureDebut());
                        
                        // Date de fin (2 heures par défaut si pas spécifiée)
                        LocalDateTime endTime = evenement.getDateHeureDebut().plusHours(2);
                        eventDto.setEnd(endTime);
                        
                        // Description et lieu
                        if (evenement.getDescription() != null && !evenement.getDescription().trim().isEmpty()) {
                            eventDto.setDescription(evenement.getDescription());
                        }
                        
                        if (evenement.getLieu() != null && !evenement.getLieu().trim().isEmpty()) {
                            eventDto.setLocation(evenement.getLieu());
                        }
                        
                        // Couleur selon le statut
                        eventDto.setColor("#0d6efd"); // Bleu pour publié
                        
                        // URL pour les détails
                        eventDto.setUrl("/gestion-club/evenements/" + evenement.getId().toString());
                        
                        events.add(eventDto);
                    }
                }
            }
            
            logger.info("{} événements trouvés pour le calendrier", events.size());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(events);
                
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des événements calendrier: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * DTO pour les événements du calendrier
     */
    public static class CalendarEventDto {
        private String id;
        private String title;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime start;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime end;
        
        private String description;
        private String location;
        private String color;
        private String url;
        
        // Getters et setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public LocalDateTime getStart() { return start; }
        public void setStart(LocalDateTime start) { this.start = start; }
        
        public LocalDateTime getEnd() { return end; }
        public void setEnd(LocalDateTime end) { this.end = end; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

}