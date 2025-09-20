package com.universite.UniClubs.services.impl;


import com.universite.UniClubs.dto.UserRegistrationDto;
import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Utilisateur creerEtudiant(UserRegistrationDto registrationDto){

        // On crée une instance de l'entité Utilisateur
        Utilisateur utilisateur = new Utilisateur();
        // On mappe les données du DTO vers l'entité
        utilisateur.setNom(registrationDto.getNom());
        utilisateur.setPrenom(registrationDto.getPrenom());
        utilisateur.setEmail(registrationDto.getEmail());
        // On crypte le mot de passe et on le mappe dans l'utilsateur
        utilisateur.setMotDePasse(passwordEncoder.encode(registrationDto.getMotDePasse()));
        // On s'assure que le rôle est bien ETUDIANT
        utilisateur.setRole(Role.ETUDIANT);
        // On sauvegarde l'utilsateur dans la base de donnée
        return utilisateurRepository.save(utilisateur);

    }
}
