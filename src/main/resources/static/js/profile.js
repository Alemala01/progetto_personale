// Funzione per mostrare un tab e nascondere gli altri
function showTab(tabId) {
    // Nascondi tutti i tab
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Mostra il tab richiesto
    const targetTab = document.getElementById(tabId);
    if (targetTab) {
        targetTab.classList.add('active');
    }
    
    // Aggiorna la classe active nel menu
    document.querySelectorAll('.profile-menu a').forEach(menuItem => {
        menuItem.classList.remove('active');
    });
    
    // Aggiungi la classe active all'elemento di menu corrispondente
    const activeMenuItem = document.querySelector('.profile-menu a[href="#' + tabId + '"]');
    if (activeMenuItem) {
        activeMenuItem.classList.add('active');
    }
}

// Valida i campi password nel form
function validatePasswordForm() {
    const newPassword = document.querySelector('input[name="newPassword"]').value;
    const confirmPassword = document.querySelector('input[name="confirmPassword"]').value;
    
    if (newPassword !== confirmPassword) {
        alert('Le password non coincidono');
        return false;
    }
    
    // Verifica complessità password
    const passwordRegex = /^(?=.*[A-Z])(?=.*[0-9]).{8,}$/;
    if (!passwordRegex.test(newPassword)) {
        alert('La password deve contenere almeno 8 caratteri, una lettera maiuscola e un numero');
        return false;
    }
    
    return true;
}

function validateUsernameForm() {
    const usernameInput = document.querySelector('input[name="username"]');
    if (!usernameInput) return true;
    
    const username = usernameInput.value.trim();
    
    if (username.length < 3 || username.length > 20) {
        alert('L\'username deve essere tra 3 e 20 caratteri');
        return false;
    }
    
    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
        alert('L\'username può contenere solo lettere, numeri e underscore');
        return false;
    }
    
    return true;
}

// Imposta il tab corretto all'avvio
document.addEventListener('DOMContentLoaded', function() {
    // Se ci sono notifiche di password o profilo, apri il tab settings
    const passwordNotification = document.querySelector('.alert:has([th\\:text*="password"], [th\\:text*="Password"])');
    const hasPasswordAlert = document.querySelector('.alert') && 
                           (document.querySelector('.alert').textContent.toLowerCase().includes('password'));
    
    // Controlla anche i messaggi di successo/errore del profilo
    const hasProfileAlert = document.querySelector('.alert') && 
                          (document.querySelector('.alert').textContent.toLowerCase().includes('profilo') ||
                           document.querySelector('.alert').textContent.toLowerCase().includes('username'));
    
    if (hasPasswordAlert || hasProfileAlert) {
        showTab('settings');
    } else {
        // Altrimenti imposta dashboard come predefinito
        if (document.getElementById('dashboard')) {
            document.getElementById('dashboard').classList.add('active');
        }
    }
    
    // Aggiungi validazione al form password
    const passwordForm = document.querySelector('.security-form');
    if (passwordForm) {
        passwordForm.addEventListener('submit', function(e) {
            if (!validatePasswordForm()) {
                e.preventDefault();
            }
        });
    }
    
    // Aggiungi validazione al form username
    const usernameForm = document.querySelector('.settings-form');
    if (usernameForm) {
        usernameForm.addEventListener('submit', function(e) {
            if (!validateUsernameForm()) {
                e.preventDefault();
            }
        });
        
        // Validazione real-time per username
        const usernameInput = usernameForm.querySelector('input[name="username"]');
        if (usernameInput) {
            usernameInput.addEventListener('input', function() {
                const username = this.value.trim();
                
                if (username.length >= 3 && username.length <= 20 && /^[a-zA-Z0-9_]+$/.test(username)) {
                    this.style.borderColor = '#28a745';
                } else if (username.length > 0) {
                    this.style.borderColor = '#dc3545';
                } else {
                    this.style.borderColor = '#ddd';
                }
            });
        }
    }
    
    // Auto-hide alerts dopo 5 secondi
    document.querySelectorAll('.alert').forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => {
                alert.remove();
            }, 500);
        }, 5000);
    });
});
