package com.universite.UniClubs.entities;

public enum TypeNotification {
    CLUB("Club"),
    EVENEMENT("Événement"),
    INSCRIPTION("Inscription"),
    ADMIN("Administration"),
    SYSTEME("Système");

    private final String displayName;

    TypeNotification(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
