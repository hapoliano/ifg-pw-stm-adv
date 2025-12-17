// ------------------------------
// FUNÇÕES
// ------------------------------

// Função para fazer logout
function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;
    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => window.location.href = '/login');
}

// Carregar dados do dashboard
async function carregarDashboard() {
    try {
        console.log('📊 Carregando dados do dashboard...');
        // O path do controller é /dashboard/master
        const response = await fetch('/dashboard/master');

        if (!response.ok) throw new Error('Erro HTTP: ' + response.status);

        const dados = await response.json();
        console.log('✅ Dados recebidos:', dados);

        // Atualiza os cards
        document.getElementById('totalUsuarios').textContent = dados.totalUsuarios || 0;
        document.getElementById('totalProcessos').textContent = dados.totalProcessos || 0;
        document.getElementById('totalClientes').textContent = dados.totalClientes || 0;
        document.getElementById('processosAtivos').textContent = dados.processosAtivos || 0;
        document.getElementById('advogadosPendentes').textContent = dados.advogadosPendentes || 0;

        // Atualiza gráficos de status
        const totalProcessos = dados.totalProcessos || 0;
        const processosAtivos = dados.processosAtivos || 0;
        const totalUsuarios = dados.totalUsuarios || 0;
        const usuariosAtivos = dados.usuariosAtivos || 0;

        // Processos
        const percentualP = totalProcessos > 0 ? Math.round((processosAtivos * 100) / totalProcessos) : 0;
        document.getElementById('statusProcessosBarra').style.width = percentualP + '%';
        document.getElementById('statusProcessosTexto').textContent = processosAtivos + ' / ' + totalProcessos;
        document.getElementById('statusProcessosPercentual').textContent = percentualP + '%';

        // Usuários
        const percentualU = totalUsuarios > 0 ? Math.round((usuariosAtivos * 100) / totalUsuarios) : 0;
        document.getElementById('statusUsuariosBarra').style.width = percentualU + '%';
        document.getElementById('statusUsuariosTexto').textContent = usuariosAtivos + ' / ' + totalUsuarios;
        document.getElementById('statusUsuariosPercentual').textContent = percentualU + '%';

    } catch (error) {
        console.error('❌ Erro ao carregar dados:', error);
        ['totalUsuarios', 'totalProcessos', 'totalClientes', 'processosAtivos', 'advogadosPendentes'].forEach(id => {
            const element = document.getElementById(id);
            if (element) element.textContent = '0';
        });
    }
}

// Carregar alertas
async function carregarAlertas() {
    const container = document.getElementById('alertasSistema');
    if (!container) return; // Checa se o elemento existe

    try {
        console.log('🔔 Carregando alertas...');
        // Assumindo que o endpoint para alertas ainda é /dashboard-master/alertas
        const response = await fetch('/dashboard-master/alertas');

        if (!response.ok) throw new Error('Erro HTTP: ' + response.status);

        const alertas = await response.json();
        console.log('✅ Alertas recebidos:', alertas);

        container.innerHTML = '';

        if (!alertas || alertas.length === 0) {
            container.innerHTML = `
                    <div class="alert alert-success d-flex align-items-center">
                        <i class="bi bi-check-circle-fill me-2"></i>
                        <div>Sistema operando normalmente</div>
                    </div>`;
            return;
        }

        for (let alerta of alertas) {
            if (!alerta || !alerta.mensagem) continue;
            let mensagem = alerta.mensagem.replace(/\$/g, '');
            if (!mensagem.trim()) continue;

            const div = document.createElement('div');
            div.className = `alert alert-${alerta.nivel || 'info'} d-flex align-items-center`;
            div.innerHTML = `
                    <i class="bi bi-${alerta.icone || 'info-circle'} me-2"></i>
                    <div>${mensagem}</div>
                `;
            container.appendChild(div);
        }

        if (container.children.length === 0) {
            container.innerHTML = `
                    <div class="alert alert-info d-flex align-items-center">
                        <i class="bi bi-info-circle me-2"></i>
                        <div>Nenhum alerta para exibir</div>
                    </div>`;
        }

    } catch (error) {
        console.error('❌ Erro ao carregar alertas:', error);
        container.innerHTML = `
                <div class="alert alert-danger d-flex align-items-center">
                    <i class="bi bi-exclamation-triangle-fill me-2"></i>
                    <div>Erro ao carregar alertas</div>
                </div>`;
    }
}

// TESTE DIRETO - Força a exibição de alertas
function testeAlertasManualmente() {
    const container = document.getElementById('alertasSistema');
    if (!container) return;

    // Limpa e adiciona alertas FIXOS (Exemplo de alertas reais)
    container.innerHTML = '';

    const alert1 = document.createElement('div');
    alert1.className = 'alert alert-warning d-flex align-items-center';
    alert1.innerHTML = '<i class="bi bi-person-exclamation me-2"></i><div>1 advogado aguardando aprovação</div>';
    container.appendChild(alert1);

    const alert2 = document.createElement('div');
    alert2.className = 'alert alert-danger d-flex align-items-center';
    alert2.innerHTML = '<i class="bi bi-folder-x me-2"></i><div>1 processo sem advogado responsável</div>';
    container.appendChild(alert2);
}

// ------------------------------
// INICIALIZAÇÃO DA DASHBOARD (PADRÃO)
// ------------------------------
function inicializarDashboardMaster() {
    const sidebar = document.getElementById('sidebarNav');
    const overlay = document.querySelector('.sidebar-overlay');

    // 1. Configura sidebar mobile
    if (sidebar && overlay) {
        sidebar.addEventListener('show.bs.collapse', () => overlay.style.display = 'block');
        sidebar.addEventListener('hide.bs.collapse', () => overlay.style.display = 'none');
        overlay.addEventListener('click', () => {
            const bsCollapse = bootstrap.Collapse.getInstance(sidebar);
            if (bsCollapse) bsCollapse.hide();
        });
    }

    // 2. Ano do footer
    const footerYear = document.getElementById('footerYear');
    if(footerYear) footerYear.textContent = new Date().getFullYear();

    // 3. Carrega dados (assíncronos)
    carregarDashboard();

    // 4. TESTE: Executa teste manual após 3 segundos
    setTimeout(testeAlertasManualmente, 3000);

    console.log('✅ Dashboard Master inicializado com sucesso!');
}

// Inicia quando o DOM estiver pronto
document.addEventListener('DOMContentLoaded', inicializarDashboardMaster);