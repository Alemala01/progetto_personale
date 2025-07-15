// SOLUZIONE SEMPLICE: File input sovrapposto
console.log('=== PRODUCT CREATE JS LOADED ===');

// RIMOSSO IL PRIMO GESTORE - ora tutto viene gestito dalla classe EnhancedProductCreator

// Enhanced Product Creation JavaScript - Sitarello
console.log('Enhanced Product Create JS loaded');

class EnhancedProductCreator {
    constructor() {
        // Configuration
        this.currentStep = 1;
        this.totalSteps = 3;
        this.images = [];
        this.maxImages = 5;
        this.maxFileSize = 5 * 1024 * 1024; // 5MB
        this.allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
        
        // Auto-save
        this.autoSaveTimer = null;
        this.formData = {};
        this.hasUnsavedChanges = false;
        
        // Validation
        this.validationRules = {
            productName: { required: true, minLength: 3, maxLength: 100 },
            annoPubblicazione: { required: false, min: 1000, max: 2024 },
            productDescription: { required: true, minLength: 10, maxLength: 2000 },
            productCategory: { required: true }
        };
        
        // Price suggestions data (mock)
        this.priceSuggestions = {};
        
        // Category suggestions
        this.categorySuggestions = {
            'romanzo': ['Fiction', 'Letteratura'],
            'giallo': ['Fiction', 'Thriller'],
            'fantasy': ['Fiction', 'Fantasy'],
            'storia': ['Saggistica', 'Storia'],
            'biografia': ['Saggistica', 'Biografie'],
            'scienza': ['Saggistica', 'Scienza'],
            'cucina': ['Hobby', 'Cucina'],
            'viaggi': ['Hobby', 'Viaggi']
        };
        
        this.init();
    }
    
