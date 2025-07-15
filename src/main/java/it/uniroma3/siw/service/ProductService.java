package it.uniroma3.siw.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import it.uniroma3.siw.dto.ProductFormDTO;
import it.uniroma3.siw.model.Author;
import it.uniroma3.siw.model.Category;
import it.uniroma3.siw.model.Product;
import it.uniroma3.siw.model.ProductImage;
import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.AuthorRepository;
import it.uniroma3.siw.repository.CategoryRepository;
import it.uniroma3.siw.repository.ProductImageRepository;
import it.uniroma3.siw.repository.ProductRepository;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private AuthorRepository authorRepository;

    // METODI RIMOSSI: saveImagesToSession e saveDetailsToSession non più necessari
    // Ora si usa solo saveProduct per il flusso moderno

    @Transactional
    public Product saveProduct(ProductFormDTO productFormDTO, List<byte[]> imageDataList, Users seller) {
        Product product = new Product();
        product.setName(productFormDTO.getNome());
        
        // Convert categoria string to Category entity
        if (productFormDTO.getCategoria() != null && !productFormDTO.getCategoria().trim().isEmpty()) {
            Category category = categoryRepository.findByName(productFormDTO.getCategoria())
                .orElse(null);
            product.setCategory(category);
        }
        
        product.setDescription(productFormDTO.getDescrizione());
        product.setAnnoPubblicazione(productFormDTO.getAnnoPubblicazione());
        product.setSeller(seller);
        
        // Aggiungi gli autori
        if (productFormDTO.hasAutori()) {
            List<Author> autori = authorRepository.findAllById(productFormDTO.getAutoriIds());
            product.setAutori(autori);
            // Aggiorna la relazione bidirezionale
            for (Author autore : autori) {
                autore.getLibri().add(product);
            }
        }
        
        // Ensure images list is initialized
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }
        
        product = productRepository.save(product);

        for (byte[] imageData : imageDataList) {
            ProductImage image = new ProductImage();
            image.setImageData(imageData);
            image.setContentType("image/jpeg"); // Default content type
            image.setProduct(product);
            productImageRepository.save(image);
            product.getImages().add(image);
        }
        return productRepository.save(product);
    }
    
    @Transactional
    public Product createProductWithImages(ProductFormDTO productFormDTO, List<String> imagePaths, Users seller) throws IOException {
        Product product = new Product();
        product.setName(productFormDTO.getNome());
        
        // Convert categoria string to Category entity
        if (productFormDTO.getCategoria() != null && !productFormDTO.getCategoria().trim().isEmpty()) {
            Category category = categoryRepository.findByName(productFormDTO.getCategoria())
                .orElse(null);
            product.setCategory(category);
        }
        
        product.setDescription(productFormDTO.getDescrizione());
        product.setAnnoPubblicazione(productFormDTO.getAnnoPubblicazione());
        product.setSeller(seller);
        
        // Aggiungi gli autori
        if (productFormDTO.hasAutori()) {
            List<Author> autori = authorRepository.findAllById(productFormDTO.getAutoriIds());
            product.setAutori(autori);
            // Aggiorna la relazione bidirezionale
            for (Author autore : autori) {
                autore.getLibri().add(product);
            }
        }
        
        // Ensure images list is initialized
        if (product.getImages() == null) {
            product.setImages(new ArrayList<>());
        }
        
        product = productRepository.save(product);

        // Create ProductImage entities from image paths
        for (String imagePath : imagePaths) {
            // For this implementation, we'll assume imagePaths contains base64 data or file paths
            // In a real implementation, you'd load the image data from the file system
            ProductImage image = new ProductImage();
            // This is a simplified implementation - in reality you'd load from files
            image.setImageData(imagePath.getBytes()); // Placeholder
            image.setContentType("image/jpeg"); // Default content type
            image.setProduct(product);
            productImageRepository.save(image);
            product.getImages().add(image);
        }
        return productRepository.save(product);
    }
    
    @Transactional
    public void updateProductImages(Product product, List<MultipartFile> images) throws IOException {
        // Remove existing images
        for (ProductImage image : product.getImages()) {
            productImageRepository.delete(image);
        }
        product.getImages().clear();
        
        // Add new images
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                ProductImage productImage = new ProductImage();
                productImage.setImageData(image.getBytes());
                productImage.setContentType(image.getContentType());
                productImage.setProduct(product);
                productImageRepository.save(productImage);
                product.getImages().add(productImage);
            }
        }
        productRepository.save(product);
    }
    
    public void updateProductDetails(Product product, ProductFormDTO productFormDTO) {
        product.setName(productFormDTO.getNome());
        
        // Convert categoria string to Category entity
        if (productFormDTO.getCategoria() != null && !productFormDTO.getCategoria().trim().isEmpty()) {
            Category category = categoryRepository.findByName(productFormDTO.getCategoria())
                .orElse(null);
            product.setCategory(category);
        }
        
        product.setDescription(productFormDTO.getDescrizione());
        product.setAnnoPubblicazione(productFormDTO.getAnnoPubblicazione());
        
        // Aggiorna gli autori
        if (productFormDTO.hasAutori()) {
            // Rimuovi le relazioni esistenti
            for (Author autore : product.getAutori()) {
                autore.getLibri().remove(product);
            }
            product.getAutori().clear();
            
            // Aggiungi i nuovi autori
            List<Author> nuoviAutori = authorRepository.findAllById(productFormDTO.getAutoriIds());
            product.setAutori(nuoviAutori);
            // Aggiorna la relazione bidirezionale
            for (Author autore : nuoviAutori) {
                autore.getLibri().add(product);
            }
        }
        
        productRepository.save(product);
    }
    
    @Transactional
    public void deleteProduct(Product product) {
        // Rimuovi le relazioni con gli autori
        for (Author autore : product.getAutori()) {
            autore.getLibri().remove(product);
        }
        product.getAutori().clear();
        
        // Delete associated images first
        for (ProductImage image : product.getImages()) {
            productImageRepository.delete(image);
        }
        productRepository.delete(product);
    }
}