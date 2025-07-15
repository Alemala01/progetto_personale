package it.uniroma3.siw.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    
    @Query("SELECT i FROM ProductImage i WHERE i.product = :product")
    List<ProductImage> findByProduct(@Param("product") Product product);
    
    @Query("SELECT i FROM ProductImage i WHERE i.product.id = :productId")
    List<ProductImage> findByProductId(@Param("productId") Long productId);
}