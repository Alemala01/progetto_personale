package it.uniroma3.siw.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

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

    @Column(columnDefinition = "TEXT")
    private String biografia; // Biografia dell'autore

    @Column(columnDefinition = "BYTEA")
    private byte[] fotografia; // Fotografia dell'autore

    private String fotografiaContentType;

    @ManyToMany(mappedBy = "autori")
    private List<Product> libri; // Relazione con i libri

    public Author() {
        this.libri = new ArrayList<>();
    }

    public Author(String nome, String cognome, LocalDate dataNascita, String nazionalita) {
        this();
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.nazionalita = nazionalita;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public byte[] getFotografia() {
        return fotografia;
    }

    public void setFotografia(byte[] fotografia) {
        this.fotografia = fotografia;
    }

    public String getFotografiaContentType() {
        return fotografiaContentType;
    }

    public void setFotografiaContentType(String fotografiaContentType) {
        this.fotografiaContentType = fotografiaContentType;
    }

    public List<Product> getLibri() {
        return libri;
    }

    public void setLibri(List<Product> libri) {
        this.libri = libri != null ? libri : new ArrayList<>();
    }

    // Metodi utility
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    public boolean isVivo() {
        return dataMorte == null;
    }

    public int getEta() {
        if (dataNascita == null) return 0;
        LocalDate dataFine = isVivo() ? LocalDate.now() : dataMorte;
        return dataFine.getYear() - dataNascita.getYear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Author author = (Author) o;
        return Objects.equals(id, author.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", nazionalita='" + nazionalita + '\'' +
                '}';
    }
}
