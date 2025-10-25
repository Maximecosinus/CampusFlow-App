package com.universite.UniClubs.config;

import com.universite.UniClubs.entities.Role;
import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.services.CustomUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        
        if (authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Utilisateur utilisateur = userDetails.getUtilisateur();
            
            // Redirection basée sur le rôle
            if (utilisateur.getRole() == Role.SUPER_ADMIN) {
                response.sendRedirect("/super-admin");
            } else if (utilisateur.getRole() == Role.ADMIN) {
                response.sendRedirect("/admin");
            } else if (utilisateur.getRole() == Role.CHEF_DE_CLUB) {
                response.sendRedirect("/gestion-club");
            } else if (utilisateur.getRole() == Role.ETUDIANT) {
                // Rôle ETUDIANT - redirection vers la page d'accueil
                response.sendRedirect("/");
            } else {
                // Autres rôles
                response.sendRedirect("/");
            }
        } else {
            // Fallback par défaut
            response.sendRedirect("/");
        }
    }
}
