package com.universite.UniClubs.controllers;


import com.universite.UniClubs.dto.UserProfileDto;
import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

@Controller
@RequestMapping("/profil")
public class UserController {

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/mes-clubs")
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

    //méthode pour afficher le formulaire du profil
    @GetMapping
    public String showProfilePage(Model model, Principal principal){
        if (principal == null) {
            return "redirect:/login";
        }

        //On récupère le DTO du profil et on l'ajoute au modèle
        UserProfileDto userProfile = utilisateurService.getUserProfileByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Profil utiliateur introuvable"));

        model.addAttribute("profile", userProfile);

        return "profil";
    }

    //Méthode pour traiter la mise à jour
    @PostMapping("/modifier")
    public String processProfileUpdate(@ModelAttribute("profile") UserProfileDto ProfileDto, Principal principal) {
        if(principal == null) {
            return "redirect:/login";
        }
        //On appelle le service pour mettre à jour les infromations
        utilisateurService.updateUserProfile((principal.getName()), ProfileDto);
        return "redirect:/profil?success";
    }

}
