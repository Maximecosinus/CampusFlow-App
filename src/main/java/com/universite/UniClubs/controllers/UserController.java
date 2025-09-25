package com.universite.UniClubs.controllers;


import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

@Controller
@RequestMapping("/mes-clubs")
public class UserController {

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping
    public String ShowMyClubsPage(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        //On récupère l'utilisateur connecté et ses clubs
        Utilisateur utilisateur = utilisateurService.findByEmailWithClubsInscrits(principal.getName()).orElse(null);

        Set<Club> mesClubs = Collections.emptySet();
        if (utilisateur != null) {
            mesClubs= utilisateur.getClubsInscrits();
        }

        //Ajouter la liste des clubs de l'utilissteur au modèle
        model.addAttribute("mesClubs", mesClubs);

        return "mes-clubs";
    }

}
