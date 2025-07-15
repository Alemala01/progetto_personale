package it.uniroma3.siw.repository;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Category;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Rating;
import it.uniroma3.siw.model.Users;

public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Basic queries
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.images WHERE p.seller = :seller")
    List<Product> findBySellerWithImages(@Param("seller") Users seller);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.images")
    List<Product> findAllWithImages();
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);
    
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.seller WHERE p.id = :id")
    Optional<Product> findByIdWithSeller(@Param("id") Long id);
    
    List<Product> findBySeller(Users seller);
    
    // Products by category
    List<Product> findByCategory(Category category);
    
    // Recent products ordered by ID descending
    @Query("SELECT p FROM Product p ORDER BY p.id DESC")
    List<Product> findAllOrderByIdDesc();
    
    // Products with ratings for featured section
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.ratings")
    List<Product> findAllWithRatings();
    
    // Get ratings for a specific product
    @Query("SELECT r FROM Rating r WHERE r.product.id = :productId")
    List<Rating> findRatingsForProduct(@Param("productId") Long productId);
    
    // Search and filter queries for books (updated for library system)
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.images LEFT JOIN p.autori a " +
           "WHERE (:searchTerm = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:autore = '' OR LOWER(CONCAT(a.nome, ' ', a.cognome)) LIKE LOWER(CONCAT('%', :autore, '%'))) " +
           "AND (:annoMin IS NULL OR p.annoPubblicazione >= :annoMin) " +
           "AND (:annoMax IS NULL OR p.annoPubblicazione <= :annoMax)")
    List<Product> findProductsWithFilters(@Param("searchTerm") String searchTerm,
                                         @Param("category") Category category,
                                         @Param("autore") String autore,
                                         @Param("annoMin") Year annoMin,
                                         @Param("annoMax") Year annoMax);
    
    // Search with pagination for books
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.images LEFT JOIN p.autori a " +
           "WHERE (:searchTerm = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:autore = '' OR LOWER(CONCAT(a.nome, ' ', a.cognome)) LIKE LOWER(CONCAT('%', :autore, '%'))) " +
           "AND (:annoMin IS NULL OR p.annoPubblicazione >= :annoMin) " +
           "AND (:annoMax IS NULL OR p.annoPubblicazione <= :annoMax)")
    Page<Product> findProductsWithFiltersPageable(@Param("searchTerm") String searchTerm,
                                                 @Param("category") Category category,
                                                 @Param("autore") String autore,
                                                 @Param("annoMin") Year annoMin,
                                                 @Param("annoMax") Year annoMax,
                                                 Pageable pageable);
    
    // Additional queries for books
    @Query("SELECT DISTINCT p FROM Product p JOIN p.autori a WHERE a = :author")
    List<Product> findByAuthor(@Param("author") Author author);
    
    @Query("SELECT DISTINCT p FROM Product p WHERE p.annoPubblicazione = :anno")
    List<Product> findByAnnoPubblicazione(@Param("anno") Year anno);
    
    @Query("SELECT DISTINCT p FROM Product p WHERE p.annoPubblicazione BETWEEN :annoMin AND :annoMax")
    List<Product> findByAnnoPubblicazioneBetween(@Param("annoMin") Year annoMin, @Param("annoMax") Year annoMax);
    
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.autori WHERE p.id = :id")
    Optional<Product> findByIdWithAuthors(@Param("id") Long id);
    
    // Methods for homepage and statistics
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findTop6ByOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findTop10ByOrderByCreatedAtDesc(Pageable pageable);
}