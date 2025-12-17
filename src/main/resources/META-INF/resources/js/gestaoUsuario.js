const API_BASE = '/gestao-usuarios/api';

function showAlert(message, type = 'success') {
    const container = document.getElementById('statusAlerts');
    const alertHtml =
        '<div class="alert alert-' + type + ' alert-dismissible fade show" role="alert">' +
        '    <i class="bi bi-' + (type === 'success' ? 'check-circle-fill' : 'exclamation-triangle-fill') + ' me-2"></i>' +
        message +
        '    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>' +
        '</div>';
    container.innerHTML = alertHtml;
    setTimeout(() => container.innerHTML = '', 5000);
}

async function carregarEstatisticas() {
    try {
        // CORRIGIDO: Concatenação para evitar erro do Qute
        const response = await fetch(API_BASE + '/estatisticas');
        const stats = await response.json();
        document.getElementById('statTotalUsuarios').textContent = stats.totalUsuarios || 0;
        document.getElementById('statPendentes').textContent = stats.advogadosPendentes || 0;
        document.getElementById('statAprovados').textContent = stats.advogadosAprovados || 0;
        document.getElementById('statClientes').textContent = stats.totalClientes || 0;

    } catch (error) {
        console.error('Erro ao carregar estatísticas:', error);
    }
}

// Ações na Aba Pendentes
window.aprovarAdvogado = async function(id) {
    if (!confirm('Tem certeza que deseja APROVAR este advogado?')) return;

    try {
        // CORRIGIDO: Concatenação
        const response = await fetch(API_BASE + '/advogados/aprovar/' + id, { method: 'PUT' });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.mensagem || errorData.error || 'Falha na aprovação.');
        }

        showAlert('Advogado aprovado com sucesso!', 'success');

        carregarPendentes();
        carregarEstatisticas();
    } catch (error) {
        showAlert(error.message || 'Erro ao aprovar advogado.', 'danger');
    }
}

window.rejeitarAdvogado = async function(id) {
    const comentario = prompt("Informe o motivo da rejeição (Obrigatório):");
    if (!comentario) return;

    try {
        // CORRIGIDO: Concatenação
        const response = await fetch(API_BASE + '/advogados/rejeitar/' + id, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ comentario: comentario })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.mensagem || errorData.error || 'Falha na rejeição.');
        }

        showAlert('Advogado rejeitado com sucesso.', 'warning');

        carregarPendentes();
        carregarEstatisticas();
    } catch (error) {
        showAlert(error.message || 'Erro ao rejeitar advogado.', 'danger');
    }
}

// Carrega Tabela de Pendentes
async function carregarPendentes() {
    const tbody = document.getElementById('tabelaPendentes');
    tbody.innerHTML = '<tr><td colspan="5" class="text-center">Carregando...</td></tr>';

    try {
        // CORRIGIDO: Concatenação
        const response = await fetch(API_BASE + '/advogados/pendentes');
        if(!response.ok) throw new Error("Erro na API");

        const lista = await response.json();

        if (!lista || lista.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-success">Nenhum advogado pendente de verificação.</td></tr>';
            return;
        }

        // CORRIGIDO: Uso de concatenação para evitar conflito com Qute (usando 'u' como variável)
        tbody.innerHTML = lista.map(u =>
            '<tr data-id="' + u.id + '">' +
            '    <td>' + u.nome + '</td>' +
            '    <td>' + u.email + '</td>' +
            '    <td>' + (u.oab || 'N/A') + '</td>' +
            '    <td>' + (u.dataCadastro || u.dataCriacao || 'N/A') + '</td>' +
            '    <td>' +
            '        <button class="btn btn-sm btn-success btn-action" onclick="aprovarAdvogado(' + u.id + ')" title="Aprovar Advogado">' +
            '            <i class="bi bi-check-circle"></i> Aprovar' +
            '        </button>' +
            '        <button class="btn btn-sm btn-danger btn-action" onclick="rejeitarAdvogado(' + u.id + ')" title="Rejeitar Advogado">' +
            '            <i class="bi bi-x-circle"></i> Rejeitar' +
            '        </button>' +
            '    </td>' +
            '</tr>'
        ).join('');

    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Erro ao carregar dados.</td></tr>';
        console.error('Erro ao carregar pendentes:', error);
    }
}

// Ações na Aba Geral
window.toggleAtivo = async function(id, statusAtual) {
    const acao = statusAtual ? 'INATIVAR' : 'ATIVAR';
    if (!confirm('Tem certeza que deseja ' + acao + ' este usuário?')) return;

    try {
        // CORRIGIDO: Concatenação
        const response = await fetch(API_BASE + '/' + id + '/toggle-ativo', { method: 'PUT' });

        if (!response.ok) throw new Error('Falha ao ' + acao + ' usuário.');

        showAlert('Usuário ' + acao.toLowerCase() + ' com sucesso.', 'success');
        carregarTodosUsuarios();
        carregarEstatisticas();
    } catch (error) {
        showAlert(error.message || 'Erro na operação.', 'danger');
    }
}

// Carrega Tabela de Todos os Usuários
async function carregarTodosUsuarios() {
    const tbody = document.getElementById('tabelaTodosUsuarios');
    tbody.innerHTML = '<tr><td colspan="5" class="text-center">Carregando...</td></tr>';

    try {
        // CORRIGIDO: Concatenação
        const response = await fetch(API_BASE + '/todos');
        if(!response.ok) throw new Error("Erro na API");

        const lista = await response.json();

        if (!lista || lista.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-info">Nenhum usuário cadastrado.</td></tr>';
            return;
        }

        // CORRIGIDO: Uso de concatenação e variável 'item' para evitar conflito com Qute
        tbody.innerHTML = lista.map(item => {
            const isAtivo = item.ativo;
            const statusTexto = isAtivo ? 'Ativo' : 'Inativo';
            const statusClasse = isAtivo ? 'badge bg-success' : 'badge bg-danger';
            const acaoTexto = isAtivo ? 'Inativar' : 'Ativar';
            const acaoClasse = isAtivo ? 'btn-danger' : 'btn-success';
            const acaoIcone = isAtivo ? 'bi-lock' : 'bi-unlock';

            return '<tr data-id="' + item.id + '">' +
                '    <td>' + item.nome + '</td>' +
                '    <td>' + item.email + '</td>' +
                '    <td><span class="badge bg-primary">' + item.perfil + '</span></td>' +
                '    <td><span class="' + statusClasse + '">' + statusTexto + '</span></td>' +
                '    <td>' +
                '        <button class="btn btn-sm ' + acaoClasse + ' btn-action" onclick="toggleAtivo(' + item.id + ', ' + isAtivo + ')" title="' + acaoTexto + ' Usuário">' +
                '            <i class="bi ' + acaoIcone + '"></i> ' + acaoTexto +
                '        </button>' +
                '    </td>' +
                '</tr>';
        }).join('');

    } catch (error) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-danger">Erro ao carregar dados.</td></tr>';
        console.error('Erro ao carregar todos os usuários:', error);
    }
}


document.addEventListener('DOMContentLoaded', () => {
    carregarEstatisticas();
    carregarPendentes();

    const todosTab = document.getElementById('todos-tab');
    new bootstrap.Tab(todosTab);
    todosTab.addEventListener('shown.bs.tab', carregarTodosUsuarios);

    // Inicialização da Sidebar Mobile
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
});

function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;
    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => window.location.href = '/login');
}