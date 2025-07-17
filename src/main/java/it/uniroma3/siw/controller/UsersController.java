package it.uniroma3.siw.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.dto.UsersDTO;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Rating;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.repository.RatingRepository;
import it.uniroma3.siw.service.UsersService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UsersService usersService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private RatingRepository ratingRepository;

    private void addAuthenticationAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");
        
        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && auth != null) {
            model.addAttribute("username", auth.getName());
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        addAuthenticationAttributes(model);
        model.addAttribute("usersDTO", new UsersDTO());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute UsersDTO usersDTO,
                              BindingResult bindingResult, Model model) {
        addAuthenticationAttributes(model);
        
        // Validazione personalizzata per conferma password
        if (usersDTO.getConfirmPassword() == null || 
            !usersDTO.getPassword().equals(usersDTO.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", 
                                    "Le password non coincidono");
        }
        
        // Controlla se l'username è già in uso
        if (usersService.findByUsername(usersDTO.getUsername()) != null) {
            bindingResult.rejectValue("username", "username.exists", 
                                    "Username già in uso");
        }
        
        if (bindingResult.hasErrors()) {
            return "register";
        }
        
        try {
            usersService.saveUser(usersDTO);
            model.addAttribute("successMessage", "Registrazione completata con successo! Ora puoi accedere.");
            return "redirect:/users/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante la registrazione. Riprova.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        addAuthenticationAttributes(model);
        return "login";
    }

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public String showProfilePage(Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                // Get the username
                String username = authentication.getName();
                
                // Retrieve the user from database directly instead of using principal
                Users user = usersService.findByUsername(username);
                
                if (user != null) {
                    model.addAttribute("user", user);
                    
                    // Carica le recensioni dell'utente invece dei prodotti
                    List<Rating> userReviews = ratingRepository.findByUser(user);
                    // Ordina per data di creazione decrescente
                    userReviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
                    
                    // Calcola statistiche delle recensioni
                    int totalReviews = userReviews.size();
                    double averageRating = 0.0;
                    if (!userReviews.isEmpty()) {
                        averageRating = userReviews.stream()
                            .mapToInt(Rating::getValue)
                            .average()
                            .orElse(0.0);
                    }
                    
                    // Trova i libri più recensiti dall'utente (conta le categorie più recensite)
                    Map<String, Integer> categoryCounts = new HashMap<>();
                    for (Rating review : userReviews) {
                        Product book = review.getProduct();
                        if (book.getCategory() != null) {
                            String categoryName = book.getCategory().getName();
                            categoryCounts.put(categoryName, categoryCounts.getOrDefault(categoryName, 0) + 1);
                        }
                    }
                    
                    // Trova la categoria più recensita
                    String favoriteCategory = categoryCounts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("Nessuna");
                    
                    // Prendi solo le ultime 6 recensioni per la dashboard
                    List<Rating> recentReviews = userReviews.stream()
                        .limit(6)
                        .collect(java.util.stream.Collectors.toList());
                    
                    model.addAttribute("userReviews", recentReviews);
                    model.addAttribute("totalReviews", totalReviews);
                    model.addAttribute("averageUserRating", averageRating);
                    model.addAttribute("favoriteCategory", favoriteCategory);
                    model.addAttribute("allUserReviews", userReviews); // Per eventuali altre sezioni
                    
                    // Aggiungi gli attributi flash se presenti
                    if (model.containsAttribute("passwordSuccess")) {
                        model.addAttribute("passwordTab", true);
                    }
                    if (model.containsAttribute("passwordError")) {
                        model.addAttribute("passwordTab", true);
                    }
                    
                    return "profile";
                }
            }
            
            // Se l'utente non è autenticato correttamente, reindirizza al login
            return "redirect:/users/login";
            
        } catch (Exception e) {
            // Log the error in a safe way
            System.err.println("Errore nel caricamento del profilo: " + e.getMessage());
            model.addAttribute("errorMessage", "Errore nel caricamento del profilo: " + e.getMessage());
            return "error";
        }
    }
    
    @GetMapping("/products")
    @Transactional(readOnly = true)
    public String showUserProducts(Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !authentication.getName().equals("anonymousUser")) {
                
                // Get the username
                String username = authentication.getName();
                
                // Retrieve the user from database directly
                Users user = usersService.findByUsername(username);
                
                if (user != null) {
                    model.addAttribute("products", productRepository.findBySellerWithImages(user));
                    model.addAttribute("user", user);
                    return "products";
                }
            }
            
            return "redirect:/users/login";
            
        } catch (Exception e) {
            // Log the error in a safe way
            System.err.println("Errore nel caricamento dei prodotti: " + e.getMessage());
            model.addAttribute("errorMessage", "Impossibile caricare i tuoi prodotti. Riprova più tardi.");
            return "error";
        }
    }

    @GetMapping("/seller/{username}")
    @Transactional(readOnly = true)
    public String showSellerProfile(@PathVariable String username, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            // Cerchiamo l'utente dal repository
            Users seller = usersService.findByUsername(username);
            
            // Se l'utente non esiste, redirect alla homepage
            if (seller == null) {
                return "redirect:/";
            }
            
            // Carica i prodotti dell'utente
            List<Product> sellerProducts = productRepository.findBySellerWithImages(seller);
            
            // Calcola il rating medio del venditore basato sui rating dei suoi prodotti
            double averageSellerRating = 0.0;
            int totalRatings = 0;
            
            // Carica i rating per ogni prodotto
            for (Product product : sellerProducts) {
                product.setRatings(productRepository.findRatingsForProduct(product.getId()));
                if (product.getRatingCount() > 0) {
                    averageSellerRating += product.getAverageRating() * product.getRatingCount();
                    totalRatings += product.getRatingCount();
                }
            }
            
            // Calcola la media ponderata dei rating
            if (totalRatings > 0) {
                averageSellerRating = averageSellerRating / totalRatings;
            }
            
            model.addAttribute("sellerUsername", username);
            model.addAttribute("sellerProducts", sellerProducts);
            model.addAttribute("averageSellerRating", averageSellerRating);
            model.addAttribute("totalSellerRatings", totalRatings);
            
            return "seller-profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Impossibile caricare il profilo del venditore. Riprova più tardi.");
            return "error";
        }
    }
    
    /**
     * Gestisce la richiesta di cambio password
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                return "redirect:/users/login";
            }
            
            // Get the username
            String username = authentication.getName();
            
            // Verifica che le due password coincidano
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("passwordError", "Le nuove password non coincidono");
                redirectAttributes.addFlashAttribute("passwordTab", true);
                return "redirect:/users/profile";
            }
            
            // Verifica complessità password (minimo 8 caratteri, una lettera maiuscola, un numero)
            if (newPassword.length() < 8 || !newPassword.matches(".*[A-Z].*") || !newPassword.matches(".*[0-9].*")) {
                redirectAttributes.addFlashAttribute("passwordError", 
                    "La password deve contenere almeno 8 caratteri, una lettera maiuscola e un numero");
                redirectAttributes.addFlashAttribute("passwordTab", true);
                return "redirect:/users/profile";
            }
            
            // Chiama il servizio per cambiare la password
            boolean success = usersService.changePassword(username, currentPassword, newPassword);
            
            if (success) {
                redirectAttributes.addFlashAttribute("passwordSuccess", "Password modificata con successo");
                redirectAttributes.addFlashAttribute("passwordTab", true);
            } else {
                redirectAttributes.addFlashAttribute("passwordError", "Password attuale non corretta");
                redirectAttributes.addFlashAttribute("passwordTab", true);
            }
            
            return "redirect:/users/profile";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("passwordError", "Errore durante il cambio password: " + e.getMessage());
            redirectAttributes.addFlashAttribute("passwordTab", true);
            return "redirect:/users/profile";
        }
    }
    
    /**
     * Gestisce la richiesta di aggiornamento del profilo
     */
    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("username") String newUsername,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || 
                authentication.getName().equals("anonymousUser")) {
                return "redirect:/users/login";
            }
            
            // Get the current username
            String currentUsername = authentication.getName();
            
            // Validazione del nuovo username
            if (newUsername == null || newUsername.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("profileError", "L'username è obbligatorio");
                return "redirect:/users/profile";
            }
            
            newUsername = newUsername.trim();
            
            // Validazione lunghezza e formato
            if (newUsername.length() < 3 || newUsername.length() > 20) {
                redirectAttributes.addFlashAttribute("profileError", "L'username deve essere tra 3 e 20 caratteri");
                return "redirect:/users/profile";
            }
            
            if (!newUsername.matches("^[a-zA-Z0-9_]+$")) {
                redirectAttributes.addFlashAttribute("profileError", "L'username può contenere solo lettere, numeri e underscore");
                return "redirect:/users/profile";
            }
            
            // Controlla se l'username è già in uso (e non è lo stesso dell'utente corrente)
            if (!newUsername.equals(currentUsername) && usersService.findByUsername(newUsername) != null) {
                redirectAttributes.addFlashAttribute("profileError", "Username già in uso");
                return "redirect:/users/profile";
            }
            
            // Chiama il servizio per aggiornare l'username
            boolean success = usersService.updateUsername(currentUsername, newUsername);
            
            if (success) {
                redirectAttributes.addFlashAttribute("profileSuccess", "Username aggiornato con successo");
                // Dopo aver cambiato l'username, l'utente deve riautenticarsi se l'username è cambiato
                if (!newUsername.equals(currentUsername)) {
                    redirectAttributes.addFlashAttribute("usernameChanged", "true");
                    return "redirect:/users/login?usernameChanged=true";
                }
            } else {
                redirectAttributes.addFlashAttribute("profileError", "Errore durante l'aggiornamento dell'username");
            }
            
            return "redirect:/users/profile";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileError", "Errore durante l'aggiornamento: " + e.getMessage());
            return "redirect:/users/profile";
        }
    }
}
