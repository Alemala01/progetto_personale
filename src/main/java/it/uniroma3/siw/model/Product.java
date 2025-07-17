package it.uniroma3.siw.model;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Il titolo è obbligatorio")
    private String name; // Ora rappresenta il titolo del libro

    @NotNull(message = "La categoria è obbligatoria")
    @ManyToOne
    private Category category; // Categorie per libri (Fiction, Non-fiction, etc.)

    @PastOrPresent(message = "L'anno di pubblicazione non può essere nel futuro")
    private Year annoPubblicazione; // Anno di pubblicazione del libro

    @ManyToOne
    private Users seller; // Chi ha inserito il libro nel sistema (admin o utente)

    @NotBlank(message = "La descrizione è obbligatoria")
    private String description; // Descrizione/trama del libro

    @ManyToMany
    @JoinTable(
        name = "libro_autore",
        joinColumns = @JoinColumn(name = "libro_id"),
        inverseJoinColumns = @JoinColumn(name = "autore_id")
    )
    private List<Author> autori; // Gli autori del libro (relazione many-to-many)

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images; // Immagini del libro
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings; // Recensioni del libro
    
    private LocalDateTime createdAt; // Data e ora di creazione
    
    @Transient
    private String base64Image;

    public Product() {
        this.images = new ArrayList<>();
        this.ratings = new ArrayList<>();
        this.autori = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Year getAnnoPubblicazione() { return annoPubblicazione; }
    public void setAnnoPubblicazione(Year annoPubblicazione) { this.annoPubblicazione = annoPubblicazione; }
    public Users getSeller() { return seller; }
    public void setSeller(Users seller) { this.seller = seller; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<Author> getAutori() { return autori; }
    public void setAutori(List<Author> autori) { this.autori = autori != null ? autori : new ArrayList<>(); }
    public List<ProductImage> getImages() { return images; }
    public void setImages(List<ProductImage> images) { this.images = images != null ? images : new ArrayList<>(); }
    public List<Rating> getRatings() { return ratings; }
    public void setRatings(List<Rating> ratings) { this.ratings = ratings != null ? ratings : new ArrayList<>(); }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public String getBase64Image() { return base64Image; }
    public void setBase64Image(String base64Image) { this.base64Image = base64Image; }
    
    // Metodi utility per i rating/recensioni
    public Double getAverageRating() {
        if (this.ratings == null || this.ratings.isEmpty()) {
            return 0.0;
        }
        return this.ratings.stream()
                .mapToInt(Rating::getValue)
                .average()
                .orElse(0.0);
    }
    
    public Integer getRatingCount() {
        return this.ratings != null ? this.ratings.size() : 0;
    }
    
    // Metodi utility per gli autori
    public String getAutoriNomi() {
        if (this.autori == null || this.autori.isEmpty()) {
            return "Autore sconosciuto";
        }
        return this.autori.stream()
                .map(Author::getNomeCompleto)
                .reduce((a, b) -> a + ", " + b)
                .orElse("Autore sconosciuto");
    }
    
    // Alias method for templates that expect getAutoriNames()
    public String getAutoriNames() {
        return getAutoriNomi();
    }
    
    public boolean hasAutore(Author autore) {
        return this.autori != null && this.autori.contains(autore);
    }
    
    // Metodo per aggiungere un autore
    public void addAutore(Author autore) {
        if (this.autori == null) {
            this.autori = new ArrayList<>();
        }
        if (!this.autori.contains(autore)) {
            this.autori.add(autore);
            if (autore.getLibri() == null) {
                autore.setLibri(new ArrayList<>());
            }
            if (!autore.getLibri().contains(this)) {
                autore.getLibri().add(this);
            }
        }
    }
    
    // Metodo per rimuovere un autore
    public void removeAutore(Author autore) {
        if (this.autori != null) {
            this.autori.remove(autore);
            if (autore.getLibri() != null) {
                autore.getLibri().remove(this);
            }
        }
    }
}