package it.uniroma3.siw.service;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Category;
import it.uniroma3.siw.repository.CategoryRepository;

@Service
public class CategoryInitializationService implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoryInitializationService.class);
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    // Categorie predefinite per libri
    private static final List<CategoryData> BOOK_CATEGORIES = Arrays.asList(
        new CategoryData("Fiction", "fas fa-magic", "Romanzi e narrativa di fantasia"),
        new CategoryData("Saggistica", "fas fa-book-open", "Saggi e opere di non-fiction"),
        new CategoryData("Romanzi", "fas fa-heart", "Romanzi di vario genere"),
        new CategoryData("Biografie", "fas fa-user-circle", "Biografie e autobiografie"),
        new CategoryData("Storia", "fas fa-landmark", "Libri di storia e storiografia"),
        new CategoryData("Filosofia", "fas fa-brain", "Opere filosofiche e di pensiero"),
        new CategoryData("Scienze", "fas fa-microscope", "Libri scientifici e divulgativi"),
        new CategoryData("Arte", "fas fa-palette", "Libri d'arte e creatività"),
        new CategoryData("Psicologia", "fas fa-head-side-virus", "Libri di psicologia e self-help"),
        new CategoryData("Cucina", "fas fa-utensils", "Ricettari e libri di cucina"),
        new CategoryData("Viaggi", "fas fa-map-marked-alt", "Guide di viaggio e racconti"),
        new CategoryData("Tecnologia", "fas fa-laptop-code", "Libri su tecnologia e informatica"),
        new CategoryData("Economia", "fas fa-chart-line", "Libri di economia e finanza"),
        new CategoryData("Religione", "fas fa-pray", "Testi religiosi e spirituali"),
        new CategoryData("Poesia", "fas fa-feather-alt", "Raccolte poetiche e versi"),
        new CategoryData("Teatro", "fas fa-theater-masks", "Opere teatrali e drammatiche"),
        new CategoryData("Bambini", "fas fa-child", "Libri per bambini e ragazzi"),
        new CategoryData("Fumetti", "fas fa-comments", "Fumetti e graphic novel"),
        new CategoryData("Gialli", "fas fa-search", "Romanzi gialli e thriller"),
        new CategoryData("Fantasy", "fas fa-dragon", "Libri fantasy e fantascientifici")
    );
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Inizializzazione categorie libri...");
        initializeBookCategories();
        logger.info("Inizializzazione categorie completata.");
    }
    
    private void initializeBookCategories() {
        for (CategoryData categoryData : BOOK_CATEGORIES) {
            // Controlla se la categoria esiste già
            if (!categoryRepository.existsByName(categoryData.name)) {
                Category category = new Category();
                category.setName(categoryData.name);
                category.setIconClass(categoryData.iconClass);
                
                categoryRepository.save(category);
                logger.info("Creata categoria: {}", categoryData.name);
            } else {
                logger.debug("Categoria già esistente: {}", categoryData.name);
            }
        }
    }
    
    // Classe helper per i dati delle categorie
    private static class CategoryData {
        final String name;
        final String iconClass;
        final String description;
        
        CategoryData(String name, String iconClass, String description) {
            this.name = name;
            this.iconClass = iconClass;
            this.description = description;
        }
    }
    
    // Metodo pubblico per aggiungere nuove categorie programmaticamente
    @Transactional
    public Category createBookCategory(String name, String iconClass) {
        if (categoryRepository.existsByName(name)) {
            logger.warn("Tentativo di creare categoria già esistente: {}", name);
            return categoryRepository.findByName(name).orElse(null);
        }
        
        Category category = new Category();
        category.setName(name);
        category.setIconClass(iconClass != null ? iconClass : "fas fa-book");
        
        category = categoryRepository.save(category);
        logger.info("Creata nuova categoria: {}", name);
        return category;
    }
    
    // Metodo per ottenere tutte le categorie di libri
    public List<Category> getAllBookCategories() {
        return (List<Category>) categoryRepository.findAll();
    }
    
    // Metodo per ottenere categorie più popolari (con più libri)
    public List<Category> getPopularBookCategories(int limit) {
        List<Category> allCategories = (List<Category>) categoryRepository.findAll();
        
        // Ordina per numero di prodotti associati
        allCategories.forEach(category -> {
            long productCount = category.getProducts() != null ? category.getProducts().size() : 0;
            category.setProductCount(productCount);
        });
        
        return allCategories.stream()
                .sorted((c1, c2) -> Long.compare(c2.getProductCount(), c1.getProductCount()))
                .limit(limit)
                .toList();
    }
}
