package com.universite.UniClubs.repositories;


import com.universite.UniClubs.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    Optional<Utilisateur> findByEmail(String email);
    @Query("SELECT COUNT(u) > 0 FROM Utilisateur u JOIN u.clubsInscrits c WHERE u.email = :email AND c.id = :clubId")
    boolean isUserSubscribedToClub(@Param("email") String email, @Param("clubId") UUID clubId);

    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.clubsInscrits WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithClubsInscrits(@Param("email") String email);

    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.clubsDiriges WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithClubsDiriges(@Param("email") String email);

    String email(String email);
}
