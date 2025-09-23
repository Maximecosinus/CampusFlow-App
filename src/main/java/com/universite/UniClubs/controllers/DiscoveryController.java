package com.universite.UniClubs.controllers;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/decouvrir")
public class DiscoveryController {

    @GetMapping
    public String showDiscoveryPortal(){
        return "decouvrir";
    }
}
