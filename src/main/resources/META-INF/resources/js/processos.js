let todosProcessos = [];
let processoEditandoId = null;

// Função de fetch padronizada para comunicação com o backend
function fetchAutenticado(url, options = {}) {
    return fetch(url, {
        ...options,
        credentials: 'include' // Essencial para cookies de sessão/autenticação
    });
}

// Função de Logout (caso usado fora da sidebar)
function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;
    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => {
            document.cookie = "token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
            window.location.href = '/login';
        });
}

document.addEventListener('DOMContentLoaded', function() {
    carregarProcessos();

    // Inicialização de layout (sidebar, footer, etc.)
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
    if(document.getElementById('footerYear')) {
        document.getElementById('footerYear').textContent = new Date().getFullYear();
    }
});

// ----------------------------------------------------
// LÓGICA DE COMUNICAÇÃO PARA LISTAGEM
// ----------------------------------------------------
async function carregarProcessos() {
    try {
        // Endpoint corrigido: /processos/api
        const response = await fetchAutenticado('/processos/api');
        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (!response.ok) {
            throw new Error('Erro: ' + response.status);
        }

        todosProcessos = await response.json();
        filtrarProcessos(); // Exibe todos inicialmente
    } catch (error) {
        console.error('Erro ao carregar processos:', error);
        document.getElementById('tabelaProcessos').innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-danger py-4">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        Erro ao carregar processos. Verifique o console.
                    </td>
                </tr>
            `;
    }
}

// ----------------------------------------------------
// LÓGICA DE EXIBIÇÃO E FILTRAGEM
// ----------------------------------------------------
function atualizarTabela(processos) {
    const tbody = document.getElementById('tabelaProcessos');
    if (!processos || processos.length === 0) {
        tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">
                        <i class="bi bi-folder-x" style="font-size: 3rem;"></i>
                        <p class="mt-2 mb-0">Nenhum processo encontrado</p>
                    </td>
                </tr>
            `;
        return;
    }

    tbody.innerHTML = processos.map(processo => {
        let statusClass;
        // Verifica se status existe para evitar erro
        let statusText = processo.status || 'N/A';

        switch(statusText) {
            case 'EM_ANDAMENTO': statusClass = 'bg-gradient-warning'; break;
            case 'CONCLUIDO': statusClass = 'bg-gradient-success'; break;
            case 'CANCELADO': statusClass = 'bg-gradient-danger'; break;
            default: statusClass = 'bg-gradient-secondary'; break;
        }

        let prazoInfo = processo.prazoFinal || '-';
        // Conversão de data simples para evitar erro no JS
        if (processo.prazoFinal && new Date(processo.prazoFinal) < new Date() && statusText === 'EM_ANDAMENTO') {
            prazoInfo = '<span class="text-danger fw-bold">' + processo.prazoFinal + ' (Atrasado)</span>';
        }

        // CORREÇÃO AQUI: Usando concatenação (+) em vez de ${} para o ID
        // Isso impede que o Qute tente processar a variável processo.id
        return `
                <tr>
                    <td class="ps-3 text-sm">
                        <p class="fw-bold mb-0">` + (processo.numeroProcesso || 'N/A') + `</p>
                        <small class="text-muted">` + (processo.titulo || '-') + `</small>
                    </td>
                    <td class="text-xs">` + (processo.cliente || '-') + `</td>
                    <td class="text-xs">` + (processo.advogadoResponsavel || '-') + `</td>
                    <td><span class="badge ` + statusClass + `">` + statusText.replace('_', ' ') + `</span></td>
                    <td class="text-xs">` + prazoInfo + `</td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            <a href="/processodetalhe/` + processo.id + `" class="btn btn-outline-info" title="Ver Detalhes">
                                <i class="bi bi-eye"></i>
                            </a>
                            <button class="btn btn-outline-primary" onclick="editarProcesso(` + processo.id + `)" title="Editar" data-bs-toggle="modal" data-bs-target="#modalProcesso">
                                <i class="bi bi-pencil"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
    }).join('');
}

function filtrarProcessos() {
    const searchTerm = document.getElementById('searchInput').value.toLowerCase();
    const statusFilter = document.getElementById('statusFilter').value;

    const processosFiltrados = todosProcessos.filter(processo => {
        const matchesSearch =
            (processo.numeroProcesso && processo.numeroProcesso.toLowerCase().includes(searchTerm)) ||
            (processo.titulo && processo.titulo.toLowerCase().includes(searchTerm)) ||
            (processo.cliente && processo.cliente.toLowerCase().includes(searchTerm));

        const matchesStatus = !statusFilter || processo.status === statusFilter;

        return matchesSearch && matchesStatus;
    });

    atualizarTabela(processosFiltrados);
}

async function carregarSelects() {
    // Carrega Clientes
    try {
        // Ajuste a URL conforme seu ClientesController (/clientes/api ou similar)
        const respClientes = await fetchAutenticado('/clientes/api/simples');
        if (respClientes.ok) {
            const clientes = await respClientes.json();
            const select = document.getElementById('processoCliente');
            select.innerHTML = '<option value="">Selecione...</option>' +
                clientes.map(c => '<option value="' + c.id + '">' + c.nome + '</option>').join('');
        }
    } catch(e) { console.error("Erro ao carregar clientes", e); }

    // Carrega Advogados (apenas se for Master)
    try {
        // Ajuste a URL conforme seu UsuarioController
        const respAdv = await fetchAutenticado('/usuarios/api/advogados');
        if (respAdv.ok) {
            const advogados = await respAdv.json();
            const select = document.getElementById('processoAdvogado');
            select.innerHTML = '<option value="">Selecione...</option>' +
                advogados.map(a => '<option value="' + a.id + '">' + a.nome + '</option>').join('');
        }
    } catch(e) { console.error("Erro ao carregar advogados", e); }
}

function novoProcesso() {
    processoEditandoId = null;
    document.getElementById('modalProcessoLabel').textContent = 'Novo Processo';
    document.getElementById('formProcesso').reset();

    // Chama a função para preencher os selects
    carregarSelects();
}

async function editarProcesso(id) {
    try {
        // 1. Carrega os selects PRIMEIRO e aguarda o término (await)
        // Se não aguardar, tentará setar o valor em um select vazio
        await carregarSelects();

        // 2. Busca os dados do processo no backend
        const response = await fetchAutenticado('/processos/api/' + id);

        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (!response.ok) throw new Error('Falha ao carregar processo');

        const processo = await response.json();

        processoEditandoId = id;
        document.getElementById('modalProcessoLabel').textContent = 'Editar Processo';

        // 3. Preenchimento dos campos do modal
        document.getElementById('processoNumero').value = processo.numeroProcesso || '';
        document.getElementById('processoTitulo').value = processo.titulo || '';
        document.getElementById('processoDescricao').value = processo.descricao || '';
        document.getElementById('processoStatus').value = processo.status;

        // Preenchimento dos Selects (Cliente e Advogado)
        // Usa o ID vindo do DTO (clienteId e advogadoResponsavelId)
        if (processo.clienteId) {
            document.getElementById('processoCliente').value = processo.clienteId;
        }

        if (processo.advogadoResponsavelId) {
            document.getElementById('processoAdvogado').value = processo.advogadoResponsavelId;
        }

    } catch (error) {
        console.error(error);
        alert('Erro ao carregar dados do processo para edição.');
    }
}

async function salvarProcesso() {
    // Coleta de dados do formulário
    const dados = {
        numeroProcesso: document.getElementById('processoNumero').value,
        titulo: document.getElementById('processoTitulo').value,
        descricao: document.getElementById('processoDescricao').value,
        status: document.getElementById('processoStatus').value,
        clienteId: document.getElementById('processoCliente').value || null,
        advogadoResponsavelId: document.getElementById('processoAdvogado').value || null
    };

    const method = processoEditandoId ? 'PUT' : 'POST';

    const url = processoEditandoId ? '/processos/api/' + processoEditandoId : '/processos/api';

    try {
        const response = await fetchAutenticado(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados)
        });

        if (response.ok) {
            alert('Processo ' + (processoEditandoId ? 'atualizado' : 'criado') + ' com sucesso!');

            const modal = bootstrap.Modal.getInstance(document.getElementById('modalProcesso'));
            if (modal) modal.hide();

            carregarProcessos();
        } else {
            const errorData = await response.json();
            alert('Erro ao salvar processo: ' + (errorData.error || response.statusText));
        }
    } catch (error) {
        console.error('Erro de comunicação:', error);
        alert('Erro de comunicação ao tentar salvar o processo.');
    }
}