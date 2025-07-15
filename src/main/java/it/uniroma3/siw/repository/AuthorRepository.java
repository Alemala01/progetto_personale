package it.uniroma3.siw.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    
    // Trova autori per nome
    List<Author> findByNomeContainingIgnoreCase(String nome);
    
    // Trova autori per cognome
    List<Author> findByCognomeContainingIgnoreCase(String cognome);
    
    // Trova autori per nome completo
    @Query("SELECT a FROM Author a WHERE LOWER(CONCAT(a.nome, ' ', a.cognome)) LIKE LOWER(CONCAT('%', :nomeCompleto, '%'))")
    List<Author> findByNomeCompletoContainingIgnoreCase(@Param("nomeCompleto") String nomeCompleto);
    
    // Trova autori per nazionalità
    List<Author> findByNazionalitaIgnoreCase(String nazionalita);
    
    // Trova autori ancora vivi
    @Query("SELECT a FROM Author a WHERE a.dataMorte IS NULL")
    List<Author> findAutoriVivi();
    
    // Trova autori morti
    @Query("SELECT a FROM Author a WHERE a.dataMorte IS NOT NULL")
    List<Author> findAutoriMorti();
    
    // Trova autori per anno di nascita
    @Query("SELECT a FROM Author a WHERE YEAR(a.dataNascita) = :anno")
    List<Author> findByAnnoNascita(@Param("anno") int anno);
    
    // Cerca autori con filtri multipli
    @Query("SELECT a FROM Author a WHERE " +
           "(:nome IS NULL OR LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))) AND " +
           "(:cognome IS NULL OR LOWER(a.cognome) LIKE LOWER(CONCAT('%', :cognome, '%'))) AND " +
           "(:nazionalita IS NULL OR LOWER(a.nazionalita) LIKE LOWER(CONCAT('%', :nazionalita, '%')))")
    Page<Author> findAutoriWithFilters(@Param("nome") String nome, 
                                      @Param("cognome") String cognome, 
                                      @Param("nazionalita") String nazionalita, 
                                      Pageable pageable);
    
    // Trova autori che hanno scritto almeno un libro
    @Query("SELECT DISTINCT a FROM Author a JOIN a.libri l")
    List<Author> findAutoriConLibri();
    
    // Trova autori per ID del libro
    @Query("SELECT a FROM Author a JOIN a.libri l WHERE l.id = :libroId")
    List<Author> findByLibroId(@Param("libroId") Long libroId);
    
    // Verifica se esiste un autore con nome e cognome specifici
    boolean existsByNomeAndCognome(String nome, String cognome);
    
    // Trova autore per nome e cognome esatti
    Optional<Author> findByNomeAndCognome(String nome, String cognome);
    
    // Methods for homepage and statistics
    List<Author> findTop6ByOrderByNomeAsc();
    
    @Query("SELECT a.nome, a.cognome, SIZE(a.libri) FROM Author a ORDER BY SIZE(a.libri) DESC")
    List<Object[]> findAuthorsWithBookCount();
    
    // Trova autori per nome o cognome (per ricerca)
    List<Author> findByNomeContainingIgnoreCaseOrCognomeContainingIgnoreCase(String nome, String cognome);
}
