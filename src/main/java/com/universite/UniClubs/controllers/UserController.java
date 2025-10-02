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

    @GetMapping("/mes-clubs")
    public String ShowMyClubsPage(Model model, @ModelAttribute("utilisateurConnecte") Utilisateur utilisateurConnecte) {
        if (utilisateurConnecte == null) {
            return "redirect:/login";
        }
        //Ajouter la liste des clubs de l'utilissteur au modèle
        model.addAttribute("mesClubs", utilisateurConnecte.getClubsInscrits());

        return "mes-clubs";
    }

    //méthode pour afficher le formulaire du profil
    @GetMapping
    public String showProfilePage(Model model,  @ModelAttribute("utilisateurConnecte") Utilisateur utilisateurConnecte){
        if (utilisateurConnecte == null) {
            return "redirect:/login";
        }
        // On peut créer le DTO directement à partir de l'objet déjà chargé
        UserProfileDto userProfile = new UserProfileDto();
        userProfile.setNom(utilisateurConnecte.getNom());
        userProfile.setPrenom(utilisateurConnecte.getPrenom());
        userProfile.setBio(utilisateurConnecte.getBio());

        model.addAttribute("profile", userProfile);
        // On passe aussi l'utilisateur complet pour la photo
        model.addAttribute("utilisateur", utilisateurConnecte);
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
