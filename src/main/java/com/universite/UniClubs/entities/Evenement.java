package com.universite.UniClubs.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "evenements")
//@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Evenement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateHeureDebut;

    private String lieu;

    private Integer capaciteMax;

    @Column(nullable = false, updatable = false)
    private LocalDateTime datePublication;

    private String photo;

    @PrePersist
    protected void onPublish(){
        this.datePublication= LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="club_id")
    private Club club;

}
