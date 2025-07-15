package it.uniroma3.siw.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.siw.dto.RatingDTO;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Rating;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.repository.UsersRepository;
import it.uniroma3.siw.service.RatingService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/ratings")
public class RatingController {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    
    @Autowired
    private RatingService ratingService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    /**
     * Ottiene l'utente autenticato
     */
    private Users getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            try {
                // Prova prima il cast diretto
                if (auth.getPrincipal() instanceof Users users) {
                    return users;
                }
                // Se non funziona, cerca per username nel database
                String username = auth.getName();
                return usersRepository.findByUsername(username).orElse(null);
            } catch (ClassCastException e) {
                logger.error("Error casting principal to Users: {}", e.getMessage());
                // Fallback: cerca per username
                String username = auth.getName();
                return usersRepository.findByUsername(username).orElse(null);
            }
        }
        return null;
    }
    
    /**
     * Aggiunge attributi di autenticazione al modello
     */
    private void addAuthenticationAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && auth != null) {
            model.addAttribute("username", auth.getName());
        }
    }
    
    /**
     * Visualizza i rating di un prodotto
     */
    @GetMapping("/product/{productId}")
    @Transactional(readOnly = true)
    public String showProductRatings(@PathVariable Long productId, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            List<Rating> ratings = ratingService.findByProductId(productId);
            Double averageRating = ratingService.findAverageRatingByProductId(productId);
            Integer ratingCount = ratingService.countRatingsByProductId(productId);
            
            // Controlla se l'utente ha già inserito un rating
            Users authenticatedUser = getAuthenticatedUser();
            boolean hasUserRated = false;
            Rating userRating = null;
            
            if (authenticatedUser != null) {
                Optional<Rating> userRatingOpt = ratingService.findByProductIdAndUserId(productId, authenticatedUser.getId());
                hasUserRated = userRatingOpt.isPresent();
                userRating = userRatingOpt.orElse(null);
            }
            
            model.addAttribute("product", product);
            model.addAttribute("ratings", ratings);
            model.addAttribute("averageRating", averageRating != null ? averageRating : 0.0);
            model.addAttribute("ratingCount", ratingCount != null ? ratingCount : 0);
            model.addAttribute("hasUserRated", hasUserRated);
            model.addAttribute("userRating", userRating);
            
            // Inizializza il DTO con productId preimpostato
            RatingDTO ratingDTO = new RatingDTO();
            ratingDTO.setProductId(productId);
            if (userRating != null) {
                ratingDTO.setValue(userRating.getValue());
                ratingDTO.setComment(userRating.getComment());
            }
            model.addAttribute("ratingDTO", ratingDTO);
            
            return "product-ratings";
        } catch (Exception e) {
            logger.error("Error showing product ratings: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore nel caricamento delle valutazioni: " + e.getMessage());
            return "error";
        }
    }
    
    /**
     * Aggiunge o aggiorna un rating
     */
    @PostMapping("/submit")
    public String submitRating(@Valid RatingDTO ratingDTO, BindingResult bindingResult, Model model) {
        addAuthenticationAttributes(model);
        
        // Verifica se l'utente è autenticato
        Users authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            model.addAttribute("errorMessage", "Devi essere autenticato per valutare un prodotto.");
            return "error";
        }
        
        logger.info("Submitting rating for product ID: {} with value: {}", ratingDTO.getProductId(), ratingDTO.getValue());
        
        // Verifica se il prodotto esiste
        Optional<Product> productOpt = productRepository.findById(ratingDTO.getProductId());
        if (productOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Prodotto non trovato.");
            return "error";
        }
        
        Product product = productOpt.get();
        
        // Verifica che l'utente non stia valutando un proprio prodotto
        if (product.getSeller() != null && product.getSeller().getId().equals(authenticatedUser.getId())) {
            model.addAttribute("errorMessage", "Non puoi valutare i tuoi prodotti.");
            return "error";
        }
        
        if (bindingResult.hasErrors()) {
            logger.warn("Binding errors detected: {}", bindingResult.getAllErrors());
            model.addAttribute("product", product);
            model.addAttribute("ratings", ratingService.findByProductId(product.getId()));
            model.addAttribute("averageRating", ratingService.findAverageRatingByProductId(product.getId()));
            model.addAttribute("ratingCount", ratingService.countRatingsByProductId(product.getId()));
            model.addAttribute("hasUserRated", ratingService.hasUserRatedProduct(product, authenticatedUser));
            return "product-ratings";
        }
        
        try {
            // Salva il rating - questa operazione è transazionale nel service
            Rating savedRating = ratingService.saveRating(ratingDTO, authenticatedUser);
            logger.info("Rating saved successfully with ID: {}", savedRating.getId());
            
            return "redirect:/product/details/" + product.getId();
        } catch (Exception e) {
            logger.error("Error submitting rating: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'invio della valutazione: " + e.getMessage());
            model.addAttribute("product", product);
            return "error";
        }
    }
    
    /**
     * Elimina un rating
     */
    @DeleteMapping("/{ratingId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> deleteRating(@PathVariable Long ratingId) {
        try {
            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                return ResponseEntity.status(401).body("Devi essere autenticato per eliminare una valutazione.");
            }
            
            ratingService.deleteRating(ratingId, authenticatedUser);
            return ResponseEntity.ok("Valutazione eliminata con successo.");
        } catch (Exception e) {
            logger.error("Error deleting rating: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Errore durante l'eliminazione della valutazione: " + e.getMessage());
        }
    }
}
