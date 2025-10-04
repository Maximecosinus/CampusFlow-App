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
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Inscription;
import java.util.Optional;

@Service
public class InscriptionServiceImpl implements InscriptionService {

    @Autowired
    private InscriptionRepository inscriptionRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private ClubRepository clubRepository;

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

    @Override
    @Transactional(readOnly = true) // C'est une opération de lecture seule, c'est bien de le préciser
    public Optional<StatutInscription> findStatutInscription(String emailUtilisateur, UUID idClub) {
        // On récupère les entités
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(emailUtilisateur);
        Optional<Club> clubOpt = clubRepository.findById(idClub);

        // Si l'un ou l'autre n'existe pas, il n'y a pas d'inscription
        if (utilisateurOpt.isEmpty() || clubOpt.isEmpty()) {
            return Optional.empty();
        }

        // On cherche l'inscription
        Optional<Inscription> inscriptionOpt = inscriptionRepository.findByUtilisateurAndClub(utilisateurOpt.get(), clubOpt.get());

        // On renvoie le statut s'il existe, sinon un Optional vide
        return inscriptionOpt.map(Inscription::getStatut);
    }
}