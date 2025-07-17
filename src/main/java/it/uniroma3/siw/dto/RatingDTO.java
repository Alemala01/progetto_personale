package it.uniroma3.siw.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RatingDTO {
    
    @NotBlank(message = "Il titolo della recensione è obbligatorio")
    private String titolo;
    
    @NotNull(message = "Il valore del rating è obbligatorio")
    @Min(value = 1, message = "Il rating minimo è 1")
    @Max(value = 5, message = "Il rating massimo è 5")
    private Integer value;
    
    @NotBlank(message = "Il testo della recensione è obbligatorio")
    private String comment;
    
    @NotNull(message = "L'ID del prodotto è obbligatorio")
    private Long productId;
    
    // Getters and Setters
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
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
