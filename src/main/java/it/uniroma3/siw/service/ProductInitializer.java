package it.uniroma3.siw.service;

import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Category;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.AuthorRepository;
import it.uniroma3.siw.repository.CategoryRepository;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.repository.UsersRepository;

// @Component - TEMPORARILY DISABLED
public class ProductInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ProductInitializer.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Verifica se ci sono già libri
            if (productRepository.count() > 0) {
                logger.info("Prodotti già presenti nel database. Skip inizializzazione.");
                return;
            }

            logger.info("Inizializzazione libri di esempio...");

            // Trova l'admin per assegnare come seller
            Optional<Users> adminOpt = usersRepository.findByUsername("admin");
            if (adminOpt.isEmpty()) {
                logger.warn("Admin non trovato, impossibile inizializzare i libri");
                return;
            }
            Users admin = adminOpt.get();

            // Trova o crea le categorie
            Category romanzo = findOrCreateCategory("Romanzo", "fas fa-book");
            Category fantascienza = findOrCreateCategory("Fantascienza", "fas fa-rocket");
            Category giallo = findOrCreateCategory("Giallo", "fas fa-search");

            // Crea alcuni autori di esempio
            Author dante = findOrCreateAuthor("Dante", "Alighieri", "Italiana");
            Author asimov = findOrCreateAuthor("Isaac", "Asimov", "Russa");
            Author christie = findOrCreateAuthor("Agatha", "Christie", "Inglese");

            // Crea libri di esempio
            createProduct("La Divina Commedia", "Il viaggio di Dante attraverso Inferno, Purgatorio e Paradiso", 
                         Year.of(1320), romanzo, admin, Arrays.asList(dante));

            createProduct("Io, Robot", "Una raccolta di racconti sui robot e le tre leggi della robotica", 
                         Year.of(1950), fantascienza, admin, Arrays.asList(asimov));

            createProduct("Assassinio sull'Orient Express", "Un mistero che si svolge su un treno bloccato dalla neve", 
                         Year.of(1934), giallo, admin, Arrays.asList(christie));

            createProduct("Fondazione", "La storia dell'Impero Galattico e della psicostoria", 
                         Year.of(1951), fantascienza, admin, Arrays.asList(asimov));

            createProduct("Il mistero di Styles", "Il primo caso di Hercule Poirot", 
                         Year.of(1920), giallo, admin, Arrays.asList(christie));

            createProduct("Vita Nova", "L'opera giovanile di Dante dedicata a Beatrice", 
                         Year.of(1295), romanzo, admin, Arrays.asList(dante));

            logger.info("Inizializzazione libri completata con successo!");

        } catch (Exception e) {
            logger.error("Errore durante l'inizializzazione dei libri: {}", e.getMessage(), e);
        }
    }

    private Category findOrCreateCategory(String name, String icon) {
        return categoryRepository.findByName(name)
            .orElseGet(() -> {
                Category category = new Category(name, icon);
                return categoryRepository.save(category);
            });
    }

    private Author findOrCreateAuthor(String nome, String cognome, String nazionalita) {
        return authorRepository.findByNomeAndCognome(nome, cognome)
            .orElseGet(() -> {
                Author author = new Author();
                author.setNome(nome);
                author.setCognome(cognome);
                author.setNazionalita(nazionalita);
                return authorRepository.save(author);
            });
    }

    private void createProduct(String name, String description, Year annoPubblicazione, 
                              Category category, Users seller, List<Author> autori) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setAnnoPubblicazione(annoPubblicazione);
        product.setCategory(category);
        product.setSeller(seller);
        product.setAutori(autori);
        
        productRepository.save(product);
        logger.info("Creato libro: {}", name);
    }
}
