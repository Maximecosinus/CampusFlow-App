package com.universite.UniClubs.services;

import com.universite.UniClubs.entities.Club;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubService {
    List<Club> getAllClubs();
    List<Club> findRecentclubs();
    Optional<Club> findClubById(UUID id);
}
