package com.universite.UniClubs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Quand une URL commence par /uploads/**
        registry.addResourceHandler("/uploads/**")
                // Va chercher les fichiers dans le dossier "uploads" à la racine du projet
                .addResourceLocations("file:./uploads/");
    }
}