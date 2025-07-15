package it.uniroma3.siw.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il titolo della recensione è obbligatorio")
    private String titolo; // Titolo della recensione

    @NotNull(message = "Il valore del rating è obbligatorio")
    @Min(value = 1, message = "Il rating minimo è 1")
    @Max(value = 5, message = "Il rating massimo è 5")
    private Integer value; // Voto da 1 a 5

    @NotBlank(message = "Il testo della recensione è obbligatorio")
    private String comment; // Testo della recensione

    @ManyToOne
    @NotNull(message = "Il libro è obbligatorio")
    private Product product; // Il libro recensito

    @ManyToOne
    @NotNull(message = "L'utente è obbligatorio")
    private Users user; // L'utente che ha scritto la recensione

    private LocalDateTime createdAt;

    // Constructor
    public Rating() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
