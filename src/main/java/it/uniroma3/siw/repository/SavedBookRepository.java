package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.SavedBook;
import it.uniroma3.siw.model.Users;

@Repository
public interface SavedBookRepository extends JpaRepository<SavedBook, Long> {
    
    List<SavedBook> findByUserOrderBySavedAtDesc(Users user);
    
    Optional<SavedBook> findByUserAndProduct(Users user, Product product);
    
    boolean existsByUserAndProduct(Users user, Product product);
    
    void deleteByUserAndProduct(Users user, Product product);
    
    @Query("SELECT COUNT(sb) FROM SavedBook sb WHERE sb.user = :user")
    long countByUser(@Param("user") Users user);
    
    @Query("SELECT sb.product FROM SavedBook sb WHERE sb.user = :user ORDER BY sb.savedAt DESC")
    List<Product> findProductsByUserOrderBySavedAtDesc(@Param("user") Users user);
}
