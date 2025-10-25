package com.universite.UniClubs.repositories;

import com.universite.UniClubs.entities.Notification;
import com.universite.UniClubs.entities.TypeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Récupérer les notifications d'un utilisateur spécifique, triées par date de création (plus récentes en premier)
    @Query("SELECT n FROM Notification n WHERE n.destinataireId = :destinataireId ORDER BY n.dateCreation DESC")
    Page<Notification> findByDestinataireIdOrderByDateCreationDesc(@Param("destinataireId") UUID destinataireId, Pageable pageable);

    // Compter les notifications non lues d'un utilisateur
    long countByDestinataireIdAndLuFalse(UUID destinataireId);

    // Récupérer les 10 notifications non lues les plus récentes d'un utilisateur
    @Query("SELECT n FROM Notification n WHERE n.destinataireId = :destinataireId AND n.lu = false ORDER BY n.dateCreation DESC")
    List<Notification> findTop10ByDestinataireIdAndLuFalseOrderByDateCreationDesc(@Param("destinataireId") UUID destinataireId, Pageable pageable);

    // Récupérer toutes les notifications non lues d'un utilisateur
    @Query("SELECT n FROM Notification n WHERE n.destinataireId = :destinataireId AND n.lu = false ORDER BY n.dateCreation DESC")
    List<Notification> findByDestinataireIdAndLuFalseOrderByDateCreationDesc(@Param("destinataireId") UUID destinataireId);

    // Récupérer les notifications broadcast (destinataireId = null)
    @Query("SELECT n FROM Notification n WHERE n.destinataireId IS NULL ORDER BY n.dateCreation DESC")
    Page<Notification> findBroadcastNotificationsOrderByDateCreationDesc(Pageable pageable);

    // Récupérer les notifications par type
    @Query("SELECT n FROM Notification n WHERE n.destinataireId = :destinataireId AND n.type = :type ORDER BY n.dateCreation DESC")
    Page<Notification> findByDestinataireIdAndTypeOrderByDateCreationDesc(@Param("destinataireId") UUID destinataireId, @Param("type") TypeNotification type, Pageable pageable);

    // Supprimer les anciennes notifications (plus de 30 jours)
    @Query("DELETE FROM Notification n WHERE n.dateCreation < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
}
