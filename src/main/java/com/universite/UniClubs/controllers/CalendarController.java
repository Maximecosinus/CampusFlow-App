package com.universite.UniClubs.controllers;


import com.universite.UniClubs.services.EvenementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/calendrier")
public class CalendarController {

    @Autowired
    private EvenementService evenementService;

    @GetMapping
    public  String showCalendarPage(Model model){
        model.addAttribute("evenements",
                evenementService.findAllUpcomingEvents());
        return "calendrier";
    }

}
