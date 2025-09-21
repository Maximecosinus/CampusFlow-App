package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;


@Controller
public class HomeController {


    @Autowired
    private ClubService clubService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/accueil")
    public String showHomePage(Model model, Principal principal) {

        //récupérer l'email de l'utilisateur connecté
        String email = principal.getName();

        //récupéerer l,objet Utilisateur complet depuis la base de données
        Utilisateur utilsateurConnecte = utilisateurRepository.findByEmail(email).orElse(null);

        //Ajoute l'utilisateur et son rôle
        model.addAttribute("utilisateur", utilsateurConnecte);
        model.addAttribute("clubs", clubService.getAllClubs());
        return "accueil";
    }
}
