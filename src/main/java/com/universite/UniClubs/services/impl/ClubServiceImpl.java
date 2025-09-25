package com.universite.UniClubs.services.impl;

import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.services.ClubService;
import org.springframework.transaction.annotation.Transactional; // Import correct
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClubServiceImpl implements ClubService {

    @Autowired
    private ClubRepository clubRepository;

    @Override
    public List<Club> getAllClubs() {
        return clubRepository.findAll();
    }

    @Override
    public List<Club> findRecentclubs() {
        return clubRepository.findTop3ByOrderByDateCreationDesc();
    }

    // Cette méthode reste inchangée, elle est correcte.
    @Override
    public Optional<Club> findClubById(UUID id){
        return clubRepository.findById(id);
    }

    // J'ai renommé la méthode pour qu'elle soit plus claire et qu'elle ne crée pas de confusion
    // avec la méthode findClubById ci-dessus. N'oubliez pas de mettre à jour l'interface ClubService aussi.
    // MODIFIEZ CETTE MÉTHODE COMME CECI
    @Override
    @Transactional(readOnly = true)
    public Club findClubWithDetailsById(UUID clubId) {
        // Appelez la nouvelle méthode du repository.
        // Les membres et les événements seront déjà chargés. Pas besoin de .size() !
        return clubRepository.findByIdWithDetails(clubId).orElse(null);
    }
}