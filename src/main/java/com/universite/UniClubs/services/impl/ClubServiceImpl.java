package com.universite.UniClubs.services.impl;

import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.TypeNotification;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ClubServiceImpl implements ClubService {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Club> getClubById(UUID id) {
        return clubRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Club> findRecentclubs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        return clubRepository.findByDateCreationAfterOrderByDateCreationDesc(cutoffDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Club findClubWithDetailsById(UUID clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
    }

    @Override
    public Club createClub(String nom, String description, String categorie, MultipartFile logo, UUID chefDeClubId) {
        Club club = new Club();
        club.setNom(nom);
        club.setDescription(description);
        
        // Assigner le chef de club si spécifié
        if (chefDeClubId != null) {
            Utilisateur chef = utilisateurRepository.findById(chefDeClubId)
                    .orElseThrow(() -> new RuntimeException("Chef de club non trouvé"));
            club.setChefClub(chef);
        }
        
        club = clubRepository.save(club);
        
        // Notifier tous les étudiants du nouveau club
        String titre = "Nouveau club créé !";
        String message = String.format("Un nouveau club '%s' a été créé : %s", nom, description);
        notificationService.envoyerNotificationBroadcast(titre, message, TypeNotification.CLUB);
        
        // Notifier le chef de club s'il a été assigné
        if (chefDeClubId != null) {
            String titreChef = "Vous êtes maintenant chef de club !";
            String messageChef = String.format("Vous avez été nommé chef du club '%s'. Félicitations !", nom);
            notificationService.envoyerNotification(chefDeClubId, titreChef, messageChef, TypeNotification.CLUB);
        }
        
        return club;
    }

    @Override
    public Club updateClub(UUID clubId, String nom, String description, String categorie, MultipartFile logo) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        
        String ancienNom = club.getNom();
        club.setNom(nom);
        club.setDescription(description);
        
        club = clubRepository.save(club);
        
        // Notifier les membres du club des modifications
        String titre = "Club modifié";
        String message = String.format("Le club '%s' a été modifié. Vérifiez les nouvelles informations.", nom);
        notificationService.envoyerNotificationBroadcast(titre, message, TypeNotification.CLUB);
        
        return club;
    }

    @Override
    public void assignChef(UUID clubId, UUID chefId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        
        Utilisateur nouveauChef = utilisateurRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Utilisateur ancienChef = club.getChefClub();
        club.setChefClub(nouveauChef);
        clubRepository.save(club);
        
        // Notifier le nouveau chef
        String titre = "Vous êtes maintenant chef de club !";
        String message = String.format("Vous avez été nommé chef du club '%s'. Félicitations !", club.getNom());
        notificationService.envoyerNotification(chefId, titre, message, TypeNotification.CLUB);
        
        // Notifier l'ancien chef s'il existait
        if (ancienChef != null && !ancienChef.getId().equals(chefId)) {
            String titreAncien = "Changement de chef de club";
            String messageAncien = String.format("Vous n'êtes plus chef du club '%s'. Merci pour votre service !", club.getNom());
            notificationService.envoyerNotification(ancienChef.getId(), titreAncien, messageAncien, TypeNotification.CLUB);
        }
    }

    @Override
    public void deleteClub(UUID clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club non trouvé"));
        
        String nomClub = club.getNom();
        clubRepository.delete(club);
        
        // Notifier tous les utilisateurs de la suppression
        String titre = "Club supprimé";
        String message = String.format("Le club '%s' a été supprimé.", nomClub);
        notificationService.envoyerNotificationBroadcast(titre, message, TypeNotification.CLUB);
    }
}