package com.universite.UniClubs.services.impl;


import com.universite.UniClubs.dto.UserRegistrationDto;
import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 1. Importer l'annotation


import java.util.Optional;
import java.util.UUID;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ClubRepository clubRepository;

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

    @Override
    @Transactional // L'annotation reste cruciale
    public void inscrireUtilisateurAuclub(String userEmail, UUID clubId) {
        // Étape 1: Charger les entités AVEC LEURS COLLECTIONS en une seule fois.
        Utilisateur utilisateur = utilisateurRepository.findByEmailWithClubsInscrits(userEmail)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour l'email: " + userEmail));

        Club club = clubRepository.findByIdWithDetails(clubId) // On réutilise la méthode que vous avez déjà créée !
                .orElseThrow(() -> new RuntimeException("Club non trouvé pour l'ID: " + clubId));

        // Étape 2: Maintenant que tout est chargé, la modification est une simple
        // opération en mémoire, sans risque de chargement paresseux conflictuel.
        utilisateur.getClubsInscrits().add(club);
        club.getMembres().add(utilisateur);

        // La transaction se termine ici et Hibernate sauvegarde les changements.
    }

    @Override
    public boolean estInscrit(String emailUtilisateur, UUID idClub) {
        return utilisateurRepository.isUserSubscribedToClub(emailUtilisateur, idClub);

    }

}
