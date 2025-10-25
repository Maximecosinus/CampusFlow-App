// Créez ce nouveau fichier : StatutEvenement.java
package com.universite.UniClubs.entities;

public enum StatutEvenement {
    BROUILLON, // L'événement est en cours de création, non visible
    PUBLIE,    // L'événement est visible par tout le monde
    ANNULE,    // L'événement a été annulé
    TERMINE    // L'événement est passé
}