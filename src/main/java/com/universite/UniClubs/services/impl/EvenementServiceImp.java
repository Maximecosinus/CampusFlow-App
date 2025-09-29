package com.universite.UniClubs.services.impl;


import com.universite.UniClubs.entities.Evenement;
import com.universite.UniClubs.repositories.EvenementRepository;
import com.universite.UniClubs.services.EvenementService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EvenementServiceImp implements EvenementService {

    @Autowired
    private EvenementRepository evenementRepository;

    @Override
    public List<Evenement> findUpcomingEvents(){
        return evenementRepository.findTop3ByDateHeureDebutAfterOrderByDateHeureDebutAsc(LocalDateTime.now());
    }

    @Override
    // Garde la session ouverte
    public List<Evenement> findAllUpcomingEvents(){
        return evenementRepository.findAllByDateHeureDebutAfterOrderByDateHeureDebutAsc(LocalDateTime.now());
    }
}
