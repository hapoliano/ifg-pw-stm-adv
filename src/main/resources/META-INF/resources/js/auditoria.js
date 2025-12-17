// FUNÇÕES PADRONIZADAS
function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;
    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => window.location.href = '/login');
}

// Lógica de filtro (mantida do seu original)
function filtrarLogs() {
    const input = document.getElementById('logSearch');
    const filter = input.value.toUpperCase();
    const table = document.getElementById('logsTable');
    const tr = table.getElementsByTagName('tr');

    // Começa em 1 para pular o cabeçalho
    for (let i = 1; i < tr.length; i++) {
        let visible = false;
        const cells = tr[i].getElementsByTagName('td');
        // Busca nas colunas 1 (Usuário), 2 (Ação), 3 (Entidade), 4 (Descrição)
        for (let j = 1; j <= 4; j++) {
            const cell = cells[j];
            if (cell) {
                const textValue = cell.textContent || cell.innerText;
                if (textValue.toUpperCase().indexOf(filter) > -1) {
                    visible = true;
                    break;
                }
            }
        }
        tr[i].style.display = visible ? '' : 'none';
    }
}

// Inicialização da Sidebar Mobile
function inicializarAuditoria() {
    const sidebar = document.getElementById('sidebarNav');
    const overlay = document.querySelector('.sidebar-overlay');

    if (sidebar && overlay) {
        sidebar.addEventListener('show.bs.collapse', () => overlay.style.display = 'block');
        sidebar.addEventListener('hide.bs.collapse', () => overlay.style.display = 'none');
        overlay.addEventListener('click', () => {
            const bsCollapse = bootstrap.Collapse.getInstance(sidebar);
            if (bsCollapse) bsCollapse.hide();
        });
    }
}
document.addEventListener('DOMContentLoaded', inicializarAuditoria);