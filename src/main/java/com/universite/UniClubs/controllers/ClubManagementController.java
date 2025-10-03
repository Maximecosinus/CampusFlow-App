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

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/gestion-club")
@PreAuthorize("hasRole('CHEF_DE_CLUB')")
public class ClubManagementController {

    @Autowired
    private ClubRepository clubRepository;

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


}