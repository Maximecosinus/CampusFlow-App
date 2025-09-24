package com.universite.UniClubs.services.impl;

import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.repositories.ClubRepository;
import com.universite.UniClubs.services.ClubService;
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

    @Override
    public Optional<Club> findClubById(UUID id){
        return clubRepository.findById(id);
    }
}
