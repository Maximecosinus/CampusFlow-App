package com.universite.UniClubs.services;

import com.universite.UniClubs.entities.Notification;
import com.universite.UniClubs.entities.TypeNotification;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NotificationService {

    /**
     * Envoyer une notification à un utilisateur spécifique
     */
    void envoyerNotification(UUID destinataireId, String titre, String message, TypeNotification type);

    /**
     * Envoyer une notification à un utilisateur spécifique avec métadonnées
     */
    void envoyerNotification(UUID destinataireId, String titre, String message, TypeNotification type, 
                           Map<String, String> metadata, String lienAction);

    /**
     * Envoyer une notification broadcast à tous les utilisateurs
     */
    void envoyerNotificationBroadcast(String titre, String message, TypeNotification type);

    /**
     * Envoyer une notification broadcast avec métadonnées
     */
    void envoyerNotificationBroadcast(String titre, String message, TypeNotification type, 
                                     Map<String, String> metadata, String lienAction);

    /**
     * Marquer une notification comme lue
     */
    void marquerCommeLu(UUID notificationId, UUID utilisateurId);

    /**
     * Marquer toutes les notifications d'un utilisateur comme lues
     */
    void marquerToutCommeLu(UUID utilisateurId);

    /**
     * Récupérer les notifications non lues d'un utilisateur
     */
    List<Notification> getNotificationsNonLues(UUID utilisateurId);

    /**
     * Récupérer l'historique des notifications d'un utilisateur (paginé)
     */
    Page<Notification> getHistorique(UUID utilisateurId, int page, int size);

    /**
     * Compter les notifications non lues d'un utilisateur
     */
    long countNotificationsNonLues(UUID utilisateurId);

    /**
     * Récupérer les notifications récentes d'un utilisateur (10 dernières)
     */
    List<Notification> getNotificationsRecentes(UUID utilisateurId);

    /**
     * Supprimer les anciennes notifications (nettoyage automatique)
     */
    void nettoyerAnciennesNotifications();

    /**
     * Envoyer notification avec émetteur spécifique
     */
    void envoyerNotification(UUID destinataireId, String titre, String message, TypeNotification type, UUID emetteurId);

    /**
     * Envoyer notification broadcast avec émetteur spécifique
     */
    void envoyerNotificationBroadcast(String titre, String message, TypeNotification type, UUID emetteurId);
}
