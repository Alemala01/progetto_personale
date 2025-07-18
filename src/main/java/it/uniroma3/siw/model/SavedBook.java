package it.uniroma3.siw.model;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "saved_books", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class SavedBook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    private LocalDateTime savedAt;
    
    public SavedBook() {}
    
    public SavedBook(Users user, Product product) {
        this.user = user;
        this.product = product;
    }
    
    @PrePersist
    protected void onCreate() {
        savedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Users getUser() {
        return user;
    }
    
    public void setUser(Users user) {
        this.user = user;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public LocalDateTime getSavedAt() {
        return savedAt;
    }
    
    public void setSavedAt(LocalDateTime savedAt) {
        this.savedAt = savedAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SavedBook)) return false;
        SavedBook savedBook = (SavedBook) o;
        return Objects.equals(user, savedBook.user) && Objects.equals(product, savedBook.product);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, product);
    }
    
    @Override
    public String toString() {
        return "SavedBook{" +
                "id=" + id +
                ", user=" + user.getUsername() +
                ", product=" + product.getName() +
                ", savedAt=" + savedAt +
                '}';
    }
}
