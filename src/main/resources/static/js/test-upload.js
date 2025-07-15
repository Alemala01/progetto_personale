// Test semplice per verificare il caricamento
console.log('Test JavaScript loaded');

// Test immediato al caricamento della pagina
window.addEventListener('load', function() {
    console.log('Window loaded');
    
    // Trova gli elementi
    const uploadArea = document.getElementById('imageUploadArea');
    const fileInput = document.getElementById('imageInput');
    
    console.log('Upload area found:', uploadArea);
    console.log('File input found:', fileInput);
    
    if (uploadArea) {
        // Test click semplice
        uploadArea.addEventListener('click', function() {
            console.log('Upload area clicked!');
            alert('Area cliccata!');
            if (fileInput) {
                fileInput.click();
            }
        });
        
        // Test drag and drop semplice
        uploadArea.addEventListener('drop', function(e) {
            e.preventDefault();
            console.log('File dropped!');
            alert('File trascinato!');
        });
        
        uploadArea.addEventListener('dragover', function(e) {
            e.preventDefault();
            console.log('Drag over');
        });
    }
});
