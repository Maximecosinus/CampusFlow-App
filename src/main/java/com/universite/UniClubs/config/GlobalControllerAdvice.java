package com.universite.UniClubs.config;


import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @ModelAttribute("utilisateur")
    public Utilisateur getUtilisateurConnecte(Principal principal) {
        if (principal != null) {
            //récupérer l'email de l'utilisateur connecté
            String email = principal.getName();
            //récupéerer l,objet Utilisateur complet depuis la base de données
            Utilisateur utilsateurConnecte = utilisateurRepository.findByEmail(email).orElse(null);
            return utilsateurConnecte;
        }
        return null;
    }
}
