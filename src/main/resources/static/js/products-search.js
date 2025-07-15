/**
 * Advanced Products Search Page - Sitarello
 * Interactive and modern search experience with animations, filters, and dynamic content
 */

// Global variables
let currentView = 'list';
let searchTimeout;
let isLoading = false;
let quickViewModal = null;
let filterCount = 0;

// Configuration
const CONFIG = {
    SEARCH_DELAY: 300,
    ANIMATION_DURATION: 300,
    STAGGER_DELAY: 50,
    SUGGESTIONS_LIMIT: 5
};

// Sample search suggestions (in real app, these would come from API)
const SEARCH_SUGGESTIONS = [
    'iPhone', 'Samsung', 'Laptop', 'Cuffie', 'Smartphone', 'Tablet', 
    'Fotocamera', 'Orologio', 'Scarpe', 'Abbigliamento', 'Libri', 'Giochi'
];

/**
 * Initialize the page when DOM is loaded
 */
document.addEventListener('DOMContentLoaded', function() {
    initializeSearchPage();
    initializeAnimations();
    initializeEventListeners();
    initializeFilterPills();
    initializeQuickView();
    initializeViewToggle();
    initializeAdvancedSearch();
    initializeResponsiveFeatures();
});

/**
 * Initialize main search page functionality
 */
function initializeSearchPage() {
    // Add loading states
    addLoadingStates();
    
    // Initialize search suggestions
    initializeSearchSuggestions();
    
    // Initialize filter counter
    updateFilterCounter();
    
    // Initialize product cards with enhanced animations
    initializeProductCards();
    
    // Initialize scroll animations
    initializeScrollAnimations();
    
    console.log('🔍 Search page initialized successfully');
}

/**
 * Initialize enhanced animations
 */
function initializeAnimations() {
    // Animate hero section
    animateHeroSection();
    
    // Animate search container
    animateSearchContainer();
    
    // Animate product cards with stagger effect
    animateProductCards();
    
    // Add parallax effects
    initializeParallaxEffects();
}

/**
 * Initialize all event listeners
 */
function initializeEventListeners() {
    // Search input with debouncing
    const searchInput = document.getElementById('quickSearch');
    if (searchInput) {
        searchInput.addEventListener('input', handleSearchInput);
        searchInput.addEventListener('focus', showSearchSuggestions);
        searchInput.addEventListener('blur', hideSearchSuggestions);
    }
    
    // Filter pills
    const filterPills = document.querySelectorAll('.filter-pill');
    filterPills.forEach(pill => {
        pill.addEventListener('click', handleFilterPillClick);
    });
    
    // Sort and filter changes
    const sortSelect = document.querySelector('[name="sortBy"]');
    const categorySelect = document.querySelector('[name="categoria"]');
    const ratingSelect = document.querySelector('[name="ratingMin"]');
    
    if (sortSelect) sortSelect.addEventListener('change', handleFilterChange);
    if (categorySelect) categorySelect.addEventListener('change', handleFilterChange);
    if (ratingSelect) ratingSelect.addEventListener('change', handleFilterChange);
    
    // Price range inputs
    const priceInputs = document.querySelectorAll('[name="prezzoMin"], [name="prezzoMax"]');
    priceInputs.forEach(input => {
        input.addEventListener('input', handlePriceRangeChange);
    });
    
    // Remove filter buttons
    const removeFilterButtons = document.querySelectorAll('.remove-filter');
    removeFilterButtons.forEach(button => {
        button.addEventListener('click', handleRemoveFilter);
    });
    
    // Product card hover effects
    const productCards = document.querySelectorAll('.product-card');
    productCards.forEach(card => {
        card.addEventListener('mouseenter', handleCardHover);
        card.addEventListener('mouseleave', handleCardLeave);
    });
    
    // Quick view buttons
    const quickViewButtons = document.querySelectorAll('.quick-view-btn');
    quickViewButtons.forEach(button => {
        button.addEventListener('click', handleQuickViewClick);
    });
    
    // Image gallery
    initializeImageGallery();
}

/**
 * Initialize filter pills functionality
 */
function initializeFilterPills() {
    const filterPills = document.querySelectorAll('.filter-pill');
    
    filterPills.forEach(pill => {
        // Add ripple effect
        pill.addEventListener('click', function(e) {
            createRippleEffect(e, this);
        });
        
        // Add hover animations
        pill.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-2px) scale(1.02)';
        });
        
        pill.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
        });
    });
}

