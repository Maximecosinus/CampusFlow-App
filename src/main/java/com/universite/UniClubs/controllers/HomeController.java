package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.ClubService;
import com.universite.UniClubs.services.EvenementService;
import com.universite.UniClubs.services.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class HomeController {


    @Autowired
    private ClubService clubService;

    @Autowired
    private EvenementService evenementService;

    @GetMapping("/")
    public String redirectToHome(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
                if (utilisateur.getRole().name().equals("ADMIN") || utilisateur.getRole().name().equals("SUPER_ADMIN")) {
                    return "redirect:/admin/accueil";
                }
            }
        }
        return "redirect:/accueil";
    }

    @GetMapping("/accueil")
    public String showHomePage(Model model, Authentication authentication) {
        // Vérifier si l'utilisateur est un admin et le rediriger
        if (authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof CustomUserDetails) {
                Utilisateur utilisateur = ((CustomUserDetails) authentication.getPrincipal()).getUtilisateur();
                if (utilisateur.getRole().name().equals("ADMIN") || utilisateur.getRole().name().equals("SUPER_ADMIN")) {
                    return "redirect:/admin/accueil";
                }
            }
        }

        model.addAttribute("clubs", clubService.getAllClubs());
        model.addAttribute("clubsRecents", clubService.findRecentclubs());
        model.addAttribute("evenementsAVenir",evenementService.findUpcomingEvents());
        return "accueil";
    }
}
