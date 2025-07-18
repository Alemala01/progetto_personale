package it.uniroma3.siw.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.siw.dto.ProductFormDTO;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Category;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.ProductImage;
import it.uniroma3.siw.model.Rating;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.AuthorRepository;
import it.uniroma3.siw.repository.CategoryRepository;
import it.uniroma3.siw.repository.ProductImageRepository;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.repository.UsersRepository;
import it.uniroma3.siw.service.ProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/product")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private AuthorRepository authorRepository;

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

    private boolean isAdmin() {
        Users user = getAuthenticatedUser();
        return user != null && "ADMIN".equals(user.getRole());
    }

    // ENDPOINT RIMOSSI: I vecchi endpoint per il flusso multi-step sono stati rimossi
    // Ora si usa solo /product/create per il flusso moderno

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String showProduct(@PathVariable Long id, Model model) {
        try {
            logger.info("=== STARTING showProduct for ID: {} ===", id);
            
            addAuthenticationAttributes(model);
            logger.info("Authentication attributes added");
            
            Optional<Product> productOpt = productRepository.findById(id);
            logger.info("Repository query executed, product found: {}", productOpt.isPresent());
            
            if (productOpt.isEmpty()) {
                logger.warn("Product with ID {} not found", id);
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            logger.info("Product loaded: name={}, id={}", product.getName(), product.getId());
            
            model.addAttribute("product", product);
            logger.info("Product added to model");
            
            logger.info("Returning template: product-minimal");
            return "product-minimal";
        } catch (Exception e) {
            logger.error("=== ERROR in showProduct ===", e);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Full stack trace: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante il caricamento del prodotto: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/image/{imageId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public org.springframework.http.ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        try {
            Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
            if (imageOpt.isEmpty()) {
                return org.springframework.http.ResponseEntity.notFound().build();
            }
            ProductImage image = imageOpt.get();
            
            // Se contentType è null, usa un valore di default
            String contentType = image.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/jpeg"; // Default content type
            }
            
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(image.getImageData());
        } catch (Exception e) {
            logger.error("Error loading image with ID: {}", imageId, e);
            return org.springframework.http.ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint per pagina di scelta modifica prodotto
    @GetMapping("/edit/{id}")
    @Transactional(readOnly = true)
    public String showEditProductPage(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                model.addAttribute("errorMessage", "Devi essere autenticato per modificare un prodotto.");
                return "error";
            }
            
            Optional<Product> productOpt = productRepository.findByIdWithSeller(id);
            if (productOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            
            // Verifica che l'autore del prodotto non sia null
            if (product.getSeller() == null) {
                logger.error("Product with ID {} has null author", id);
                model.addAttribute("errorMessage", "Errore: prodotto senza autore.");
                return "error";
            }
            
            // Verifica che l'utente sia il proprietario del prodotto
            if (!product.getSeller().getId().equals(authenticatedUser.getId())) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo prodotto.");
                return "error";
            }
            
            model.addAttribute("product", product);
            return "edit-product";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Error loading edit page: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante il caricamento della pagina di modifica: " + e.getMessage());
            return "error";
        }
    }

    // Endpoint per modificare le immagini di un prodotto
    @GetMapping("/edit/{id}/images")
    @Transactional(readOnly = true)
    public String showEditImages(@PathVariable Long id, Model model, HttpSession session) {
        try {
            addAuthenticationAttributes(model);
            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                model.addAttribute("errorMessage", "Devi essere autenticato per modificare un prodotto.");
                return "error";
            }
            
            Optional<Product> productOpt = productRepository.findByIdWithImages(id);
            if (productOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            
            // Verifica che l'utente sia il proprietario del prodotto
            if (!product.getSeller().getId().equals(authenticatedUser.getId())) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo prodotto.");
                return "error";
            }
            
            // Salva l'ID del prodotto in sessione per i passaggi successivi
            session.setAttribute("editingProductId", id);
            model.addAttribute("product", product);
            return "edit-images";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Error loading images for edit: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante il caricamento delle immagini: " + e.getMessage());
            return "error";
        }
    }

    // Endpoint per modificare i dettagli di un prodotto
    @GetMapping("/edit/{id}/details")
    public String showEditDetails(@PathVariable Long id, Model model, HttpSession session) {
        try {
            addAuthenticationAttributes(model);
            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                model.addAttribute("errorMessage", "Devi essere autenticato per modificare un prodotto.");
                return "error";
            }
            
            Optional<Product> productOpt = productRepository.findByIdWithSeller(id);
            if (productOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            
            // Verifica che l'autore del prodotto non sia null
            if (product.getSeller() == null) {
                logger.error("Product with ID {} has null author", id);
                model.addAttribute("errorMessage", "Errore: prodotto senza autore.");
                return "error";
            }
            
            // Verifica che l'utente sia il proprietario del prodotto
            if (!product.getSeller().getId().equals(authenticatedUser.getId())) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo prodotto.");
                return "error";
            }
            
            // Crea un DTO dal libro esistente
            ProductFormDTO productFormDTO = new ProductFormDTO();
            productFormDTO.setNome(product.getName());
            productFormDTO.setCategoria(product.getCategory() != null ? product.getCategory().getName() : "");
            productFormDTO.setDescrizione(product.getDescription());
            productFormDTO.setAnnoPubblicazione(product.getAnnoPubblicazione());
            
            // Imposta gli ID degli autori attuali
            if (product.getAutori() != null && !product.getAutori().isEmpty()) {
                List<Long> autoriIds = product.getAutori().stream()
                        .map(autore -> autore.getId())
                        .toList();
                productFormDTO.setAutoriIds(autoriIds);
            }
            
            // Salva l'ID del prodotto in sessione per i passaggi successivi
            session.setAttribute("editingProductId", id);
            model.addAttribute("productFormDTO", productFormDTO);
            model.addAttribute("product", product);
            
            // Aggiungi le categorie al modello
            List<Category> categories = (List<Category>) categoryRepository.findAll();
            model.addAttribute("categories", categories);
            
            // Aggiungi tutti gli autori disponibili
            List<Author> authors = (List<Author>) authorRepository.findAll();
            model.addAttribute("authors", authors);
            
            return "edit-details";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Error loading details for edit: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante il caricamento dei dettagli: " + e.getMessage());
            return "error";
        }
    }

    // Endpoint per salvare le modifiche alle immagini
    @PostMapping("/edit/images")
    @Transactional
    public String handleEditImages(@RequestParam("images") List<MultipartFile> images,
                                   HttpSession session, Model model) {
        try {
            addAuthenticationAttributes(model);
            Long productId = (Long) session.getAttribute("editingProductId");
            if (productId == null) {
                model.addAttribute("errorMessage", "Sessione scaduta. Riprova.");
                return "error";
            }

            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                model.addAttribute("errorMessage", "Devi essere autenticato per modificare un prodotto.");
                return "error";
            }
            
            Optional<Product> productOpt = productRepository.findByIdWithImages(productId);
            if (productOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            
            // Verifica che l'utente sia il proprietario del prodotto
            if (!product.getSeller().getId().equals(authenticatedUser.getId())) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo prodotto.");
                return "error";
            }

            if (images.size() < 1 || images.size() > 10) {
                model.addAttribute("errorMessage", "Seleziona da 1 a 10 immagini.");
                model.addAttribute("product", product);
                return "edit-images";
            }
            
            // Aggiorna le immagini del prodotto
            productService.updateProductImages(product, images);
            session.removeAttribute("editingProductId");
            
            logger.debug("Product images updated successfully for ID: {}", productId);
            return "redirect:/product/" + productId;
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Error updating images: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento delle immagini: " + e.getMessage());
            return "error";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Error updating images: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento delle immagini: " + e.getMessage());
            return "error";
        }
    }

    // Endpoint per salvare le modifiche ai dettagli
    @PostMapping("/edit/details")
    public String handleEditDetails(@Valid @ModelAttribute ProductFormDTO productFormDTO,
                                    BindingResult result, HttpSession session, Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            addAuthenticationAttributes(model);
            Long productId = (Long) session.getAttribute("editingProductId");
            if (productId == null) {
                model.addAttribute("errorMessage", "Sessione scaduta. Riprova.");
                return "error";
            }

            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                model.addAttribute("errorMessage", "Devi essere autenticato per modificare un prodotto.");
                return "error";
            }
            
            Optional<Product> productOpt = productRepository.findByIdWithSeller(productId);
            if (productOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            
            // Verifica che l'autore del prodotto non sia null
            if (product.getSeller() == null) {
                logger.error("Product with ID {} has null author", productId);
                model.addAttribute("errorMessage", "Errore: prodotto senza autore.");
                return "error";
            }
            
            // Verifica che l'utente sia il proprietario del prodotto
            if (!product.getSeller().getId().equals(authenticatedUser.getId())) {
                model.addAttribute("errorMessage", "Non hai i permessi per modificare questo prodotto.");
                return "error";
            }

            if (result.hasErrors()) {
                model.addAttribute("product", product);
                // Aggiungi le categorie e autori quando ci sono errori
                List<Category> categories = (List<Category>) categoryRepository.findAll();
                model.addAttribute("categories", categories);
                List<Author> authors = (List<Author>) authorRepository.findAll();
                model.addAttribute("authors", authors);
                return "edit-details";
            }
            
            // Aggiorna i dettagli del prodotto
            productService.updateProductDetails(product, productFormDTO);
            session.removeAttribute("editingProductId");
            
            // Aggiungi messaggio di successo
            redirectAttributes.addFlashAttribute("successMessage", "Prodotto aggiornato con successo!");
            
            logger.info("Product details updated successfully for ID: {} by user: {}", productId, authenticatedUser.getUsername());
            return "redirect:/product/details/" + productId;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Error updating details: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento dei dettagli: " + e.getMessage());
            return "error";
        }
    }

    // Endpoint per eliminare un prodotto
    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteProduct(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                model.addAttribute("errorMessage", "Devi essere autenticato per eliminare un prodotto.");
                return "error";
            }
            
            Optional<Product> productOpt = productRepository.findByIdWithSeller(id);
            if (productOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            
            // Verifica che l'autore del prodotto non sia null
            if (product.getSeller() == null) {
                logger.error("Product with ID {} has null author", id);
                model.addAttribute("errorMessage", "Errore: prodotto senza autore.");
                return "error";
            }
            
            // Verifica che l'utente sia il proprietario del prodotto
            if (!product.getSeller().getId().equals(authenticatedUser.getId())) {
                model.addAttribute("errorMessage", "Non hai i permessi per eliminare questo prodotto.");
                return "error";
            }
            
            productService.deleteProduct(product);
            logger.debug("Product deleted successfully with ID: {}", id);
            return "redirect:/users/products";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Error deleting product: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante l'eliminazione del prodotto: " + e.getMessage());
            return "error";
        }
    }

    // ENDPOINT RIMOSSO: /create/image/{index} non più necessario

    @GetMapping("/details/{id}")
    @Transactional(readOnly = true)
    public String showProductDetails(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            Optional<Product> productOpt = productRepository.findByIdWithImages(id);
            if (productOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Prodotto non trovato.");
                return "error";
            }
            
            Product product = productOpt.get();
            model.addAttribute("product", product);
            
            // Controlla se l'utente corrente è il proprietario del prodotto
            Users authenticatedUser = getAuthenticatedUser();
            boolean isOwner = authenticatedUser != null && 
                            product.getSeller() != null && 
                            product.getSeller().getId().equals(authenticatedUser.getId());
            model.addAttribute("isOwner", isOwner);
            
            // Aggiungi informazioni sui rating tramite il servizio dedicato
            try {
                Double avgRating = 0.0;
                Integer ratingCount = 0;
                
                // Carica ratings dal repository se necessario
                if (product.getRatings() != null && !product.getRatings().isEmpty()) {
                    avgRating = product.getAverageRating();
                    ratingCount = product.getRatingCount();
                }
                
                model.addAttribute("averageRating", avgRating);
                model.addAttribute("ratingCount", ratingCount);
                
                // Aggiungi le recensioni per l'anteprima nella pagina del prodotto
                List<Rating> ratings = new ArrayList<>(product.getRatings());
                // Ordina le recensioni per data (più recenti prima)
                ratings.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
                model.addAttribute("ratings", ratings);
                
            } catch (Exception e) {
                // Fallback se c'è un problema con i rating
                logger.warn("Impossibile caricare i rating: {}", e.getMessage());
                model.addAttribute("averageRating", 0.0);
                model.addAttribute("ratingCount", 0);
            }
            
            logger.debug("Displaying product details for ID: {}", id);
            return "product-details-view";
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("Error loading product details: {}", sw.toString());
            model.addAttribute("errorMessage", "Errore durante il caricamento dei dettagli del prodotto: " + e.getMessage());
            return "error";
        }
    }
    
    // Nuovo endpoint per il flusso moderno di creazione libri (solo admin)
    @GetMapping("/create")
    public String showModernCreatePage(Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono aggiungere libri.");
            return "error";
        }
        
        try {
            addAuthenticationAttributes(model);
            
            // Aggiungi le categorie al modello (ora categorie di libri)
            List<Category> categories = (List<Category>) categoryRepository.findAll();
            model.addAttribute("categories", categories);
            logger.debug("Loaded {} categories", categories.size());
            
            // Aggiungi tutti gli autori disponibili
            List<Author> authors = (List<Author>) authorRepository.findAll();
            model.addAttribute("authors", authors);
            logger.debug("Loaded {} authors", authors.size());
            
            model.addAttribute("productFormDTO", new ProductFormDTO());
            
            logger.debug("Displaying modern book creation page");
            return "product-create";
        } catch (Exception e) {
            logger.error("Error loading book creation page", e);
            model.addAttribute("errorMessage", "Errore nel caricamento della pagina: " + e.getMessage());
            return "error";
        }
    }
    
    @PostMapping("/create")
    @ResponseBody
    public String handleModernProductCreation(@RequestParam("name") String name,
                                            @RequestParam("annoPubblicazione") String annoPubblicazioneStr,
                                            @RequestParam("description") String description,
                                            @RequestParam("category") Long categoryId,
                                            @RequestParam("autoriIds") List<Long> autoriIds,
                                            @RequestParam("images") List<MultipartFile> images,
                                            Model model) {
        if (!isAdmin()) {
            return "{\"error\": \"Accesso negato. Solo gli amministratori possono aggiungere libri.\"}";
        }
        
        try {
            // Validazione base
            if (images == null || images.isEmpty()) {
                return "{\"error\": \"Carica almeno un'immagine del libro\"}";
            }
            
            if (images.size() > 5) {
                return "{\"error\": \"Massimo 5 immagini consentite\"}";
            }
            
            if (autoriIds == null || autoriIds.isEmpty()) {
                return "{\"error\": \"Almeno un autore è obbligatorio\"}";
            }
            
            // Validazione dimensioni e tipo file
            for (MultipartFile image : images) {
                if (image.getSize() > 5 * 1024 * 1024) { // 5MB
                    return "{\"error\": \"Immagine troppo grande (max 5MB): " + image.getOriginalFilename() + "\"}";
                }
                
                String contentType = image.getContentType();
                if (contentType == null || !isValidImageType(contentType)) {
                    return "{\"error\": \"Formato immagine non supportato: " + image.getOriginalFilename() + "\"}";
                }
            }
            
            Users authenticatedUser = getAuthenticatedUser();
            if (authenticatedUser == null) {
                return "{\"error\": \"Utente non autenticato\"}";
            }
            
            // Crea il DTO del libro
            ProductFormDTO productFormDTO = new ProductFormDTO();
            productFormDTO.setNome(name);
            productFormDTO.setDescrizione(description);
            
            // Valida e converte l'anno di pubblicazione
            try {
                Year annoPubblicazione = Year.parse(annoPubblicazioneStr);
                productFormDTO.setAnnoPubblicazione(annoPubblicazione);
            } catch (Exception e) {
                return "{\"error\": \"Anno di pubblicazione non valido\"}";
            }
            
            // Trova la categoria per nome
            Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
            if (categoryOpt.isPresent()) {
                productFormDTO.setCategoria(categoryOpt.get().getName());
            } else {
                return "{\"error\": \"Categoria non trovata\"}";
            }
            
            // Imposta gli autori
            productFormDTO.setAutoriIds(autoriIds);
            
            // Converti le immagini in byte array
            List<byte[]> imageDataList = new ArrayList<>();
            for (MultipartFile image : images) {
                imageDataList.add(image.getBytes());
            }
            
            // Salva il libro
            Product savedProduct = productService.saveProduct(productFormDTO, imageDataList, authenticatedUser);
            
            logger.info("Book created successfully with ID: {}", savedProduct.getId());
            return "{\"success\": true, \"productId\": " + savedProduct.getId() + "}";
            
        } catch (Exception e) {
            logger.error("Error creating book: {}", e.getMessage(), e);
            return "{\"error\": \"Errore durante la creazione del libro: " + e.getMessage() + "\"}";
        }
    }
    
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/jpg") || 
               contentType.equals("image/png") || 
               contentType.equals("image/webp");
    }
}
