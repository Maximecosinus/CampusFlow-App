package com.universite.UniClubs.entities;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeNotification type;

    @Column(name = "destinataire_id")
    private UUID destinataireId; // null = broadcast à tous

    @Column(name = "emetteur_id")
    private UUID emetteurId;

    @Column(nullable = false)
    private boolean lu = false;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreation;

    @Column(name = "lien_action")
    private String lienAction; // URL optionnelle pour redirection

    @ElementCollection
    @CollectionTable(name = "notification_metadata", joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata; // JSON pour données additionnelles

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }

    // Constructeur pour notifications personnelles
    public Notification(String titre, String message, TypeNotification type, UUID destinataireId, UUID emetteurId) {
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.destinataireId = destinataireId;
        this.emetteurId = emetteurId;
    }

    // Constructeur pour notifications broadcast
    public Notification(String titre, String message, TypeNotification type, UUID emetteurId) {
        this.titre = titre;
        this.message = message;
        this.type = type;
        this.emetteurId = emetteurId;
        this.destinataireId = null; // null = broadcast
    }
}
