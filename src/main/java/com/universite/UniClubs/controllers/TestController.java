package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.UtilisateurService;
import com.universite.UniClubs.repositories.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;

@Controller
public class TestController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private ClubRepository clubRepository;

    /**
     * Endpoint temporaire pour tester et créer des utilisateurs (SANS PROTECTION)
     */
    @GetMapping("/test-users")
    @ResponseBody
    public String testUsers() {
        StringBuilder result = new StringBuilder();
        
        // Vérifier si tototiti@gmail.com existe
        try {
            Optional<Utilisateur> userOpt = utilisateurService.findByEmail("tototiti@gmail.com");
            if (userOpt.isPresent()) {
                Utilisateur user = userOpt.get();
                result.append("✅ tototiti@gmail.com existe:\n");
                result.append("- Nom: ").append(user.getNom()).append("\n");
                result.append("- Prénom: ").append(user.getPrenom()).append("\n");
                result.append("- Rôle: ").append(user.getRole()).append("\n");
                result.append("- ID: ").append(user.getId()).append("\n");
            } else {
                result.append("❌ tototiti@gmail.com n'existe pas\n");
            }
        } catch (Exception e) {
            result.append("❌ tototiti@gmail.com n'existe pas ou erreur: ").append(e.getMessage()).append("\n");
        }
        
        // Vérifier s'il y a un admin
        try {
            List<Utilisateur> admins = utilisateurService.searchStudentsByNameOrEmail("admin");
            result.append("\n🔍 Recherche d'admins:\n");
            for (Utilisateur admin : admins) {
                if (admin.getRole().name().equals("ADMIN")) {
                    result.append("- Admin trouvé: ").append(admin.getEmail()).append(" (").append(admin.getRole()).append(")\n");
                }
            }
        } catch (Exception e) {
            result.append("❌ Erreur lors de la recherche d'admins: ").append(e.getMessage()).append("\n");
        }
        
        // Créer un utilisateur admin si nécessaire
        try {
            Optional<Utilisateur> adminExists = utilisateurService.findByEmail("admin@campusflow.com");
            if (adminExists.isPresent()) {
                result.append("\n✅ Admin existe déjà: ").append(adminExists.get().getEmail()).append("\n");
            } else {
                result.append("\n❌ Admin n'existe pas\n");
            }
        } catch (Exception e) {
            try {
                result.append("\n🔧 Création d'un utilisateur admin...\n");
                
                Utilisateur admin = new Utilisateur();
                admin.setEmail("admin@campusflow.com");
                admin.setNom("Admin");
                admin.setPrenom("Admin");
                admin.setMotDePasse("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi"); // "admin"
                admin.setRole(com.universite.UniClubs.entities.Role.ADMIN);
                
                utilisateurService.updateUser(admin);
                result.append("✅ Admin créé avec succès: ").append(admin.getEmail()).append("\n");
            } catch (Exception e2) {
                result.append("\n❌ Erreur lors de la création de l'admin: ").append(e2.getMessage()).append("\n");
            }
        }
        
        return result.toString();
    }

    /**
     * Endpoint temporaire pour tester la validation d'email (SANS PROTECTION)
     */
    @GetMapping("/test-validate-email")
    @ResponseBody
    public String testValidateEmail(@RequestParam("email") String email) {
        StringBuilder result = new StringBuilder();
        
        try {
            Optional<Utilisateur> userOpt = utilisateurService.findByEmail(email);
            if (userOpt.isPresent()) {
                Utilisateur user = userOpt.get();
                result.append("✅ ").append(email).append(" existe:\n");
                result.append("- Nom: ").append(user.getNom()).append("\n");
                result.append("- Prénom: ").append(user.getPrenom()).append("\n");
                result.append("- Rôle: ").append(user.getRole()).append("\n");
                result.append("- ID: ").append(user.getId()).append("\n");
            } else {
                result.append("❌ ").append(email).append(" n'existe pas\n");
            }
            
            // Vérifier s'il dirige déjà un club (seulement si l'utilisateur existe)
            if (userOpt.isPresent()) {
                boolean isAlreadyChef = clubRepository.existsByChefClub(userOpt.get());
                if (isAlreadyChef) {
                    result.append("❌ Cet étudiant dirige déjà un club\n");
                } else {
                    result.append("✅ Cet étudiant peut être assigné comme chef de club\n");
                }
            }
            
        } catch (Exception e) {
            result.append("❌ ").append(email).append(" n'existe pas ou erreur: ").append(e.getMessage()).append("\n");
        }
        
        return result.toString();
    }
}
