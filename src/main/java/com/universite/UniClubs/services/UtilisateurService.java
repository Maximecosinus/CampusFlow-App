package com.universite.UniClubs.services;

import com.universite.UniClubs.dto.UserRegistrationDto;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.dto.UserProfileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UtilisateurService {

    Utilisateur creerEtudiant(UserRegistrationDto registrationDto);

    void inscrireUtilisateurAuclub(String emailUtilisateur, UUID idClub);

    boolean estInscrit(String emailUtilisateur, UUID idClub);

    void desinscrireUtilisateurDuClub(String emailUtilisateur, UUID idClub);

    Optional<UserProfileDto> getUserProfileByEmail(String email);

    void updateUserProfile(String email, UserProfileDto profileDto);

    void updateUserPhoto(String email, MultipartFile photo);

    // Méthodes pour l'administration
    List<Utilisateur> getAllStudents();
    int countStudents();
    
    // Méthodes manquantes pour les contrôleurs
    Optional<Utilisateur> findByEmail(String email);
    Utilisateur saveUser(Utilisateur utilisateur);
    Utilisateur updateUser(Utilisateur utilisateur);
    Optional<Utilisateur> findById(UUID id);
    void deleteUser(UUID id);
    List<Utilisateur> findAllUsers();
    long countAllUsers();
    long countUsersByRole(com.universite.UniClubs.entities.Role role);
    List<Utilisateur> searchStudentsByNameOrEmail(String searchTerm);

    // --- CORRECTION : La méthode suivante a été supprimée ---
    // Elle était basée sur l'ancienne architecture.
    // Optional<Utilisateur> findByEmailWithClubsInscrits(String email);
}