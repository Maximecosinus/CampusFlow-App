package com.universite.UniClubs.services;

import java.util.UUID;

public interface InscriptionService {
    void approuverInscription(UUID inscriptionId);
    void refuserInscription(UUID inscriptionId, String motif);
}