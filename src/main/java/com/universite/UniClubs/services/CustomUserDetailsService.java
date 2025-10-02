package com.universite.UniClubs.services;


import com.universite.UniClubs.entities.Utilisateur;
import com.universite.UniClubs.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

// Dans CustomUserDetailsService.java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        //Utilisateur utilisateur = utilisateurRepository.findByEmailWithAllDetails(email)
                //.orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email:" + email));
        Utilisateur utilisateur = utilisateurRepository.findByEmailForSecurity(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'email:" + email));

        // On retourne notre nouvel objet qui contient l'entité complète
        return new CustomUserDetails(utilisateur);
    }
}