/**
 * Initialize quick view modal
 */
function initializeQuickView() {
    // Create modal if it doesn't exist
    if (!document.getElementById('quickViewModal')) {
        createQuickViewModal();
    }
    
    // Initialize modal events
    const modal = document.getElementById('quickViewModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeQuickView();
            }
        });
    }
    
    // ESC key to close modal
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && quickViewModal) {
            closeQuickView();
        }
    });
}

/**
 * Initialize view toggle functionality
 */
function initializeViewToggle() {
    const gridBtn = document.getElementById('gridView');
    const listBtn = document.getElementById('listView');
    
    if (gridBtn) {
        gridBtn.addEventListener('click', () => toggleView('grid'));
    }
    
    if (listBtn) {
        listBtn.addEventListener('click', () => toggleView('list'));
    }
}

/**
 * Initialize advanced search features
 */
function initializeAdvancedSearch() {
    // Voice search (if supported)
    if ('webkitSpeechRecognition' in window) {
        initializeVoiceSearch();
    }
    
    // Keyboard shortcuts
    document.addEventListener('keydown', function(e) {
        // Ctrl/Cmd + K to focus search
        if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
            e.preventDefault();
            document.getElementById('quickSearch').focus();
        }
    });
}

/**
 * Initialize responsive features
 */
function initializeResponsiveFeatures() {
    // Mobile swipe gestures for product cards
    if ('ontouchstart' in window) {
        initializeTouchGestures();
    }
    
    // Intersection Observer for lazy loading
    initializeIntersectionObserver();
}

/**
 * Handle search input with debouncing
 */
function handleSearchInput(e) {
    clearTimeout(searchTimeout);
    const query = e.target.value;
    
    searchTimeout = setTimeout(() => {
        if (query.length >= 2) {
            showSearchSuggestions();
            // In real app, make API call here
        } else {
            hideSearchSuggestions();
        }
    }, CONFIG.SEARCH_DELAY);
}

/**
 * Initialize search suggestions
 */
function initializeSearchSuggestions() {
    const searchInput = document.getElementById('quickSearch');
    if (!searchInput) return;
    
    // Create suggestions container
    const suggestionsContainer = document.createElement('div');
    suggestionsContainer.className = 'search-suggestions';
    suggestionsContainer.innerHTML = `
        <div class="suggestions-header">
            <span class="suggestions-title">💡 Suggerimenti</span>
        </div>
        <div class="suggestions-list"></div>
    `;
    
    searchInput.parentNode.appendChild(suggestionsContainer);
}

/**
 * Show search suggestions
 */
function showSearchSuggestions() {
    const suggestionsContainer = document.querySelector('.search-suggestions');
    const searchInput = document.getElementById('quickSearch');
    
    if (!suggestionsContainer || !searchInput) return;
    
    const query = searchInput.value.toLowerCase();
    const suggestions = SEARCH_SUGGESTIONS.filter(suggestion => 
        suggestion.toLowerCase().includes(query)
    ).slice(0, CONFIG.SUGGESTIONS_LIMIT);
    
    const suggestionsList = suggestionsContainer.querySelector('.suggestions-list');
    suggestionsList.innerHTML = suggestions.map(suggestion => `
        <div class="suggestion-item" onclick="selectSuggestion('${suggestion}')">
            <span class="suggestion-icon">🔍</span>
            <span class="suggestion-text">${suggestion}</span>
        </div>
    `).join('');
    
    suggestionsContainer.classList.add('active');
    
    // Add animation
    setTimeout(() => {
        suggestionsContainer.style.opacity = '1';
        suggestionsContainer.style.transform = 'translateY(0)';
    }, 10);
}

/**
 * Hide search suggestions
 */
function hideSearchSuggestions() {
    setTimeout(() => {
        const suggestionsContainer = document.querySelector('.search-suggestions');
        if (suggestionsContainer) {
            suggestionsContainer.classList.remove('active');
            suggestionsContainer.style.opacity = '0';
            suggestionsContainer.style.transform = 'translateY(-10px)';
        }
    }, 200);
}

/**
 * Select a search suggestion
 */
function selectSuggestion(suggestion) {
    const searchInput = document.getElementById('quickSearch');
    if (searchInput) {
        searchInput.value = suggestion;
        hideSearchSuggestions();
        // Auto-submit form
        document.querySelector('.search-form').submit();
    }
}

