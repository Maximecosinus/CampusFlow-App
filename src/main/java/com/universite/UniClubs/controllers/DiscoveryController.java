package com.universite.UniClubs.controllers;


import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.ClubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

import java.security.Principal;

@Controller
@RequestMapping("/decouvrir")
public class DiscoveryController {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ClubService clubService;

    @GetMapping
    public String showDiscoveryPortal(Model model, Principal principal ) {

        String email = principal.getName();
        Utilisateur utilisateurConnecte = utilisateurRepository.findByEmail(email).orElse(null);
        model.addAttribute("utilisateur", utilisateurConnecte);

        return "decouvrir";
    }

    @GetMapping("/clubs")
    public String showClubsListPage(Model model, Principal principal ) {
        String email = principal.getName();
        Utilisateur utilisateurConnecte = utilisateurRepository.findByEmail(email).orElse(null);
        model.addAttribute("utilisateur", utilisateurConnecte);
        model.addAttribute("allClubs",clubService.getAllClubs());

    return "Clubs-list";
    }
}
