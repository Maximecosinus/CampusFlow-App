package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable; // <-- IMPORTER
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, UUID> {

    @Query("SELECT e FROM Evenement e LEFT JOIN FETCH e.club " +
            "WHERE e.dateHeureDebut > :maintenant " +
            "ORDER BY e.dateHeureDebut ASC")
    List<Evenement> findUpcomingEventsWithClub(@Param("maintenant") LocalDateTime maintenant, Pageable pageable);
    List<Evenement> findAllByDateHeureDebutAfterOrderByDateHeureDebutAsc(LocalDateTime maintenant);
}
