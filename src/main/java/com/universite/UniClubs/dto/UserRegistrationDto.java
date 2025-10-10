package com.universite.UniClubs.dto;


import lombok.Data;

@Data
public class UserRegistrationDto {

    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
}
