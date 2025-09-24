package com.universite.UniClubs.controllers;


import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/clubs")
public class ClubController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private ClubService clubService;

    @GetMapping("/{id}")
    public String showCLubDetailsPage(@PathVariable UUID id, Model model, Principal principal ){

        String email = principal.getName();
        Utilisateur utilsateurConnecte = utilisateurRepository.findByEmail(email).orElse(null);
        model.addAttribute("utilisateur", utilsateurConnecte);
        //On trouve le club par son ID
        Club club = clubService.findClubById(id).orElseThrow(() -> new IllegalArgumentException("Invalid club ID: " + id));

        //Ajoute le club trouvé au modèle pour l'envoyer à la vue
        model.addAttribute("club", club);

        return  "club-details";
    }
}
