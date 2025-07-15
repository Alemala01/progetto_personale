package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import it.uniroma3.siw.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c JOIN c.products p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Category> findTopCategories();
    
    Optional<Category> findByName(String name);
    
    // Controlla se esiste una categoria con il nome specificato
    boolean existsByName(String name);
    
    // Trova categorie per nome parziale (case insensitive)
    List<Category> findByNameContainingIgnoreCase(String name);
    
    // Trova tutte le categorie ordinate per nome
    @Query("SELECT c FROM Category c ORDER BY c.name ASC")
    List<Category> findAllOrderByName();

}
