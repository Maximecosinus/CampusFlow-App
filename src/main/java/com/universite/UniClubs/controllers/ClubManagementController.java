package com.universite.UniClubs.controllers;


import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/gestion-club")
@PreAuthorize("hasRole('CHEF_DE_CLUB')")
public class ClubManagementController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping
    public String showManagementDashboard(Model model, Principal principal) {
        if(principal == null){
            return "redirect:/login";
        }
        // Récupérer le chef de club
        Utilisateur chefDeClub = utilisateurRepository.findByEmailWithClubsDiriges(principal.getName())
                .orElseThrow(() -> new RuntimeException("Chef de club non trouvé"));
        //Trouver le club qu'il dirige(on suppose qu'un chef ne dirige qu'un seul club pour l'instant
        Optional<Club> clubDirige = chefDeClub.getClubsDiriges().stream().findFirst();

        if (clubDirige.isEmpty()){
            model.addAttribute("error", "Vous n'êtes assigné à la direction d'aucun club.");
            return "error-page";
        }
        model.addAttribute("club", clubDirige.get());
        return "gestion-club/dashboard";
    }
}
