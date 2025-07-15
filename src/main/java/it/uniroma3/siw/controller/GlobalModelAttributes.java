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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth.getName() != null && auth.getName().equals("anonymousUser"));
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && auth != null && auth.getName() != null) {
            model.addAttribute("username", auth.getName());
        }
    }
}
