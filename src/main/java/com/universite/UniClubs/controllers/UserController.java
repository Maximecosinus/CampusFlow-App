package com.universite.UniClubs.controllers;


import com.universite.UniClubs.dto.UserProfileDto;
import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Inscription;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
        
        // Utiliser la nouvelle méthode qui charge les inscriptions avec l'utilisateur
        Utilisateur utilisateurConnecte = utilisateurService.findByEmailWithInscriptions(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        Set<Inscription> mesInscriptions = utilisateurConnecte.getInscriptions();

        // On transforme ce Set<Inscription> en un Set<Club>
        Set<Club> mesClubs = mesInscriptions.stream()
                .map(Inscription::getClub) // Pour chaque objet Inscription, on extrait l'objet Club
                .collect(Collectors.toSet()); // On rassemble les clubs dans un nouveau Set

        model.addAttribute("mesClubs", mesClubs);

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

    // Méthode pour afficher le profil d'un autre utilisateur (vue en lecture seule)
    @GetMapping("/membre/{id}")
    public String showMemberProfile(@PathVariable UUID id, Model model) {
        System.out.println("=== DEBUG: Recherche utilisateur avec ID: " + id + " ===");
        
        // D'abord essayer de récupérer l'utilisateur avec ses inscriptions
        Optional<Utilisateur> utilisateurOpt = utilisateurService.findByIdWithInscriptions(id);
        
        if (utilisateurOpt.isEmpty()) {
            System.out.println("=== DEBUG: Utilisateur non trouvé ===");
            model.addAttribute("error", "Utilisateur non trouvé.");
            return "error-page";
        }
        
        Utilisateur utilisateur = utilisateurOpt.get();
        System.out.println("=== DEBUG: Utilisateur trouvé: " + utilisateur.getPrenom() + " " + utilisateur.getNom() + " ===");
        System.out.println("=== DEBUG: Nombre d'inscriptions: " + (utilisateur.getInscriptions() != null ? utilisateur.getInscriptions().size() : "null") + " ===");
        
        model.addAttribute("membre", utilisateur);
        
        return "membre-profil";
    }

}
