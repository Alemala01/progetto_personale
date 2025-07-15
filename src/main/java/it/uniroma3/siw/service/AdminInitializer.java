package it.uniroma3.siw.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.uniroma3.siw.model.Users;
import it.uniroma3.siw.repository.UsersRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);
    
    @Autowired
    private UsersRepository usersRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();
    }
    
    private void createDefaultAdmin() {
        try {
            // Controlla se esiste già un admin con username "admin"
            Optional<Users> existingAdmin = usersRepository.findByUsername("admin");
            
            if (existingAdmin.isEmpty()) {
                // Crea l'admin di default
                Users admin = new Users();
                admin.setUsername("admin");
                admin.setEmail("admin@libreria.com");
                admin.setPassword(passwordEncoder.encode("admin123")); // Password temporanea
                admin.setRole("ADMIN");
                
                usersRepository.save(admin);
                
                logger.info("=".repeat(60));
                logger.info("🔐 ACCOUNT AMMINISTRATORE CREATO");
                logger.info("=".repeat(60));
                logger.info("Username: admin");
                logger.info("Password: admin123");
                logger.info("Email: admin@libreria.com");
                logger.info("IMPORTANTE: Cambiare la password dopo il primo accesso!");
                logger.info("=".repeat(60));
                
            } else {
                logger.info("Account amministratore già presente nel sistema");
            }
            
        } catch (Exception e) {
            logger.error("Errore durante la creazione dell'account admin: {}", e.getMessage(), e);
        }
    }
}
