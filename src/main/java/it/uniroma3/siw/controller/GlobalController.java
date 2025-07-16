package it.uniroma3.siw.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Category;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.repository.AuthorRepository;
import it.uniroma3.siw.repository.CategoryRepository;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.repository.UsersRepository;

@Controller
public class GlobalController {

    private static final Logger logger = LoggerFactory.getLogger(GlobalController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UsersRepository usersRepository;

    private void addAuthenticationAttributes(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser");

        model.addAttribute("isAuthenticated", isAuthenticated);
        if (isAuthenticated && auth != null) {
            model.addAttribute("username", auth.getName());
            
            // Aggiungi informazioni sull'utente se autenticato
            usersRepository.findByUsername(auth.getName()).ifPresent(user -> {
                model.addAttribute("currentUser", user);
                model.addAttribute("isAdmin", "ADMIN".equals(user.getRole()));
            });
        }
    }

    @GetMapping("/")
    @Transactional(readOnly = true)
    public String index(Model model, @RequestParam(required = false) String logout) {
        try {
            addAuthenticationAttributes(model);
            
            // Aggiungi messaggio di logout success se presente
            if ("success".equals(logout)) {
                model.addAttribute("logoutSuccess", "Logout effettuato con successo!");
            }

            // Ultimi libri aggiunti (massimo 6) - con gestione errori
            List<Product> latestBooks = new ArrayList<>();
            try {
                // Prima proviamo a prendere tutti i libri
                List<Product> allBooks = productRepository.findAll();
                logger.info("Found {} total books in database", allBooks.size());
                
                if (allBooks != null && !allBooks.isEmpty()) {
                    // Prendiamo solo i primi 6 per la homepage
                    latestBooks = allBooks.stream()
                        .limit(6)
                        .collect(java.util.stream.Collectors.toList());
                }
                
                logger.info("Selected {} books for homepage", latestBooks.size());
                
                // Log dei primi libri per debug
                for (int i = 0; i < Math.min(3, latestBooks.size()); i++) {
                    Product book = latestBooks.get(i);
                    logger.info("Book {}: ID={}, Name='{}', CreatedAt={}", 
                        i+1, book.getId(), book.getName(), book.getCreatedAt());
                }
            } catch (Exception e) {
                logger.error("Errore nel caricamento dei libri recenti: {}", e.getMessage(), e);
                latestBooks = new ArrayList<>();
            }

            // Autori più popolari (con più libri) - con gestione errori
            List<Author> popularAuthors = new ArrayList<>();
            try {
                popularAuthors = authorRepository.findTop6ByOrderByNomeAsc();
                if (popularAuthors == null) {
                    popularAuthors = new ArrayList<>();
                }
            } catch (Exception e) {
                logger.warn("Errore nel caricamento degli autori: {}", e.getMessage());
                popularAuthors = new ArrayList<>();
            }

            // Categorie disponibili - con gestione errori
            List<Category> categories = new ArrayList<>();
            try {
                categories = categoryRepository.findAll();
                if (categories == null) {
                    categories = new ArrayList<>();
                }
            } catch (Exception e) {
                logger.warn("Errore nel caricamento delle categorie: {}", e.getMessage());
                categories = new ArrayList<>();
            }

            // Statistiche generali - con gestione errori
            long totalBooks = 0;
            long totalAuthors = 0;
            long totalCategories = 0;
            
            try {
                totalBooks = productRepository.count();
                totalAuthors = authorRepository.count();
                totalCategories = categoryRepository.count();
            } catch (Exception e) {
                logger.warn("Errore nel caricamento delle statistiche: {}", e.getMessage());
            }

            model.addAttribute("latestBooks", latestBooks);
            model.addAttribute("popularAuthors", popularAuthors);
            model.addAttribute("categories", categories);
            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("totalAuthors", totalAuthors);
            model.addAttribute("totalCategories", totalCategories);

            logger.debug("Homepage loaded with {} books, {} authors, {} categories", 
                        latestBooks.size(), popularAuthors.size(), categories.size());

            return "index-minimal";  // Use minimal template to avoid errors

        } catch (Exception e) {
            logger.error("Error loading homepage: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento della homepage.");
            return "error";
        }
    }

    @GetMapping("/about")
    public String about(Model model) {
        addAuthenticationAttributes(model);
        return "about"; // Template da creare per informazioni sulla libreria
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        addAuthenticationAttributes(model);
        return "contact"; // Template da creare per i contatti
    }

    @GetMapping("/statistics")
    @Transactional(readOnly = true)
    public String statistics(Model model) {
        try {
            addAuthenticationAttributes(model);

            // Statistiche dettagliate per la dashboard
            long totalBooks = productRepository.count();
            long totalAuthors = authorRepository.count();
            long totalUsers = usersRepository.count();
            long totalCategories = categoryRepository.count();

            // Libri per categoria
            List<Category> categoriesWithCounts = categoryRepository.findAll();

            // Top 10 autori per numero di libri
            List<Object[]> authorsBookCounts = authorRepository.findAuthorsWithBookCount();

            // Ultimi libri aggiunti
            Pageable top10 = PageRequest.of(0, 10);
            List<Product> recentBooks = productRepository.findTop10ByOrderByCreatedAtDesc(top10);

            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("totalAuthors", totalAuthors);
            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalCategories", totalCategories);
            model.addAttribute("categoriesWithCounts", categoriesWithCounts);
            model.addAttribute("authorsBookCounts", authorsBookCounts);
            model.addAttribute("recentBooks", recentBooks);

            logger.debug("Statistics page loaded successfully");
            return "statistics"; // Template da creare per le statistiche

        } catch (Exception e) {
            logger.error("Error loading statistics: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento delle statistiche.");
            return "error";
        }
    }

    @GetMapping("/debug")
    @ResponseBody
    public String debug() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== DEBUG INFO ===\n");
            
            // Test database connections
            try {
                long productCount = productRepository.count();
                result.append("Products count: ").append(productCount).append("\n");
                
                // Prova a recuperare i primi 3 libri
                List<Product> allProducts = productRepository.findAll();
                result.append("All products size: ").append(allProducts.size()).append("\n");
                
                if (!allProducts.isEmpty()) {
                    result.append("First 3 products:\n");
                    for (int i = 0; i < Math.min(3, allProducts.size()); i++) {
                        Product p = allProducts.get(i);
                        result.append("- ID: ").append(p.getId())
                              .append(", Name: ").append(p.getName())
                              .append(", CreatedAt: ").append(p.getCreatedAt())
                              .append("\n");
                    }
                }
            } catch (Exception e) {
                result.append("ERROR counting/getting products: ").append(e.getMessage()).append("\n");
                e.printStackTrace();
            }
            
            try {
                long authorCount = authorRepository.count();
                result.append("Authors count: ").append(authorCount).append("\n");
            } catch (Exception e) {
                result.append("ERROR counting authors: ").append(e.getMessage()).append("\n");
            }
            
            try {
                long categoryCount = categoryRepository.count();
                result.append("Categories count: ").append(categoryCount).append("\n");
            } catch (Exception e) {
                result.append("ERROR counting categories: ").append(e.getMessage()).append("\n");
            }
            
            try {
                long userCount = usersRepository.count();
                result.append("Users count: ").append(userCount).append("\n");
            } catch (Exception e) {
                result.append("ERROR counting users: ").append(e.getMessage()).append("\n");
            }
            
            // Test authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            result.append("Is authenticated: ").append(auth != null && auth.isAuthenticated()).append("\n");
            if (auth != null) {
                result.append("Username: ").append(auth.getName()).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "CRITICAL ERROR: " + e.getMessage();
        }
    }

    @GetMapping("/simple")
    @ResponseBody
    public String simpleHomepage() {
        return "<html><body><h1>Homepage Semplice</h1><p>Se vedi questo, il controller funziona!</p><a href='/debug'>Debug Info</a></body></html>";
    }

    @GetMapping("/test")
    @Transactional(readOnly = true)
    public String testIndex(Model model) {
        try {
            addAuthenticationAttributes(model);

            // Dati semplici senza errori
            model.addAttribute("latestBooks", new ArrayList<>());
            model.addAttribute("popularAuthors", new ArrayList<>());
            model.addAttribute("categories", new ArrayList<>());
            model.addAttribute("totalBooks", 0L);
            model.addAttribute("totalAuthors", 0L);
            model.addAttribute("totalCategories", 0L);

            return "index-simple";

        } catch (Exception e) {
            logger.error("Error loading test page: {}", e.getMessage(), e);
            return "error";
        }
    }
}
