package com.universite.UniClubs.services;
import com.universite.UniClubs.entities.Evenement;
import java.util.List;

public interface EvenementService {
    List<Evenement> findUpcomingEvents();
}
