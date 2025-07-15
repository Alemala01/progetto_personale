package it.uniroma3.siw.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import it.uniroma3.siw.dto.AuthorFormDTO;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.AuthorRepository;
import it.uniroma3.siw.repository.UsersRepository;
import it.uniroma3.siw.service.AuthorService;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/authors")
public class AuthorController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthorController.class);
    
    @Autowired
    private AuthorService authorService;
    
    @Autowired
    private AuthorRepository authorRepository;
    
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
    
    // Visualizza tutti gli autori (pubblico)
    @GetMapping
    public String showAllAuthors(Model model) {
        try {
            addAuthenticationAttributes(model);
            
            List<Author> authors = authorService.findAll();
            model.addAttribute("authors", authors);
            model.addAttribute("totalAuthors", authors.size());
            
            logger.info("Loaded {} authors for public view", authors.size());
            return "authors"; // Creeremo questo template
            
        } catch (Exception e) {
            logger.error("Error loading authors: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore nel caricamento degli autori.");
            return "error";
        }
    }
    
    // Dettagli di un autore specifico
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public String showAuthorDetails(@PathVariable Long id, Model model) {
        try {
            addAuthenticationAttributes(model);
            
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Autore non trovato.");
                return "error";
            }
            
            Author author = authorOpt.get();
            model.addAttribute("author", author);
            model.addAttribute("isAdmin", isAdmin());
            
            logger.debug("Displaying author details for ID: {}", id);
            return "author-details"; // Creeremo questo template
            
        } catch (Exception e) {
            logger.error("Error loading author details: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento dei dettagli dell'autore.");
            return "error";
        }
    }
    
    // Endpoint per la fotografia dell'autore
    @GetMapping("/photo/{authorId}")
    @ResponseBody
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> getAuthorPhoto(@PathVariable Long authorId) {
        try {
            Optional<Author> authorOpt = authorRepository.findById(authorId);
            if (authorOpt.isPresent() && authorOpt.get().getFotografia() != null) {
                Author author = authorOpt.get();
                String contentType = author.getFotografiaContentType() != null ? 
                                   author.getFotografiaContentType() : "image/jpeg";
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(author.getFotografia());
            }
        } catch (Exception e) {
            logger.error("Error loading author photo: {}", e.getMessage(), e);
        }
        
        // Restituisce un'immagine placeholder se non trovata
        return ResponseEntity.notFound().build();
    }
    
    // === SEZIONE ADMIN ===
    
    // Form per creare un nuovo autore (solo admin)
    @GetMapping("/create")
    public String showCreateAuthorForm(Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono creare autori.");
            return "error";
        }
        
        addAuthenticationAttributes(model);
        model.addAttribute("authorFormDTO", new AuthorFormDTO());
        
        logger.debug("Displaying author creation form");
        return "author-form";
    }
    
    // Gestisce la creazione di un nuovo autore (solo admin)
    @PostMapping("/create")
    public String handleCreateAuthor(@Valid @ModelAttribute AuthorFormDTO authorFormDTO,
                                   BindingResult result, Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono creare autori.");
            return "error";
        }
        
        try {
            addAuthenticationAttributes(model);
            
            if (result.hasErrors()) {
                return "author-form";
            }
            
            // Controlla se esiste già un autore con questo nome e cognome
            if (authorService.existsByNomeAndCognome(authorFormDTO.getNome(), authorFormDTO.getCognome())) {
                model.addAttribute("errorMessage", "Esiste già un autore con questo nome e cognome.");
                return "author-form";
            }
            
            // Crea il nuovo autore
            Author author = new Author();
            author.setNome(authorFormDTO.getNome());
            author.setCognome(authorFormDTO.getCognome());
            author.setDataNascita(authorFormDTO.getDataNascita());
            author.setDataMorte(authorFormDTO.getDataMorte());
            author.setNazionalita(authorFormDTO.getNazionalita());
            author.setBiografia(authorFormDTO.getBiografia());
            
            // Salva l'autore con la fotografia se presente
            Author savedAuthor = authorService.saveAuthorWithPhoto(author, authorFormDTO.getFotografia());
            
            logger.info("Author created successfully with ID: {}", savedAuthor.getId());
            return "redirect:/author/" + savedAuthor.getId();
            
        } catch (IOException e) {
            logger.error("Error saving author photo: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il salvataggio della fotografia.");
            return "author-form";
        } catch (Exception e) {
            logger.error("Error creating author: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante la creazione dell'autore: " + e.getMessage());
            return "author-form";
        }
    }
    
    // Form per modificare un autore (solo admin)
    @GetMapping("/edit/{id}")
    public String showEditAuthorForm(@PathVariable Long id, Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono modificare autori.");
            return "error";
        }
        
        try {
            addAuthenticationAttributes(model);
            
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Autore non trovato.");
                return "error";
            }
            
            Author author = authorOpt.get();
            
            // Crea il DTO dall'autore esistente
            AuthorFormDTO authorFormDTO = new AuthorFormDTO();
            authorFormDTO.setNome(author.getNome());
            authorFormDTO.setCognome(author.getCognome());
            authorFormDTO.setDataNascita(author.getDataNascita());
            authorFormDTO.setDataMorte(author.getDataMorte());
            authorFormDTO.setNazionalita(author.getNazionalita());
            authorFormDTO.setBiografia(author.getBiografia());
            
            model.addAttribute("authorFormDTO", authorFormDTO);
            model.addAttribute("author", author);
            
            return "author-edit"; // Creeremo questo template
            
        } catch (Exception e) {
            logger.error("Error loading author for edit: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante il caricamento dell'autore.");
            return "error";
        }
    }
    
    // Gestisce la modifica di un autore (solo admin)
    @PostMapping("/edit/{id}")
    public String handleEditAuthor(@PathVariable Long id,
                                 @Valid @ModelAttribute AuthorFormDTO authorFormDTO,
                                 BindingResult result, Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono modificare autori.");
            return "error";
        }
        
        try {
            addAuthenticationAttributes(model);
            
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Autore non trovato.");
                return "error";
            }
            
            if (result.hasErrors()) {
                model.addAttribute("author", authorOpt.get());
                return "author-edit";
            }
            
            Author author = authorOpt.get();
            
            // Aggiorna i dati dell'autore
            author.setNome(authorFormDTO.getNome());
            author.setCognome(authorFormDTO.getCognome());
            author.setDataNascita(authorFormDTO.getDataNascita());
            author.setDataMorte(authorFormDTO.getDataMorte());
            author.setNazionalita(authorFormDTO.getNazionalita());
            author.setBiografia(authorFormDTO.getBiografia());
            
            // Salva l'autore con la nuova fotografia se presente
            Author updatedAuthor = authorService.updateAuthorWithPhoto(author, authorFormDTO.getFotografia());
            
            logger.info("Author updated successfully with ID: {}", updatedAuthor.getId());
            return "redirect:/author/" + updatedAuthor.getId();
            
        } catch (IOException e) {
            logger.error("Error updating author photo: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento della fotografia.");
            return "author-edit";
        } catch (Exception e) {
            logger.error("Error updating author: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento dell'autore: " + e.getMessage());
            return "author-edit";
        }
    }
    
    // Elimina un autore (solo admin)
    @PostMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable Long id, Model model) {
        if (!isAdmin()) {
            model.addAttribute("errorMessage", "Accesso negato. Solo gli amministratori possono eliminare autori.");
            return "error";
        }
        
        try {
            Optional<Author> authorOpt = authorService.findById(id);
            if (authorOpt.isEmpty()) {
                model.addAttribute("errorMessage", "Autore non trovato.");
                return "error";
            }
            
            Author author = authorOpt.get();
            
            // Controlla se l'autore ha libri associati
            if (author.getLibri() != null && !author.getLibri().isEmpty()) {
                model.addAttribute("errorMessage", 
                    "Impossibile eliminare l'autore: ha " + author.getLibri().size() + " libri associati. " +
                    "Rimuovi prima tutti i libri associati o assegnali ad altri autori.");
                return "error";
            }
            
            authorService.deleteAuthor(author);
            
            logger.info("Author deleted successfully with ID: {}", id);
            return "redirect:/author";
            
        } catch (Exception e) {
            logger.error("Error deleting author: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore durante l'eliminazione dell'autore: " + e.getMessage());
            return "error";
        }
    }
    
    // === API ENDPOINTS ===
    
    // API per cercare autori (usata dal form di creazione libro)
    @GetMapping("/api/search")
    @ResponseBody
    public List<Author> searchAuthors(@RequestParam("q") String query) {
        try {
            if (query == null || query.trim().length() < 2) {
                return List.of();
            }
            
            // Cerca per nome o cognome
            List<Author> authors = authorRepository.findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(
                query.trim(), query.trim());
            
            // Limita i risultati a 10
            return authors.stream().limit(10).toList();
            
        } catch (Exception e) {
            logger.error("Error searching authors: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // DTO per i risultati di ricerca autori
    public static class AuthorSearchResultDTO {
        private Long id;
        private String nome;
        private String cognome;
        private String nazionalita;
        private Integer annoNascita;
        
        public AuthorSearchResultDTO(Long id, String nome, String cognome, String nazionalita, Integer annoNascita) {
            this.id = id;
            this.nome = nome;
            this.cognome = cognome;
            this.nazionalita = nazionalita;
            this.annoNascita = annoNascita;
        }
        
        // Getters
        public Long getId() { return id; }
        public String getNome() { return nome; }
        public String getCognome() { return cognome; }
        public String getNazionalita() { return nazionalita; }
        public Integer getAnnoNascita() { return annoNascita; }
        public String getNomeCompleto() { return nome + " " + cognome; }
    }
}
