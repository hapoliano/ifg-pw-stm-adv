document.addEventListener("DOMContentLoaded", function() {
    const currentPath = window.location.pathname;
    const navLinks = document.querySelectorAll('.sidebar .nav-link');

    navLinks.forEach(link => {
        const href = link.getAttribute('href');

        // Lógica:
        // 1. Se for a página exata (ex: /processos) -> Ativa
        // 2. Se estiver dentro da seção (ex: /processos/detalhe/1) -> Ativa também
        if (href === currentPath || (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        }
    });
});