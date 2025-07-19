package it.uniroma3.siw.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import it.uniroma3.siw.service.UsersService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private UsersService usersService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Pubblico: tutti possono visualizzare
                        .requestMatchers("/", "/products", "/products/**", "/authors", "/authors/**", 
                                        "/product/details/**", "/product/image/**", "/authors/photo/**",
                                        "/users/register", "/users/login", "/login", "/css/**", "/js/**", 
                                        "/immagini/**", "/images/**").permitAll()
                        
                        // Solo admin: possono aggiungere libri e autori
                        .requestMatchers("/product/create/**", "/product/edit/**", "/product/delete/**",
                                        "/author/create/**", "/author/edit/**", "/author/delete/**",
                                        "/admin/**").hasRole("ADMIN")
                        
                        // Utenti autenticati e admin: possono lasciare recensioni e salvare libri
                        .requestMatchers("/review/**", "/users/profile", "/users/products", "/saved-books/**").hasAnyRole("USER", "ADMIN")
                        
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/users/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/perform-logout")
                        .logoutSuccessUrl("/?logout=success")
                        .permitAll()
                )
                .userDetailsService(usersService);
        return http.build();
    }
}