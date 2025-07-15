package it.uniroma3.siw.dto;

import java.time.Year;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PastOrPresent;

public class ProductFormDTO {
    @NotBlank(message = "Il titolo è obbligatorio")
    private String nome; // Titolo del libro

    @NotBlank(message = "La categoria è obbligatoria")
    private String categoria; // Categoria del libro

    @NotBlank(message = "La descrizione è obbligatoria")
    private String descrizione; // Descrizione/trama del libro

    @PastOrPresent(message = "L'anno di pubblicazione non può essere nel futuro")
    private Year annoPubblicazione; // Anno di pubblicazione

    @NotEmpty(message = "Almeno un autore è obbligatorio")
    private List<Long> autoriIds; // ID degli autori selezionati

    private List<MultipartFile> images; // Immagini del libro

    // Getters and Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }
    public Year getAnnoPubblicazione() { return annoPubblicazione; }
    public void setAnnoPubblicazione(Year annoPubblicazione) { this.annoPubblicazione = annoPubblicazione; }
    public List<Long> getAutoriIds() { return autoriIds; }
    public void setAutoriIds(List<Long> autoriIds) { this.autoriIds = autoriIds; }
    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }
    
    // Metodi utility
    public boolean hasAutori() {
        return autoriIds != null && !autoriIds.isEmpty();
    }
    
    public boolean hasImages() {
        return images != null && !images.isEmpty();
    }
}