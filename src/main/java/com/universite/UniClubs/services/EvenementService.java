package com.universite.UniClubs.services;
import com.universite.UniClubs.entities.Evenement;
import com.universite.UniClubs.entities.StatutEvenement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvenementService {
    List<Evenement> findUpcomingEvents();
    List<Evenement> findAllUpcomingEvents();
    Evenement saveEvent(Evenement evenement);
    Optional<Evenement> findById(UUID id);
    Optional<Evenement> findByIdWithClub(UUID id);
    void deleteEvent(UUID id);
    Evenement updateEventStatus(UUID id, StatutEvenement newStatus);
}
