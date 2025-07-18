package it.uniroma3.siw.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.SavedBook;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.SavedBookRepository;

@Service
@Transactional
public class SavedBookService {
    
    @Autowired
    private SavedBookRepository savedBookRepository;
    
    public SavedBook saveBook(Users user, Product product) {
        // Check if already saved
        if (savedBookRepository.existsByUserAndProduct(user, product)) {
            throw new IllegalStateException("Book is already saved by this user");
        }
        
        SavedBook savedBook = new SavedBook(user, product);
        return savedBookRepository.save(savedBook);
    }
    
    public void unsaveBook(Users user, Product product) {
        savedBookRepository.deleteByUserAndProduct(user, product);
    }
    
    public boolean isBookSaved(Users user, Product product) {
        return savedBookRepository.existsByUserAndProduct(user, product);
    }
    
    public List<SavedBook> getUserSavedBooks(Users user) {
        return savedBookRepository.findByUserOrderBySavedAtDesc(user);
    }
    
    public List<Product> getUserSavedProducts(Users user) {
        return savedBookRepository.findProductsByUserOrderBySavedAtDesc(user);
    }
    
    public long countUserSavedBooks(Users user) {
        return savedBookRepository.countByUser(user);
    }
    
    public Optional<SavedBook> findByUserAndProduct(Users user, Product product) {
        return savedBookRepository.findByUserAndProduct(user, product);
    }
    
    public boolean toggleSavedBook(Users user, Product product) {
        if (isBookSaved(user, product)) {
            unsaveBook(user, product);
            return false; // Book was removed
        } else {
            saveBook(user, product);
            return true; // Book was saved
        }
    }
}
