package it.uniroma3.siw.dto;

import java.time.Year;

public class ProductSearchDTO {
    private String searchTerm; // Ricerca per titolo
    private String categoria;
    private String autore; // Ricerca per autore
    private Year annoPubblicazioneMin;
    private Year annoPubblicazioneMax;
    private Integer ratingMin;
    private String sortBy = "id"; // Default sort by id (newest first)
    
    public ProductSearchDTO() {}
    
    public ProductSearchDTO(String searchTerm, String categoria, String autore, Year annoPubblicazioneMin, Year annoPubblicazioneMax, Integer ratingMin, String sortBy) {
        this.searchTerm = searchTerm;
        this.categoria = categoria;
        this.autore = autore;
        this.annoPubblicazioneMin = annoPubblicazioneMin;
        this.annoPubblicazioneMax = annoPubblicazioneMax;
        this.ratingMin = ratingMin;
        this.sortBy = sortBy != null ? sortBy : "id";
    }
    
    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }
    
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public String getAutore() {
        return autore;
    }
    
    public void setAutore(String autore) {
        this.autore = autore;
    }
    
    public Year getAnnoPubblicazioneMin() {
        return annoPubblicazioneMin;
    }
    
    public void setAnnoPubblicazioneMin(Year annoPubblicazioneMin) {
        this.annoPubblicazioneMin = annoPubblicazioneMin;
    }
    
    public Year getAnnoPubblicazioneMax() {
        return annoPubblicazioneMax;
    }
    
    public void setAnnoPubblicazioneMax(Year annoPubblicazioneMax) {
        this.annoPubblicazioneMax = annoPubblicazioneMax;
    }
    
    public Integer getRatingMin() {
        return ratingMin;
    }
    
    public void setRatingMin(Integer ratingMin) {
        this.ratingMin = ratingMin;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy != null ? sortBy : "id";
    }
    
    // Metodi di utilità
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }
    
    public boolean hasCategoria() {
        return categoria != null && !categoria.trim().isEmpty();
    }
    
    public boolean hasAutore() {
        return autore != null && !autore.trim().isEmpty();
    }
    
    public boolean hasAnnoPubblicazioneMin() {
        return annoPubblicazioneMin != null;
    }
    
    public boolean hasAnnoPubblicazioneMax() {
        return annoPubblicazioneMax != null;
    }
    
    public boolean hasRatingMin() {
        return ratingMin != null && ratingMin > 0;
    }
    
    public boolean hasFilters() {
        return hasSearchTerm() || hasCategoria() || hasAutore() || hasAnnoPubblicazioneMin() || hasAnnoPubblicazioneMax() || hasRatingMin();
    }
    
    @Override
    public String toString() {
        return "ProductSearchDTO{" +
                "searchTerm='" + searchTerm + '\'' +
                ", categoria='" + categoria + '\'' +
                ", autore='" + autore + '\'' +
                ", annoPubblicazioneMin=" + annoPubblicazioneMin +
                ", annoPubblicazioneMax=" + annoPubblicazioneMax +
                ", ratingMin=" + ratingMin +
                ", sortBy='" + sortBy + '\'' +
                '}';
    }
}
