package com.universite.UniClubs.services;

import com.universite.UniClubs.entities.Club;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubService {
    List<Club> getAllClubs();
    List<Club> findRecentclubs();
    Optional<Club> getClubById(UUID id);
    Club findClubWithDetailsById(UUID clubId);
    
    // Nouvelles méthodes pour la gestion des clubs
    Club createClub(String nom, String description, String categorie, MultipartFile logo, UUID chefDeClubId);
    Club updateClub(UUID clubId, String nom, String description, String categorie, MultipartFile logo);
    void assignChef(UUID clubId, UUID chefId);
    void deleteClub(UUID clubId);
}
