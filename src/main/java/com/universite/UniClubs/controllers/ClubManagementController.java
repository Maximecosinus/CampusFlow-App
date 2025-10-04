package com.universite.UniClubs.controllers;


import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.repositories.UtilisateurRepository;
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

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/gestion-club")
@PreAuthorize("hasRole('CHEF_DE_CLUB')")
public class ClubManagementController {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private InscriptionService inscriptionService; // Injecter le nouveau service

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


}