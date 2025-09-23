package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, UUID> {

    List<Club> findTop3ByOrderByDateCreationDesc();

}
