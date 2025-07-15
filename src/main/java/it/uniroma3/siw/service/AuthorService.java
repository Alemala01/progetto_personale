package it.uniroma3.siw.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.repository.AuthorRepository;

@Service
public class AuthorService {
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Transactional
    public Author saveAuthor(Author author) {
        return authorRepository.save(author);
    }
    
    @Transactional
    public Author saveAuthorWithPhoto(Author author, MultipartFile fotografia) throws IOException {
        if (fotografia != null && !fotografia.isEmpty()) {
            author.setFotografia(fotografia.getBytes());
            author.setFotografiaContentType(fotografia.getContentType());
        }
        return authorRepository.save(author);
    }
    
    public Optional<Author> findById(Long id) {
        return authorRepository.findById(id);
    }
    
    public List<Author> findAll() {
        return authorRepository.findAll();
    }
    
    public List<Author> findByNome(String nome) {
        return authorRepository.findByNomeContainingIgnoreCase(nome);
    }
    
    public List<Author> findByCognome(String cognome) {
        return authorRepository.findByCognomeContainingIgnoreCase(cognome);
    }
    
    public List<Author> findByNomeCompleto(String nomeCompleto) {
        return authorRepository.findByNomeCompletoContainingIgnoreCase(nomeCompleto);
    }
    
    public List<Author> findByNazionalita(String nazionalita) {
        return authorRepository.findByNazionalitaIgnoreCase(nazionalita);
    }
    
    public List<Author> findAutoriVivi() {
        return authorRepository.findAutoriVivi();
    }
    
    public List<Author> findAutoriMorti() {
        return authorRepository.findAutoriMorti();
    }
    
    public List<Author> findByAnnoNascita(int anno) {
        return authorRepository.findByAnnoNascita(anno);
    }
    
    public Page<Author> findAutoriWithFilters(String nome, String cognome, String nazionalita, Pageable pageable) {
        return authorRepository.findAutoriWithFilters(nome, cognome, nazionalita, pageable);
    }
    
    public List<Author> findAutoriConLibri() {
        return authorRepository.findAutoriConLibri();
    }
    
    public List<Author> findByLibroId(Long libroId) {
        return authorRepository.findByLibroId(libroId);
    }
    
    public boolean existsByNomeAndCognome(String nome, String cognome) {
        return authorRepository.existsByNomeAndCognome(nome, cognome);
    }
    
    public Optional<Author> findByNomeAndCognome(String nome, String cognome) {
        return authorRepository.findByNomeAndCognome(nome, cognome);
    }
    
    @Transactional
    public Author updateAuthor(Author author) {
        return authorRepository.save(author);
    }
    
    @Transactional
    public Author updateAuthorWithPhoto(Author author, MultipartFile fotografia) throws IOException {
        if (fotografia != null && !fotografia.isEmpty()) {
            author.setFotografia(fotografia.getBytes());
            author.setFotografiaContentType(fotografia.getContentType());
        }
        return authorRepository.save(author);
    }
    
    @Transactional
    public void deleteAuthor(Long id) {
        authorRepository.deleteById(id);
    }
    
    @Transactional
    public void deleteAuthor(Author author) {
        // Prima rimuovere la relazione con i libri
        if (author.getLibri() != null) {
            author.getLibri().forEach(libro -> libro.removeAutore(author));
        }
        authorRepository.delete(author);
    }
    
    // Metodi utility
    public List<String> getAllNazionalita() {
        return authorRepository.findAll().stream()
                .map(Author::getNazionalita)
                .distinct()
                .sorted()
                .toList();
    }
    
    public long countAuthors() {
        return authorRepository.count();
    }
    
    public long countAutoriVivi() {
        return authorRepository.findAutoriVivi().size();
    }
    
    public long countAutoriMorti() {
        return authorRepository.findAutoriMorti().size();
    }
}
