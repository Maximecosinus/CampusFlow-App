package com.universite.UniClubs.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "user_email", nullable = false)
    private String userEmail;
    
    @Column(name = "action", nullable = false, length = 500)
    private String action;
    
    @Column(name = "action_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionType actionType;
    
    @Column(name = "entity_type")
    @Enumerated(EnumType.STRING)
    private EntityType entityType;
    
    @Column(name = "entity_id")
    private String entityId;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    public enum ActionType {
        CREATE("Création"),
        UPDATE("Modification"),
        DELETE("Suppression"),
        LOGIN("Connexion"),
        LOGOUT("Déconnexion"),
        REGISTER("Inscription"),
        UNREGISTER("Désinscription"),
        ASSIGN("Assignation"),
        UNASSIGN("Désassignation"),
        APPROVE("Approbation"),
        REJECT("Rejet"),
        CANCEL("Annulation"),
        PUBLISH("Publication"),
        UNPUBLISH("Dépublication");
        
        private final String displayName;
        
        ActionType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public enum EntityType {
        USER("Utilisateur"),
        CLUB("Club"),
        EVENT("Événement"),
        INSCRIPTION("Inscription"),
        ADMIN("Administrateur"),
        SYSTEM("Système");
        
        private final String displayName;
        
        EntityType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Constructeur pour faciliter la création
    public AuditLog(String userEmail, String action, ActionType actionType, EntityType entityType, String entityId) {
        this.userEmail = userEmail;
        this.action = action;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructeur avec détails supplémentaires
    public AuditLog(String userEmail, String action, ActionType actionType, EntityType entityType, String entityId, String details) {
        this.userEmail = userEmail;
        this.action = action;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
}
