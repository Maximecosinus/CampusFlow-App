package com.universite.UniClubs.entities;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.Set;
@Entity
@Table(name="utilisateurs")
//@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable=false)
    private String nom;

    @Column(nullable=false)
    private String prenom;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String motDePasse;

    private String photoProfil;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Role role;

    @OneToMany(mappedBy = "chefClub")
    private Set <Club> clubsDiriges = new HashSet<>();

    // AJOUTER LA NOUVELLE RELATION
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Inscription> inscriptions = new HashSet<>();
}
