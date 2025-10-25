package com.universite.UniClubs.config;

import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.repositories.ClubRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.List;

@Component
public class SuperAdminInitializer implements CommandLineRunner {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Vérifier si un utilisateur avec l'email 'superadmin@campusflow.com' existe déjà
        boolean superAdminExists = utilisateurRepository.findByEmail("superadmin@campusflow.com").isPresent();
        
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
        
        // Créer un utilisateur de test étudiant (mettre à jour l'existant s'il y en a un)
        Optional<Utilisateur> existingTestUser = utilisateurRepository.findByEmail("test1@gmail.com");
        if (existingTestUser.isPresent()) {
            Utilisateur testUser = existingTestUser.get();
            testUser.setPrenom("Test");
            testUser.setNom("User");
            testUser.setMotDePasse(passwordEncoder.encode("motdepasse123"));
            testUser.setRole(Role.ETUDIANT);
            testUser.setBio("Utilisateur de test pour CampusFlow");
            utilisateurRepository.save(testUser);
            System.out.println("🔄 Utilisateur de test mis à jour avec le rôle ETUDIANT");
        } else {
            // Créer le nouvel utilisateur de test étudiant
            System.out.println("=== CRÉATION DE L'UTILISATEUR DE TEST ÉTUDIANT ===");
            
            Utilisateur testUser = new Utilisateur();
            testUser.setPrenom("Test");
            testUser.setNom("User");
            testUser.setEmail("test1@gmail.com");
            testUser.setMotDePasse(passwordEncoder.encode("motdepasse123"));
            testUser.setRole(Role.ETUDIANT);
            testUser.setBio("Utilisateur de test pour CampusFlow");
            
            utilisateurRepository.save(testUser);
            
            System.out.println("✅ Utilisateur de test créé avec succès !");
            System.out.println("📧 Email: test1@gmail.com");
            System.out.println("🔑 Mot de passe: motdepasse123");
            System.out.println("👤 Rôle: ETUDIANT");
        }
        
        // Créer un utilisateur de test chef de club
        Optional<Utilisateur> existingChefUser = utilisateurRepository.findByEmail("chef@test.com");
        if (existingChefUser.isPresent()) {
            Utilisateur chefUser = existingChefUser.get();
            chefUser.setPrenom("Chef");
            chefUser.setNom("Test");
            chefUser.setMotDePasse(passwordEncoder.encode("chef123"));
            chefUser.setRole(Role.CHEF_DE_CLUB);
            chefUser.setBio("Chef de club de test pour CampusFlow");
            utilisateurRepository.save(chefUser);
            System.out.println("🔄 Utilisateur chef de test mis à jour avec le rôle CHEF_DE_CLUB");
        } else {
            // Créer le nouvel utilisateur chef de test
            System.out.println("=== CRÉATION DE L'UTILISATEUR CHEF DE TEST ===");
            
            Utilisateur chefUser = new Utilisateur();
            chefUser.setPrenom("Chef");
            chefUser.setNom("Test");
            chefUser.setEmail("chef@test.com");
            chefUser.setMotDePasse(passwordEncoder.encode("chef123"));
            chefUser.setRole(Role.CHEF_DE_CLUB);
            chefUser.setBio("Chef de club de test pour CampusFlow");
            
            utilisateurRepository.save(chefUser);
            
            System.out.println("✅ Utilisateur chef de test créé avec succès !");
            System.out.println("📧 Email: chef@test.com");
            System.out.println("🔑 Mot de passe: chef123");
            System.out.println("👤 Rôle: CHEF_DE_CLUB");
        }
        
        // Assigner l'utilisateur chef de test à un club existant
        Optional<Utilisateur> chefUser = utilisateurRepository.findByEmail("chef@test.com");
        if (chefUser.isPresent()) {
            List<Club> clubs = clubRepository.findAll();
            if (!clubs.isEmpty()) {
                // Assigner le chef au premier club trouvé
                Club clubToAssign = clubs.get(0);
                clubToAssign.setChefClub(chefUser.get());
                clubRepository.save(clubToAssign);
                System.out.println("✅ Utilisateur chef assigné au club: " + clubToAssign.getNom());
            } else {
                System.out.println("⚠️  Aucun club trouvé pour assigner le chef");
            }
        }
    }
}
