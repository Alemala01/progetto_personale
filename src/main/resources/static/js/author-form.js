// Author Form JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeAuthorForm();
    checkBookFormData();
});

function initializeAuthorForm() {
    // File input handling
    initializeFileInput();
    
    // Biography character counter
    initializeBiographyCounter();
    
    // Date validation
    initializeDateValidation();
    
    // Form validation
    initializeFormValidation();
}

// File Input Handling
function initializeFileInput() {
    const fileInput = document.getElementById('fotografia');
    const fileInputText = document.querySelector('.file-input-text');
    const filePreview = document.getElementById('file-preview');
    const previewImg = document.getElementById('preview-img');
    
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            
            if (file) {
                // Update file input text
                fileInputText.textContent = file.name;
                
                // Validate file
                if (validateImageFile(file)) {
                    // Show preview
                    const reader = new FileReader();
                    reader.onload = function(e) {
                        previewImg.src = e.target.result;
                        filePreview.style.display = 'block';
                    };
                    reader.readAsDataURL(file);
                } else {
                    // Reset if invalid
                    resetFileInput();
                }
            } else {
                resetFileInput();
            }
        });
    }
}

function validateImageFile(file) {
    // Check file size (5MB max)
    const maxSize = 5 * 1024 * 1024; // 5MB in bytes
    if (file.size > maxSize) {
        showError('La foto deve essere massimo 5MB');
        return false;
    }
    
    // Check file type
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'];
    if (!allowedTypes.includes(file.type)) {
        showError('Formato file non supportato. Usa JPG, PNG o GIF');
        return false;
    }
    
    return true;
}

function removePreview() {
    resetFileInput();
}

function resetFileInput() {
    const fileInput = document.getElementById('fotografia');
    const fileInputText = document.querySelector('.file-input-text');
    const filePreview = document.getElementById('file-preview');
    
    if (fileInput) {
        fileInput.value = '';
        fileInputText.textContent = 'Scegli file...';
        filePreview.style.display = 'none';
    }
}

// Biography Character Counter
function initializeBiographyCounter() {
    const biografiaTextarea = document.getElementById('biografia');
    const charCount = document.getElementById('bio-char-count');
    
    if (biografiaTextarea && charCount) {
        // Initial count
        updateCharacterCount();
        
        // Update on input
        biografiaTextarea.addEventListener('input', updateCharacterCount);
        
        function updateCharacterCount() {
            const currentLength = biografiaTextarea.value.length;
            const maxLength = biografiaTextarea.maxLength || 2000;
            
            charCount.textContent = currentLength;
            
            // Change color based on usage
            if (currentLength > maxLength * 0.9) {
                charCount.style.color = '#e74c3c';
            } else if (currentLength > maxLength * 0.7) {
                charCount.style.color = '#f39c12';
            } else {
                charCount.style.color = '#3498db';
            }
        }
    }
}

// Date Validation
function initializeDateValidation() {
    const dataNascita = document.getElementById('dataNascita');
    const dataMorte = document.getElementById('dataMorte');
    
    if (dataNascita && dataMorte) {
        dataNascita.addEventListener('change', validateDates);
        dataMorte.addEventListener('change', validateDates);
        
        function validateDates() {
            const birthDate = new Date(dataNascita.value);
            const deathDate = new Date(dataMorte.value);
            const today = new Date();
            
            // Clear previous errors
            clearDateErrors();
            
            // Validate birth date
            if (dataNascita.value && birthDate > today) {
                showDateError(dataNascita, 'La data di nascita non può essere futura');
                return;
            }
            
            // Validate death date
            if (dataMorte.value) {
                if (deathDate > today) {
                    showDateError(dataMorte, 'La data di morte non può essere futura');
                    return;
                }
                
                if (dataNascita.value && deathDate <= birthDate) {
                    showDateError(dataMorte, 'La data di morte deve essere successiva alla nascita');
                    return;
                }
            }
        }
    }
}

function showDateError(input, message) {
    // Remove existing error
    const existingError = input.parentNode.querySelector('.date-error');
    if (existingError) {
        existingError.remove();
    }
    
    // Add new error
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message date-error';
    errorDiv.innerHTML = `<i class="fas fa-exclamation-circle"></i><span>${message}</span>`;
    input.parentNode.appendChild(errorDiv);
    
    // Highlight input
    input.style.borderColor = '#e74c3c';
}

function clearDateErrors() {
    const dateInputs = ['dataNascita', 'dataMorte'];
    dateInputs.forEach(id => {
        const input = document.getElementById(id);
        if (input) {
            const error = input.parentNode.querySelector('.date-error');
            if (error) {
                error.remove();
            }
            input.style.borderColor = '';
        }
    });
}

// Form Validation
function initializeFormValidation() {
    const form = document.querySelector('.author-form');
    
    if (form) {
        form.addEventListener('submit', function(e) {
            if (!validateForm()) {
                e.preventDefault();
                showError('Correggi gli errori nel form prima di continuare');
                
                // Scroll to first error
                const firstError = document.querySelector('.error-message');
                if (firstError) {
                    firstError.scrollIntoView({ 
                        behavior: 'smooth', 
                        block: 'center' 
                    });
                }
            }
        });
    }
}

