package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Rating;
import it.uniroma3.siw.model.Users;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    List<Rating> findByProduct(Product product);
    
    List<Rating> findByUser(Users user);
    
    @Query("SELECT r FROM Rating r WHERE r.product.id = :productId")
    List<Rating> findByProductId(@Param("productId") Long productId);
    
    @Query("SELECT AVG(r.value) FROM Rating r WHERE r.product.id = :productId")
    Double findAverageRatingByProductId(@Param("productId") Long productId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.product.id = :productId")
    Integer countByProductId(@Param("productId") Long productId);
    
    Optional<Rating> findByProductAndUser(Product product, Users user);
    
    @Query("SELECT r FROM Rating r WHERE r.product.id = :productId AND r.user.id = :userId")
    Optional<Rating> findByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);
    
    boolean existsByProductAndUser(Product product, Users user);
    
    @Query("SELECT r FROM Rating r WHERE r.product = :product ORDER BY r.createdAt DESC")
    List<Rating> findByProductOrderByCreatedAtDesc(@Param("product") Product product);
}
