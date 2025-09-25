package com.universite.UniClubs.services;

import com.universite.UniClubs.dto.UserRegistrationDto;
import com.universite.UniClubs.entities.Utilisateur;

import java.util.Optional;
import java.util.UUID;

public interface UtilisateurService {

    Utilisateur creerEtudiant(UserRegistrationDto registrationDto);
    void inscrireUtilisateurAuclub(String emailUtilisateur, UUID idClub);
    boolean estInscrit(String emailUtilisateur, UUID idClub);
    void desinscrireUtilisateurDuClub(String emailUtilisateur, UUID idClub);
    Optional<Utilisateur> findByEmailWithClubsInscrits(String email);
}
