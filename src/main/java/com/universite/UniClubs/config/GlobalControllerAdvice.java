package com.universite.UniClubs.config;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.CustomUserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.security.core.Authentication;

// Dans GlobalControllerAdvice.java
@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("utilisateurConnecte")
    public Utilisateur getUtilisateurConnecte(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
        }
        return null;
    }
}