package com.universite.UniClubs.services.impl;

import com.universite.UniClubs.entities.Notification;
import com.universite.UniClubs.entities.TypeNotification;
import com.universite.UniClubs.repositories.NotificationRepository;
import com.universite.UniClubs.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void envoyerNotification(UUID destinataireId, String titre, String message, TypeNotification type) {
        envoyerNotification(destinataireId, titre, message, type, null, null, null);
    }

    @Override
    public void envoyerNotification(UUID destinataireId, String titre, String message, TypeNotification type, 
                                  Map<String, String> metadata, String lienAction) {
        envoyerNotification(destinataireId, titre, message, type, metadata, lienAction, null);
    }

    @Override
    public void envoyerNotification(UUID destinataireId, String titre, String message, TypeNotification type, UUID emetteurId) {
        envoyerNotification(destinataireId, titre, message, type, null, null, emetteurId);
    }

    private void envoyerNotification(UUID destinataireId, String titre, String message, TypeNotification type, 
                                   Map<String, String> metadata, String lienAction, UUID emetteurId) {
        Notification notification = new Notification(titre, message, type, destinataireId, emetteurId);
        notification.setMetadata(metadata);
        notification.setLienAction(lienAction);
        
        // Sauvegarder en base de données
        notification = notificationRepository.save(notification);
        
        // Envoyer via WebSocket
        String destination = "/queue/notifications/" + destinataireId;
        messagingTemplate.convertAndSend(destination, notification);
        
        System.out.println("Notification envoyée à l'utilisateur " + destinataireId + ": " + titre);
    }

    @Override
    public void envoyerNotificationBroadcast(String titre, String message, TypeNotification type) {
        envoyerNotificationBroadcast(titre, message, type, null, null, null);
    }

    @Override
    public void envoyerNotificationBroadcast(String titre, String message, TypeNotification type, 
                                           Map<String, String> metadata, String lienAction) {
        envoyerNotificationBroadcast(titre, message, type, metadata, lienAction, null);
    }

    @Override
    public void envoyerNotificationBroadcast(String titre, String message, TypeNotification type, UUID emetteurId) {
        envoyerNotificationBroadcast(titre, message, type, null, null, emetteurId);
    }

    private void envoyerNotificationBroadcast(String titre, String message, TypeNotification type, 
                                           Map<String, String> metadata, String lienAction, UUID emetteurId) {
        Notification notification = new Notification(titre, message, type, emetteurId);
        notification.setMetadata(metadata);
        notification.setLienAction(lienAction);
        
        // Sauvegarder en base de données
        notification = notificationRepository.save(notification);
        
        // Envoyer via WebSocket à tous les utilisateurs connectés
        String destination = "/topic/notifications";
        messagingTemplate.convertAndSend(destination, notification);
        
        System.out.println("Notification broadcast envoyée: " + titre);
    }

    @Override
    @Transactional
    public void marquerCommeLu(UUID notificationId, UUID utilisateurId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification non trouvée"));
        
        // Vérifier que la notification appartient à l'utilisateur
        if (notification.getDestinataireId() != null && notification.getDestinataireId().equals(utilisateurId)) {
            notification.setLu(true);
            notificationRepository.save(notification);
        } else if (notification.getDestinataireId() == null) {
            // Pour les notifications broadcast, on peut les marquer comme lues
            notification.setLu(true);
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void marquerToutCommeLu(UUID utilisateurId) {
        List<Notification> notificationsNonLues = notificationRepository
                .findByDestinataireIdAndLuFalseOrderByDateCreationDesc(utilisateurId);
        
        for (Notification notification : notificationsNonLues) {
            notification.setLu(true);
        }
        
        notificationRepository.saveAll(notificationsNonLues);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsNonLues(UUID utilisateurId) {
        return notificationRepository.findByDestinataireIdAndLuFalseOrderByDateCreationDesc(utilisateurId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Notification> getHistorique(UUID utilisateurId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findByDestinataireIdOrderByDateCreationDesc(utilisateurId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countNotificationsNonLues(UUID utilisateurId) {
        return notificationRepository.countByDestinataireIdAndLuFalse(utilisateurId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsRecentes(UUID utilisateurId) {
        Pageable pageable = PageRequest.of(0, 10);
        return notificationRepository.findTop10ByDestinataireIdAndLuFalseOrderByDateCreationDesc(utilisateurId, pageable);
    }

    @Override
    @Transactional
    public void nettoyerAnciennesNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteOldNotifications(cutoffDate);
        System.out.println("Nettoyage des anciennes notifications effectué");
    }
}
