package com.universite.UniClubs.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {


    @GetMapping("/acceuil")
    public String showHomePage() {
        return "acceuil";
    }
}
