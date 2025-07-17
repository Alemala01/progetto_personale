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
            logger.info("Loading homepage...");
            addAuthenticationAttributes(model);
            
            // Aggiungi messaggio di logout success se presente
            if ("success".equals(logout)) {
                model.addAttribute("logoutSuccess", "Logout effettuato con successo!");
            }

            // Inizializzazione con valori di default
            List<Product> latestBooks = new ArrayList<>();
            List<Author> popularAuthors = new ArrayList<>(); 
            List<Category> categories = new ArrayList<>();
            long totalBooks = 0;
            long totalAuthors = 0;
            long totalCategories = 0;

            // Caricamento dati dal database con gestione errori
            try {
                logger.info("Attempting to load data from database...");
                
                // Prima carica le statistiche
                totalBooks = productRepository.count();
                totalAuthors = authorRepository.count();
                totalCategories = categoryRepository.count();
                
                logger.info("Database stats: {} books, {} authors, {} categories", 
                           totalBooks, totalAuthors, totalCategories);
                
                // Carica i libri più recenti (massimo 6)
                if (totalBooks > 0) {
                    List<Product> allBooks = productRepository.findAll();
                    latestBooks = allBooks.stream()
                        .limit(6)
                        .collect(java.util.stream.Collectors.toList());
                    logger.info("Loaded {} books for homepage", latestBooks.size());
                }
                
                // Carica gli autori (massimo 6)
                if (totalAuthors > 0) {
                    popularAuthors = authorRepository.findTop6ByOrderByNomeAsc();
                    if (popularAuthors == null) {
                        popularAuthors = new ArrayList<>();
                    }
                    logger.info("Loaded {} authors for homepage", popularAuthors.size());
                }
                
                // Carica tutte le categorie
                if (totalCategories > 0) {
                    categories = categoryRepository.findAll();
                    if (categories == null) {
                        categories = new ArrayList<>();
                    }
                    logger.info("Loaded {} categories for homepage", categories.size());
                }
                
            } catch (Exception e) {
                logger.error("Database error during homepage loading: {}", e.getMessage(), e);
                // Mantieni i valori di default in caso di errore
                totalBooks = 0;
                totalAuthors = 0;
                totalCategories = 0;
                latestBooks = new ArrayList<>();
                popularAuthors = new ArrayList<>();
                categories = new ArrayList<>();
            }

            // Aggiungi tutti gli attributi al modello
            model.addAttribute("latestBooks", latestBooks);
            model.addAttribute("popularAuthors", popularAuthors);
            model.addAttribute("categories", categories);
            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("totalAuthors", totalAuthors);
            model.addAttribute("totalCategories", totalCategories);

            logger.info("Homepage model prepared: {} books, {} authors, {} categories", 
                       latestBooks.size(), popularAuthors.size(), categories.size());

            return "index-minimal";

        } catch (Exception e) {
            logger.error("Critical error loading homepage: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento della homepage: " + e.getMessage());
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

    @GetMapping("/test-data")
    @ResponseBody
    public String testData() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== TEST DATA LOADING ===\n");
            
            // Test products
            List<Product> allProducts = productRepository.findAll();
            result.append("Total products: ").append(allProducts.size()).append("\n");
            
            if (!allProducts.isEmpty()) {
                result.append("First 3 products:\n");
                for (int i = 0; i < Math.min(3, allProducts.size()); i++) {
                    Product p = allProducts.get(i);
                    result.append("- ").append(p.getName())
                          .append(" (ID: ").append(p.getId()).append(")")
                          .append(" - Authors: ");
                    
                    if (p.getAutori() != null && !p.getAutori().isEmpty()) {
                        for (Author a : p.getAutori()) {
                            result.append(a.getNome()).append(" ").append(a.getCognome()).append(", ");
                        }
                    } else {
                        result.append("No authors");
                    }
                    result.append("\n");
                }
            }
            
            // Test authors
            List<Author> allAuthors = authorRepository.findAll();
            result.append("\nTotal authors: ").append(allAuthors.size()).append("\n");
            
            if (!allAuthors.isEmpty()) {
                result.append("Authors:\n");
                for (Author a : allAuthors) {
                    result.append("- ").append(a.getNome()).append(" ").append(a.getCognome()).append("\n");
                }
            }
            
            // Test the specific method used in homepage
            List<Author> top6Authors = authorRepository.findTop6ByOrderByNomeAsc();
            result.append("\nTop 6 authors (homepage method): ").append(top6Authors != null ? top6Authors.size() : "null").append("\n");
            
            return result.toString();
            
        } catch (Exception e) {
            return "ERROR: " + e.getMessage() + "\n" + java.util.Arrays.toString(e.getStackTrace());
        }
    }
}
