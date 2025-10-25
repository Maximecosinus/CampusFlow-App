package com.universite.UniClubs.services.impl;


import com.universite.UniClubs.entities.Evenement;
import com.universite.UniClubs.repositories.EvenementRepository;
import com.universite.UniClubs.services.EvenementService;
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
}
