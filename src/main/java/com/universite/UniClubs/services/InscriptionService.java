package com.universite.UniClubs.services;

import java.util.Optional;
import java.util.UUID;
import com.universite.UniClubs.entities.StatutInscription; // Importer le statut


public interface InscriptionService {
    void approuverInscription(UUID inscriptionId);
    void refuserInscription(UUID inscriptionId, String motif);
    Optional<StatutInscription> findStatutInscription(String emailUtilisateur, UUID idClub);


}