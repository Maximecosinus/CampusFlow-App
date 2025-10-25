package com.universite.UniClubs.services;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ChefClubValidationService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ClubRepository clubRepository;

    /**
     * Valide qu'un utilisateur peut être assigné comme chef de club
     * @param email L'email de l'utilisateur à valider
     * @return ValidationResult avec les détails de la validation
     */
    public ValidationResult validateChefClubAssignment(String email) {
        ValidationResult result = new ValidationResult();

        // Vérifier que l'email existe
        Optional<Utilisateur> userOpt = utilisateurRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            result.setValid(false);
            result.setMessage("Aucun étudiant trouvé avec l'email : " + email);
            return result;
        }

        Utilisateur user = userOpt.get();

        // Vérifier que c'est bien un étudiant
        if (user.getRole() != Role.ETUDIANT) {
            result.setValid(false);
            result.setMessage("L'utilisateur " + email + " n'est pas un étudiant (rôle actuel : " + user.getRole() + ")");
            return result;
        }

        // Vérifier qu'il ne dirige pas déjà un club
        boolean isAlreadyChef = clubRepository.existsByChefClub(user);
        if (isAlreadyChef) {
            result.setValid(false);
            result.setMessage("L'étudiant " + user.getPrenom() + " " + user.getNom() + " dirige déjà un club");
            return result;
        }

        // Validation réussie
        result.setValid(true);
        result.setMessage("L'étudiant peut être assigné comme chef de club");
        result.setUser(user);
        return result;
    }

    /**
     * Valide qu'un utilisateur peut être assigné comme chef de club (version avec UUID)
     * @param userId L'ID de l'utilisateur à valider
     * @return ValidationResult avec les détails de la validation
     */
    public ValidationResult validateChefClubAssignmentById(java.util.UUID userId) {
        ValidationResult result = new ValidationResult();

        // Vérifier que l'utilisateur existe
        Optional<Utilisateur> userOpt = utilisateurRepository.findById(userId);
        if (userOpt.isEmpty()) {
            result.setValid(false);
            result.setMessage("Utilisateur non trouvé avec l'ID : " + userId);
            return result;
        }

        Utilisateur user = userOpt.get();

        // Vérifier que c'est bien un étudiant
        if (user.getRole() != Role.ETUDIANT) {
            result.setValid(false);
            result.setMessage("L'utilisateur " + user.getEmail() + " n'est pas un étudiant (rôle actuel : " + user.getRole() + ")");
            return result;
        }

        // Vérifier qu'il ne dirige pas déjà un club
        boolean isAlreadyChef = clubRepository.existsByChefClub(user);
        if (isAlreadyChef) {
            result.setValid(false);
            result.setMessage("L'étudiant " + user.getPrenom() + " " + user.getNom() + " dirige déjà un club");
            return result;
        }

        // Validation réussie
        result.setValid(true);
        result.setMessage("L'étudiant peut être assigné comme chef de club");
        result.setUser(user);
        return result;
    }

    /**
     * Classe interne pour représenter le résultat de validation
     */
    public static class ValidationResult {
        private boolean valid;
        private String message;
        private Utilisateur user;

        // Constructeurs
        public ValidationResult() {}

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        // Getters et setters
        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Utilisateur getUser() {
            return user;
        }

        public void setUser(Utilisateur user) {
            this.user = user;
        }
    }
}
