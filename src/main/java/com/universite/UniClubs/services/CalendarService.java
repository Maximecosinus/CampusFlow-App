package com.universite.UniClubs.services;

import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Evenement;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service pour la génération de données calendrier au format ICS
 */
@Service
public class CalendarService {

    private static final String LINE_SEPARATOR = "\r\n";
    private static final DateTimeFormatter ICS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    /**
     * Génère le contenu ICS pour tous les événements d'un club
     * @param club Le club dont on veut exporter les événements
     * @return Le contenu ICS formaté
     */
    public String generateICSContent(Club club) {
        StringBuilder icsContent = new StringBuilder();
        
        // En-tête ICS
        icsContent.append("BEGIN:VCALENDAR").append(LINE_SEPARATOR);
        icsContent.append("VERSION:2.0").append(LINE_SEPARATOR);
        icsContent.append("PRODID:-//CampusFlow//Club Calendar//FR").append(LINE_SEPARATOR);
        icsContent.append("CALSCALE:GREGORIAN").append(LINE_SEPARATOR);
        icsContent.append("METHOD:PUBLISH").append(LINE_SEPARATOR);
        
        // Informations du calendrier
        icsContent.append("X-WR-CALNAME:").append(escapeText(club.getNom())).append(" - Calendrier").append(LINE_SEPARATOR);
        icsContent.append("X-WR-CALDESC:Événements du club ").append(escapeText(club.getNom())).append(LINE_SEPARATOR);
        icsContent.append("X-WR-TIMEZONE:Europe/Paris").append(LINE_SEPARATOR);
        
        // Ajouter chaque événement
        if (club.getEvenementsOrganises() != null) {
            for (Evenement evenement : club.getEvenementsOrganises()) {
                // Ne pas inclure les événements en brouillon
                if (evenement.getStatut() != null && 
                    evenement.getStatut().name().equals("PUBLIE")) {
                    icsContent.append(generateEventICS(evenement, club));
                }
            }
        }
        
        // Pied de page ICS
        icsContent.append("END:VCALENDAR").append(LINE_SEPARATOR);
        
        return icsContent.toString();
    }

    /**
     * Génère le contenu ICS pour un événement spécifique
     * @param evenement L'événement à convertir
     * @param club Le club propriétaire de l'événement
     * @return Le contenu ICS de l'événement
     */
    private String generateEventICS(Evenement evenement, Club club) {
        StringBuilder eventContent = new StringBuilder();
        
        // Début de l'événement
        eventContent.append("BEGIN:VEVENT").append(LINE_SEPARATOR);
        
        // ID unique de l'événement
        eventContent.append("UID:").append(evenement.getId().toString()).append("@campusflow.app").append(LINE_SEPARATOR);
        
        // Date de création/modification
        LocalDateTime now = LocalDateTime.now();
        eventContent.append("DTSTAMP:").append(formatICSDate(now)).append(LINE_SEPARATOR);
        
        // Titre de l'événement
        eventContent.append("SUMMARY:").append(escapeText(evenement.getTitre())).append(LINE_SEPARATOR);
        
        // Description
        if (evenement.getDescription() != null && !evenement.getDescription().trim().isEmpty()) {
            eventContent.append("DESCRIPTION:").append(escapeText(evenement.getDescription())).append(LINE_SEPARATOR);
        }
        
        // Lieu
        if (evenement.getLieu() != null && !evenement.getLieu().trim().isEmpty()) {
            eventContent.append("LOCATION:").append(escapeText(evenement.getLieu())).append(LINE_SEPARATOR);
        }
        
        // Date de début
        if (evenement.getDateHeureDebut() != null) {
            eventContent.append("DTSTART:").append(formatICSDate(evenement.getDateHeureDebut())).append(LINE_SEPARATOR);
        }
        
        // Date de fin (on peut estimer une durée par défaut si pas spécifiée)
        LocalDateTime dateFin = null;
        if (evenement.getDateHeureDebut() != null) {
            // Durée par défaut de 2 heures
            dateFin = evenement.getDateHeureDebut().plusHours(2);
            eventContent.append("DTEND:").append(formatICSDate(dateFin)).append(LINE_SEPARATOR);
        }
        
        // Statut
        eventContent.append("STATUS:CONFIRMED").append(LINE_SEPARATOR);
        
        // Catégorie
        eventContent.append("CATEGORIES:CLUB_EVENT").append(LINE_SEPARATOR);
        
        // Informations supplémentaires
        eventContent.append("X-CLUB:").append(escapeText(club.getNom())).append(LINE_SEPARATOR);
        eventContent.append("X-CLUB-ID:").append(club.getId().toString()).append(LINE_SEPARATOR);
        
        // Capacité maximale si définie
        if (evenement.getCapaciteMax() != null && evenement.getCapaciteMax() > 0) {
            eventContent.append("X-CAPACITY:").append(evenement.getCapaciteMax()).append(LINE_SEPARATOR);
        }
        
        // Fin de l'événement
        eventContent.append("END:VEVENT").append(LINE_SEPARATOR);
        
        return eventContent.toString();
    }

    /**
     * Formate une date LocalDateTime au format ICS
     * @param dateTime La date à formater
     * @return La date formatée au format ICS
     */
    private String formatICSDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(ICS_DATE_FORMAT);
    }

    /**
     * Échappe le texte pour le format ICS
     * @param text Le texte à échapper
     * @return Le texte échappé
     */
    private String escapeText(String text) {
        if (text == null) {
            return "";
        }
        
        return text
            .replace("\\", "\\\\")
            .replace(",", "\\,")
            .replace(";", "\\;")
            .replace("\n", "\\n")
            .replace("\r", "");
    }

    /**
     * Génère un nom de fichier ICS pour un club
     * @param club Le club
     * @return Le nom de fichier suggéré
     */
    public String generateICSFileName(Club club) {
        String clubName = club.getNom()
            .replaceAll("[^a-zA-Z0-9\\s]", "")
            .replaceAll("\\s+", "_")
            .toLowerCase();
        
        return "calendrier_" + clubName + ".ics";
    }
}
