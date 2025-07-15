/* Homepage JavaScript - Sitarello */

document.addEventListener('DOMContentLoaded', function() {
    // Gestione scroll orizzontale per i prodotti recenti
    initHorizontalScroll();
    
    // Gestione errori immagini
    initImageErrorHandling();
    
    // Animazioni al caricamento
    initScrollAnimations();
    
    // Gestione ricerca con Enter
    initSearchEnhancements();
});

/**
 * Inizializza lo scroll orizzontale per le sezioni dei prodotti
 */
function initHorizontalScroll() {
    const scrollContainers = document.querySelectorAll('.products-scroll');
    
    scrollContainers.forEach(container => {
        // Scroll con rotella del mouse
        container.addEventListener('wheel', function(e) {
            if (e.deltaY !== 0) {
                e.preventDefault();
                container.scrollLeft += e.deltaY;
            }
        });
        
        // Scroll con touch su dispositivi mobili
        let isDown = false;
        let startX;
        let scrollLeft;
        
        container.addEventListener('mousedown', function(e) {
            isDown = true;
            container.classList.add('scrolling');
            startX = e.pageX - container.offsetLeft;
            scrollLeft = container.scrollLeft;
        });
        
        container.addEventListener('mouseleave', function() {
            isDown = false;
            container.classList.remove('scrolling');
        });
        
        container.addEventListener('mouseup', function() {
            isDown = false;
            container.classList.remove('scrolling');
        });
        
        container.addEventListener('mousemove', function(e) {
            if (!isDown) return;
            e.preventDefault();
            const x = e.pageX - container.offsetLeft;
            const walk = (x - startX) * 2;
            container.scrollLeft = scrollLeft - walk;
        });
    });
}

/**
 * Gestisce gli errori di caricamento delle immagini
 */
function initImageErrorHandling() {
    const images = document.querySelectorAll('img');
    
    images.forEach(img => {
        img.addEventListener('error', function() {
            // Sostituisce l'immagine con un placeholder
            this.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDMwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIzMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRjNGNEY2Ii8+CjxwYXRoIGQ9Ik0xMzAgOTBIMTcwVjExMEgxMzBWOTBaIiBmaWxsPSIjOUNBM0FGIi8+CjxwYXRoIGQ9Ik0xNDAgMTAwSDEyMFYxMjBIMTQwVjEwMFoiIGZpbGw9IiM5Q0EzQUYiLz4KPHN2Zz4K';
            this.alt = 'Immagine non disponibile';
            this.style.backgroundColor = '#f3f4f6';
        });
        
        // Lazy loading per le immagini
        if ('loading' in HTMLImageElement.prototype) {
            img.loading = 'lazy';
        } else {
            // Fallback per browser che non supportano lazy loading nativo
            const imageObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const lazyImage = entry.target;
                        lazyImage.src = lazyImage.dataset.src;
                        observer.unobserve(lazyImage);
                    }
                });
            });
            
            if (img.dataset.src) {
                imageObserver.observe(img);
            }
        }
    });
}

/**
 * Inizializza le animazioni al scroll
 */
function initScrollAnimations() {
    // Controllo se l'API IntersectionObserver è supportata
    if ('IntersectionObserver' in window) {
        const animateElements = document.querySelectorAll('.product-card, .feature-card, .category-card');
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                    observer.unobserve(entry.target);
                }
            });
        }, {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px'
        });
        
        // Imposta lo stato iniziale per l'animazione
        animateElements.forEach(el => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(20px)';
            el.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
            observer.observe(el);
        });
    }
}

/**
 * Migliora la funzionalità di ricerca
 */
function initSearchEnhancements() {
    const searchInput = document.querySelector('.search-input');
    const searchForm = document.querySelector('.search-form');
    
    if (searchInput && searchForm) {
        // Gestione del placeholder animato
        const placeholders = [
            'Cerca prodotti, categorie...',
            'Prova "elettronica"...',
            'Cerca "abbigliamento"...',
            'Trova "libri"...',
            'Scopri "arte"...'
        ];
        
        let currentPlaceholder = 0;
        
        // Cambia placeholder ogni 3 secondi se l'input non è focalizzato
        setInterval(() => {
            if (document.activeElement !== searchInput) {
                currentPlaceholder = (currentPlaceholder + 1) % placeholders.length;
                searchInput.placeholder = placeholders[currentPlaceholder];
            }
        }, 3000);
        
        // Gestione focus
        searchInput.addEventListener('focus', function() {
            this.placeholder = 'Cerca...';
        });
        
        searchInput.addEventListener('blur', function() {
            if (this.value === '') {
                this.placeholder = placeholders[currentPlaceholder];
            }
        });
        
        // Validazione form
        searchForm.addEventListener('submit', function(e) {
            if (searchInput.value.trim() === '') {
                e.preventDefault();
                searchInput.focus();
                searchInput.style.border = '1px solid #ef4444';
                setTimeout(() => {
                    searchInput.style.border = 'none';
                }, 2000);
            }
        });
    }
}

/**
 * Gestione della chiusura del messaggio di benvenuto
 */
function closeWelcomeMessage() {
    const welcomeSection = document.querySelector('.welcome-section');
    if (welcomeSection) {
        welcomeSection.style.opacity = '0';
        welcomeSection.style.transform = 'translateY(-20px)';
        
        setTimeout(() => {
            welcomeSection.style.display = 'none';
        }, 300);
    }
}

/**
 * Funzione per il feedback di caricamento
 */
function showLoading(element) {
    if (element) {
        element.style.opacity = '0.7';
        element.style.pointerEvents = 'none';
        
        // Aggiunge un indicatore di caricamento
        const loader = document.createElement('div');
        loader.className = 'loading-indicator';
        loader.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
        loader.style.position = 'absolute';
        loader.style.top = '50%';
        loader.style.left = '50%';
        loader.style.transform = 'translate(-50%, -50%)';
        loader.style.zIndex = '1000';
        
        element.style.position = 'relative';
        element.appendChild(loader);
    }
}

/**
 * Rimuove l'indicatore di caricamento
 */
function hideLoading(element) {
    if (element) {
        element.style.opacity = '1';
        element.style.pointerEvents = 'auto';
        
        const loader = element.querySelector('.loading-indicator');
        if (loader) {
            loader.remove();
        }
    }
}

// Esporta le funzioni per uso globale se necessario
window.SitarelloHomepage = {
    closeWelcomeMessage,
    showLoading,
    hideLoading
};

// Rendi disponibile la funzione anche globalmente per facilità d'uso
window.closeWelcomeMessage = closeWelcomeMessage;
