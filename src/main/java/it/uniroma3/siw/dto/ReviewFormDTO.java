package it.uniroma3.siw.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReviewFormDTO {
    
    @NotBlank(message = "Il titolo della recensione è obbligatorio")
    private String titolo;

    @NotNull(message = "Il voto è obbligatorio")
    @Min(value = 1, message = "Il voto minimo è 1")
    @Max(value = 5, message = "Il voto massimo è 5")
    private Integer voto; // Voto da 1 a 5

    @NotBlank(message = "Il testo della recensione è obbligatorio")
    private String testo; // Testo della recensione

    private Long libroId; // ID del libro da recensire

    public ReviewFormDTO() {}

    public ReviewFormDTO(String titolo, Integer voto, String testo, Long libroId) {
        this.titolo = titolo;
        this.voto = voto;
        this.testo = testo;
        this.libroId = libroId;
    }

    // Getters and Setters
    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public Integer getVoto() {
        return voto;
    }

    public void setVoto(Integer voto) {
        this.voto = voto;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }

    public Long getLibroId() {
        return libroId;
    }

    public void setLibroId(Long libroId) {
        this.libroId = libroId;
    }

    // Metodi utility
    public boolean hasAllRequiredFields() {
        return titolo != null && !titolo.trim().isEmpty() &&
               voto != null && voto >= 1 && voto <= 5 &&
               testo != null && !testo.trim().isEmpty() &&
               libroId != null;
    }

    @Override
    public String toString() {
        return "ReviewFormDTO{" +
                "titolo='" + titolo + '\'' +
                ", voto=" + voto +
                ", testo='" + testo + '\'' +
                ", libroId=" + libroId +
                '}';
    }
}
