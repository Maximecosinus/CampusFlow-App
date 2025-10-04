// InscriptionServiceImpl.java
package com.universite.UniClubs.services.impl;

import com.universite.UniClubs.entities.Inscription;
import com.universite.UniClubs.entities.StatutInscription;
import com.universite.UniClubs.repositories.InscriptionRepository;
import com.universite.UniClubs.services.InscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InscriptionServiceImpl implements InscriptionService {

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Override
    @Transactional
    public void approuverInscription(UUID inscriptionId) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Demande d'inscription non trouvée"));

        // On vérifie que la demande est bien en attente avant de la modifier
        if (inscription.getStatut() == StatutInscription.EN_ATTENTE) {
            inscription.setStatut(StatutInscription.ACCEPTE);
            // Pas besoin de save() explicite grâce à @Transactional
        }
    }

    @Override
    @Transactional
    public void refuserInscription(UUID inscriptionId, String motif) {
        Inscription inscription = inscriptionRepository.findById(inscriptionId)
                .orElseThrow(() -> new RuntimeException("Demande d'inscription non trouvée"));

        if (inscription.getStatut() == StatutInscription.EN_ATTENTE) {
            inscription.setStatut(StatutInscription.REFUSE);
            inscription.setMotifRefus(motif); // On stocke le motif
        }
    }
}