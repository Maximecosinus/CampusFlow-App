package com.universite.UniClubs.config;

import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SuperAdminInitializer implements CommandLineRunner {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si un Super Admin existe déjà
        boolean superAdminExists = utilisateurRepository.findByRole(Role.SUPER_ADMIN).size() > 0;
        
        if (!superAdminExists) {
            System.out.println("=== CRÉATION DU SUPER ADMIN PRÉDÉFINI ===");
            
            // Créer le Super Admin prédéfini
            Utilisateur superAdmin = new Utilisateur();
            superAdmin.setPrenom("Super");
            superAdmin.setNom("Administrateur");
            superAdmin.setEmail("superadmin@campusflow.com");
            superAdmin.setMotDePasse(passwordEncoder.encode("superadmin123"));
            superAdmin.setRole(Role.SUPER_ADMIN);
            superAdmin.setBio("Super Administrateur système de CampusFlow");
            
            utilisateurRepository.save(superAdmin);
            
            System.out.println("✅ Super Admin créé avec succès !");
            System.out.println("📧 Email: superadmin@campusflow.com");
            System.out.println("🔑 Mot de passe: superadmin123");
            System.out.println("⚠️  IMPORTANT: Changez le mot de passe après la première connexion !");
        } else {
            System.out.println("ℹ️  Super Admin existe déjà dans le système.");
        }
    }
}
