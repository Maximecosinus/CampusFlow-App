package com.universite.UniClubs.services;
import com.universite.UniClubs.entities.Evenement;
import java.util.List;
import java.util.UUID;

public interface EvenementService {
    List<Evenement> findUpcomingEvents();
    List<Evenement> findAllUpcomingEvents();
    List<Evenement> getAllEvents();
    List<Evenement> findRecentEvents();
    List<Evenement> findEventsByCreator(UUID creatorId);
    List<Evenement> findPlannedEvents();
    int countEventsThisMonth();
    double calculateAverageParticipationRate();
    
    // Méthode manquante pour les contrôleurs
    List<Evenement> findAllEvents();
}
