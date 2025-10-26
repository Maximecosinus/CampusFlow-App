package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Notification;
import com.universite.UniClubs.services.CustomUserDetails;
import com.universite.UniClubs.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Récupérer les notifications non lues d'un utilisateur
     */
    @GetMapping("/non-lues")
    public ResponseEntity<List<Notification>> getNotificationsNonLues(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        UUID utilisateurId = getUserIdFromAuthentication(authentication);
        List<Notification> notifications = notificationService.getNotificationsNonLues(utilisateurId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Récupérer l'historique des notifications d'un utilisateur (paginé)
     */
    @GetMapping("/historique")
    public ResponseEntity<Page<Notification>> getHistorique(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        UUID utilisateurId = getUserIdFromAuthentication(authentication);
        Page<Notification> notifications = notificationService.getHistorique(utilisateurId, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Marquer une notification comme lue
     */
    @PostMapping("/{notificationId}/marquer-lu")
    public ResponseEntity<Map<String, String>> marquerCommeLu(
            @PathVariable UUID notificationId,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        UUID utilisateurId = getUserIdFromAuthentication(authentication);
        
        try {
            notificationService.marquerCommeLu(notificationId, utilisateurId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification marquée comme lue");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors du marquage de la notification");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Marquer toutes les notifications d'un utilisateur comme lues
     */
    @PostMapping("/marquer-tout-lu")
    public ResponseEntity<Map<String, String>> marquerToutCommeLu(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        UUID utilisateurId = getUserIdFromAuthentication(authentication);
        
        try {
            notificationService.marquerToutCommeLu(utilisateurId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Toutes les notifications ont été marquées comme lues");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors du marquage des notifications");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Compter les notifications non lues d'un utilisateur (pour le badge)
     */
    @GetMapping("/count-non-lues")
    public ResponseEntity<Map<String, Long>> countNotificationsNonLues(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        UUID utilisateurId = getUserIdFromAuthentication(authentication);
        long count = notificationService.countNotificationsNonLues(utilisateurId);
        
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer les notifications récentes d'un utilisateur (10 dernières)
     */
    @GetMapping("/recentes")
    public ResponseEntity<List<Notification>> getNotificationsRecentes(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        UUID utilisateurId = getUserIdFromAuthentication(authentication);
        List<Notification> notifications = notificationService.getNotificationsRecentes(utilisateurId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Endpoint pour envoyer une notification manuelle (admin seulement)
     */
    @PostMapping("/envoyer")
    public ResponseEntity<Map<String, String>> envoyerNotification(
            @RequestParam String titre,
            @RequestParam String message,
            @RequestParam(required = false) UUID destinataireId,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // Vérifier que l'utilisateur est admin
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).build();
        }

        UUID emetteurId = getUserIdFromAuthentication(authentication);
        
        try {
            if (destinataireId != null) {
                // Notification personnelle
                notificationService.envoyerNotification(destinataireId, titre, message, 
                    com.universite.UniClubs.entities.TypeNotification.ADMIN, emetteurId);
            } else {
                // Notification broadcast
                notificationService.envoyerNotificationBroadcast(titre, message, 
                    com.universite.UniClubs.entities.TypeNotification.ADMIN, emetteurId);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification envoyée avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors de l'envoi de la notification");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Nettoyer les anciennes notifications (admin seulement)
     */
    @PostMapping("/nettoyer")
    public ResponseEntity<Map<String, String>> nettoyerAnciennesNotifications(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // Vérifier que l'utilisateur est admin
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).build();
        }

        try {
            notificationService.nettoyerAnciennesNotifications();
            Map<String, String> response = new HashMap<>();
            response.put("message", "Nettoyage des anciennes notifications effectué");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Erreur lors du nettoyage");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Page d'historique des notifications (pour l'interface web)
     */
    @GetMapping("/page-historique")
    public String showNotificationHistoryPage(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        UUID utilisateurId = getUserIdFromAuthentication(authentication);
        model.addAttribute("utilisateurConnecte", ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur());
        
        return "notifications-historique";
    }

    // Méthodes utilitaires
    private UUID getUserIdFromAuthentication(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur().getId();
        }
        throw new RuntimeException("Utilisateur non trouvé");
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            String role = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur().getRole().name();
            return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
        }
        return false;
    }
}
