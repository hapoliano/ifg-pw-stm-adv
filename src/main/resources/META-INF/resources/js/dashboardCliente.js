// ------------------------------
// FUNÇÕES PADRONIZADAS
// ------------------------------
function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;

    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => window.location.href = '/login');
}

// ------------------------------
// ATUALIZAÇÃO ASSÍNCRONA DOS CARDS
// ------------------------------
async function carregarDashboardAsync() {
    try {
        // Este endpoint deve retornar um JSON com os dados para os cards
        const response = await fetch('/dashboard/cliente/api/stats');
        if (!response.ok) throw new Error('Erro ao carregar dashboard: ' + response.status);

        const dados = await response.json();

        // Atualiza os elementos com os dados
        document.getElementById('totalProcessos').textContent = dados.totalProcessos || 0;
        document.getElementById('processosAtivos').textContent = dados.processosAtivos || 0;
        document.getElementById('proximasAudiencias').textContent = dados.proximasAudiencias || 0;
        document.getElementById('mensagensNaoLidas').textContent = dados.mensagensNaoLidas || 0;

    } catch (error) {
        console.error('Erro ao carregar dados do dashboard:', error);
        // Se houver erro, garante que os valores do Qute (iniciais) permaneçam ou use um fallback
        document.getElementById('totalProcessos').textContent = document.getElementById('totalProcessos').textContent || 'N/A';
        document.getElementById('processosAtivos').textContent = document.getElementById('processosAtivos').textContent || 'N/A';
        document.getElementById('proximasAudiencias').textContent = document.getElementById('proximasAudiencias').textContent || 'N/A';
        document.getElementById('mensagensNaoLidas').textContent = document.getElementById('mensagensNaoLidas').textContent || 'N/A';
    }
}


// ------------------------------
// INICIALIZAÇÃO DA DASHBOARD (PADRÃO)
// ------------------------------
function inicializarDashboardCliente() {
    const sidebar = document.getElementById('sidebarNav');
    const overlay = document.querySelector('.sidebar-overlay');

    // 1. Configuração da Sidebar (Mobile)
    if (sidebar && overlay) {
        sidebar.addEventListener('show.bs.collapse', () => overlay.style.display = 'block');
        sidebar.addEventListener('hide.bs.collapse', () => overlay.style.display = 'none');
        overlay.addEventListener('click', () => {
            const bsCollapse = bootstrap.Collapse.getInstance(sidebar);
            if (bsCollapse) bsCollapse.hide();
        });
    }

    // 2. Ano do footer
    document.getElementById('footerYear').textContent = new Date().getFullYear();

    // 3. Carrega os dados assincronamente
    // Nota: Removido o carregamento assíncrono para evitar duplicidade com o Qute.
    // Se a lógica de back-end fornecer o JSON separadamente, descomente a linha abaixo.
    // carregarDashboardAsync();

    console.log('✅ Dashboard Cliente inicializado.');
}

// Inicia quando o DOM estiver completamente carregado
document.addEventListener('DOMContentLoaded', inicializarDashboardCliente);