    init() {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', () => this.setupAll());
        } else {
            this.setupAll();
        }
    }
    
    setupAll() {
        console.log('Setting up enhanced product creator...');
        
        this.setupEventListeners();
        this.setupImageUpload();
        this.setupFormValidation();
        this.setupPreview();
        this.setupAutoSave();
        this.setupSmartFeatures();
        this.updateProgressBar();
        this.showStep(this.currentStep);
        
        console.log('Enhanced product creator initialized');
    }
    
    setupEventListeners() {
        // Navigation buttons
        const nextBtn = document.getElementById('nextBtn');
        const prevBtn = document.getElementById('prevBtn');
        const submitBtn = document.getElementById('submitBtn');
        
        if (nextBtn) nextBtn.addEventListener('click', () => this.nextStep());
        if (prevBtn) prevBtn.addEventListener('click', () => this.prevStep());
        if (submitBtn) submitBtn.addEventListener('click', (e) => this.submitForm(e));
        
        // Form inputs for live preview and auto-save
        const inputs = ['productName', 'annoPubblicazione', 'productDescription', 'productCategory'];
        inputs.forEach(inputId => {
            const element = document.getElementById(inputId);
            if (element) {
                element.addEventListener('input', () => {
                    this.updatePreview();
                    this.markUnsavedChanges();
                    this.scheduleAutoSave();
                });
                element.addEventListener('blur', () => this.validateField(inputId));
            }
        });
        
        // Character counters
        this.setupCharacterCounters();
        
        // Tips toggle
        this.setupTipsToggle();
        
        // Format buttons (if present)
        this.setupFormatButtons();
        
        // Initialize author selection
        initializeAuthorSelection();
    }
    
    setupImageUpload() {
        const uploadArea = document.getElementById('imageUploadArea');
        const fileInput = document.getElementById('imageInput');
        const uploadButton = document.getElementById('uploadButton');
        
        if (!uploadArea || !fileInput || !uploadButton) {
            console.error('Upload elements not found');
            return;
        }
        
        console.log('Setting up image upload handlers...');
        
        // File input configuration
        fileInput.setAttribute('accept', 'image/jpeg,image/jpg,image/png,image/webp');
        fileInput.setAttribute('multiple', 'true');
        
        // Flags per evitare doppie aperture
        let isFileDialogOpen = false;
        
        // Funzione per aprire il file dialog con protezione contro doppie aperture
        const openFileDialog = (source) => {
            if (isFileDialogOpen) {
                console.log('🚫 File dialog già aperto, ignoro click da:', source);
                return;
            }
            
            console.log('🎯 Opening file dialog from:', source);
            isFileDialogOpen = true;
            
            // Reset flag dopo un breve delay
            setTimeout(() => {
                isFileDialogOpen = false;
            }, 500);
            
            fileInput.click();
        };
        
        // Gestore per il bottone di upload
        uploadButton.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            openFileDialog('button');
        });
        
        // Gestore per l'area di upload (solo se il click non viene dal bottone)
        uploadArea.addEventListener('click', (e) => {
            // Ignora i click che vengono dal bottone
            if (e.target === uploadButton || uploadButton.contains(e.target)) {
                return;
            }
            
            e.preventDefault();
            e.stopPropagation();
            openFileDialog('upload-area');
        });
        
        // File input change
        fileInput.addEventListener('change', (e) => {
            if (e.target.files.length > 0) {
                console.log(`🎉 ${e.target.files.length} file(s) selected`);
                this.handleFileSelect(e.target.files);
                e.target.value = ''; // Reset input
            }
        });
        
        // Drag and drop
        uploadArea.addEventListener('dragover', (e) => {
            e.preventDefault();
            uploadArea.classList.add('dragover');
        });
        
        uploadArea.addEventListener('dragenter', (e) => {
            e.preventDefault();
            uploadArea.classList.add('dragover');
        });
        
        uploadArea.addEventListener('dragleave', (e) => {
            e.preventDefault();
            if (!uploadArea.contains(e.relatedTarget)) {
                uploadArea.classList.remove('dragover');
            }
        });
        
        uploadArea.addEventListener('drop', (e) => {
            e.preventDefault();
            uploadArea.classList.remove('dragover');
            this.handleFileSelect(e.dataTransfer.files);
        });
        
        // Prevent default drag behaviors
        document.addEventListener('dragover', (e) => e.preventDefault());
        document.addEventListener('drop', (e) => e.preventDefault());
    }
    
    handleFileSelect(files) {
        if (!files || files.length === 0) return;
        
        const remainingSlots = this.maxImages - this.images.length;
        
        Array.from(files).slice(0, remainingSlots).forEach((file, index) => {
            if (!this.allowedTypes.includes(file.type)) {
                this.showNotification(`Formato non supportato: ${file.name}`, 'error');
                return;
            }
            
            if (file.size > this.maxFileSize) {
                this.showNotification(`File troppo grande: ${file.name} (max 5MB)`, 'error');
                return;
            }
            
            this.addImage(file);
        });
        
        if (files.length > remainingSlots) {
            this.showNotification(`Puoi caricare solo ${remainingSlots} immagini aggiuntive`, 'warning');
        }
    }
    
    addImage(file) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const imageData = {
                file: file,
                dataUrl: e.target.result,
                id: Date.now() + Math.random(),
                size: file.size
            };
            
            this.images.push(imageData);
            this.renderImagePreviews();
            this.updateUploadInfo();
            this.updatePreview();
            this.markUnsavedChanges();
            this.scheduleAutoSave();
        };
        reader.readAsDataURL(file);
    }
    
    removeImage(imageId) {
        this.images = this.images.filter(img => img.id !== imageId);
        this.renderImagePreviews();
        this.updateUploadInfo();
        this.updatePreview();
        this.markUnsavedChanges();
        this.scheduleAutoSave();
    }
    
    renderImagePreviews() {
        const container = document.getElementById('imagePreviewGrid');
        if (!container) return;
        
        container.innerHTML = '';
        
        this.images.forEach((image, index) => {
            const item = document.createElement('div');
            item.className = 'image-preview-item';
            item.innerHTML = `
                <img src="${image.dataUrl}" alt="Preview ${index + 1}">
                <button type="button" class="image-remove-btn" onclick="productCreator.removeImage(${image.id})" title="Rimuovi immagine">
                    <i class="fas fa-times"></i>
                </button>
            `;
            container.appendChild(item);
        });
        
        // Hide upload area if max images reached
        const uploadArea = document.getElementById('imageUploadArea');
        if (uploadArea) {
            uploadArea.style.display = this.images.length >= this.maxImages ? 'none' : 'block';
        }
    }
    
    updateUploadInfo() {
        const imageCount = document.getElementById('imageCount');
        const totalSize = document.getElementById('totalSize');
        
        if (imageCount) {
            imageCount.textContent = this.images.length;
        }
        
        if (totalSize) {
            const totalSizeBytes = this.images.reduce((sum, img) => sum + img.size, 0);
            const totalSizeMB = (totalSizeBytes / (1024 * 1024)).toFixed(1);
            totalSize.textContent = `${totalSizeMB} MB`;
        }
    }
    
    setupCharacterCounters() {
        const fields = [
            { id: 'productName', max: 100 },
            { id: 'productDescription', max: 2000 }
        ];
        
        fields.forEach(field => {
            const element = document.getElementById(field.id);
            const counter = document.getElementById(field.id + 'Counter');
            
            if (element && counter) {
                const updateCounter = () => {
                    const length = element.value.length;
                    counter.textContent = length;
                    
                    counter.className = 'input-counter';
                    if (length > field.max * 0.8) counter.classList.add('warning');
                    if (length > field.max) counter.classList.add('error');
                };
                
                element.addEventListener('input', updateCounter);
                updateCounter(); // Initialize
            }
        });
    }
    
    setupTipsToggle() {
        const tipsHeader = document.querySelector('.tips-header');
        const tipsContent = document.querySelector('.tips-content');
        
        if (tipsHeader && tipsContent) {
            tipsHeader.addEventListener('click', () => {
                tipsHeader.classList.toggle('expanded');
                tipsContent.classList.toggle('expanded');
            });
        }
    }
    
    setupFormatButtons() {
        document.querySelectorAll('.format-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                const format = btn.dataset.format;
                const textarea = document.getElementById('productDescription');
                
                if (textarea && format) {
                    this.applyTextFormat(textarea, format);
                }
            });
        });
    }
    
    applyTextFormat(textarea, format) {
        const start = textarea.selectionStart;
        const end = textarea.selectionEnd;
        const selectedText = textarea.value.substring(start, end);
        
        let formattedText = '';
        switch (format) {
            case 'bold':
                formattedText = `**${selectedText || 'testo in grassetto'}**`;
                break;
            case 'italic':
                formattedText = `*${selectedText || 'testo in corsivo'}*`;
                break;
            case 'list':
                formattedText = selectedText ? selectedText.split('\n').map(line => `• ${line}`).join('\n') : '• Elemento lista';
                break;
        }
        
        const newValue = textarea.value.substring(0, start) + formattedText + textarea.value.substring(end);
        textarea.value = newValue;
        textarea.focus();
        
        // Update counter and preview
        textarea.dispatchEvent(new Event('input'));
    }
    
    setupFormValidation() {
        // Setup real-time validation for all fields
        Object.keys(this.validationRules).forEach(fieldId => {
            const field = document.getElementById(fieldId) || document.querySelector(`input[name="${fieldId}"]`);
            if (field) {
                field.addEventListener('blur', () => this.validateField(fieldId));
                field.addEventListener('input', () => this.clearFieldError(fieldId));
            }
        });
    }
    
    validateField(fieldId) {
        let field, value;
        
        // Gestione speciale per radio buttons (condition)
        if (fieldId === 'condition') {
            const checkedRadio = document.querySelector(`input[name="${fieldId}"]:checked`);
            field = checkedRadio;
            value = checkedRadio ? checkedRadio.value : '';
        } else {
            field = document.getElementById(fieldId);
            value = field ? field.value.trim() : '';
        }
        
        const rules = this.validationRules[fieldId];
        
        if (!rules) {
            return true;
        }
        
        let isValid = true;
        let errorMessage = '';
        
        // Required validation
        if (rules.required && !value) {
            isValid = false;
            errorMessage = 'Questo campo è obbligatorio';
        }
        
        // Length validation
        if (value && rules.minLength && value.length < rules.minLength) {
            isValid = false;
            errorMessage = `Minimo ${rules.minLength} caratteri`;
        }
        
        if (value && rules.maxLength && value.length > rules.maxLength) {
            isValid = false;
            errorMessage = `Massimo ${rules.maxLength} caratteri`;
        }
        
        // Number validation
        if (value && rules.min !== undefined && parseFloat(value) < rules.min) {
            isValid = false;
            errorMessage = `Valore minimo: ${rules.min}`;
        }
        
        this.showFieldError(fieldId, isValid, errorMessage);
        return isValid;
    }
    
    showFieldError(fieldId, isValid, message) {
        const field = document.getElementById(fieldId) || document.querySelector(`input[name="${fieldId}"]`);
        if (!field) return;
        
        const formGroup = field.closest('.form-group');
        if (!formGroup) return;
        
        let errorElement = formGroup.querySelector('.error-message');
        
        if (isValid) {
            field.classList.remove('form-error');
            if (errorElement) {
                errorElement.classList.remove('show');
                setTimeout(() => errorElement.remove(), 200);
            }
        } else {
            field.classList.add('form-error');
            
            if (!errorElement) {
                errorElement = document.createElement('div');
                errorElement.className = 'error-message';
                formGroup.appendChild(errorElement);
            }
            
            errorElement.innerHTML = `<i class="fas fa-exclamation-triangle"></i> ${message}`;
            errorElement.classList.add('show');
        }
    }
    
    clearFieldError(fieldId) {
        const field = document.getElementById(fieldId);
        if (field) {
            field.classList.remove('form-error');
        }
    }
    
    validateStep1() {
        const isValid = this.images.length > 0;
        return isValid;
    }
    
    validateStep2() {
        const fields = ['productName', 'productDescription', 'productCategory'];
        let allValid = true;
        
        fields.forEach(fieldId => {
            const fieldValid = this.validateField(fieldId);
            if (!fieldValid) {
                allValid = false;
            }
        });
        
        // Check if at least one author is selected
        if (selectedAuthors.length === 0) {
            showNotification('Seleziona almeno un autore', 'error');
            allValid = false;
        }
        
        return allValid;
    }
    
    setupPreview() {
        this.updatePreview();
    }
    
    updatePreview() {
        // Update preview title
        const titleElement = document.getElementById('previewTitle');
        const nameInput = document.getElementById('productName');
        if (titleElement && nameInput) {
            titleElement.textContent = nameInput.value || 'Titolo del libro';
        }
        
        // Update preview year
        const yearElement = document.getElementById('previewYear');
        const yearInput = document.getElementById('annoPubblicazione');
        if (yearElement && yearInput) {
            yearElement.textContent = yearInput.value || 'Anno';
        }
        
        // Update preview category
        const categoryElement = document.getElementById('previewCategory');
        const categorySelect = document.getElementById('productCategory');
        if (categoryElement && categorySelect) {
            const selectedOption = categorySelect.options[categorySelect.selectedIndex];
            categoryElement.textContent = selectedOption ? selectedOption.text : 'Categoria';
        }
        
        // Update preview description
        const descElement = document.getElementById('previewDescription');
        const descInput = document.getElementById('productDescription');
        if (descElement && descInput) {
            descElement.textContent = descInput.value || 'Descrizione del libro...';
        }
        
        // Update preview authors
        this.updatePreviewAuthors();
        
        // Update preview images
        this.updatePreviewImages();
        
        // Update condition badge
        this.updateConditionBadge();
    }
    
    updatePreviewAuthors() {
        const authorsElement = document.getElementById('previewAuthors');
        const selectedAuthorsContainer = document.getElementById('selectedAuthors');
        
        if (authorsElement && selectedAuthorsContainer) {
            const authorElements = selectedAuthorsContainer.querySelectorAll('.selected-author');
            
            if (authorElements.length > 0) {
                const authorNames = Array.from(authorElements).map(el => {
                    const nameElement = el.querySelector('.author-name');
                    return nameElement ? nameElement.textContent : '';
                }).filter(name => name);
                
                authorsElement.textContent = authorNames.join(', ');
            } else {
                authorsElement.textContent = 'Nessun autore selezionato';
            }
        }
    }
    
    updatePreviewImages() {
        const mainImageContainer = document.getElementById('previewMainImageContainer');
        const thumbnailsContainer = document.getElementById('previewThumbnails');
        const placeholder = document.getElementById('previewPlaceholder');
        const mainImage = document.getElementById('previewMainImage');
        
        if (this.images.length > 0) {
            // Show main image
            if (mainImage && placeholder) {
                mainImage.src = this.images[0].dataUrl;
                mainImage.style.display = 'block';
                placeholder.style.display = 'none';
            }
            
            // Show thumbnails
            if (thumbnailsContainer) {
                thumbnailsContainer.innerHTML = '';
                this.images.forEach((image, index) => {
                    const thumb = document.createElement('div');
                    thumb.className = `preview-thumbnail ${index === 0 ? 'active' : ''}`;
                    thumb.innerHTML = `<img src="${image.dataUrl}" alt="Thumbnail ${index + 1}">`;
                    thumb.addEventListener('click', () => this.switchMainImage(index));
                    thumbnailsContainer.appendChild(thumb);
                });
            }
        } else {
            // Show placeholder
            if (mainImage && placeholder) {
                mainImage.style.display = 'none';
                placeholder.style.display = 'flex';
            }
            
            if (thumbnailsContainer) {
                thumbnailsContainer.innerHTML = '';
            }
        }
    }
    
    switchMainImage(index) {
        if (index < 0 || index >= this.images.length) return;
        
        const mainImage = document.getElementById('previewMainImage');
        if (mainImage) {
            mainImage.src = this.images[index].dataUrl;
        }
        
        // Update thumbnail active state
        document.querySelectorAll('.preview-thumbnail').forEach((thumb, i) => {
            thumb.classList.toggle('active', i === index);
        });
    }
    
    updateConditionBadge() {
        const badge = document.getElementById('conditionBadge');
        const conditionInput = document.querySelector('input[name="condition"]:checked');
        
        if (badge && conditionInput) {
            const conditionValue = conditionInput.value;
            const conditionText = conditionInput.closest('label').querySelector('.condition-title').textContent;
            
            badge.innerHTML = `<i class="fas fa-star"></i><span>${conditionText}</span>`;
            badge.style.display = 'flex';
        } else if (badge) {
            badge.style.display = 'none';
        }
    }
    
    setupAutoSave() {
        // Load saved data on page load
        this.loadAutoSavedData();
        
        // Save data before page unload
        window.addEventListener('beforeunload', (e) => {
            if (this.hasUnsavedChanges) {
                this.autoSave();
                e.preventDefault();
                e.returnValue = 'Hai modifiche non salvate. Sei sicuro di voler uscire?';
            }
        });
    }
    
    markUnsavedChanges() {
        this.hasUnsavedChanges = true;
    }
    
    scheduleAutoSave() {
        if (this.autoSaveTimer) {
            clearTimeout(this.autoSaveTimer);
        }
        
        this.autoSaveTimer = setTimeout(() => {
            this.autoSave();
        }, 2000); // Auto-save after 2 seconds of inactivity
    }
    
    autoSave() {
        const formData = this.gatherFormData();
        
        try {
            localStorage.setItem('productCreateAutoSave', JSON.stringify({
                data: formData,
                timestamp: Date.now(),
                images: this.images.map(img => ({ ...img, file: null })) // Don't save file objects
            }));
            
            this.showAutoSaveIndicator();
            this.hasUnsavedChanges = false;
        } catch (error) {
            console.warn('Auto-save failed:', error);
        }
    }
    
    loadAutoSavedData() {
        try {
            const saved = localStorage.getItem('productCreateAutoSave');
            if (saved) {
                const { data, timestamp } = JSON.parse(saved);
                
                // Only load if saved within last 24 hours
                if (Date.now() - timestamp < 24 * 60 * 60 * 1000) {
                    this.populateForm(data);
                    this.showNotification('Dati precedenti ripristinati automaticamente', 'info');
                }
            }
        } catch (error) {
            console.warn('Failed to load auto-saved data:', error);
        }
    }
    
    showAutoSaveIndicator() {
        const indicator = document.getElementById('autoSaveStatus');
        if (indicator) {
            indicator.parentElement.classList.add('show');
            setTimeout(() => {
                indicator.parentElement.classList.remove('show');
            }, 2000);
        }
    }
    
    gatherFormData() {
        return {
            name: document.getElementById('productName')?.value || '',
            description: document.getElementById('productDescription')?.value || '',
            category: document.getElementById('productCategory')?.value || '',
            annoPubblicazione: document.getElementById('annoPubblicazione')?.value || '',
            condition: document.querySelector('input[name="condition"]:checked')?.value || ''
        };
    }
    
    populateForm(data) {
        Object.entries(data).forEach(([key, value]) => {
            if (key === 'condition') {
                const radio = document.querySelector(`input[name="condition"][value="${value}"]`);
                if (radio) radio.checked = true;
            } else {
                const field = document.getElementById(`product${key.charAt(0).toUpperCase() + key.slice(1)}`);
                if (field) field.value = value;
            }
        });
        
        this.updatePreview();
    }
    
    setupSmartFeatures() {
        this.setupNameSuggestions();
        this.setupPriceSuggestions();
    }
    
    setupNameSuggestions() {
        const nameInput = document.getElementById('productName');
        const suggestionsContainer = document.getElementById('nameSuggestions');
        
        if (nameInput && suggestionsContainer) {
            nameInput.addEventListener('input', () => {
                const value = nameInput.value.toLowerCase();
                this.showNameSuggestions(value, suggestionsContainer);
            });
        }
    }
    
    showNameSuggestions(query, container) {
        container.innerHTML = '';
        
        if (query.length < 2) return;
        
        const suggestions = [];
        Object.entries(this.categorySuggestions).forEach(([keyword, categories]) => {
            if (keyword.includes(query)) {
                suggestions.push(`${keyword.charAt(0).toUpperCase() + keyword.slice(1)} - ${categories[0]}`);
            }
        });
        
        if (suggestions.length > 0) {
            container.style.display = 'block';
            suggestions.slice(0, 5).forEach(suggestion => {
                const item = document.createElement('div');
                item.className = 'suggestion-item';
                item.textContent = suggestion;
                item.addEventListener('click', () => {
                    document.getElementById('productName').value = suggestion.split(' - ')[0];
                    container.style.display = 'none';
                    this.updatePreview();
                });
                container.appendChild(item);
            });
        } else {
            container.style.display = 'none';
        }
    }
    
    setupPriceSuggestions() {
        const priceInput = document.getElementById('productPrice');
        const categorySelect = document.getElementById('productCategory');
        
        if (priceInput && categorySelect) {
            const showPriceSuggestion = () => {
                const category = categorySelect.value;
                const name = document.getElementById('productName')?.value.toLowerCase() || '';
                
                // Mock price suggestion logic
                if (category && name) {
                    this.showPriceSuggestion(category, name);
                }
            };
            
            priceInput.addEventListener('blur', showPriceSuggestion);
            categorySelect.addEventListener('change', showPriceSuggestion);
        }
    }
    
    showPriceSuggestion(category, name) {
        const container = document.getElementById('priceSuggestions');
        if (!container) return;
        
        // Mock price suggestion (in real app, this would call an API)
        let suggestedPrice = 0;
        if (name.includes('iphone')) suggestedPrice = 500;
        else if (name.includes('laptop')) suggestedPrice = 800;
        else if (name.includes('scarpe')) suggestedPrice = 80;
        
        if (suggestedPrice > 0) {
            container.innerHTML = `
                <div class="price-suggestion">
                    <i class="fas fa-lightbulb"></i>
                    <span>Prezzo suggerito: €${suggestedPrice}</span>
                    <button type="button" onclick="document.getElementById('productPrice').value=${suggestedPrice}; productCreator.updatePreview()">
                        Usa
                    </button>
                </div>
            `;
            container.style.display = 'block';
        } else {
            container.style.display = 'none';
        }
    }
    
    showStep(step) {
        // Hide all steps
        document.querySelectorAll('.form-step').forEach(stepEl => {
            stepEl.classList.remove('active');
        });
        
        // Show current step
        const currentStepEl = document.getElementById(`step${step}`);
        if (currentStepEl) {
            currentStepEl.classList.add('active');
        }
        
        // Update navigation buttons
        this.updateNavigationButtons();
        
        // Update step indicator
        this.updateStepIndicator();
        
        // Update progress bar
        this.updateProgressBar();
        
        // Update preview when on step 3
        if (step === 3) {
            this.updatePreview();
        }
    }
    
    updateNavigationButtons() {
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');
        const submitBtn = document.getElementById('submitBtn');
        
        if (prevBtn) {
            prevBtn.style.display = this.currentStep > 1 ? 'inline-flex' : 'none';
        }
        
        if (nextBtn) {
            nextBtn.style.display = this.currentStep < this.totalSteps ? 'inline-flex' : 'none';
            // I bottoni sono sempre abilitati - la validazione avviene solo al click
            nextBtn.disabled = false;
        }
        
        if (submitBtn) {
            submitBtn.style.display = this.currentStep === this.totalSteps ? 'inline-flex' : 'none';
            // Il bottone submit è sempre abilitato - la validazione avviene solo al click
            submitBtn.disabled = false;
        }
    }
    
    updateStepIndicator() {
        const indicator = document.getElementById('currentStepText');
        if (indicator) {
            indicator.textContent = `Passo ${this.currentStep} di ${this.totalSteps}`;
        }
    }
    
    updateProgressBar() {
        const progressLine = document.getElementById('progressLine');
        const steps = document.querySelectorAll('.step');
        
        // Update progress line width
        if (progressLine) {
            const progressPercentage = ((this.currentStep - 1) / (this.totalSteps - 1)) * 100;
            progressLine.style.width = `${progressPercentage}%`;
        }
        
        // Update step states
        steps.forEach((stepEl, index) => {
            const stepNumber = index + 1;
            stepEl.classList.remove('active', 'completed');
            
            if (stepNumber < this.currentStep) {
                stepEl.classList.add('completed');
            } else if (stepNumber === this.currentStep) {
                stepEl.classList.add('active');
            }
        });
    }
    
    nextStep() {
        let canProceed = false;
        let errorMessage = '';
        
        switch (this.currentStep) {
            case 1:
                canProceed = this.validateStep1();
                if (!canProceed) {
                    errorMessage = 'Carica almeno una foto del prodotto prima di continuare';
                }
                break;
            case 2:
                canProceed = this.validateStep2();
                if (!canProceed) {
                    errorMessage = 'Completa tutti i campi obbligatori prima di continuare';
                }
                break;
            default:
                canProceed = true;
        }
        
        if (canProceed && this.currentStep < this.totalSteps) {
            this.currentStep++;
            this.showStep(this.currentStep);
        } else if (!canProceed) {
            this.showNotification(errorMessage, 'error');
        }
    }
    
    prevStep() {
        if (this.currentStep > 1) {
            this.currentStep--;
            this.showStep(this.currentStep);
        }
    }
    
    async submitForm(e) {
        e.preventDefault();
        
        // Validazione completa: campi obbligatori + almeno una foto
        if (!this.validateStep1()) {
            showNotification('Carica almeno una foto del libro', 'error');
            return;
        }
        
        if (!this.validateStep2()) {
            showNotification('Completa tutti i campi obbligatori', 'error');
            return;
        }
        
        const submitBtn = document.getElementById('submitBtn');
        const loadingOverlay = document.getElementById('loadingOverlay');
        
        try {
            // Show loading
            if (submitBtn) {
                submitBtn.disabled = true;
                submitBtn.querySelector('.btn-loading').style.display = 'inline-block';
            }
            if (loadingOverlay) loadingOverlay.style.display = 'flex';
            
            // Prepare form data
            const formData = new FormData();
            const data = this.gatherFormData();
            
            // Add basic fields
            Object.entries(data).forEach(([key, value]) => {
                if (!key.startsWith('autoriIds')) {
                    formData.append(key, value);
                }
            });
            
            // Add authors separately to handle List<Long> parameter
            selectedAuthors.forEach(author => {
                formData.append('autoriIds', author.id);
            });
            
            // Add images
            this.images.forEach((image, index) => {
                if (image.file) {
                    formData.append('images', image.file);
                }
            });
            
            // Get CSRF token
            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
            
            // Prepare headers
            const headers = {};
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            }
            
            // Submit form
            const response = await fetch('/product/create', {
                method: 'POST',
                headers: headers,
                body: formData
            });
            
            const responseText = await response.text();
            
            if (response.ok) {
                try {
                    const result = JSON.parse(responseText);
                    if (result.success) {
                        // Clear auto-saved data
                        localStorage.removeItem('productCreateAutoSave');
                        
                        // Show success modal
                        this.showSuccessModal();
                    } else {
                        throw new Error(result.error || 'Errore sconosciuto');
                    }
                } catch (parseError) {
                    // If not JSON, probably HTML success page
                    localStorage.removeItem('productCreateAutoSave');
                    this.showSuccessModal();
                }
            } else {
                try {
                    const errorResult = JSON.parse(responseText);
                    throw new Error(errorResult.error || `Errore HTTP ${response.status}`);
                } catch (parseError) {
                    throw new Error(`Errore durante il salvataggio (${response.status})`);
                }
            }
            
        } catch (error) {
            console.error('Submit error:', error);
            showNotification(error.message || 'Errore durante il salvataggio del libro', 'error');
        } finally {
            // Hide loading
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.querySelector('.btn-loading').style.display = 'none';
            }
            if (loadingOverlay) loadingOverlay.style.display = 'none';
        }
    }
    
    showSuccessModal() {
        const modal = document.getElementById('successModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }
    
    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <i class="fas fa-${this.getNotificationIcon(type)}"></i>
                <span>${message}</span>
                <button class="notification-close">&times;</button>
            </div>
        `;
        
        // Add to page
        document.body.appendChild(notification);
        
        // Show with animation
        setTimeout(() => notification.classList.add('show'), 100);
        
        // Auto-hide after 5 seconds
        setTimeout(() => this.hideNotification(notification), 5000);
        
        // Close button
        notification.querySelector('.notification-close').addEventListener('click', () => {
            this.hideNotification(notification);
        });
    }
    
    hideNotification(notification) {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }
    
    getNotificationIcon(type) {
        switch (type) {
            case 'success': return 'check-circle';
            case 'error': return 'exclamation-triangle';
            case 'warning': return 'exclamation-circle';
            default: return 'info-circle';
        }
    }
}

// Funzionalità per la selezione autori
let selectedAuthors = [];

function initializeAuthorSelection() {
    const authorSearch = document.getElementById('authorSearch');
    const authorsSuggestions = document.getElementById('authorsSuggestions');
    const selectedAuthorsContainer = document.getElementById('selectedAuthors');
    const addNewAuthorBtn = document.getElementById('addNewAuthorBtn');
    
    if (!authorSearch) return;
    
    let searchTimeout;
    
    // Gestisce la ricerca autori
    authorSearch.addEventListener('input', function() {
        const query = this.value.trim();
        
        clearTimeout(searchTimeout);
        
        if (query.length < 2) {
            hideSuggestions();
            return;
        }
        
        searchTimeout = setTimeout(() => {
            searchAuthors(query);
        }, 300);
    });
    
    // Nasconde suggerimenti quando clicchi fuori
    document.addEventListener('click', function(e) {
        if (!authorSearch.contains(e.target) && !authorsSuggestions.contains(e.target)) {
            hideSuggestions();
        }
    });
    
    // Gestisce il pulsante per nuovo autore
    if (addNewAuthorBtn) {
        addNewAuthorBtn.addEventListener('click', function() {
            // Invece di aprire un popup, naviga nella stessa finestra
            // Salva i dati del form nel localStorage prima di navigare
            saveFormDataToLocalStorage();
            
            // Naviga alla pagina di creazione autore
            const currentHost = window.location.protocol + '//' + window.location.host;
            window.location.href = currentHost + '/authors/create';
        });
    }
    
    updateSelectedAuthorsDisplay();
}

function searchAuthors(query) {
    // Usa l'URL completo per evitare problemi di porta
    const currentHost = window.location.protocol + '//' + window.location.host;
    fetch(`${currentHost}/authors/api/search?q=${encodeURIComponent(query)}`)
        .then(response => response.json())
        .then(authors => {
            displayAuthorSuggestions(authors);
        })
        .catch(error => {
            console.error('Error searching authors:', error);
            hideSuggestions();
        });
}

function displayAuthorSuggestions(authors) {
    const authorsSuggestions = document.getElementById('authorsSuggestions');
    if (!authorsSuggestions) return;
    
    if (authors.length === 0) {
        authorsSuggestions.innerHTML = '<div style="padding: 1rem; text-align: center; color: #7f8c8d;">Nessun autore trovato</div>';
    } else {
        authorsSuggestions.innerHTML = authors.map(author => `
            <div class="author-suggestion" onclick="selectAuthor(${author.id}, '${author.nome}', '${author.cognome}', '${author.nazionalita || ''}', ${author.annoNascita || 'null'})">
                <div class="author-avatar">
                    ${author.nome.charAt(0)}${author.cognome.charAt(0)}
                </div>
                <div class="author-info">
                    <div class="author-name">${author.nome} ${author.cognome}</div>
                    <div class="author-details">
                        ${author.nazionalita ? author.nazionalita : ''} 
                        ${author.annoNascita ? '• ' + author.annoNascita : ''}
                    </div>
                </div>
            </div>
        `).join('');
    }
    
    authorsSuggestions.classList.add('show');
}

function selectAuthor(id, nome, cognome, nazionalita, annoNascita) {
    // Controlla se l'autore è già selezionato
    if (selectedAuthors.find(author => author.id === id)) {
        showNotification('Autore già selezionato', 'warning');
        hideSuggestions();
        return;
    }
    
    // Aggiungi l'autore alla selezione
    selectedAuthors.push({
        id: id,
        nome: nome,
        cognome: cognome,
        nazionalita: nazionalita,
        annoNascita: annoNascita
    });
    
    updateSelectedAuthorsDisplay();
    hideSuggestions();
    document.getElementById('authorSearch').value = '';
}

function removeAuthor(authorId) {
    selectedAuthors = selectedAuthors.filter(author => author.id !== authorId);
    updateSelectedAuthorsDisplay();
}

function updateSelectedAuthorsDisplay() {
    const selectedAuthorsContainer = document.getElementById('selectedAuthors');
    const selectedAuthorsInputsContainer = document.getElementById('selectedAuthorsInputs');
    
    if (!selectedAuthorsContainer) return;
    
    if (selectedAuthors.length === 0) {
        selectedAuthorsContainer.className = 'selected-authors empty';
        selectedAuthorsContainer.innerHTML = '';
        
        // Clear hidden inputs
        if (selectedAuthorsInputsContainer) {
            selectedAuthorsInputsContainer.innerHTML = '';
        }
    } else {
        selectedAuthorsContainer.className = 'selected-authors has-authors';
        selectedAuthorsContainer.innerHTML = selectedAuthors.map(author => `
            <div class="selected-author">
                <span class="author-name">${author.nome} ${author.cognome}</span>
                <button type="button" class="remove-author" onclick="removeAuthor(${author.id})">
                    <i class="fas fa-times"></i>
                </button>
            </div>
        `).join('');
        
        // Update hidden inputs for form submission
        if (selectedAuthorsInputsContainer) {
            selectedAuthorsInputsContainer.innerHTML = selectedAuthors.map(author => 
                `<input type="hidden" name="autoriIds" value="${author.id}">`
            ).join('');
        }
    }
    
    // Update preview when on step 3
    if (window.productCreator) {
        window.productCreator.updatePreviewAuthors();
    }
}

function hideSuggestions() {
    const authorsSuggestions = document.getElementById('authorsSuggestions');
    if (authorsSuggestions) {
        authorsSuggestions.classList.remove('show');
    }
}

function showNotification(message, type = 'info') {
    // Crea una notifica toast
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.innerHTML = `
        <i class="fas fa-${type === 'warning' ? 'exclamation-triangle' : 'info-circle'}"></i>
        <span>${message}</span>
    `;
    
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: ${type === 'warning' ? '#f39c12' : '#3498db'};
        color: white;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
        z-index: 1001;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        animation: slideInRight 0.3s ease;
    `;
    
    document.body.appendChild(notification);
    
    setTimeout(() => {
        notification.remove();
    }, 3000);
}

// Funzione per salvare i dati del form nel localStorage
function saveFormDataToLocalStorage() {
    const formData = {
        productName: document.getElementById('productName')?.value || '',
        productDescription: document.getElementById('productDescription')?.value || '',
        productCategory: document.getElementById('productCategory')?.value || '',
        annoPubblicazione: document.getElementById('annoPubblicazione')?.value || '',
        condition: document.querySelector('input[name="condition"]:checked')?.value || '',
        selectedAuthors: Array.from(document.querySelectorAll('.selected-author')).map(author => ({
            id: author.dataset.authorId,
            name: author.querySelector('.author-name').textContent
        }))
    };
    
    localStorage.setItem('bookFormData', JSON.stringify(formData));
    console.log('Form data saved to localStorage:', formData);
}

// Funzione per ripristinare i dati del form dal localStorage
function restoreFormDataFromLocalStorage() {
    const savedData = localStorage.getItem('bookFormData');
    if (savedData) {
        try {
            const formData = JSON.parse(savedData);
            
            // Ripristina i campi del form
            if (formData.productName) document.getElementById('productName').value = formData.productName;
            if (formData.productDescription) document.getElementById('productDescription').value = formData.productDescription;
            if (formData.productCategory) document.getElementById('productCategory').value = formData.productCategory;
            if (formData.annoPubblicazione) document.getElementById('annoPubblicazione').value = formData.annoPubblicazione;
            if (formData.condition) {
                const conditionInput = document.querySelector(`input[name="condition"][value="${formData.condition}"]`);
                if (conditionInput) conditionInput.checked = true;
            }
            
            // Ripristina gli autori selezionati
            if (formData.selectedAuthors && formData.selectedAuthors.length > 0) {
                formData.selectedAuthors.forEach(author => {
                    addSelectedAuthor(author.id, author.name);
                });
            }
            
            console.log('Form data restored from localStorage');
            
            // Pulisce il localStorage dopo il ripristino
            localStorage.removeItem('bookFormData');
            
        } catch (e) {
            console.error('Error restoring form data:', e);
            localStorage.removeItem('bookFormData');
        }
    }
}

// Initialize when page loads
let productCreator;
document.addEventListener('DOMContentLoaded', () => {
    productCreator = new EnhancedProductCreator();
    // Make it globally accessible for onclick handlers
    window.productCreator = productCreator;
    
    // Initialize author selection
    initializeAuthorSelection();
    
    // Restore form data from localStorage
    restoreFormDataFromLocalStorage();
});
