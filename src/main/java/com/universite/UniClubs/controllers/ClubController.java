package com.universite.UniClubs.controllers;


import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/clubs")
public class ClubController {


    @Autowired
    private ClubService clubService;
    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/{id}")
    public String showCLubDetailsPage(@PathVariable UUID id, Model model, @ModelAttribute("utilisateurConnecte") Utilisateur utilisateurConnecte){
        boolean estDejaInscrit = false;
        if(utilisateurConnecte != null){
            estDejaInscrit = utilisateurService.estInscrit(utilisateurConnecte.getEmail(), id);
        }
        Club club = clubService.findClubWithDetailsById(id);
        //Ajoute le club trouvé au modèle pour l'envoyer à la vue
        model.addAttribute("club", club);
        model.addAttribute("estDejaInscrit", estDejaInscrit);
        return  "club-details";
    }

    @PostMapping("/{id}/inscrire")
    public String processInscriotion(@PathVariable UUID id,Principal principal ){
        //vérifier si l'utilisateur est connecté
        if(principal ==null){
            return "redirect:/login";
        }
        utilisateurService.inscrireUtilisateurAuclub(principal.getName(), id);
        return "redirect:/clubs/" + id;
    }

    @PostMapping("/{id}/desinscrire")
    public String processDesinscription(@PathVariable UUID id,Principal principal ){
        if(principal ==null){
            return "redirect:/login";
        }

        utilisateurService.desinscrireUtilisateurDuClub(principal.getName(), id);
        return "redirect:/clubs/" + id;
    }
}
