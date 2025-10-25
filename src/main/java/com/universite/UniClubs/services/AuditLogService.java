package com.universite.UniClubs.services;

import com.universite.UniClubs.entities.AuditLog;
import com.universite.UniClubs.repositories.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    /**
     * Enregistre une action dans le journal d'audit
     */
    public void logAction(String userEmail, String action, AuditLog.ActionType actionType, AuditLog.EntityType entityType, String entityId) {
        AuditLog auditLog = new AuditLog(userEmail, action, actionType, entityType, entityId);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Enregistre une action avec des détails supplémentaires
     */
    public void logAction(String userEmail, String action, AuditLog.ActionType actionType, AuditLog.EntityType entityType, String entityId, String details) {
        AuditLog auditLog = new AuditLog(userEmail, action, actionType, entityType, entityId, details);
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Enregistre une action avec les informations de la requête HTTP
     */
    public void logAction(String userEmail, String action, AuditLog.ActionType actionType, AuditLog.EntityType entityType, String entityId, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog(userEmail, action, actionType, entityType, entityId);
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Enregistre une action avec détails et informations HTTP
     */
    public void logAction(String userEmail, String action, AuditLog.ActionType actionType, AuditLog.EntityType entityType, String entityId, String details, HttpServletRequest request) {
        AuditLog auditLog = new AuditLog(userEmail, action, actionType, entityType, entityId, details);
        
        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }
        
        auditLogRepository.save(auditLog);
    }
    
    /**
     * Récupère tous les logs avec pagination
     */
    public Page<AuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }
    
    /**
     * Récupère les logs d'un utilisateur
     */
    public List<AuditLog> getUserLogs(String userEmail) {
        return auditLogRepository.findByUserEmailOrderByTimestampDesc(userEmail);
    }
    
    /**
     * Récupère les logs d'un utilisateur avec pagination
     */
    public Page<AuditLog> getUserLogs(String userEmail, Pageable pageable) {
        return auditLogRepository.findByUserEmailOrderByTimestampDesc(userEmail, pageable);
    }
    
    /**
     * Récupère les logs récents (dernières 24h)
     */
    public List<AuditLog> getRecentLogs() {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        return auditLogRepository.findRecentLogs(yesterday);
    }
    
    /**
     * Récupère les logs dans une période donnée
     */
    public List<AuditLog> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(startDate, endDate);
    }
    
    /**
     * Récupère les logs d'un type d'action
     */
    public List<AuditLog> getLogsByActionType(AuditLog.ActionType actionType) {
        return auditLogRepository.findByActionTypeOrderByTimestampDesc(actionType);
    }
    
    /**
     * Récupère les logs d'un type d'entité
     */
    public List<AuditLog> getLogsByEntityType(AuditLog.EntityType entityType) {
        return auditLogRepository.findByEntityTypeOrderByTimestampDesc(entityType);
    }
    
    /**
     * Recherche dans les logs par terme de recherche
     */
    public List<AuditLog> searchLogs(String searchTerm) {
        return auditLogRepository.findByActionContainingIgnoreCase(searchTerm);
    }
    
    /**
     * Recherche dans les logs avec pagination
     */
    public Page<AuditLog> searchLogs(String searchTerm, Pageable pageable) {
        return auditLogRepository.findByActionContainingIgnoreCase(searchTerm, pageable);
    }
    
    /**
     * Compte le nombre total de logs
     */
    public long getTotalLogCount() {
        return auditLogRepository.count();
    }
    
    /**
     * Compte le nombre de logs d'un utilisateur
     */
    public long getUserLogCount(String userEmail) {
        return auditLogRepository.countByUserEmail(userEmail);
    }
    
    /**
     * Compte le nombre de logs d'un type d'action
     */
    public long getActionTypeLogCount(AuditLog.ActionType actionType) {
        return auditLogRepository.countByActionType(actionType);
    }
    
    /**
     * Supprime les anciens logs (plus de 90 jours)
     */
    public void cleanOldLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        List<AuditLog> oldLogs = auditLogRepository.findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime.of(2020, 1, 1, 0, 0), cutoffDate);
        auditLogRepository.deleteAll(oldLogs);
    }
    
    /**
     * Méthodes utilitaires pour les actions courantes
     */
    
    public void logClubCreation(String userEmail, String clubName, String clubId, HttpServletRequest request) {
        String action = String.format("Création du club '%s'", clubName);
        logAction(userEmail, action, AuditLog.ActionType.CREATE, AuditLog.EntityType.CLUB, clubId, request);
    }
    
    public void logClubDeletion(String userEmail, String clubName, String clubId, HttpServletRequest request) {
        String action = String.format("Suppression du club '%s'", clubName);
        logAction(userEmail, action, AuditLog.ActionType.DELETE, AuditLog.EntityType.CLUB, clubId, request);
    }
    
    public void logEventCreation(String userEmail, String eventTitle, String eventId, HttpServletRequest request) {
        String action = String.format("Création de l'événement '%s'", eventTitle);
        logAction(userEmail, action, AuditLog.ActionType.CREATE, AuditLog.EntityType.EVENT, eventId, request);
    }
    
    public void logEventCancellation(String userEmail, String eventTitle, String eventId, HttpServletRequest request) {
        String action = String.format("Annulation de l'événement '%s'", eventTitle);
        logAction(userEmail, action, AuditLog.ActionType.CANCEL, AuditLog.EntityType.EVENT, eventId, request);
    }
    
    public void logStudentRegistration(String userEmail, String studentName, String clubName, String inscriptionId, HttpServletRequest request) {
        String action = String.format("Inscription de '%s' au club '%s'", studentName, clubName);
        logAction(userEmail, action, AuditLog.ActionType.REGISTER, AuditLog.EntityType.INSCRIPTION, inscriptionId, request);
    }
    
    public void logStudentUnregistration(String userEmail, String studentName, String clubName, String inscriptionId, HttpServletRequest request) {
        String action = String.format("Désinscription de '%s' du club '%s'", studentName, clubName);
        logAction(userEmail, action, AuditLog.ActionType.UNREGISTER, AuditLog.EntityType.INSCRIPTION, inscriptionId, request);
    }
    
    public void logAdminCreation(String userEmail, String adminEmail, String adminId, HttpServletRequest request) {
        String action = String.format("Ajout de l'administrateur '%s'", adminEmail);
        logAction(userEmail, action, AuditLog.ActionType.CREATE, AuditLog.EntityType.ADMIN, adminId, request);
    }
    
    public void logAdminDeletion(String userEmail, String adminEmail, String adminId, HttpServletRequest request) {
        String action = String.format("Suppression de l'administrateur '%s'", adminEmail);
        logAction(userEmail, action, AuditLog.ActionType.DELETE, AuditLog.EntityType.ADMIN, adminId, request);
    }
    
    /**
     * Obtient l'adresse IP du client
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
