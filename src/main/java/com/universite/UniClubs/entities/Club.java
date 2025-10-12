package com.universite.UniClubs.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="clubs")
//@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable=false, unique = true)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String logo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "chef_club_id")
    private Utilisateur chefClub;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate(){
        this.dateCreation = LocalDateTime.now();
    }

    /*@ManyToMany(mappedBy = "clubsInscrits")
    @EqualsAndHashCode.Exclude // <-- AJOUTER ÇA
    @ToString.Exclude          // <-- AJOUTER ÇA
    private Set<Utilisateur> membres = new HashSet<>();*/

    // AJOUTER LA NOUVELLE RELATION
    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Inscription> inscriptions = new HashSet<>();

    @OneToMany(mappedBy = "club")
    @EqualsAndHashCode.Exclude // <-- AJOUTER ÇA
    @ToString.Exclude          // <-- AJOUTER ÇA
    private Set <Evenement> evenementsOrganises = new HashSet<>();


}
