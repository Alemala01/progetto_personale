package it.uniroma3.siw.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.service.SavedBookService;
import it.uniroma3.siw.service.UsersService;

@Controller
@RequestMapping("/saved-books")
public class SavedBookController {
    
    private static final Logger logger = LoggerFactory.getLogger(SavedBookController.class);
    
    @Autowired
    private SavedBookService savedBookService;
    
    @Autowired
    private UsersService usersService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @GetMapping("/test")
    @ResponseBody
    public String testEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return "Test endpoint working. User: " + (auth != null ? auth.getName() : "null") + 
               ", Role: " + (auth != null ? auth.getAuthorities() : "null");
    }
    
    @GetMapping("/debug")
    public String debugSavedBooks(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Users user = usersService.findByUsername(username);
            
            List<Product> savedProducts = savedBookService.getUserSavedProducts(user);
            long totalSavedBooks = savedBookService.countUserSavedBooks(user);
            
            model.addAttribute("savedProducts", savedProducts);
            model.addAttribute("totalSavedBooks", totalSavedBooks);
            model.addAttribute("user", user);
            
            logger.info("DEBUG: User: {}, Role: {}, Saved count: {}", 
                       user.getUsername(), user.getRole(), savedProducts.size());
            
            return "saved-books-debug";
            
        } catch (Exception e) {
            logger.error("Error in debug endpoint", e);
            return "error";
        }
    }
    
    @GetMapping("/minimal")
    public String minimalTest(Model model) {
        logger.info("MINIMAL TEST CALLED");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        model.addAttribute("username", username);
        model.addAttribute("message", "Minimal test working for admin");
        
        return "saved-books-minimal";
    }
    
    @GetMapping
    public String savedBooks(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            logger.info("Authentication object: {}", auth);
            logger.info("Is authenticated: {}", auth != null ? auth.isAuthenticated() : "null");
            logger.info("Principal: {}", auth != null ? auth.getPrincipal() : "null");
            
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                logger.warn("User not authenticated, redirecting to login");
                return "redirect:/login";
            }
            
            String username = auth.getName();
            logger.info("Username from auth: {}", username);
            
            Users user = usersService.findByUsername(username);
            logger.info("User found: {}, Role: {}", user != null ? user.getUsername() : "null", user != null ? user.getRole() : "null");
            
            if (user == null) {
                logger.error("User not found: {}", username);
                return "redirect:/login";
            }
            
            List<Product> savedProducts = savedBookService.getUserSavedProducts(user);
            long totalSavedBooks = savedBookService.countUserSavedBooks(user);
            
            logger.info("Saved products count: {}, Total saved books: {}", savedProducts.size(), totalSavedBooks);
            
            model.addAttribute("savedProducts", savedProducts);
            model.addAttribute("totalSavedBooks", totalSavedBooks);
            model.addAttribute("user", user);
            
            logger.info("Showing saved books for user: {}, total: {}", username, totalSavedBooks);
            
            return "saved-books";
            
        } catch (Exception e) {
            logger.error("Error loading saved books", e);
            model.addAttribute("error", "Errore nel caricamento dei libri salvati");
            return "error";
        }
    }
    
    @PostMapping("/toggle/{productId}")
    @ResponseBody
    public ResponseEntity<?> toggleSavedBook(@PathVariable Long productId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return ResponseEntity.status(401).body("{\"error\": \"User not authenticated\"}");
            }
            
            String username = auth.getName();
            Users user = usersService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.status(401).body("{\"error\": \"User not found\"}");
            }
            
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.status(404).body("{\"error\": \"Product not found\"}");
            }
            
            boolean isSaved = savedBookService.toggleSavedBook(user, product);
            
            logger.info("User {} {} book {}", username, isSaved ? "saved" : "unsaved", productId);
            
            return ResponseEntity.ok("{\"saved\": " + isSaved + ", \"message\": \"" + 
                    (isSaved ? "Libro salvato nei preferiti" : "Libro rimosso dai preferiti") + "\"}");
            
        } catch (Exception e) {
            logger.error("Error toggling saved book", e);
            return ResponseEntity.status(500).body("{\"error\": \"Internal server error\"}");
        }
    }
    
    @GetMapping("/check/{productId}")
    @ResponseBody
    public ResponseEntity<?> checkSavedBook(@PathVariable Long productId) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return ResponseEntity.ok("{\"saved\": false, \"authenticated\": false}");
            }
            
            String username = auth.getName();
            Users user = usersService.findByUsername(username);
            
            if (user == null) {
                return ResponseEntity.ok("{\"saved\": false, \"authenticated\": false}");
            }
            
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return ResponseEntity.status(404).body("{\"error\": \"Product not found\"}");
            }
            
            boolean isSaved = savedBookService.isBookSaved(user, product);
            
            return ResponseEntity.ok("{\"saved\": " + isSaved + ", \"authenticated\": true}");
            
        } catch (Exception e) {
            logger.error("Error checking saved book", e);
            return ResponseEntity.status(500).body("{\"error\": \"Internal server error\"}");
        }
    }
}
