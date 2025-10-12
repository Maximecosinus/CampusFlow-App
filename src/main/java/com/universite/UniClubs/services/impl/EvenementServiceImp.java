package com.universite.UniClubs.services.impl;


import com.universite.UniClubs.entities.Evenement;
import com.universite.UniClubs.entities.StatutEvenement;
import com.universite.UniClubs.repositories.EvenementRepository;
import com.universite.UniClubs.services.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public List<Evenement> findAllEvents() {
        return evenementRepository.findAllWithClub();
    }

    @Override
    @Transactional
    public Evenement saveEvent(Evenement evenement) {
        // S'assurer que l'événement est ajouté à la collection du club
        if (evenement.getClub() != null && !evenement.getClub().getEvenementsOrganises().contains(evenement)) {
            evenement.getClub().getEvenementsOrganises().add(evenement);
        }
        return evenementRepository.save(evenement);
    }

    @Override
    public Optional<Evenement> findById(UUID id) {
        return evenementRepository.findById(id);
    }

    @Override
    public Optional<Evenement> findByIdWithClub(UUID id) {
        return evenementRepository.findByIdWithClub(id);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID id) {
        evenementRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Evenement updateEventStatus(UUID id, StatutEvenement newStatus) {
        Evenement evenement = evenementRepository.findByIdWithClub(id)
                .orElseThrow(() -> new RuntimeException("Événement non trouvé"));
        evenement.setStatut(newStatus);
        return evenementRepository.save(evenement);
    }
}
