package com.universite.UniClubs.services.impl;


import com.universite.UniClubs.entities.Evenement;
import com.universite.UniClubs.entities.TypeNotification;
import com.universite.UniClubs.repositories.EvenementRepository;
import com.universite.UniClubs.services.EvenementService;
import com.universite.UniClubs.services.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

@Service
public class EvenementServiceImp implements EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Evenement> findUpcomingEvents(){
        return evenementRepository.findUpcomingEventsWithClub(LocalDateTime.now(), PageRequest.of(0, 3));
    }

    @Override
    // Garde la session ouverte
    public List<Evenement> findAllUpcomingEvents(){
        return evenementRepository.findAllByDateHeureDebutAfterOrderByDateHeureDebutAsc(LocalDateTime.now());
    }

    @Override
    public List<Evenement> getAllEvents() {
        return evenementRepository.findAll();
    }

    @Override
    public List<Evenement> findRecentEvents() {
        // Implémentation temporaire - retourne les événements récents
        return evenementRepository.findAllByDateHeureDebutAfterOrderByDateHeureDebutAsc(LocalDateTime.now().minusDays(30));
    }

    @Override
    public List<Evenement> findEventsByCreator(UUID creatorId) {
        // Implémentation temporaire - retourne tous les événements pour l'instant
        return evenementRepository.findAll();
    }

    @Override
    public List<Evenement> findPlannedEvents() {
        // Implémentation temporaire - retourne tous les événements pour l'instant
        return evenementRepository.findAll();
    }

    @Override
    public int countEventsThisMonth() {
        // Implémentation temporaire - retourne 0 pour l'instant
        return 0;
    }

    @Override
    public double calculateAverageParticipationRate() {
        // Implémentation temporaire - retourne 0.0 pour l'instant
        return 0.0;
    }

    // Nouvelles méthodes pour la gestion des événements avec notifications
    public Evenement createEvenement(String titre, String description, LocalDateTime dateHeureDebut, 
                                   String lieu, Integer capaciteMax, UUID clubId, UUID createurId) {
        Evenement evenement = new Evenement();
        evenement.setTitre(titre);
        evenement.setDescription(description);
        evenement.setDateHeureDebut(dateHeureDebut);
        evenement.setLieu(lieu);
        evenement.setCapaciteMax(capaciteMax);
        
        evenement = evenementRepository.save(evenement);
        
        // Notifier tous les utilisateurs du nouvel événement
        String titreNotif = "Nouvel événement !";
        String messageNotif = String.format("Un nouvel événement '%s' a été créé le %s à %s", 
                                          titre, dateHeureDebut.toLocalDate(), lieu);
        notificationService.envoyerNotificationBroadcast(titreNotif, messageNotif, TypeNotification.EVENEMENT);
        
        return evenement;
    }

    public Evenement updateEvenement(UUID evenementId, String titre, String description, 
                                   LocalDateTime dateHeureDebut, String lieu, Integer capaciteMax) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        
        String ancienTitre = evenement.getTitre();
        evenement.setTitre(titre);
        evenement.setDescription(description);
        evenement.setDateHeureDebut(dateHeureDebut);
        evenement.setLieu(lieu);
        evenement.setCapaciteMax(capaciteMax);
        
        evenement = evenementRepository.save(evenement);
        
        // Notifier les participants de la modification
        String titreNotif = "Événement modifié";
        String messageNotif = String.format("L'événement '%s' a été modifié. Vérifiez les nouvelles informations.", titre);
        notificationService.envoyerNotificationBroadcast(titreNotif, messageNotif, TypeNotification.EVENEMENT);
        
        return evenement;
    }

    public void cancelEvenement(UUID evenementId, String motif) {
        Evenement evenement = evenementRepository.findById(evenementId)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        
        String titreEvenement = evenement.getTitre();
        evenementRepository.delete(evenement);
        
        // Notifier tous les utilisateurs de l'annulation
        String titreNotif = "Événement annulé";
        String messageNotif = String.format("L'événement '%s' a été annulé. Motif: %s", titreEvenement, motif);
        notificationService.envoyerNotificationBroadcast(titreNotif, messageNotif, TypeNotification.EVENEMENT);
    }
}
