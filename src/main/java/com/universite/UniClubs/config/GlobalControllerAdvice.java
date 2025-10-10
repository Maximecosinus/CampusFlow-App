package com.universite.UniClubs.config;


import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.CustomUserDetails;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

// Dans GlobalControllerAdvice.java
import org.springframework.security.core.Authentication; // <-- Nouvel import

// Dans GlobalControllerAdvice.java
@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("utilisateurConnecte")
    public Utilisateur getUtilisateurConnecte(Authentication authentication) {
        // AJOUTEZ CETTE LIGNE
        System.out.println("--- GLOBAL CONTROLLER ADVICE EXÉCUTÉ ---");

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            // AJOUTEZ CETTE LIGNE
            System.out.println("--- Instance de CustomUserDetails trouvée ! ---");
            return ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        }

        // AJOUTEZ CETTE LIGNE
        System.out.println("--- ATTENTION: Pas de CustomUserDetails trouvé ou pas d'authentification. ---");
        return null;
    }
}