package com.universite.UniClubs.dto;

import lombok.Data;

@Data
public class UserProfileDto {
    // On ne met que les champs que l'utilisateur a le droit de modifier
    private String nom;
    private String prenom;
    private String bio;
}
