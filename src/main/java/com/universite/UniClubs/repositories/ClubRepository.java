package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, UUID> {

    List<Club> findTop3ByOrderByDateCreationDesc();
    @Query("SELECT c FROM Club c LEFT JOIN FETCH c.membres LEFT JOIN FETCH c.evenementsOrganises WHERE c.id = :id")
    Optional<Club> findByIdWithDetails(UUID id);

}
