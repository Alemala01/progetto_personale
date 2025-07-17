package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.dto.RatingDTO;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.Rating;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.ProductRepository;
import it.uniroma3.siw.repository.RatingRepository;

@Service
public class RatingService {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    /**
     * Trova tutti i rating per un prodotto specifico
     */
    @Transactional(readOnly = true)
    public List<Rating> findByProductId(Long productId) {
        return ratingRepository.findByProductId(productId);
    }
    
    /**
     * Trova il rating medio per un prodotto
     */
    @Transactional(readOnly = true)
    public Double findAverageRatingByProductId(Long productId) {
        Double avgRating = ratingRepository.findAverageRatingByProductId(productId);
        return avgRating != null ? avgRating : 0.0;
    }
    
    /**
     * Conta i rating per un prodotto
     */
    @Transactional(readOnly = true)
    public Integer countRatingsByProductId(Long productId) {
        return ratingRepository.countByProductId(productId);
    }
    
    /**
     * Trova il rating di un utente specifico per un prodotto
     */
    @Transactional(readOnly = true)
    public Optional<Rating> findByProductIdAndUserId(Long productId, Long userId) {
        return ratingRepository.findByProductIdAndUserId(productId, userId);
    }
    
    /**
     * Verifica se un utente ha già inserito un rating per un prodotto
     */
    @Transactional(readOnly = true)
    public boolean hasUserRatedProduct(Product product, Users user) {
        return ratingRepository.existsByProductAndUser(product, user);
    }
    
    /**
     * Salva un nuovo rating o aggiorna un rating esistente
     */
    @Transactional
    public Rating saveRating(RatingDTO ratingDTO, Users user) {
        if (ratingDTO == null || ratingDTO.getProductId() == null || user == null) {
            throw new IllegalArgumentException("I dati del rating o dell'utente sono nulli");
        }
        
        Optional<Product> productOpt = productRepository.findById(ratingDTO.getProductId());
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("Prodotto non trovato con ID: " + ratingDTO.getProductId());
        }
        
        Product product = productOpt.get();
        
        // Verifica che l'utente non stia valutando un proprio prodotto
        if (product.getSeller() != null && product.getSeller().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Non puoi valutare i tuoi prodotti");
        }
        
        // Verifica se l'utente ha già inserito un rating per questo prodotto
        Optional<Rating> existingRatingOpt = ratingRepository.findByProductAndUser(product, user);
        
        Rating rating;
        if (existingRatingOpt.isPresent()) {
            // Aggiorna il rating esistente
            rating = existingRatingOpt.get();
            rating.setTitolo(ratingDTO.getTitolo());
            rating.setValue(ratingDTO.getValue());
            rating.setComment(ratingDTO.getComment());
        } else {
            // Crea un nuovo rating
            rating = new Rating();
            rating.setProduct(product);
            rating.setUser(user);
            rating.setTitolo(ratingDTO.getTitolo());
            rating.setValue(ratingDTO.getValue());
            rating.setComment(ratingDTO.getComment());
        }
        
        return ratingRepository.save(rating);
    }
    
    /**
     * Elimina un rating
     */
    @Transactional
    public void deleteRating(Long ratingId, Users user) {
        Optional<Rating> ratingOpt = ratingRepository.findById(ratingId);
        if (ratingOpt.isPresent()) {
            Rating rating = ratingOpt.get();
            // Verifica che l'utente sia il proprietario del rating
            if (rating.getUser().getId().equals(user.getId())) {
                ratingRepository.delete(rating);
            } else {
                throw new IllegalArgumentException("Non hai i permessi per eliminare questo rating");
            }
        } else {
            throw new IllegalArgumentException("Recensione non trovata");
        }
    }
    
    /**
     * Elimina un rating come amministratore (può eliminare qualsiasi rating)
     */
    @Transactional
    public void deleteRatingAsAdmin(Long ratingId) {
        Optional<Rating> ratingOpt = ratingRepository.findById(ratingId);
        if (ratingOpt.isPresent()) {
            Rating rating = ratingOpt.get();
            ratingRepository.delete(rating);
        } else {
            throw new IllegalArgumentException("Recensione non trovata");
        }
    }
}
