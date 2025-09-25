package com.universite.UniClubs.entities;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode; // <-- Import à ajouter
import lombok.ToString;         // <-- Import à ajouter
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name="clubs")
@Data
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

    @Column(nullable = false, updatable = false)
    protected void onCreate(){
        this.dateCreation= LocalDateTime.now();
    }

    @ManyToMany(mappedBy = "clubsInscrits")
    @EqualsAndHashCode.Exclude // <-- AJOUTER ÇA
    @ToString.Exclude          // <-- AJOUTER ÇA
    private Set<Utilisateur> membres = new HashSet<>();

    @OneToMany(mappedBy = "club")
    @EqualsAndHashCode.Exclude // <-- AJOUTER ÇA
    @ToString.Exclude          // <-- AJOUTER ÇA
    private Set <Evenement> evenementsOrganises = new HashSet<>();


}
