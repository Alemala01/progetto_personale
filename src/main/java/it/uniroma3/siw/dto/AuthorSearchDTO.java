package it.uniroma3.siw.dto;

public class AuthorSearchDTO {
    
    private String searchTerm; // Ricerca per nome o cognome
    private String nazionalita; // Filtro per nazionalità
    private Integer annoNascitaMin;
    private Integer annoNascitaMax;
    private Boolean soloVivi; // Mostra solo autori ancora vivi
    private String sortBy = "nome"; // Default sort by nome
    
    public AuthorSearchDTO() {}
    
    public AuthorSearchDTO(String searchTerm, String nazionalita, Integer annoNascitaMin, 
                          Integer annoNascitaMax, Boolean soloVivi, String sortBy) {
        this.searchTerm = searchTerm;
        this.nazionalita = nazionalita;
        this.annoNascitaMin = annoNascitaMin;
        this.annoNascitaMax = annoNascitaMax;
        this.soloVivi = soloVivi;
        this.sortBy = sortBy != null ? sortBy : "nome";
    }
    
    // Getters and Setters
    public String getSearchTerm() {
        return searchTerm;
    }
    
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
    
    public String getNazionalita() {
        return nazionalita;
    }
    
    public void setNazionalita(String nazionalita) {
        this.nazionalita = nazionalita;
    }
    
    public Integer getAnnoNascitaMin() {
        return annoNascitaMin;
    }
    
    public void setAnnoNascitaMin(Integer annoNascitaMin) {
        this.annoNascitaMin = annoNascitaMin;
    }
    
    public Integer getAnnoNascitaMax() {
        return annoNascitaMax;
    }
    
    public void setAnnoNascitaMax(Integer annoNascitaMax) {
        this.annoNascitaMax = annoNascitaMax;
    }
    
    public Boolean getSoloVivi() {
        return soloVivi;
    }
    
    public void setSoloVivi(Boolean soloVivi) {
        this.soloVivi = soloVivi;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy != null ? sortBy : "nome";
    }
    
    // Metodi di utilità
    public boolean hasSearchTerm() {
        return searchTerm != null && !searchTerm.trim().isEmpty();
    }
    
    public boolean hasNazionalita() {
        return nazionalita != null && !nazionalita.trim().isEmpty();
    }
    
    public boolean hasAnnoNascitaMin() {
        return annoNascitaMin != null && annoNascitaMin > 0;
    }
    
    public boolean hasAnnoNascitaMax() {
        return annoNascitaMax != null && annoNascitaMax > 0;
    }
    
    public boolean hasSoloVivi() {
        return soloVivi != null && soloVivi;
    }
    
    public boolean hasFilters() {
        return hasSearchTerm() || hasNazionalita() || hasAnnoNascitaMin() || 
               hasAnnoNascitaMax() || hasSoloVivi();
    }
    
    @Override
    public String toString() {
        return "AuthorSearchDTO{" +
                "searchTerm='" + searchTerm + '\'' +
                ", nazionalita='" + nazionalita + '\'' +
                ", annoNascitaMin=" + annoNascitaMin +
                ", annoNascitaMax=" + annoNascitaMax +
                ", soloVivi=" + soloVivi +
                ", sortBy='" + sortBy + '\'' +
                '}';
    }
}
