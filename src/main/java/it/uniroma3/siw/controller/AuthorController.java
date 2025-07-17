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
import it.uniroma3.siw.dto.AuthorSearchDTO;
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
    
    // Visualizza tutti gli autori con possibilità di ricerca e filtri
    @GetMapping
    public String showAllAuthors(@RequestParam(required = false) String nome,
                                @RequestParam(required = false) String cognome,
                                @RequestParam(required = false) String nazionalita,
                                @RequestParam(required = false) Integer annoNascitaMin,
                                @RequestParam(required = false) Integer annoNascitaMax,
                                @RequestParam(required = false) Boolean soloVivi,
                                @RequestParam(required = false) String sortBy,
                                Model model) {
        try {
            addAuthenticationAttributes(model);
            
            // Crea il DTO di ricerca con i parametri ricevuti
            AuthorSearchDTO searchDTO = new AuthorSearchDTO();
            searchDTO.setSearchTerm(nome != null && !nome.trim().isEmpty() ? nome : 
                                   (cognome != null && !cognome.trim().isEmpty() ? cognome : 
                                    (nome != null && cognome != null && (!nome.trim().isEmpty() || !cognome.trim().isEmpty()) ? 
                                     (nome.trim() + " " + cognome.trim()).trim() : null)));
            searchDTO.setNazionalita(nazionalita);
            searchDTO.setAnnoNascitaMin(annoNascitaMin);
            searchDTO.setAnnoNascitaMax(annoNascitaMax);
            searchDTO.setSoloVivi(soloVivi);
            searchDTO.setSortBy(sortBy != null ? sortBy : "nome");
            
            List<Author> authors;
            
            // Se non ci sono filtri, carica tutti gli autori
            if (!hasAnyFilter(nome, cognome, nazionalita, annoNascitaMin, annoNascitaMax, soloVivi)) {
                authors = authorService.findAll();
                logger.info("No filters applied, loaded all {} authors", authors.size());
            } else {
                // Applica i filtri di ricerca
                authors = searchAuthorsWithFilters(nome, cognome, nazionalita, annoNascitaMin, annoNascitaMax, soloVivi);
                logger.info("Applied filters, found {} authors", authors.size());
            }
            
            // Ordinamento
            sortAuthors(authors, searchDTO.getSortBy());
            
            // Crea un oggetto per il template che mantiene i valori separati
            AuthorSearchFormDTO formDTO = new AuthorSearchFormDTO();
            formDTO.setNome(nome);
            formDTO.setCognome(cognome);
            formDTO.setNazionalita(nazionalita);
            formDTO.setAnnoNascitaMin(annoNascitaMin);
            formDTO.setAnnoNascitaMax(annoNascitaMax);
            formDTO.setSoloVivi(soloVivi);
            formDTO.setSortBy(sortBy);
            
            model.addAttribute("authors", authors);
            model.addAttribute("totalAuthors", authors.size());
            model.addAttribute("authorSearchDTO", formDTO);
            model.addAttribute("hasFilters", hasAnyFilter(nome, cognome, nazionalita, annoNascitaMin, annoNascitaMax, soloVivi));
            
            return "authors";
            
        } catch (Exception e) {
            logger.error("Error loading authors: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Errore nel caricamento degli autori.");
            return "error";
        }
    }
    
    // Helper method per verificare se ci sono filtri attivi
    private boolean hasAnyFilter(String nome, String cognome, String nazionalita, 
                                Integer annoNascitaMin, Integer annoNascitaMax, Boolean soloVivi) {
        return (nome != null && !nome.trim().isEmpty()) ||
               (cognome != null && !cognome.trim().isEmpty()) ||
               (nazionalita != null && !nazionalita.trim().isEmpty()) ||
               (annoNascitaMin != null && annoNascitaMin > 0) ||
               (annoNascitaMax != null && annoNascitaMax > 0) ||
               (soloVivi != null && soloVivi);
    }
    
    // Helper method per la ricerca con filtri
    private List<Author> searchAuthorsWithFilters(String nome, String cognome, String nazionalita,
                                                 Integer annoNascitaMin, Integer annoNascitaMax, Boolean soloVivi) {
        List<Author> authors = authorService.findAll();
        
        return authors.stream()
            .filter(author -> {
                // Filtro per nome
                if (nome != null && !nome.trim().isEmpty()) {
                    if (author.getNome() == null || 
                        !author.getNome().toLowerCase().contains(nome.trim().toLowerCase())) {
                        return false;
                    }
                }
                
                // Filtro per cognome
                if (cognome != null && !cognome.trim().isEmpty()) {
                    if (author.getCognome() == null || 
                        !author.getCognome().toLowerCase().contains(cognome.trim().toLowerCase())) {
                        return false;
                    }
                }
                
                // Filtro per nazionalità
                if (nazionalita != null && !nazionalita.trim().isEmpty()) {
                    if (author.getNazionalita() == null || 
                        !author.getNazionalita().toLowerCase().contains(nazionalita.trim().toLowerCase())) {
                        return false;
                    }
                }
                
                // Filtro per anno nascita minimo
                if (annoNascitaMin != null && annoNascitaMin > 0) {
                    if (author.getDataNascita() == null || 
                        author.getDataNascita().getYear() < annoNascitaMin) {
                        return false;
                    }
                }
                
                // Filtro per anno nascita massimo
                if (annoNascitaMax != null && annoNascitaMax > 0) {
                    if (author.getDataNascita() == null || 
                        author.getDataNascita().getYear() > annoNascitaMax) {
                        return false;
                    }
                }
                
                // Filtro solo autori vivi
                if (soloVivi != null && soloVivi) {
                    if (author.getDataMorte() != null) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    // Helper method per l'ordinamento
    private void sortAuthors(List<Author> authors, String sortBy) {
        if (sortBy == null) sortBy = "nome";
        
        switch (sortBy) {
            case "cognome" -> authors.sort((a1, a2) -> {
                String cognome1 = a1.getCognome() != null ? a1.getCognome() : "";
                String cognome2 = a2.getCognome() != null ? a2.getCognome() : "";
                return cognome1.compareToIgnoreCase(cognome2);
            });
            case "nazionalita" -> authors.sort((a1, a2) -> {
                String naz1 = a1.getNazionalita() != null ? a1.getNazionalita() : "";
                String naz2 = a2.getNazionalita() != null ? a2.getNazionalita() : "";
                return naz1.compareToIgnoreCase(naz2);
            });
            case "anno_nascita" -> authors.sort((a1, a2) -> {
                if (a1.getDataNascita() == null && a2.getDataNascita() == null) return 0;
                if (a1.getDataNascita() == null) return 1;
                if (a2.getDataNascita() == null) return -1;
                return a1.getDataNascita().compareTo(a2.getDataNascita());
            });
            case "anno_nascita_desc" -> authors.sort((a1, a2) -> {
                if (a1.getDataNascita() == null && a2.getDataNascita() == null) return 0;
                if (a1.getDataNascita() == null) return 1;
                if (a2.getDataNascita() == null) return -1;
                return a2.getDataNascita().compareTo(a1.getDataNascita());
            });
            default -> authors.sort((a1, a2) -> { // nome
                String nome1 = a1.getNome() != null ? a1.getNome() : "";
                String nome2 = a2.getNome() != null ? a2.getNome() : "";
                return nome1.compareToIgnoreCase(nome2);
            });
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
            return "redirect:/authors/" + savedAuthor.getId();
            
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
            return "redirect:/authors/" + updatedAuthor.getId();
            
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
            return "redirect:/authors";
            
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
    public List<AuthorSearchResultDTO> searchAuthors(@RequestParam("q") String query) {
        try {
            if (query == null || query.trim().length() < 2) {
                return List.of();
            }
            
            // Cerca per nome o cognome
            List<Author> authors = authorRepository.findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(
                query.trim(), query.trim());
            
            // Converti in DTO e limita i risultati a 10
            return authors.stream()
                    .limit(10)
                    .map(author -> new AuthorSearchResultDTO(
                        author.getId(),
                        author.getNome(),
                        author.getCognome(),
                        author.getNazionalita(),
                        author.getDataNascita() != null ? author.getDataNascita().getYear() : null
                    ))
                    .toList();
            
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
    
    // DTO per il form di ricerca (mantiene i campi separati)
    public static class AuthorSearchFormDTO {
        private String nome;
        private String cognome;
        private String nazionalita;
        private Integer annoNascitaMin;
        private Integer annoNascitaMax;
        private Boolean soloVivi;
        private String sortBy;
        
        // Getters and Setters
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        
        public String getCognome() { return cognome; }
        public void setCognome(String cognome) { this.cognome = cognome; }
        
        public String getNazionalita() { return nazionalita; }
        public void setNazionalita(String nazionalita) { this.nazionalita = nazionalita; }
        
        public Integer getAnnoNascitaMin() { return annoNascitaMin; }
        public void setAnnoNascitaMin(Integer annoNascitaMin) { this.annoNascitaMin = annoNascitaMin; }
        
        public Integer getAnnoNascitaMax() { return annoNascitaMax; }
        public void setAnnoNascitaMax(Integer annoNascitaMax) { this.annoNascitaMax = annoNascitaMax; }
        
        public Boolean getSoloVivi() { return soloVivi; }
        public void setSoloVivi(Boolean soloVivi) { this.soloVivi = soloVivi; }
        
        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    }
}
