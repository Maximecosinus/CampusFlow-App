package com.universite.UniClubs.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inscriptions")
@Data
@Getter
@Setter
@EqualsAndHashCode(of = "id") // Important pour éviter les boucles
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    @ToString.Exclude // Évite les boucles
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    @ToString.Exclude // Évite les boucles
    private Club club;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutInscription statut;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateDemande;

    private String motifRefus; // Pour garder une trace du motif si la demande est refusée

    @PrePersist
    protected void onCreate() {
        this.dateDemande = LocalDateTime.now();
    }
}