/**
 * Handle filter pill clicks
 */
function handleFilterPillClick(e) {
    const pill = e.currentTarget;
    const filterType = pill.dataset.filter;
    
    // Add visual feedback
    pill.classList.add('active');
    
    // Focus on the appropriate input
    const input = pill.querySelector('input, select');
    if (input) {
        input.focus();
    }
    
    // Update filter counter
    updateFilterCounter();
}

/**
 * Handle filter changes
 */
function handleFilterChange(e) {
    // Add loading state
    showLoadingState();
    
    // Submit form with animation
    setTimeout(() => {
        document.querySelector('.search-form').submit();
    }, 300);
}

/**
 * Handle price range changes
 */
function handlePriceRangeChange(e) {
    clearTimeout(searchTimeout);
    
    searchTimeout = setTimeout(() => {
        handleFilterChange(e);
    }, CONFIG.SEARCH_DELAY);
}

/**
 * Handle remove filter
 */
function handleRemoveFilter(e) {
    e.preventDefault();
    const button = e.target;
    const filter = button.closest('.active-filter');
    
    // Animate removal
    filter.style.transform = 'scale(0)';
    filter.style.opacity = '0';
    
    setTimeout(() => {
        // Clear the actual filter and submit
        const filterType = button.onclick.toString().match(/'([^']+)'/)[1];
        const input = document.querySelector(`[name="${filterType}"]`);
        if (input) {
            input.value = '';
            document.querySelector('.search-form').submit();
        }
    }, 300);
}

/**
 * Update filter counter
 */
function updateFilterCounter() {
    const activeFilters = document.querySelectorAll('.active-filter');
    filterCount = activeFilters.length;
    
    // Update visual indicators
    const filterCountElement = document.querySelector('.filter-count');
    if (filterCountElement) {
        filterCountElement.textContent = filterCount;
        filterCountElement.style.display = filterCount > 0 ? 'block' : 'none';
    }
}

/**
 * Toggle view between grid and list
 */
function toggleView(view) {
    const container = document.getElementById('productsContainer');
    const gridBtn = document.getElementById('gridView');
    const listBtn = document.getElementById('listView');
    
    if (!container) return;
    
    // Update buttons
    if (view === 'grid') {
        gridBtn.classList.add('active');
        listBtn.classList.remove('active');
        container.className = 'products-container grid-view';
    } else {
        listBtn.classList.add('active');
        gridBtn.classList.remove('active');
        container.className = 'products-container list-view';
    }
    
    currentView = view;
    
    // Re-animate cards
    animateProductCards();
    
    // Save preference
    localStorage.setItem('productViewMode', view);
}

/**
 * Handle product card hover
 */
function handleCardHover(e) {
    const card = e.currentTarget;
    const overlay = card.querySelector('.card-overlay');
    
    if (overlay) {
        overlay.style.opacity = '1';
        overlay.style.visibility = 'visible';
    }
    
    // Add 3D effect
    card.style.transform = 'translateY(-8px) rotateX(5deg)';
    card.style.boxShadow = '0 20px 40px rgba(0,0,0,0.15)';
}

/**
 * Handle product card leave
 */
function handleCardLeave(e) {
    const card = e.currentTarget;
    const overlay = card.querySelector('.card-overlay');
    
    if (overlay) {
        overlay.style.opacity = '0';
        overlay.style.visibility = 'hidden';
    }
    
    // Remove 3D effect
    card.style.transform = 'translateY(0) rotateX(0)';
    card.style.boxShadow = '0 8px 25px rgba(0,0,0,0.08)';
}

/**
 * Handle quick view click
 */
function handleQuickViewClick(e) {
    e.preventDefault();
    const productId = e.currentTarget.dataset.productId || 
                      e.currentTarget.getAttribute('onclick').match(/\d+/)[0];
    
    openQuickView(productId);
}

/**
 * Open quick view modal
 */
function openQuickView(productId) {
    const modal = document.getElementById('quickViewModal');
    if (!modal) return;
    
    // Show loading state
    showQuickViewLoading();
    
    // In real app, fetch product data
    setTimeout(() => {
        loadQuickViewContent(productId);
        modal.style.display = 'flex';
        modal.style.opacity = '0';
        
        setTimeout(() => {
            modal.style.opacity = '1';
        }, 10);
    }, 300);
}

/**
 * Close quick view modal
 */
