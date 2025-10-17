package com.universite.UniClubs.controllers;


import com.universite.UniClubs.entities.Club;
import com.universite.UniClubs.entities.StatutInscription;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.InscriptionService;
import com.universite.UniClubs.services.UtilisateurService;
import com.universite.UniClubs.services.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/clubs")
public class ClubController {


    @Autowired
    private ClubService clubService;
    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private InscriptionService inscriptionService; // Injecter le nouveau service

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/{id}")
    public String showCLubDetailsPage(@PathVariable UUID id, Model model, @ModelAttribute("utilisateurConnecte") Utilisateur utilisateurConnecte){

        // On récupère le statut de l'inscription
        Optional<StatutInscription> statutOpt = Optional.empty();
        if(utilisateurConnecte != null){
            statutOpt = inscriptionService.findStatutInscription(utilisateurConnecte.getEmail(), id);
        }

        // On passe le statut (ou null s'il n'y en a pas) à la vue
        model.addAttribute("statutInscription", statutOpt.orElse(null));
        boolean estDejaInscrit = false;
        if(utilisateurConnecte != null){
            estDejaInscrit = utilisateurService.estInscrit(utilisateurConnecte.getEmail(), id);
        }
        Club club = clubService.findClubWithDetailsById(id);
        //Ajoute le club trouvé au modèle pour l'envoyer à la vue
        model.addAttribute("club", club);
        model.addAttribute("estDejaInscrit", estDejaInscrit);
        return  "club-details";
    }

    @PostMapping("/{id}/inscrire")
    public String processInscriotion(@PathVariable UUID id, Principal principal, HttpServletRequest request ){
        //vérifier si l'utilisateur est connecté
        if(principal ==null){
            return "redirect:/login";
        }
        
        // Récupérer les informations pour l'audit
        Optional<Club> clubOpt = clubService.findClubById(id);
        Utilisateur user = utilisateurService.findByEmail(principal.getName());
        
        if (clubOpt.isPresent() && user != null) {
            Club club = clubOpt.get();
            // Enregistrer l'action dans le journal d'audit
            auditLogService.logStudentRegistration(user.getEmail(), user.getPrenom() + " " + user.getNom(), club.getNom(), id.toString(), request);
        }
        
        utilisateurService.inscrireUtilisateurAuclub(principal.getName(), id);
        return "redirect:/clubs/" + id;
    }

    @PostMapping("/{id}/desinscrire")
    public String processDesinscription(@PathVariable UUID id, Principal principal, HttpServletRequest request ){
        if(principal ==null){
            return "redirect:/login";
        }

        // Récupérer les informations pour l'audit
        Optional<Club> clubOpt = clubService.findClubById(id);
        Utilisateur user = utilisateurService.findByEmail(principal.getName());
        
        if (clubOpt.isPresent() && user != null) {
            Club club = clubOpt.get();
            // Enregistrer l'action dans le journal d'audit
            auditLogService.logStudentUnregistration(user.getEmail(), user.getPrenom() + " " + user.getNom(), club.getNom(), id.toString(), request);
        }

        utilisateurService.desinscrireUtilisateurDuClub(principal.getName(), id);
        return "redirect:/clubs/" + id;
    }
}
