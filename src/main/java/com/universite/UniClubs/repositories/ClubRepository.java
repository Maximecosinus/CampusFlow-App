package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable; // <-- IMPORTER Pageable
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
            "LEFT JOIN FETCH c.evenementsOrganises " +
            "WHERE c.id = :id")
    Optional<Club> findByIdWithDetails(@Param("id") UUID id); // N'oubliez pas @Param("id")


    // DEUXIÈME REQUÊTE À CORRIGER
    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.inscriptions i " +      // On charge les inscriptions
            "LEFT JOIN FETCH i.utilisateur " +           // Pour chaque inscription, on charge l'utilisateur
            "LEFT JOIN FETCH c.evenementsOrganises " +
            "WHERE c.chefClub.id = :chefId")
    Optional<Club> findClubWithDetailsByChefId(@Param("chefId") UUID chefId);

    // Méthode pour récupérer les clubs récents
    List<Club> findByDateCreationAfterOrderByDateCreationDesc(LocalDateTime cutoffDate);

}
