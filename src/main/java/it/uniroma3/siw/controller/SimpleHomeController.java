package it.uniroma3.siw.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;

@Controller
public class SimpleHomeController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleHomeController.class);

    @GetMapping("/simple-home")
    public String simpleHome(Model model) {
        try {
            logger.info("Loading simple homepage...");
            
            // Aggiungi valori di default senza accesso al database
            model.addAttribute("latestBooks", new ArrayList<>());
            model.addAttribute("popularAuthors", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("totalBooks", 0L);
            model.addAttribute("totalAuthors", 0L);
            model.addAttribute("totalCategories", 0L);
            model.addAttribute("isAuthenticated", false);
            
            logger.info("Simple homepage loaded successfully");
            return "index";
            
        } catch (Exception e) {
            logger.error("Error loading simple homepage: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento della homepage: " + e.getMessage());
            return "error";
        }
    }
}
