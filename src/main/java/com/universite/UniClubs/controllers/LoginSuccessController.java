package com.universite.UniClubs.controllers;

import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.services.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginSuccessController {

    @GetMapping("/login-success")
    public String handleLoginSuccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Role userRole = userDetails.getUtilisateur().getRole();
            
            // Rediriger les administrateurs vers le dashboard admin
            if (userRole == Role.ADMIN || userRole == Role.SUPER_ADMIN) {
                return "redirect:/admin";
            }
        }
        
        // Rediriger tous les autres utilisateurs vers l'accueil normal
        return "redirect:/accueil";
    }
}
