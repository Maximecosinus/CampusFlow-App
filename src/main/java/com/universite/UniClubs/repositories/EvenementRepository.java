package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Evenement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<Evenement, UUID> {

    List<Evenement> findTop3ByDateHeureDebutAfterOrderByDateHeureDebutAsc(LocalDateTime maintenant);
}