function validateForm() {
    let isValid = true;
    
    // Validate required fields
    const requiredFields = ['nome', 'cognome'];
    requiredFields.forEach(fieldId => {
        const field = document.getElementById(fieldId);
        if (field && !field.value.trim()) {
            showFieldError(field, 'Questo campo è obbligatorio');
            isValid = false;
        } else if (field) {
            clearFieldError(field);
        }
    });
    
    // Validate dates
    const dataNascita = document.getElementById('dataNascita');
    const dataMorte = document.getElementById('dataMorte');
    
    if (dataNascita && dataMorte) {
        const birthDate = new Date(dataNascita.value);
        const deathDate = new Date(dataMorte.value);
        const today = new Date();
        
        if (dataNascita.value && birthDate > today) {
            isValid = false;
        }
        
        if (dataMorte.value) {
            if (deathDate > today || (dataNascita.value && deathDate <= birthDate)) {
                isValid = false;
            }
        }
    }
    
    // Validate file if present
    const fileInput = document.getElementById('fotografia');
    if (fileInput && fileInput.files[0]) {
        if (!validateImageFile(fileInput.files[0])) {
            isValid = false;
        }
    }
    
    return isValid;
}

function showFieldError(field, message) {
    // Remove existing error
    clearFieldError(field);
    
    // Add new error
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message field-error';
    errorDiv.innerHTML = `<i class="fas fa-exclamation-circle"></i><span>${message}</span>`;
    field.parentNode.appendChild(errorDiv);
    
    // Highlight field
    field.style.borderColor = '#e74c3c';
}

function clearFieldError(field) {
    const error = field.parentNode.querySelector('.field-error');
    if (error) {
        error.remove();
    }
    field.style.borderColor = '';
}

// Delete Modal Functions
function confirmDelete() {
    const modal = document.getElementById('delete-modal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}

function closeDeleteModal() {
    const modal = document.getElementById('delete-modal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// Close modal when clicking outside
document.addEventListener('click', function(e) {
    const modal = document.getElementById('delete-modal');
    if (modal && e.target === modal) {
        closeDeleteModal();
    }
});

// Close modal with Escape key
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeDeleteModal();
    }
});

// Check if we came from book creation page
function checkBookFormData() {
    // Check if there's book form data in localStorage
    const hasBookFormData = localStorage.getItem('bookFormData') !== null;
    const referrer = document.referrer;
    const cameFromBookCreate = referrer.includes('/product/create') || hasBookFormData;
    
    if (cameFromBookCreate) {
        // Show the back to book creation links
        const backToBookLink = document.getElementById('backToBookLink');
        const backToBookBtn = document.getElementById('backToBookBtn');
        const authorsLink = document.getElementById('authorsLink');
        const cancelBtn = document.getElementById('cancelBtn');
        
        if (backToBookLink) {
            backToBookLink.style.display = 'inline';
        }
        if (backToBookBtn) {
            backToBookBtn.style.display = 'inline-block';
        }
        if (authorsLink) {
            authorsLink.style.display = 'none';
        }
        if (cancelBtn) {
            cancelBtn.style.display = 'none';
        }
    }
}

// Utility Functions
function showError(message) {
    // Create or update error notification
    let errorNotification = document.querySelector('.error-notification');
    
    if (!errorNotification) {
        errorNotification = document.createElement('div');
        errorNotification.className = 'error-notification';
        document.body.appendChild(errorNotification);
    }
    
    errorNotification.innerHTML = `
        <i class="fas fa-exclamation-circle"></i>
        <span>${message}</span>
        <button onclick="this.parentNode.remove()" class="close-notification">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    errorNotification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #e74c3c;
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 15px rgba(231, 76, 60, 0.3);
        z-index: 1001;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        max-width: 400px;
        animation: slideInRight 0.3s ease;
    `;
    
    // Auto remove after 5 seconds
    setTimeout(() => {
        if (errorNotification && errorNotification.parentNode) {
            errorNotification.remove();
        }
    }, 5000);
}

function showSuccess(message) {
    // Create success notification
    const successNotification = document.createElement('div');
    successNotification.className = 'success-notification';
    successNotification.innerHTML = `
        <i class="fas fa-check-circle"></i>
        <span>${message}</span>
        <button onclick="this.parentNode.remove()" class="close-notification">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    successNotification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #27ae60;
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 15px rgba(39, 174, 96, 0.3);
        z-index: 1001;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        max-width: 400px;
        animation: slideInRight 0.3s ease;
    `;
    
    document.body.appendChild(successNotification);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        if (successNotification && successNotification.parentNode) {
            successNotification.remove();
        }
    }, 3000);
}

// CSS for animations
const style = document.createElement('style');
style.textContent = `
    @keyframes slideInRight {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    .close-notification {
        background: none;
        border: none;
        color: inherit;
        cursor: pointer;
        padding: 0;
        margin-left: auto;
        opacity: 0.8;
        transition: opacity 0.3s ease;
    }
    
    .close-notification:hover {
        opacity: 1;
    }
`;
document.head.appendChild(style);
