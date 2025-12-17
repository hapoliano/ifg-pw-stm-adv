// Função de Logout
function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;
    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => window.location.href = '/login');
}

// Função para calcular e exibir o percentual
function calcularPercentual() {
    const processosAtivos = parseInt(document.getElementById('processosAtivos').textContent) || 0;
    const totalProcessos = parseInt(document.getElementById('meusProcessos').textContent) || 0;

    let percentual = 0;
    if (totalProcessos > 0) {
        percentual = Math.round((processosAtivos / totalProcessos) * 100);
    }

    const barraProgresso = document.getElementById('barraProgresso');
    const percentualAtivos = document.getElementById('percentualAtivos');

    if (barraProgresso) barraProgresso.style.width = percentual + '%';
    if (percentualAtivos) percentualAtivos.textContent = percentual + '%';

    const statusProcessosTexto = document.getElementById('statusProcessosTexto');
    if (statusProcessosTexto) statusProcessosTexto.textContent = processosAtivos + ' / ' + totalProcessos;
}

// Função para carregar dados assincronamente (fetch)
async function carregarDashboardAsync() {
    try {
        // Chamada ao endpoint que fornece o DTO (DashboardAdvogadoController)
        const response = await fetch('/dashboard/api/stats');
        if (!response.ok) throw new Error('Erro ao carregar dashboard: ' + response.status);

        const dados = await response.json();

        // Atualiza os elementos com os dados
        document.getElementById('meusProcessos').textContent = dados.totalProcessos || 0; // Usando totalProcessos do DTO
        document.getElementById('processosAtivos').textContent = dados.processosAtivos || 0;
        document.getElementById('meusClientes').textContent = dados.totalClientes || 0;
        document.getElementById('compromissosHoje').textContent = dados.compromissosHoje || 0;

        // Recalcula o percentual após a atualização dos dados
        calcularPercentual();

    } catch (error) {
        console.error('Erro ao carregar dados do dashboard:', error);
    }
}


document.addEventListener('DOMContentLoaded', function() {
    // Configuração de Sidebar (Mobile)
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

    // Ano do footer
    document.getElementById('footerYear').textContent = new Date().getFullYear();

    // 1. Cálcula a primeira vez com os dados do servidor (renderização Qute)
    calcularPercentual();

    // 2. Carrega dados assincronamente (fetch) para garantir que estão atualizados
    carregarDashboardAsync();
});