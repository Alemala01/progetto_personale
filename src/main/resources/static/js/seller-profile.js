// Funzione per contattare il venditore
function contactSeller() {
    const sellerUsername = /*[[${sellerUsername}]]*/ 'venditore';
    alert('Funzionalità di contatto per ' + sellerUsername + ' in sviluppo.');
    // Qui potrebbe essere implementato un sistema di messaggistica
}

// Gestione dei filtri (per demo)
document.querySelectorAll('.filter-btn').forEach(btn => {
    btn.addEventListener('click', function() {
        document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        this.classList.add('active');
        // Qui si implementerebbe la logica di filtro reale
    });
});
