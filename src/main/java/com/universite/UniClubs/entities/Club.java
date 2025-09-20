package com.universite.UniClubs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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

    @ManyToMany(mappedBy = "clubsInscrits")
    private Set<Utilisateur> membres = new HashSet<>();

    @OneToMany(mappedBy = "club")
    private Set <Evenement> evenementsOrganises = new HashSet<>();

}
