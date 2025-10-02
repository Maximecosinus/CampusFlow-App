package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Club;
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

    List<Club> findTop3ByOrderByDateCreationDesc();
    @Query("SELECT c FROM Club c LEFT JOIN FETCH c.membres LEFT JOIN FETCH c.evenementsOrganises WHERE c.id = :id")
    Optional<Club> findByIdWithDetails(UUID id);

    @Query("SELECT c FROM Club c LEFT JOIN FETCH c.chefClub ORDER BY c.dateCreation DESC")
    List<Club> findRecentClubsWithChef(Pageable pageable);

    @Override
    @Query("SELECT DISTINCT c FROM Club c LEFT JOIN FETCH c.chefClub")
    List<Club> findAll();

    @Query("SELECT c FROM Club c " +
            "LEFT JOIN FETCH c.membres " +
            "LEFT JOIN FETCH c.evenementsOrganises " +
            "WHERE c.chefClub.id = :chefId")
    Optional<Club> findClubWithDetailsByChefId(@Param("chefId") UUID chefId);

}
