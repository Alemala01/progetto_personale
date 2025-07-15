// Navbar Enhanced Interactions - Sitarello
document.addEventListener('DOMContentLoaded', function() {
    const navbar = document.querySelector('.navbar');
    const navBrand = document.querySelector('.nav-brand');
    const navLinks = document.querySelectorAll('.nav-links a');
    const dropdownToggle = document.querySelector('.dropdown-toggle');
    
    // Aggiungi classe enhanced alla navbar
    if (navbar) {
        navbar.classList.add('navbar-enhanced');
        
        // Aggiungi container per particelle
        const particles = document.createElement('div');
        particles.className = 'navbar-particles';
        navbar.appendChild(particles);
        
        // Aggiungi scroll indicator
        const scrollIndicator = document.createElement('div');
        scrollIndicator.className = 'scroll-indicator';
        navbar.appendChild(scrollIndicator);
    }
    
    // Effetto typing per il brand
    if (navBrand) {
        const brandText = navBrand.querySelector('a');
        if (brandText) {
            brandText.classList.add('nav-brand-typing');
            
            // Rimuovi l'effetto dopo l'animazione
            setTimeout(() => {
                brandText.classList.remove('nav-brand-typing');
            }, 4000);
        }
    }
    
    // Effetto magnetico sui link
    navLinks.forEach(link => {
        link.classList.add('magnetic');
        
        link.addEventListener('mouseenter', function(e) {
            this.style.transform = 'translateY(-3px) scale(1.05)';
        });
        
        link.addEventListener('mouseleave', function(e) {
            this.style.transform = '';
        });
        
        link.addEventListener('mousemove', function(e) {
            const rect = this.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            const centerX = rect.width / 2;
            const centerY = rect.height / 2;
            const deltaX = (x - centerX) * 0.1;
            const deltaY = (y - centerY) * 0.1;
            
            this.style.transform = `translateY(-3px) scale(1.05) translate(${deltaX}px, ${deltaY}px)`;
        });
    });
    
    // Ripple effect sui click
    function createRipple(event) {
        const button = event.currentTarget;
        const rect = button.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        const x = event.clientX - rect.left - size / 2;
        const y = event.clientY - rect.top - size / 2;
        
        const ripple = document.createElement('span');
        ripple.className = 'ripple';
        ripple.style.width = ripple.style.height = size + 'px';
        ripple.style.left = x + 'px';
        ripple.style.top = y + 'px';
        
        button.appendChild(ripple);
        
        setTimeout(() => {
            ripple.remove();
        }, 600);
    }
    
    // Aggiungi ripple effect ai link e bottoni
    navLinks.forEach(link => {
        link.addEventListener('click', createRipple);
        link.style.position = 'relative';
        link.style.overflow = 'hidden';
    });
    
    if (dropdownToggle) {
        dropdownToggle.addEventListener('click', createRipple);
        dropdownToggle.style.position = 'relative';
        dropdownToggle.style.overflow = 'hidden';
    }
    
    // Effetto scroll per la navbar
    let lastScrollY = window.scrollY;
    
    window.addEventListener('scroll', () => {
        const scrollY = window.scrollY;
        
        if (scrollY > 50) {
            navbar.classList.add('navbar-scrolled');
        } else {
            navbar.classList.remove('navbar-scrolled');
        }
        
        // Aggiorna scroll indicator
        const scrollIndicator = navbar.querySelector('.scroll-indicator');
        if (scrollIndicator) {
            const windowHeight = document.documentElement.scrollHeight - window.innerHeight;
            const scrolled = (scrollY / windowHeight) * 100;
            scrollIndicator.style.transform = `scaleX(${scrolled / 100})`;
        }
        
        lastScrollY = scrollY;
    });
    
    // Aggiungi glow effect agli elementi interattivi
    const interactiveElements = document.querySelectorAll('.nav-links a, .dropdown-toggle, .nav-search button');
    interactiveElements.forEach(element => {
        element.classList.add('nav-glow');
    });
    
    // Animazione staggered per i link al caricamento - rimossa per evitare sovrapposizioni
    // navLinks.forEach((link, index) => {
    //     link.style.animationDelay = `${1 + (index * 0.1)}s`;
    // });
    
    // Invece, aggiungi una classe per indicare che sono caricati
    navLinks.forEach((link, index) => {
        link.classList.add('nav-loaded');
        // Ritardo graduale per l'effetto hover
        link.style.transitionDelay = `${index * 0.05}s`;
    });
    
    // Aggiungi effetto hover personalizzato per la ricerca
    const searchForm = document.querySelector('.nav-search form');
    const searchInput = document.querySelector('.nav-search input');
    
    if (searchForm && searchInput) {
        searchInput.addEventListener('focus', function() {
            searchForm.style.transform = 'translateY(-2px) scale(1.02)';
        });
        
        searchInput.addEventListener('blur', function() {
            searchForm.style.transform = '';
        });
    }
    
    // Parallax effect sottile per la navbar
    window.addEventListener('scroll', () => {
        const scrolled = window.pageYOffset;
        const rate = scrolled * -0.5;
        
        if (navbar) {
            navbar.style.transform = `translateY(${rate}px)`;
        }
    });
    
    // Preloader per la navbar
    const preloader = document.createElement('div');
    preloader.innerHTML = `
        <div style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 4px;
            background: linear-gradient(45deg, #667eea, #764ba2);
            transform: scaleX(0);
            transform-origin: left;
            animation: loadingProgress 1.5s ease-in-out forwards;
            z-index: 9999;
        "></div>
    `;
    
    document.body.appendChild(preloader);
    
    // Rimuovi preloader dopo l'animazione
    setTimeout(() => {
        preloader.remove();
    }, 1500);
    
    // Stile per l'animazione di caricamento
    const style = document.createElement('style');
    style.textContent = `
        @keyframes loadingProgress {
            0% {
                transform: scaleX(0);
            }
            100% {
                transform: scaleX(1);
            }
        }
    `;
    document.head.appendChild(style);
    
    // Gestione responsive per evitare sovrapposizioni
    function handleResponsiveNavbar() {
        const navbar = document.querySelector('.navbar');
        const navLinks = document.querySelector('.nav-links');
        const dropdown = document.querySelector('.dropdown');
        
        if (window.innerWidth <= 768) {
            // Mobile: stack verticalmente
            navbar.classList.add('navbar-mobile');
            if (dropdown) {
                dropdown.classList.add('dropdown-mobile');
            }
        } else {
            // Desktop: layout orizzontale
            navbar.classList.remove('navbar-mobile');
            if (dropdown) {
                dropdown.classList.remove('dropdown-mobile');
            }
        }
    }
    
    // Esegui al caricamento e al resize
    handleResponsiveNavbar();
    window.addEventListener('resize', handleResponsiveNavbar);
    
    // Gestione del dropdown su mobile
    if (dropdownToggle) {
        dropdownToggle.addEventListener('click', function(e) {
            if (window.innerWidth <= 768) {
                e.preventDefault();
                const dropdown = this.closest('.dropdown');
                const menu = dropdown.querySelector('.dropdown-menu');
                
                // Toggle del menu su mobile
                if (menu.style.display === 'block') {
                    menu.style.display = 'none';
                } else {
                    menu.style.display = 'block';
                }
            }
        });
    }
});
