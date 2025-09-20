package com.universite.UniClubs.controllers;


import com.universite.UniClubs.dto.UserRegistrationDto;
import com.universite.UniClubs.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {


    @Autowired
    private UtilisateurService utilisateurService;

    //méthode pour afficher le formulaire d'inscription
    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    //Méthode pour traiter la soumission du formulaire d'inscription
    //il prend les données envoyés par le formulaire et les utilise pour remplir UserRegistrationDto
    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") UserRegistrationDto registrationDto){
        utilisateurService.creerEtudiant(registrationDto);
        return "redirect:/register?success";
    }

    //Méthode pour afficher la page de login
    @GetMapping("/login")
    public String showLoginForm(){
        return "login";
    }

}
