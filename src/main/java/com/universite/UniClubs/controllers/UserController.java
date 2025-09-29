package com.universite.UniClubs.controllers;


import com.universite.UniClubs.dto.UserProfileDto;
import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

@Controller
@RequestMapping("/profil")
public class UserController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

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
        Utilisateur utilisateur = utilisateurRepository.findByEmail(principal.getName()).orElseThrow(()-> new UsernameNotFoundException(principal.getName()));
        model.addAttribute("utilisateur", utilisateur);

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

    @PostMapping("/modifier-photo")
    public String processPhotoUpdate(@RequestParam("photo") MultipartFile photo, Principal principal) {
        if (principal == null || photo.isEmpty()) {
            return "redirect:/profil";
        }

        utilisateurService.updateUserPhoto(principal.getName(), photo);

        return "redirect:/profil?success";
    }

}
