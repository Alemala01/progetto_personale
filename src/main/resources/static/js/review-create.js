// Review Create Page JavaScript - Sitarello

document.addEventListener('DOMContentLoaded', function() {
    initializeRatingSystem();
    initializeCharacterCounter();
    initializeFormValidation();
});

// Rating system functionality
function initializeRatingSystem() {
    const ratingInputs = document.querySelectorAll('input[name="voto"]');
    const ratingDescription = document.getElementById('rating-description');
    
    const descriptions = {
        1: 'Scarso - Non lo consiglio',
        2: 'Sufficiente - Poteva essere meglio',
        3: 'Buono - Mi è piaciuto',
        4: 'Molto buono - Lo consiglio',
        5: 'Eccellente - Assolutamente da leggere!'
    };
    
    ratingInputs.forEach(input => {
        input.addEventListener('change', function() {
            const rating = this.value;
            ratingDescription.textContent = descriptions[rating] || 'Seleziona una valutazione';
            ratingDescription.style.color = rating ? '#f59e0b' : '#94a3b8';
        });
    });
    
    // Check if there's a pre-selected value
    const checkedInput = document.querySelector('input[name="voto"]:checked');
    if (checkedInput) {
        const rating = checkedInput.value;
        ratingDescription.textContent = descriptions[rating];
        ratingDescription.style.color = '#f59e0b';
    }
}

// Character counter for review text
function initializeCharacterCounter() {
    const textarea = document.getElementById('testo');
    const charCount = document.getElementById('char-count');
    
    if (textarea && charCount) {
        function updateCounter() {
            const currentLength = textarea.value.length;
            charCount.textContent = currentLength;
            
            // Change color based on character count
            if (currentLength > 900) {
                charCount.style.color = '#ef4444'; // Red when close to limit
            } else if (currentLength > 800) {
                charCount.style.color = '#f59e0b'; // Orange when getting close
            } else {
                charCount.style.color = '#94a3b8'; // Default color
            }
        }
        
        textarea.addEventListener('input', updateCounter);
        
        // Initial count
        updateCounter();
    }
}

// Form validation
function initializeFormValidation() {
    const form = document.querySelector('.review-form');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            let isValid = true;
            
            // Clear previous error styles
            clearErrorStyles();
            
            // Validate title
            const titleInput = document.getElementById('titolo');
            if (!titleInput.value.trim()) {
                showFieldError(titleInput, 'Il titolo è obbligatorio');
                isValid = false;
            } else if (titleInput.value.length > 100) {
                showFieldError(titleInput, 'Il titolo non può superare i 100 caratteri');
                isValid = false;
            }
            
            // Validate rating
            const ratingInputs = document.querySelectorAll('input[name="voto"]');
            const ratingSelected = Array.from(ratingInputs).some(input => input.checked);
            if (!ratingSelected) {
                showRatingError('Seleziona una valutazione');
                isValid = false;
            }
            
            // Validate review text
            const textareaInput = document.getElementById('testo');
            if (!textareaInput.value.trim()) {
                showFieldError(textareaInput, 'Il testo della recensione è obbligatorio');
                isValid = false;
            } else if (textareaInput.value.length > 1000) {
                showFieldError(textareaInput, 'La recensione non può superare i 1000 caratteri');
                isValid = false;
            } else if (textareaInput.value.trim().length < 10) {
                showFieldError(textareaInput, 'La recensione deve essere di almeno 10 caratteri');
                isValid = false;
            }
            
            if (!isValid) {
                e.preventDefault();
                
                // Scroll to first error
                const firstError = document.querySelector('.field-error');
                if (firstError) {
                    firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }
}

// Helper functions for validation
function clearErrorStyles() {
    // Remove existing error messages
    const existingErrors = document.querySelectorAll('.field-error');
    existingErrors.forEach(error => error.remove());
    
    // Remove error styling from inputs
    const inputs = document.querySelectorAll('.form-input, .form-textarea');
    inputs.forEach(input => {
        input.style.borderColor = '';
        input.style.boxShadow = '';
    });
    
    // Remove rating error styling
    const ratingContainer = document.querySelector('.rating-input');
    if (ratingContainer) {
        ratingContainer.style.borderColor = '';
        ratingContainer.style.boxShadow = '';
    }
}

function showFieldError(field, message) {
    // Add error styling to field
    field.style.borderColor = '#ef4444';
    field.style.boxShadow = '0 0 0 3px rgba(239, 68, 68, 0.1)';
    
    // Create and insert error message
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error error-message';
    errorDiv.innerHTML = `<i class="fas fa-exclamation-circle"></i><span>${message}</span>`;
    
    field.parentNode.insertBefore(errorDiv, field.nextSibling);
}

function showRatingError(message) {
    const ratingContainer = document.querySelector('.rating-input');
    if (ratingContainer) {
        // Add error styling
        ratingContainer.style.border = '1px solid #ef4444';
        ratingContainer.style.borderRadius = '8px';
        ratingContainer.style.padding = '8px';
        ratingContainer.style.boxShadow = '0 0 0 3px rgba(239, 68, 68, 0.1)';
        
        // Create and insert error message
        const errorDiv = document.createElement('div');
        errorDiv.className = 'field-error error-message';
        errorDiv.innerHTML = `<i class="fas fa-exclamation-circle"></i><span>${message}</span>`;
        
        ratingContainer.parentNode.insertBefore(errorDiv, ratingContainer.nextSibling);
    }
}

// Auto-resize textarea
function initializeTextareaResize() {
    const textarea = document.getElementById('testo');
    if (textarea) {
        textarea.addEventListener('input', function() {
            this.style.height = 'auto';
            this.style.height = Math.min(this.scrollHeight, 300) + 'px';
        });
    }
}

// Initialize textarea resize
document.addEventListener('DOMContentLoaded', initializeTextareaResize);
