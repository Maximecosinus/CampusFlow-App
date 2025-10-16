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

    // Méthode pour charger l'utilisateur avec ses inscriptions (pour éviter le lazy loading)
    Optional<Utilisateur> findByEmailWithInscriptions(String email);
    
    // Méthode pour trouver un utilisateur par son ID
    Optional<Utilisateur> findById(UUID id);
    
    // Méthode pour trouver un utilisateur par son ID avec ses inscriptions
    Optional<Utilisateur> findByIdWithInscriptions(UUID id);
    
    // Méthode pour trouver un utilisateur par email (pour l'admin)
    Utilisateur findByEmail(String email);
    
    // Méthode pour compter tous les utilisateurs (pour l'admin)
    long countAllUsers();
    
    // Nouvelles méthodes pour l'administration
    List<Utilisateur> searchStudentsByNameOrEmail(String query);
    void updateUser(Utilisateur utilisateur);
    
    // Méthodes pour Super Admin
    List<Utilisateur> findAllUsers();
    List<Utilisateur> findUsersByRole(com.universite.UniClubs.entities.Role role);
    long countUsersByRole(com.universite.UniClubs.entities.Role role);
    
    // Méthodes pour la gestion des utilisateurs
    Utilisateur saveUser(Utilisateur utilisateur);
    void deleteUser(UUID id);
}