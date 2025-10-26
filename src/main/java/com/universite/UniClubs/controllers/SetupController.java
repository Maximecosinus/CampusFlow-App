package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

/**
 * Contrôleur temporaire pour créer un admin de test
 */
@Controller
public class SetupController {

    @Autowired
    private UtilisateurService utilisateurService;

    /**
     * Créer un utilisateur admin pour les tests
     */
    @GetMapping("/setup-admin")
    public String createAdmin() {
        try {
            // Vérifier si l'admin existe déjà
            Optional<Utilisateur> adminExists = utilisateurService.findByEmail("admin@campusflow.com");
            if (adminExists.isPresent()) {
                return "Admin existe déjà: " + adminExists.get().getEmail();
            }
            
            // Créer un utilisateur admin
            Utilisateur admin = new Utilisateur();
            admin.setEmail("admin@campusflow.com");
            admin.setNom("Admin");
            admin.setPrenom("Admin");
            admin.setMotDePasse("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // "admin"
            admin.setRole(com.universite.UniClubs.entities.Role.ADMIN);
            
            utilisateurService.updateUser(admin);
            return "Admin créé avec succès: " + admin.getEmail() + " (mot de passe: admin)";
            
        } catch (Exception e) {
            return "Erreur lors de la création de l'admin: " + e.getMessage();
        }
    }
}
