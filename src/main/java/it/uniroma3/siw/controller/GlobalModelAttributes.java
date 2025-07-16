package it.uniroma3.siw.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        // Inizializza sempre tutti gli attributi con valori di default
        model.addAttribute("isAuthenticated", false);
        model.addAttribute("isAdmin", false);
        model.addAttribute("username", "");
        
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth != null && auth.isAuthenticated() && 
                auth.getName() != null && !("anonymousUser".equals(auth.getName()))) {
                
                model.addAttribute("isAuthenticated", true);
                model.addAttribute("username", auth.getName());
                
                // Verifica se l'utente è admin
                if (auth.getAuthorities() != null) {
                    boolean isAdmin = auth.getAuthorities().stream()
                        .anyMatch(grantedAuthority -> "ROLE_ADMIN".equals(grantedAuthority.getAuthority()));
                    model.addAttribute("isAdmin", isAdmin);
                }
            }
        } catch (Exception e) {
            // In caso di qualsiasi errore, mantieni i valori di default
            System.err.println("Error in GlobalModelAttributes: " + e.getMessage());
        }
    }
}
