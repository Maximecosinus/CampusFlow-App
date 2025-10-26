package com.universite.UniClubs.repositories;

import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.entities.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, UUID> {

    // Méthode de base, à conserver.
    Optional<Utilisateur> findByEmail(String email);

    // --- CORRECTION : Les deux méthodes suivantes ont été supprimées ---
    // Elles utilisaient l'ancienne relation @ManyToMany "clubsInscrits" qui a été retirée.
    /*
    @Query("SELECT COUNT(u) > 0 FROM Utilisateur u JOIN u.clubsInscrits c WHERE u.email = :email AND c.id = :clubId")
    boolean isUserSubscribedToClub(@Param("email") String email, @Param("clubId") UUID clubId);

    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.clubsInscrits WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithClubsInscrits(@Param("email") String email);
    */

    // Cette méthode est correcte, elle charge les clubs dirigés.
    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.clubsDiriges WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithClubsDiriges(@Param("email") String email);

    // Cette méthode a été renommée pour plus de clarté et est utilisée par Spring Security.
    // Elle est très importante et correcte.
    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.clubsDiriges WHERE u.email = :email")
    Optional<Utilisateur> findByEmailForSecurity(@Param("email") String email);

    // NOUVELLE MÉTHODE (Optionnelle mais recommandée pour la propreté)
    // Elle charge l'utilisateur avec sa nouvelle collection "inscriptions".
    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.inscriptions WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithInscriptions(@Param("email") String email);

    // Méthodes manquantes pour les contrôleurs
    long countByRole(Role role);
    List<Utilisateur> findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(String nom, String email);
}