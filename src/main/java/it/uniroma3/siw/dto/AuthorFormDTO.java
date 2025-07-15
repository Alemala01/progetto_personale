package it.uniroma3.siw.dto;

import java.time.LocalDate;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

public class AuthorFormDTO {
    
    @NotBlank(message = "Il nome è obbligatorio")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio")
    private String cognome;

    @Past(message = "La data di nascita deve essere nel passato")
    private LocalDate dataNascita;

    @Past(message = "La data di morte deve essere nel passato")
    private LocalDate dataMorte; // Opzionale

    @NotBlank(message = "La nazionalità è obbligatoria")
    private String nazionalita;

    private String biografia; // Biografia dell'autore

    private MultipartFile fotografia; // Fotografia dell'autore

    public AuthorFormDTO() {
    }

    public AuthorFormDTO(String nome, String cognome, LocalDate dataNascita, String nazionalita) {
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.nazionalita = nazionalita;
    }

    // Getters and Setters
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public void setDataNascita(LocalDate dataNascita) {
        this.dataNascita = dataNascita;
    }

    public LocalDate getDataMorte() {
        return dataMorte;
    }

    public void setDataMorte(LocalDate dataMorte) {
        this.dataMorte = dataMorte;
    }

    public String getNazionalita() {
        return nazionalita;
    }

    public void setNazionalita(String nazionalita) {
        this.nazionalita = nazionalita;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public MultipartFile getFotografia() {
        return fotografia;
    }

    public void setFotografia(MultipartFile fotografia) {
        this.fotografia = fotografia;
    }

    // Metodi utility
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    public boolean hasFotografia() {
        return fotografia != null && !fotografia.isEmpty();
    }

    @Override
    public String toString() {
        return "AuthorFormDTO{" +
                "nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", dataNascita=" + dataNascita +
                ", dataMorte=" + dataMorte +
                ", nazionalita='" + nazionalita + '\'' +
                ", biografia='" + biografia + '\'' +
                ", hasFotografia=" + hasFotografia() +
                '}';
    }
}
