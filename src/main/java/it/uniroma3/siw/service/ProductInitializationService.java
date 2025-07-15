package it.uniroma3.siw.service;

import java.time.Year;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import it.uniroma3.siw.model.Category;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.CategoryRepository;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.repository.UsersRepository;

@Service
@Order(3) // Esegui dopo l'inizializzazione delle categorie e degli utenti
public class ProductInitializationService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductInitializationService.class);
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Override
    public void run(String... args) throws Exception {
        initializeProducts();
    }
    
    private void initializeProducts() {
        try {
            // Controlla se ci sono già prodotti
            long productCount = productRepository.count();
            if (productCount > 0) {
                logger.info("Prodotti già presenti nel database: {}", productCount);
                return;
            }
            
            logger.info("Inizializzazione prodotti di esempio...");
            
            // Ottieni categorie e utente admin
            Category fiction = categoryRepository.findByName("Fiction").orElse(null);
            Category saggistica = categoryRepository.findByName("Saggistica").orElse(null);
            Category romanzi = categoryRepository.findByName("Romanzi").orElse(null);
            
            Users admin = usersRepository.findByUsername("admin").orElse(null);
            
            if (fiction == null || admin == null) {
                logger.warn("Categorie o utente admin non trovati. Saltando inizializzazione prodotti.");
                return;
            }
            
            // Crea prodotti di esempio
            createProduct("Il Nome della Rosa", "Un capolavoro di Umberto Eco ambientato in un monastero medievale.", 
                         fiction, admin, Year.of(1980));
            
            createProduct("1984", "Il celebre romanzo distopico di George Orwell.", 
                         fiction, admin, Year.of(1949));
            
            createProduct("Sapiens", "Una breve storia dell'umanità di Yuval Noah Harari.", 
                         saggistica, admin, Year.of(2011));
            
            createProduct("Il Piccolo Principe", "Il classico di Antoine de Saint-Exupéry.", 
                         romanzi, admin, Year.of(1943));
            
            createProduct("Orgoglio e Pregiudizio", "Il romanzo immortale di Jane Austen.", 
                         romanzi, admin, Year.of(1813));
            
            createProduct("Homo Deus", "Breve storia del futuro di Yuval Noah Harari.", 
                         saggistica, admin, Year.of(2015));
            
            createProduct("Il Signore degli Anelli", "L'epica fantasy di J.R.R. Tolkien.", 
                         fiction, admin, Year.of(1954));
            
            createProduct("La Divina Commedia", "Il capolavoro di Dante Alighieri.", 
                         romanzi, admin, Year.of(1320));
            
            logger.info("Inizializzazione prodotti completata.");
            
        } catch (Exception e) {
            logger.error("Errore durante l'inizializzazione dei prodotti: {}", e.getMessage(), e);
        }
    }
    
    private void createProduct(String name, String description, 
                              Category category, Users seller, Year year) {
        try {
            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setCategory(category);
            product.setSeller(seller);
            product.setAnnoPubblicazione(year);
            
            productRepository.save(product);
            logger.debug("Creato prodotto: {}", name);
            
        } catch (Exception e) {
            logger.error("Errore nella creazione del prodotto {}: {}", name, e.getMessage());
        }
    }
}
