package it.uniroma3.siw.controller;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import it.uniroma3.siw.dto.ReviewFormDTO;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Rating;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.repository.RatingRepository;
import it.uniroma3.siw.repository.UsersRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/review")
public class ReviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    private void addAuthenticationAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && auth != null) {
            model.addAttribute("username", auth.getName());
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
    
    // Form per scrivere una recensione
    @GetMapping("/create/{libroId}")
    public String showCreateReviewForm(@PathVariable Long libroId, Model model) {
        Users authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            model.addAttribute("errorMessage", "Devi essere registrato per scrivere una recensione.");
            return "error";
        }
        
        try {
            addAuthenticationAttributes(model);
            
            Optional<Product> libroOpt = productRepository.findById(libroId);
            if (libroOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato.");
                return "error";
            }
            
            Product libro = libroOpt.get();
            
            // Controlla se l'utente ha già recensito questo libro
            Optional<Rating> existingReview = ratingRepository.findByProductAndUser(libro, authenticatedUser);
            if (existingReview.isPresent()) {
                model.addAttribute("errorMessage", "Hai già recensito questo libro. Ogni utente può scrivere al massimo una recensione per libro.");
                return "error";
            }
            
            ReviewFormDTO reviewFormDTO = new ReviewFormDTO();
            reviewFormDTO.setLibroId(libroId);
            
            model.addAttribute("reviewFormDTO", reviewFormDTO);
            model.addAttribute("libro", libro);
            
            logger.debug("Displaying review creation form for book ID: {}", libroId);
            return "review-create"; // Creeremo questo template
            
        } catch (Exception e) {
            logger.error("Error loading review form: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento del form di recensione.");
            return "error";
        }
    }
    
    // Gestisce la creazione di una nuova recensione
    @PostMapping("/create")
    @Transactional
    public String handleCreateReview(@Valid @ModelAttribute ReviewFormDTO reviewFormDTO,
                                   BindingResult result, Model model) {
        Users authenticatedUser = getAuthenticatedUser();
        if (authenticatedUser == null) {
            model.addAttribute("errorMessage", "Devi essere registrato per scrivere una recensione.");
            return "error";
        }
        
        try {
            addAuthenticationAttributes(model);
            
            if (result.hasErrors()) {
                // Ricarica il libro per il form
                Optional<Product> libroOpt = productRepository.findById(reviewFormDTO.getLibroId());
                if (libroOpt.isPresent()) {
                    model.addAttribute("libro", libroOpt.get());
                }
                return "review-create";
            }
            
            Optional<Product> libroOpt = productRepository.findById(reviewFormDTO.getLibroId());
            if (libroOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato.");
                return "error";
            }
            
            Product libro = libroOpt.get();
            
            // Verifica ancora che l'utente non abbia già recensito
            Optional<Rating> existingReview = ratingRepository.findByProductAndUser(libro, authenticatedUser);
            if (existingReview.isPresent()) {
                model.addAttribute("errorMessage", "Hai già recensito questo libro.");
                return "error";
            }
            
            // Crea la nuova recensione
            Rating review = new Rating();
            review.setTitolo(reviewFormDTO.getTitolo());
            review.setValue(reviewFormDTO.getVoto());
            review.setComment(reviewFormDTO.getTesto());
            review.setProduct(libro);
            review.setUser(authenticatedUser);
            
            Rating savedReview = ratingRepository.save(review);
            
            logger.info("Review created successfully with ID: {} for book: {}", savedReview.getId(), libro.getId());
            return "redirect:/product/details/" + libro.getId();
            
        } catch (Exception e) {
            logger.error("Error creating review: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante la creazione della recensione: " + e.getMessage());
            return "review-create";
        }
    }
    
    // Visualizza una recensione specifica
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String showReviewDetails(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Optional<Rating> reviewOpt = ratingRepository.findById(id);
            if (reviewOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Recensione non trovata.");
                return "error";
            }
            
            Rating review = reviewOpt.get();
            
            Users authenticatedUser = getAuthenticatedUser();
            boolean isOwner = authenticatedUser != null && review.getUser().getId().equals(authenticatedUser.getId());
            
            model.addAttribute("review", review);
            model.addAttribute("isOwner", isOwner);
            model.addAttribute("isAdmin", isAdmin());
            
            logger.debug("Displaying review details for ID: {}", id);
            return "review-details"; // Creeremo questo template
            
        } catch (Exception e) {
            logger.error("Error loading review details: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento dei dettagli della recensione.");
            return "error";
        }
    }
    
    // Elimina una recensione (solo admin o proprietario)
    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteReview(@PathVariable Long id, Model model) {
        try {
            Optional<Rating> reviewOpt = ratingRepository.findById(id);
            if (reviewOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Recensione non trovata.");
                return "error";
            }
            
            Rating review = reviewOpt.get();
            Users authenticatedUser = getAuthenticatedUser();
            
            // Solo l'amministratore può cancellare le recensioni
            if (!isAdmin()) {
                model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono cancellare le recensioni.");
                return "error";
            }
            
            Long libroId = review.getProduct().getId();
            
            ratingRepository.delete(review);
            
            logger.info("Review deleted successfully with ID: {} by admin: {}", id, authenticatedUser.getUsername());
            return "redirect:/product/details/" + libroId;
            
        } catch (Exception e) {
            logger.error("Error deleting review: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'eliminazione della recensione: " + e.getMessage());
            return "error";
        }
    }
    
    // Lista tutte le recensioni di un libro
    @GetMapping("/book/{libroId}")
    @Transactional(readOnly = true)
    public String showBookReviews(@PathVariable Long libroId, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Optional<Product> libroOpt = productRepository.findById(libroId);
            if (libroOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Libro non trovato.");
                return "error";
            }
            
            Product libro = libroOpt.get();
            List<Rating> reviews = ratingRepository.findByProduct(libro);
            // Ordiniamo manualmente per data di creazione decrescente
            reviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
            
            model.addAttribute("libro", libro);
            model.addAttribute("reviews", reviews);
            model.addAttribute("isAdmin", isAdmin());
            
            logger.debug("Displaying {} reviews for book ID: {}", reviews.size(), libroId);
            return "book-reviews"; // Creeremo questo template
            
        } catch (Exception e) {
            logger.error("Error loading book reviews: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento delle recensioni del libro.");
            return "error";
        }
    }
}
