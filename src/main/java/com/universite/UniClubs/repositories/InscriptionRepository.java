package com.universite.UniClubs.repositories;

import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Inscription;
import com.universite.UniClubs.entities.StatutInscription;
import com.universite.UniClubs.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InscriptionRepository extends JpaRepository<Inscription, UUID> {
    // Dans InscriptionRepository.java
    boolean existsByUtilisateurAndClub(Utilisateur utilisateur, Club club);
    Optional<Inscription> findByUtilisateurEmailAndClubId(String email, UUID clubId);
    // Dans InscriptionRepository.java
    boolean existsByUtilisateurEmailAndClubIdAndStatut(String email, UUID clubId, StatutInscription statut);

}