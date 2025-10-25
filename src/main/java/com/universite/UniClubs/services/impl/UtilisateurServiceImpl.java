package com.universite.UniClubs.services.impl;

import com.universite.UniClubs.dto.UserProfileDto;
import com.universite.UniClubs.dto.UserRegistrationDto;
import com.universite.UniClubs.entities.*;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.repositories.InscriptionRepository;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.FileStorageService;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public Utilisateur creerEtudiant(UserRegistrationDto registrationDto) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setNom(registrationDto.getNom());
        utilisateur.setPrenom(registrationDto.getPrenom());
        utilisateur.setEmail(registrationDto.getEmail());
        utilisateur.setMotDePasse(passwordEncoder.encode(registrationDto.getMotDePasse()));
        utilisateur.setRole(Role.ETUDIANT);
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    @Transactional
    public void inscrireUtilisateurAuclub(String emailUtilisateur, UUID idClub) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(emailUtilisateur)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        Club club = clubRepository.findById(idClub)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));

        boolean dejaInscritOuEnAttente = inscriptionRepository.existsByUtilisateurAndClub(utilisateur, club);
        if (dejaInscritOuEnAttente) {
            // Idéalement, lancer une exception personnalisée ici
            System.out.println("L'utilisateur a déjà une demande en cours ou est déjà membre.");
            return;
        }

        Inscription nouvelleInscription = new Inscription();
        nouvelleInscription.setUtilisateur(utilisateur);
        nouvelleInscription.setClub(club);
        nouvelleInscription.setStatut(StatutInscription.EN_ATTENTE);

        inscriptionRepository.save(nouvelleInscription);
    }

    @Override
    @Transactional
    public void desinscrireUtilisateurDuClub(String emailUtilisateur, UUID idClub) {
        // --- CORRECTION : Logique entièrement réécrite ---
        // On trouve l'inscription spécifique qui lie cet utilisateur à ce club.
        Inscription inscription = inscriptionRepository.findByUtilisateurEmailAndClubId(emailUtilisateur, idClub)
                .orElseThrow(() -> new RuntimeException("Inscription non trouvée pour cet utilisateur et ce club"));

        // Et on la supprime de la base de données. C'est simple et propre.
        inscriptionRepository.delete(inscription);
    }

    @Override
    public boolean estInscrit(String emailUtilisateur, UUID idClub) {
        // --- CORRECTION : Logique réécrite pour utiliser InscriptionRepository ---
        // On vérifie s'il existe une inscription ACCEPTÉE pour cet utilisateur et ce club.
        return inscriptionRepository.existsByUtilisateurEmailAndClubIdAndStatut(emailUtilisateur, idClub, StatutInscription.ACCEPTE);
    }

    // --- CORRECTION : La méthode suivante a été supprimée ---
    /*
    @Override
    public Optional<Utilisateur> findByEmailWithClubsInscrits(String email) {
        return utilisateurRepository.findByEmailWithClubsInscrits(email);
    }
    */

    @Override
    public Optional<UserProfileDto> getUserProfileByEmail(String email) {
        return utilisateurRepository.findByEmail(email)
                .map(utilisateur -> {
                    UserProfileDto dto = new UserProfileDto();
                    dto.setNom(utilisateur.getNom());
                    dto.setPrenom(utilisateur.getPrenom());
                    dto.setBio(utilisateur.getBio());
                    return dto;
                });
    }

    @Override
    @Transactional
    public void updateUserProfile(String email, UserProfileDto profileDto) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        utilisateur.setNom(profileDto.getNom());
        utilisateur.setPrenom(profileDto.getPrenom());
        utilisateur.setBio(profileDto.getBio());
    }

    @Override
    @Transactional
    public void updateUserPhoto(String email, MultipartFile photo) {
        String photoPath = fileStorageService.storeFile(photo, "avatars");
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        utilisateur.setPhotoProfil(photoPath);
    }

    @Override
    public List<Utilisateur> getAllStudents() {
        // Implémentation temporaire - retourne tous les utilisateurs pour l'instant
        return utilisateurRepository.findAll();
    }

    @Override
    public int countStudents() {
        // Implémentation temporaire - retourne 0 pour l'instant
        return 0;
    }
}