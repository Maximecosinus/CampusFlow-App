package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.security.core.Authentication;
import java.security.Principal;

import java.security.Principal;


@Controller
public class HomeController {


    @Autowired
    private ClubService clubService;

    @Autowired
    private EvenementService evenementService;

    @GetMapping("/accueil")
    public String showHomePage(Model model) {

        model.addAttribute("clubs", clubService.getAllClubs());
        model.addAttribute("clubsRecents", clubService.findRecentclubs());
        model.addAttribute("evenementsAVenir",evenementService.findUpcomingEvents());
        return "accueil";
    }
}
