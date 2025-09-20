package com.universite.UniClubs.services;

import com.universite.UniClubs.dto.UserRegistrationDto;
import com.universite.UniClubs.entities.Utilisateur;

public interface UtilisateurService {

    Utilisateur creerEtudiant(UserRegistrationDto registrationDto);
}