function closeQuickView() {
    const modal = document.getElementById('quickViewModal');
    if (!modal) return;
    
    modal.style.opacity = '0';
    
    setTimeout(() => {
        modal.style.display = 'none';
    }, 300);
}

/**
 * Create quick view modal
 */
function createQuickViewModal() {
    const modal = document.createElement('div');
    modal.id = 'quickViewModal';
    modal.className = 'quick-view-modal';
    modal.innerHTML = `
        <div class="modal-content">
            <button class="modal-close" onclick="closeQuickView()">×</button>
            <div class="modal-body">
                <div class="loading-spinner">
                    <div class="spinner"></div>
                    <p>Caricamento...</p>
                </div>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
}

/**
 * Show quick view loading state
 */
function showQuickViewLoading() {
    const modal = document.getElementById('quickViewModal');
    const modalBody = modal.querySelector('.modal-body');
    
    modalBody.innerHTML = `
        <div class="loading-spinner">
            <div class="spinner"></div>
            <p>Caricamento prodotto...</p>
        </div>
    `;
}

/**
 * Load quick view content
 */
function loadQuickViewContent(productId) {
    // In a real application, this would make an API call
    // For now, we'll simulate it with existing data
    const productCard = document.querySelector(`[data-product-id="${productId}"]`);
    
    if (!productCard) {
        console.error('Product card not found for ID:', productId);
        return;
    }
    
    const modal = document.getElementById('quickViewModal');
    const modalBody = modal.querySelector('.modal-body');
    
    // Extract product information from the card
    const productName = productCard.querySelector('.product-name')?.textContent || 'Prodotto';
    const productPrice = productCard.querySelector('.price-value')?.textContent || '0.00';
    const productDescription = productCard.querySelector('.product-description p')?.textContent || 'Nessuna descrizione disponibile';
    const productCategory = productCard.querySelector('.badge-text')?.textContent || 'Categoria';
    const productSeller = productCard.querySelector('.meta-value')?.textContent || 'Venditore';
    const productImage = productCard.querySelector('.featured-image')?.src || '';
    const productRating = productCard.querySelector('.product-rating')?.innerHTML || '';
    
    // Create quick view content
    modalBody.innerHTML = `
        <div class="quick-view-content">
            <div class="quick-view-header">
                <div class="quick-view-image">
                    ${productImage ? `<img src="${productImage}" alt="${productName}" class="modal-product-image" />` : `
                        <div class="modal-no-image">
                            <span class="placeholder-icon">📷</span>
                            <span class="placeholder-text">Nessuna immagine</span>
                        </div>
                    `}
                </div>
                <div class="quick-view-info">
                    <div class="modal-product-category">
                        <span class="category-icon">🏷️</span>
                        <span>${productCategory}</span>
                    </div>
                    <h2 class="modal-product-title">${productName}</h2>
                    <div class="modal-product-rating">
                        ${productRating}
                    </div>
                    <div class="modal-product-price">
                        <span class="price-symbol">€</span>
                        <span class="price-value">${productPrice}</span>
                    </div>
                </div>
            </div>
            
            <div class="quick-view-body">
                <div class="modal-product-description">
                    <h3>📝 Descrizione</h3>
                    <p>${productDescription}</p>
                </div>
                
                <div class="modal-product-seller">
                    <h3>👤 Venditore</h3>
                    <p>${productSeller}</p>
                </div>
            </div>
            
            <div class="quick-view-actions">
                <a href="/product/details/${productId}" class="btn-primary modal-btn">
                    <span class="btn-icon">👁️</span>
                    <span class="btn-text">Visualizza dettagli completi</span>
                </a>
                <button onclick="closeQuickView()" class="btn-secondary modal-btn">
                    <span class="btn-icon">✖️</span>
                    <span class="btn-text">Chiudi</span>
                </button>
            </div>
        </div>
    `;
}

/**
 * Initialize product cards with enhanced animations
 */
function initializeProductCards() {
    const productCards = document.querySelectorAll('.product-card');
    
    productCards.forEach((card, index) => {
        // Initial state
        card.style.opacity = '0';
        card.style.transform = 'translateY(30px)';
        
        // Add enhanced hover effects
        card.addEventListener('mouseenter', function() {
            this.style.transform = 'translateY(-8px) scale(1.02)';
            this.style.boxShadow = '0 20px 40px rgba(0,0,0,0.15)';
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '0 8px 25px rgba(0,0,0,0.08)';
        });
    });
}

/**
 * Animate product cards with stagger effect
 */
function animateProductCards() {
    const productCards = document.querySelectorAll('.product-card');
    
    productCards.forEach((card, index) => {
        setTimeout(() => {
            card.style.transition = 'all 0.6s cubic-bezier(0.4, 0, 0.2, 1)';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * CONFIG.STAGGER_DELAY);
    });
}

/**
 * Animate hero section
 */
function animateHeroSection() {
    const heroElements = document.querySelectorAll('.hero-title, .hero-subtitle, .hero-stats');
    
    heroElements.forEach((element, index) => {
        element.style.opacity = '0';
        element.style.transform = 'translateY(30px)';
        
        setTimeout(() => {
            element.style.transition = 'all 0.8s cubic-bezier(0.4, 0, 0.2, 1)';
            element.style.opacity = '1';
            element.style.transform = 'translateY(0)';
        }, index * 200);
    });
}

/**
 * Animate search container
 */
function animateSearchContainer() {
    const searchContainer = document.querySelector('.search-container');
    if (!searchContainer) return;
    
    searchContainer.style.opacity = '0';
    searchContainer.style.transform = 'translateY(50px)';
    
    setTimeout(() => {
        searchContainer.style.transition = 'all 0.8s cubic-bezier(0.4, 0, 0.2, 1)';
        searchContainer.style.opacity = '1';
        searchContainer.style.transform = 'translateY(0)';
    }, 400);
}

/**
 * Initialize parallax effects (stable version)
 */
function initializeParallaxEffects() {
    const heroSection = document.querySelector('.hero-search-section');
    if (!heroSection) return;
    
    // More stable parallax that maintains z-index hierarchy
    let ticking = false;
    
    function updateParallax() {
        const scrolled = window.pageYOffset;
        const heroHeight = heroSection.offsetHeight;
        const rate = scrolled * -0.1; // Further reduced parallax effect
        
        // Only apply transform if section is still visible and not scrolled too far
        if (scrolled < heroHeight && scrolled >= 0) {
            heroSection.style.transform = `translate3d(0, ${rate}px, 0)`;
        } else if (scrolled >= heroHeight) {
            // Reset transform when section is out of view
            heroSection.style.transform = 'translate3d(0, 0, 0)';
        }
        
        ticking = false;
    }
    
    function requestTick() {
        if (!ticking) {
            requestAnimationFrame(updateParallax);
            ticking = true;
        }
    }
    
    window.addEventListener('scroll', requestTick);
    
    // Initial call
    updateParallax();
}

/**
 * Initialize scroll animations
 */
function initializeScrollAnimations() {
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -100px 0px'
    };
    
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
            }
        });
    }, observerOptions);
    
    const animateElements = document.querySelectorAll('.results-section, .empty-state');
    animateElements.forEach(element => observer.observe(element));
}

/**
 * Initialize image gallery
 */
function initializeImageGallery() {
    const thumbnails = document.querySelectorAll('.thumbnail-image');
    
    thumbnails.forEach(thumb => {
        thumb.addEventListener('click', function() {
            const mainImage = this.closest('.product-gallery').querySelector('.featured-image');
            if (mainImage) {
                // Add transition effect
                mainImage.style.opacity = '0';
                setTimeout(() => {
                    mainImage.src = this.src;
                    mainImage.style.opacity = '1';
                }, 150);
            }
        });
    });
}

/**
 * Create ripple effect
 */
function createRippleEffect(e, element) {
    const ripple = document.createElement('span');
    const rect = element.getBoundingClientRect();
    const size = Math.max(rect.width, rect.height);
    
    ripple.style.width = ripple.style.height = size + 'px';
    ripple.style.left = (e.clientX - rect.left - size / 2) + 'px';
    ripple.style.top = (e.clientY - rect.top - size / 2) + 'px';
    ripple.classList.add('ripple');
    
    element.appendChild(ripple);
    
    setTimeout(() => {
        ripple.remove();
    }, 600);
}

/**
 * Show loading state
 */
function showLoadingState() {
    if (isLoading) return;
    
    isLoading = true;
    const container = document.getElementById('productsContainer');
    
    if (container) {
        container.style.opacity = '0.6';
        container.style.pointerEvents = 'none';
    }
    
    // Add loading spinner
    const spinner = document.createElement('div');
    spinner.className = 'loading-overlay';
    spinner.innerHTML = `
        <div class="spinner"></div>
        <p>Caricamento...</p>
    `;
    
    document.body.appendChild(spinner);
}

/**
 * Hide loading state
 */
function hideLoadingState() {
    isLoading = false;
    const container = document.getElementById('productsContainer');
    const spinner = document.querySelector('.loading-overlay');
    
    if (container) {
        container.style.opacity = '1';
        container.style.pointerEvents = 'auto';
    }
    
    if (spinner) {
        spinner.remove();
    }
}

/**
 * Add loading states to various elements
 */
function addLoadingStates() {
    const buttons = document.querySelectorAll('.btn-search, .btn-reset');
    
    buttons.forEach(button => {
        button.addEventListener('click', function() {
            this.classList.add('loading');
            this.disabled = true;
            
            setTimeout(() => {
                this.classList.remove('loading');
                this.disabled = false;
            }, 2000);
        });
    });
}

/**
 * Initialize voice search (if supported)
 */
function initializeVoiceSearch() {
    const voiceBtn = document.createElement('button');
    voiceBtn.className = 'voice-search-btn';
    voiceBtn.innerHTML = '🎤';
    voiceBtn.title = 'Ricerca vocale';
    
    const searchGroup = document.querySelector('.search-input-group');
    if (searchGroup) {
        searchGroup.appendChild(voiceBtn);
        
        voiceBtn.addEventListener('click', startVoiceSearch);
    }
}

/**
 * Start voice search
 */
function startVoiceSearch() {
    const recognition = new webkitSpeechRecognition();
    recognition.lang = 'it-IT';
    recognition.continuous = false;
    recognition.interimResults = false;
    
    recognition.onstart = function() {
        document.querySelector('.voice-search-btn').classList.add('listening');
    };
    
    recognition.onresult = function(event) {
        const transcript = event.results[0][0].transcript;
        document.getElementById('quickSearch').value = transcript;
        document.querySelector('.search-form').submit();
    };
    
    recognition.onend = function() {
        document.querySelector('.voice-search-btn').classList.remove('listening');
    };
    
    recognition.start();
}

/**
 * Initialize touch gestures for mobile
 */
function initializeTouchGestures() {
    const productCards = document.querySelectorAll('.product-card');
    
    productCards.forEach(card => {
        let startX = 0;
        let startY = 0;
        
        card.addEventListener('touchstart', function(e) {
            startX = e.touches[0].clientX;
            startY = e.touches[0].clientY;
        });
        
        card.addEventListener('touchmove', function(e) {
            e.preventDefault();
            const deltaX = e.touches[0].clientX - startX;
            const deltaY = e.touches[0].clientY - startY;
            
            // Swipe left to quick view
            if (deltaX < -50 && Math.abs(deltaY) < 50) {
                const productId = this.dataset.productId;
                if (productId) {
                    openQuickView(productId);
                }
            }
        });
    });
}

/**
 * Initialize intersection observer for lazy loading
 */
function initializeIntersectionObserver() {
    const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const img = entry.target;
                img.src = img.dataset.src;
                img.classList.add('loaded');
                imageObserver.unobserve(img);
            }
        });
    });
    
    const lazyImages = document.querySelectorAll('img[data-src]');
    lazyImages.forEach(img => imageObserver.observe(img));
}

/**
 * Global functions for template integration
 */
window.removeFilter = function(filterName) {
    const input = document.querySelector(`[name="${filterName}"]`);
    if (input) {
        input.value = '';
        document.querySelector('.search-form').submit();
    }
};

window.switchImage = function(src, productId) {
    const card = document.querySelector(`[data-product-id="${productId}"]`);
    const mainImage = card?.querySelector('.featured-image');
    
    if (mainImage) {
        mainImage.style.opacity = '0';
        setTimeout(() => {
            mainImage.src = src;
            mainImage.style.opacity = '1';
        }, 150);
    }
};

window.quickView = function(productId) {
    openQuickView(productId);
};

// Console welcome message
console.log('🚀 Advanced Products Search initialized successfully!');
console.log('Features: Search suggestions, Voice search, Quick view, Animations, Touch gestures');
console.log('Press Ctrl+K to focus search bar');

// Performance monitoring
window.addEventListener('load', () => {
    console.log('⚡ Page loaded in:', performance.now().toFixed(2) + 'ms');
});
