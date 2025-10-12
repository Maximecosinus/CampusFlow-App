package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable; // <-- IMPORTER Pageable
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, UUID> {



    @Query("SELECT c FROM Club c LEFT JOIN FETCH c.chefClub ORDER BY c.dateCreation DESC")
    List<Club> findRecentClubsWithChef(Pageable pageable);

    @Override
    @Query("SELECT DISTINCT c FROM Club c LEFT JOIN FETCH c.chefClub")
    List<Club> findAll();

    // PREMIÈRE REQUÊTE À CORRIGER
    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.inscriptions i " +      // On charge les inscriptions
            "LEFT JOIN FETCH i.utilisateur " +           // Pour chaque inscription, on charge l'utilisateur
            "LEFT JOIN FETCH c.evenementsOrganises e " + // On charge les événements
            "LEFT JOIN FETCH e.club " +                  // Pour chaque événement, on charge le club associé
            "WHERE c.id = :id")
    Optional<Club> findByIdWithDetails(@Param("id") UUID id); // N'oubliez pas @Param("id")


    // DEUXIÈME REQUÊTE À CORRIGER
    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.inscriptions i " +      // On charge les inscriptions
            "LEFT JOIN FETCH i.utilisateur " +           // Pour chaque inscription, on charge l'utilisateur
            "LEFT JOIN FETCH c.evenementsOrganises e " + // On charge les événements
            "LEFT JOIN FETCH e.club " +                  // Pour chaque événement, on charge le club associé
            "WHERE c.chefClub.id = :chefId")
    Optional<Club> findClubWithDetailsByChefId(@Param("chefId") UUID chefId);

    // NOUVELLE REQUÊTE POUR CHARGER TOUS LES CLUBS AVEC LEURS INSCRIPTIONS
    @Query("SELECT DISTINCT c FROM Club c " +
            "LEFT JOIN FETCH c.chefClub " +
            "LEFT JOIN FETCH c.inscriptions")
    List<Club> findAllWithInscriptions();

    // Vérifier si un utilisateur dirige déjà un club
    boolean existsByChefClub(Utilisateur chefClub);

}
