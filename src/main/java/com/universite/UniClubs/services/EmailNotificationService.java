package com.universite.UniClubs.services;

import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.Evenement;
import com.universite.UniClubs.entities.Utilisateur;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmailNotificationService {

    /**
     * Envoie une notification par email à tous les membres actifs du club
     * pour les informer d'un nouvel événement publié
     */
    public void notifyClubMembersAboutNewEvent(Evenement evenement, Club club) {
        // Récupérer tous les membres actifs du club
        List<Utilisateur> activeMembers = club.getInscriptions().stream()
                .filter(inscription -> inscription.getStatut().name().equals("ACCEPTE"))
                .map(inscription -> inscription.getUtilisateur())
                .toList();

        // Pour chaque membre, envoyer un email de notification
        for (Utilisateur member : activeMembers) {
            sendEventNotificationEmail(member, evenement, club);
        }
    }

    /**
     * Envoie un email de notification à un membre spécifique
     */
    private void sendEventNotificationEmail(Utilisateur member, Evenement evenement, Club club) {
        // TODO: Implémenter l'envoi d'email réel
        // Pour l'instant, on simule l'envoi avec un log
        System.out.println("=== EMAIL NOTIFICATION ===");
        System.out.println("À: " + member.getEmail());
        System.out.println("Sujet: Nouvel événement dans votre club " + club.getNom());
        System.out.println("Contenu:");
        System.out.println("Bonjour " + member.getPrenom() + ",");
        System.out.println();
        System.out.println("Un nouvel événement a été organisé dans votre club " + club.getNom() + ":");
        System.out.println();
        System.out.println("Titre: " + evenement.getTitre());
        System.out.println("Date: " + evenement.getDateHeureDebut());
        System.out.println("Lieu: " + (evenement.getLieu() != null ? evenement.getLieu() : "Non spécifié"));
        if (evenement.getDescription() != null && !evenement.getDescription().isEmpty()) {
            System.out.println("Description: " + evenement.getDescription());
        }
        System.out.println();
        System.out.println("Connectez-vous à votre espace pour plus de détails.");
        System.out.println("=== FIN EMAIL ===");
        System.out.println();
    }
}
