package com.universite.UniClubs.repositories;

import com.universite.UniClubs.entities.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, java.util.UUID> {
    
    /**
     * Trouve tous les logs d'un utilisateur spécifique
     */
    List<AuditLog> findByUserEmailOrderByTimestampDesc(String userEmail);
    
    /**
     * Trouve tous les logs d'un type d'action spécifique
     */
    List<AuditLog> findByActionTypeOrderByTimestampDesc(AuditLog.ActionType actionType);
    
    /**
     * Trouve tous les logs d'un type d'entité spécifique
     */
    List<AuditLog> findByEntityTypeOrderByTimestampDesc(AuditLog.EntityType entityType);
    
    /**
     * Trouve tous les logs dans une période donnée
     */
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Trouve tous les logs d'un utilisateur dans une période donnée
     */
    List<AuditLog> findByUserEmailAndTimestampBetweenOrderByTimestampDesc(String userEmail, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Trouve les logs récents (dernières 24h)
     */
    @Query("SELECT a FROM AuditLog a WHERE a.timestamp >= :yesterday ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentLogs(@Param("yesterday") LocalDateTime yesterday);
    
    /**
     * Trouve les logs avec pagination
     */
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    
    /**
     * Trouve les logs d'un utilisateur avec pagination
     */
    Page<AuditLog> findByUserEmailOrderByTimestampDesc(String userEmail, Pageable pageable);
    
    /**
     * Compte le nombre total de logs
     */
    long count();
    
    /**
     * Compte le nombre de logs d'un utilisateur
     */
    long countByUserEmail(String userEmail);
    
    /**
     * Compte le nombre de logs d'un type d'action
     */
    long countByActionType(AuditLog.ActionType actionType);
    
    /**
     * Trouve les logs par recherche textuelle dans l'action
     */
    @Query("SELECT a FROM AuditLog a WHERE LOWER(a.action) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY a.timestamp DESC")
    List<AuditLog> findByActionContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    /**
     * Trouve les logs par recherche textuelle dans l'action avec pagination
     */
    @Query("SELECT a FROM AuditLog a WHERE LOWER(a.action) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY a.timestamp DESC")
    Page<AuditLog> findByActionContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);
}
