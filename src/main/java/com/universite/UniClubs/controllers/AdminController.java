package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.EvenementService;
import com.universite.UniClubs.services.UtilisateurService;
import com.universite.UniClubs.services.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ClubService clubService;

    @Autowired
    private EvenementService evenementService;

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/accueil")
    public String showAdminDashboard(Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        // Récupérer les données pour le dashboard
        List<com.universite.UniClubs.entities.Club> clubs = clubService.getAllClubs();
        List<com.universite.UniClubs.entities.Evenement> evenementsAVenir = evenementService.findUpcomingEvents();
        List<com.universite.UniClubs.entities.Evenement> evenementsRecents = evenementService.findRecentEvents();
        List<com.universite.UniClubs.entities.Evenement> mesEvenementsCrees = evenementService.findEventsByCreator(utilisateur.getId());
        List<com.universite.UniClubs.entities.Evenement> evenementsPlanifies = evenementService.findPlannedEvents();

        // Statistiques
        int nombreClubs = clubs.size();
        int nombreEtudiants = utilisateurService.countStudents();
        int evenementsCeMois = evenementService.countEventsThisMonth();
        double tauxParticipation = evenementService.calculateAverageParticipationRate();

        // Ajouter les données au modèle
        model.addAttribute("clubs", clubs);
        model.addAttribute("clubsRecents", clubs);
        model.addAttribute("evenementsAVenir", evenementsAVenir);
        model.addAttribute("evenementsRecents", evenementsRecents);
        model.addAttribute("mesEvenementsCrees", mesEvenementsCrees);
        model.addAttribute("evenementsPlanifies", evenementsPlanifies);
        
        // Statistiques
        model.addAttribute("nombreClubs", nombreClubs);
        model.addAttribute("nombreEtudiants", nombreEtudiants);
        model.addAttribute("evenementsCeMois", evenementsCeMois);
        model.addAttribute("tauxParticipation", String.format("%.1f", tauxParticipation));

        return "admin/accueil";
    }

    @GetMapping("/clubs")
    public String showClubsManagement(Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        List<com.universite.UniClubs.entities.Club> clubs = clubService.getAllClubs();
        List<Utilisateur> etudiants = utilisateurService.getAllStudents();

        model.addAttribute("clubs", clubs);
        model.addAttribute("etudiants", etudiants);

        return "admin/clubs";
    }

    @GetMapping("/evenements/liste")
    public String showEventsList(Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        List<com.universite.UniClubs.entities.Evenement> evenements = evenementService.getAllEvents();
        List<com.universite.UniClubs.entities.Club> clubs = clubService.getAllClubs();
        int evenementsCeMois = evenementService.countEventsThisMonth();

        model.addAttribute("evenements", evenements);
        model.addAttribute("clubs", clubs);
        model.addAttribute("evenementsCeMois", evenementsCeMois);

        return "admin/evenements/liste";
    }

    @GetMapping("/evenements/gestion")
    public String showEventsManagement(Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        List<com.universite.UniClubs.entities.Club> clubs = clubService.getAllClubs();
        model.addAttribute("clubs", clubs);

        return "admin/evenements/gestion";
    }

    @GetMapping("/calendrier")
    public String showCalendar(Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        List<com.universite.UniClubs.entities.Evenement> evenements = evenementService.getAllEvents();
        List<com.universite.UniClubs.entities.Club> clubs = clubService.getAllClubs();
        int evenementsCeMois = evenementService.countEventsThisMonth();
        int evenementsAVenir = evenementService.findUpcomingEvents().size();
        double tauxParticipation = evenementService.calculateAverageParticipationRate();

        model.addAttribute("evenements", evenements);
        model.addAttribute("clubs", clubs);
        model.addAttribute("evenementsCeMois", evenementsCeMois);
        model.addAttribute("evenementsAVenir", evenementsAVenir);
        model.addAttribute("tauxParticipation", String.format("%.1f", tauxParticipation));

        return "admin/calendrier";
    }

    @GetMapping("/communication")
    public String showCommunication(Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        // Données simulées pour la communication
        model.addAttribute("notificationsEnvoyees", 0);
        model.addAttribute("tauxLecture", 0);
        model.addAttribute("etudiantsActifs", utilisateurService.countStudents());
        model.addAttribute("notificationsEnAttente", 0);
        model.addAttribute("notifications", List.of()); // Liste vide pour l'instant

        return "admin/communication";
    }

    @GetMapping("/rapports")
    public String showReports(Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        // Données simulées pour les rapports
        int totalEtudiants = utilisateurService.countStudents();
        int clubsActifs = clubService.getAllClubs().size();
        int totalEvenements = evenementService.getAllEvents().size();
        double tauxParticipationGlobal = evenementService.calculateAverageParticipationRate();

        model.addAttribute("totalEtudiants", totalEtudiants);
        model.addAttribute("clubsActifs", clubsActifs);
        model.addAttribute("totalEvenements", totalEvenements);
        model.addAttribute("tauxParticipationGlobal", String.format("%.1f", tauxParticipationGlobal));
        model.addAttribute("periode", "Ce mois");
        model.addAttribute("participationMoyenne", String.format("%.1f", tauxParticipationGlobal));
        model.addAttribute("clubPlusActif", "Club le plus actif");
        model.addAttribute("recommandations", List.of("Augmenter la communication", "Créer plus d'événements"));
        model.addAttribute("tendances", List.of("Participation en hausse", "Nouveaux clubs actifs"));

        // Données pour les graphiques (simulées)
        model.addAttribute("participationData", List.of());
        model.addAttribute("evenementsMoisData", List.of());
        model.addAttribute("typeEvenementsData", List.of());
        model.addAttribute("clubsActifsData", List.of());
        model.addAttribute("participationClubData", List.of());
        model.addAttribute("topEvenements", List.of());
        model.addAttribute("statistiquesClubs", List.of());

        return "admin/rapports";
    }

    // Méthodes pour la gestion des clubs
    @PostMapping("/clubs/create")
    public String createClub(@RequestParam String nom,
                           @RequestParam String description,
                           @RequestParam(required = false) String categorie,
                           @RequestParam(required = false) MultipartFile logo,
                           @RequestParam(required = false) UUID chefDeClubId,
                           Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        // Créer le club
        try {
            // Ici, vous devriez appeler le service pour créer le club
            // clubService.createClub(nom, description, categorie, logo, chefDeClubId);
            System.out.println("Création du club: " + nom);
            return "redirect:/admin/clubs?success";
        } catch (Exception e) {
            return "redirect:/admin/clubs?error";
        }
    }

    @PostMapping("/clubs/assign-chef")
    public String assignChefToClub(@RequestParam UUID clubId,
                                 @RequestParam UUID etudiantId,
                                 Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        try {
            // Ici, vous devriez appeler le service pour assigner le chef
            // clubService.assignChefToClub(clubId, etudiantId);
            System.out.println("Assignation du chef " + etudiantId + " au club " + clubId);
            return "redirect:/admin/clubs?success";
        } catch (Exception e) {
            return "redirect:/admin/clubs?error";
        }
    }

    @GetMapping("/clubs/{id}/delete")
    public String deleteClub(@PathVariable UUID id, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        try {
            // Ici, vous devriez appeler le service pour supprimer le club
            // clubService.deleteClub(id);
            System.out.println("Suppression du club: " + id);
            return "redirect:/admin/clubs?success";
        } catch (Exception e) {
            return "redirect:/admin/clubs?error";
        }
    }

    @GetMapping("/clubs/{id}")
    public String showClubDetails(@PathVariable UUID id, Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        // Récupérer les détails du club
        // Club club = clubService.findClubWithDetailsById(id);
        // model.addAttribute("club", club);

        return "admin/club-details";
    }

    @GetMapping("/clubs/{id}/edit")
    public String showEditClubForm(@PathVariable UUID id, Model model, Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        // Récupérer le club et les étudiants
        // Club club = clubService.findClubWithDetailsById(id);
        List<Utilisateur> etudiants = utilisateurService.getAllStudents();

        // model.addAttribute("club", club);
        model.addAttribute("etudiants", etudiants);

        return "admin/edit-club";
    }

    @PostMapping("/clubs/{id}/update")
    public String updateClub(@PathVariable UUID id,
                           @RequestParam String nom,
                           @RequestParam String description,
                           @RequestParam(required = false) String categorie,
                           @RequestParam(required = false) String statut,
                           @RequestParam(required = false) MultipartFile logo,
                           @RequestParam(required = false) UUID chefDeClubId,
                           Authentication authentication) {
        // Vérifier que l'utilisateur est un admin
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        if (!utilisateur.getRole().name().equals("ADMIN") && !utilisateur.getRole().name().equals("SUPER_ADMIN")) {
            return "redirect:/accueil";
        }

        try {
            // Ici, vous devriez appeler le service pour mettre à jour le club
            // clubService.updateClub(id, nom, description, categorie, statut, logo, chefDeClubId);
            System.out.println("Mise à jour du club: " + id + " - " + nom);
            return "redirect:/admin/clubs?success";
        } catch (Exception e) {
            return "redirect:/admin/clubs?error";
        }
    }
}
