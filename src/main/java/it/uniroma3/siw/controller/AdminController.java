package it.uniroma3.siw.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private UsersRepository usersRepository;
    
    private void addAuthenticationAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && auth != null) {
            model.addAttribute("username", auth.getName());
            
            // Aggiungi anche il ruolo dell'utente
            Users user = getAuthenticatedUser();
            if (user != null) {
                model.addAttribute("userRole", user.getRole());
            }
        }
    }
    
    private Users getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            return usersRepository.findByUsername(auth.getName()).orElse(null);
        }
        return null;
    }
    
    private boolean isAdmin() {
        Users user = getAuthenticatedUser();
        return user != null && "ADMIN".equals(user.getRole());
    }
    
    // Dashboard admin
    @GetMapping
    public String showAdminDashboard(Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono accedere a questa sezione.");
            return "error";
        }
        
        try {
            addAuthenticationAttributes(model);
            
            // Carica tutti gli utenti
            List<Users> allUsers = usersRepository.findAll();
            model.addAttribute("allUsers", allUsers);
            
            // Conta admin e utenti normali
            long adminCount = allUsers.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
            long userCount = allUsers.stream().filter(u -> "USER".equals(u.getRole())).count();
            
            model.addAttribute("adminCount", adminCount);
            model.addAttribute("userCount", userCount);
            model.addAttribute("totalUsers", allUsers.size());
            
            logger.info("Admin dashboard loaded with {} users ({} admins, {} users)", 
                       allUsers.size(), adminCount, userCount);
            
            return "admin-dashboard";
            
        } catch (Exception e) {
            logger.error("Error loading admin dashboard: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore nel caricamento della dashboard admin.");
            return "error";
        }
    }
    
    // Promuovi utente ad admin
    @PostMapping("/promote/{userId}")
    public String promoteToAdmin(@PathVariable Long userId, Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono promuovere utenti.");
            return "error";
        }
        
        try {
            Optional<Users> userOpt = usersRepository.findById(userId);
            if (userOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Utente non trovato.");
                return "error";
            }
            
            Users user = userOpt.get();
            if ("ADMIN".equals(user.getRole())) {
                model.addAttribute("errorMessage", "L'utente è già un amministratore.");
                return "redirect:/admin";
            }
            
            user.setRole("ADMIN");
            usersRepository.save(user);
            
            logger.info("User {} promoted to admin by {}", user.getUsername(), getAuthenticatedUser().getUsername());
            model.addAttribute("successMessage", "Utente " + user.getUsername() + " promosso ad amministratore con successo.");
            
            return "redirect:/admin";
            
        } catch (Exception e) {
            logger.error("Error promoting user to admin: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante la promozione dell'utente.");
            return "error";
        }
    }
    
    // Rimuovi admin (degradalo a utente normale)
    @PostMapping("/demote/{userId}")
    public String demoteFromAdmin(@PathVariable Long userId, Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono degradare altri admin.");
            return "error";
        }
        
        try {
            Optional<Users> userOpt = usersRepository.findById(userId);
            if (userOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Utente non trovato.");
                return "error";
            }
            
            Users user = userOpt.get();
            Users currentAdmin = getAuthenticatedUser();
            
            // Non può degradare se stesso
            if (user.getId().equals(currentAdmin.getId())) {
                model.addAttribute("errorMessage", "Non puoi degradare te stesso.");
                return "redirect:/admin";
            }
            
            if (!"ADMIN".equals(user.getRole())) {
                model.addAttribute("errorMessage", "L'utente non è un amministratore.");
                return "redirect:/admin";
            }
            
            // Controlla che rimanga almeno un admin
            long adminCount = usersRepository.findAll().stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .count();
            
            if (adminCount <= 1) {
                model.addAttribute("errorMessage", "Non è possibile rimuovere l'ultimo amministratore del sistema.");
                return "redirect:/admin";
            }
            
            user.setRole("USER");
            usersRepository.save(user);
            
            logger.info("User {} demoted from admin by {}", user.getUsername(), currentAdmin.getUsername());
            model.addAttribute("successMessage", "Amministratore " + user.getUsername() + " degradato a utente normale.");
            
            return "redirect:/admin";
            
        } catch (Exception e) {
            logger.error("Error demoting admin: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante la rimozione dei privilegi di amministratore.");
            return "error";
        }
    }
}
