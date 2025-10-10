package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable; // <-- IMPORTER
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, UUID> {

    @Query("SELECT e FROM Evenement e LEFT JOIN FETCH e.club " +
            "WHERE e.dateHeureDebut > :maintenant " +
            "AND e.statut = 'PUBLIE' " +
            "ORDER BY e.dateHeureDebut ASC")
    List<Evenement> findUpcomingEventsWithClub(@Param("maintenant") LocalDateTime maintenant, Pageable pageable);
    @Query("SELECT e FROM Evenement e " +
            "WHERE e.dateHeureDebut > :maintenant " +
            "AND e.statut = 'PUBLIE' " +
            "ORDER BY e.dateHeureDebut ASC")
    List<Evenement> findAllByDateHeureDebutAfterOrderByDateHeureDebutAsc(@Param("maintenant") LocalDateTime maintenant);
    
    @Query("SELECT e FROM Evenement e LEFT JOIN FETCH e.club WHERE e.id = :id")
    Optional<Evenement> findByIdWithClub(@Param("id") UUID id);
}
