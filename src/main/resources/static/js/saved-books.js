// Saved Books Page JavaScript

class SavedBooksManager {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadSavedStates();
    }

    bindEvents() {
        // Bind save/unsave buttons
        document.querySelectorAll('.save-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                this.toggleSavedBook(btn);
            });
        });
    }

    async toggleSavedBook(btn) {
        const productId = btn.dataset.productId;
        if (!productId) return;

        // Prevent multiple clicks
        if (btn.classList.contains('loading')) return;

        try {
            btn.classList.add('loading');
            btn.disabled = true;

            // Get CSRF token
            const csrfToken = document.querySelector('meta[name="_csrf"]');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]');
            
            const headers = {
                'Content-Type': 'application/json',
            };
            
            if (csrfToken && csrfHeader) {
                headers[csrfHeader.content] = csrfToken.content;
            }

            const response = await fetch(`/saved-books/toggle/${productId}`, {
                method: 'POST',
                headers: headers
            });

            if (response.ok) {
                const data = await response.json();
                
                if (data.saved) {
                    // Book was saved
                    btn.classList.add('saved');
                    btn.innerHTML = '<i class="fas fa-bookmark"></i> Salvato';
                    this.showNotification('Libro salvato nei preferiti!', 'success');
                } else {
                    // Book was removed - since we're on saved books page, remove the card
                    this.removeBookCard(btn);
                    this.showNotification('Libro rimosso dai preferiti!', 'info');
                }
            } else {
                const error = await response.json();
                this.showNotification(error.message || 'Errore durante il salvataggio', 'error');
            }
        } catch (error) {
            console.error('Error toggling saved book:', error);
            this.showNotification('Errore di connessione', 'error');
        } finally {
            btn.classList.remove('loading');
            btn.disabled = false;
        }
    }

    removeBookCard(btn) {
        const bookCard = btn.closest('.book-card');
        if (bookCard) {
            bookCard.style.transition = 'all 0.3s ease';
            bookCard.style.transform = 'translateY(-10px)';
            bookCard.style.opacity = '0';
            
            setTimeout(() => {
                bookCard.remove();
                this.updateStats();
                this.checkEmptyState();
            }, 300);
        }
    }

    updateStats() {
        const remainingBooks = document.querySelectorAll('.book-card').length;
        const statNumber = document.querySelector('.stat-number');
        if (statNumber) {
            statNumber.textContent = remainingBooks;
        }
    }

    checkEmptyState() {
        const booksGrid = document.querySelector('.books-grid');
        const emptyState = document.querySelector('.empty-state');
        
        if (booksGrid && booksGrid.children.length === 0) {
            // Show empty state
            if (emptyState) {
                emptyState.style.display = 'block';
            } else {
                // Create empty state if it doesn't exist
                const emptyStateHtml = `
                    <div class="empty-state">
                        <div class="empty-icon">
                            <i class="fas fa-bookmark"></i>
                        </div>
                        <h3 class="empty-title">Nessun libro salvato</h3>
                        <p class="empty-message">Non hai ancora salvato nessun libro nei preferiti.</p>
                        <a href="/products" class="btn btn-primary">
                            <i class="fas fa-search"></i>
                            Scopri i libri
                        </a>
                    </div>
                `;
                
                const booksSection = document.querySelector('.books-section');
                if (booksSection) {
                    booksSection.innerHTML = emptyStateHtml;
                }
            }
            
            // Hide stats
            const statsContainer = document.querySelector('.stats-container');
            if (statsContainer) {
                statsContainer.style.display = 'none';
            }
        }
    }

    async loadSavedStates() {
        // Check which books are saved (for consistency)
        const saveButtons = document.querySelectorAll('.save-btn');
        
        for (const btn of saveButtons) {
            const productId = btn.dataset.productId;
            if (!productId) continue;

            try {
                const response = await fetch(`/saved-books/check/${productId}`);
                if (response.ok) {
                    const data = await response.json();
                    if (data.saved) {
                        btn.classList.add('saved');
                        btn.innerHTML = '<i class="fas fa-bookmark"></i> Rimuovi';
                    }
                }
            } catch (error) {
                console.error('Error checking saved state:', error);
            }
        }
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'error' ? 'exclamation-circle' : 'info-circle'}"></i>
                <span>${message}</span>
            </div>
        `;

        // Add notification styles if not already present
        if (!document.querySelector('#notification-styles')) {
            const styles = document.createElement('style');
            styles.id = 'notification-styles';
            styles.textContent = `
                .notification {
                    position: fixed;
                    top: 20px;
                    right: 20px;
                    padding: 15px 20px;
                    border-radius: 8px;
                    color: white;
                    font-weight: 500;
                    z-index: 1000;
                    transform: translateX(100%);
                    transition: transform 0.3s ease;
                    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
                }
                
                .notification.success { background: #27ae60; }
                .notification.error { background: #e74c3c; }
                .notification.info { background: #3498db; }
                
                .notification.show {
                    transform: translateX(0);
                }
                
                .notification-content {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                }
                
                @media (max-width: 768px) {
                    .notification {
                        right: 10px;
                        left: 10px;
                        transform: translateY(-100%);
                    }
                    
                    .notification.show {
                        transform: translateY(0);
                    }
                }
            `;
            document.head.appendChild(styles);
        }

        // Add to DOM
        document.body.appendChild(notification);

        // Show notification
        setTimeout(() => notification.classList.add('show'), 100);

        // Remove notification after 3 seconds
        setTimeout(() => {
            notification.classList.remove('show');
            setTimeout(() => notification.remove(), 300);
        }, 3000);
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new SavedBooksManager();
});

// Export for use in other scripts
window.SavedBooksManager = SavedBooksManager;
