let clienteEditando = null;

// ------------------------------
// FUNÇÕES PADRONIZADAS
// ------------------------------
function fetchAutenticado(url, options = {}) {
    return fetch(url, {
        ...options,
        credentials: 'include'
    });
}

// Nota: O botão de Logout agora está no sidebar.html
// Mas mantemos a função caso algum outro botão dependa dela
function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;

    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => {
            document.cookie = "token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";
            window.location.href = '/login';
        });
}

// ------------------------------
// LÓGICA ESPECÍFICA DO CLIENTE
// ------------------------------

// Carregar clientes quando a página abrir
document.addEventListener('DOMContentLoaded', function() {
    inicializarClientes();
    carregarClientes();
});

function inicializarClientes() {
    // Inicialização de sidebar e overlay
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

    const footerYear = document.getElementById('footerYear');
    if(footerYear) footerYear.textContent = new Date().getFullYear();

    console.log('✅ Clientes inicializado.');
}

// Lógica de Filtro/Pesquisa
function filtrarClientes() {
    const filter = document.getElementById('clienteSearch').value.toUpperCase();
    const table = document.getElementById('tabelaClientesTable');
    const tr = table.getElementsByTagName('tr');

    for (let i = 1; i < tr.length; i++) {
        let visible = false;
        const cells = tr[i].getElementsByTagName('td');
        // Colunas: 0 (Nome), 1 (Email), 2 (Telefone)
        for (let j = 0; j <= 2; j++) {
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

// Carregar lista de clientes
async function carregarClientes() {
    try {
        // A API correta é /clientes/api (GET)
        const response = await fetchAutenticado('/clientes/api');
        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (!response.ok) {
            throw new Error('Erro: ' + response.status);
        }

        const clientes = await response.json();
        atualizarTabela(clientes);
    } catch (error) {
        console.error('Erro ao carregar clientes:', error);
        document.getElementById('tabelaClientes').innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-danger py-4">
                        <i class="bi bi-exclamation-triangle me-2"></i>
                        Erro ao carregar clientes
                    </td>
                </tr>
            `;
    }
}

// Atualizar a tabela com os clientes
function atualizarTabela(clientes) {
    const tbody = document.getElementById('tabelaClientes');
    if (!clientes || clientes.length === 0) {
        tbody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-muted py-4">
                        <i class="bi bi-people" style="font-size: 2rem;"></i>
                        <p class="mt-2 mb-0">Nenhum cliente cadastrado</p>
                        <small>Clique em "Novo Cliente" para adicionar</small>
                    </td>
                </tr>
            `;
        return;
    }

    let html = '';
    clientes.forEach(cliente => {
        const nome = cliente.nome || '';
        const email = cliente.email || '';
        const telefone = cliente.telefone || '-';
        const status = cliente.status || 'ATIVO';
        const statusClass = status === 'ATIVO' ? 'bg-success' : 'bg-secondary';
        const id = cliente.id;

        // Verifica se tem advogado para montar o HTML condicional
        const advogadoHtml = cliente.advogadoResponsavel ?
            '<small class="text-muted ps-3">Adv: ' + cliente.advogadoResponsavel + '</small>' : '';

        // CORREÇÃO: Usando concatenação (+) para evitar conflito com o Qute
        html += `
                <tr>
                    <td>
                        <p class="text-sm font-weight-bold mb-0 ps-3">` + nome + `</p>
                        ` + advogadoHtml + `
                    </td>
                    <td><p class="text-sm mb-0">` + email + `</p></td>
                    <td><p class="text-sm mb-0">` + telefone + `</p></td>
                    <td>
                        <span class="badge ` + statusClass + `">
                            ` + status + `
                        </span>
                    </td>
                    <td>
                        <div class="btn-group btn-group-sm">
                            <button class="btn btn-outline-primary btn-sm" onclick="editarCliente(` + id + `)" title="Editar">
                                <i class="bi bi-pencil"></i>
                            </button>
                            <button class="btn btn-outline-danger btn-sm" onclick="excluirCliente(` + id + `)" title="Excluir">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </td>
                </tr>
            `;
    });

    tbody.innerHTML = html;
}

// Abrir modal para novo cliente
function novoCliente() {
    clienteEditando = null;
    document.getElementById('modalTitulo').textContent = 'Novo Cliente';
    document.getElementById('formCliente').reset();
    document.getElementById('clienteId').value = '';
}

// --- Carregar dados do cliente para edição (CORRIGIDO) ---
async function editarCliente(id) {
    try {
        // CORREÇÃO: Use concatenação (+) para evitar que o Qute estrague a URL
        // Antes estava: `/clientes/api/${id}` -> O Qute tentava processar o ${id}
        const url = '/clientes/api/' + id;

        const response = await fetchAutenticado(url);

        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (!response.ok) {
            console.error("Erro na requisição: ", response.status); // Veja isso no F12 se der erro
            throw new Error('Erro ao carregar cliente: ' + response.status);
        }

        const cliente = await response.json();

        clienteEditando = id;
        document.getElementById('modalTitulo').textContent = 'Editar Cliente';
        document.getElementById('clienteId').value = cliente.id || '';
        document.getElementById('clienteNome').value = cliente.nome || '';
        document.getElementById('clienteEmail').value = cliente.email || '';
        document.getElementById('clienteTelefone').value = cliente.telefone || '';
        document.getElementById('clienteCpfCnpj').value = cliente.cpfCnpj || '';
        document.getElementById('clienteEndereco').value = cliente.endereco || '';
        document.getElementById('clienteCidade').value = cliente.cidade || '';
        document.getElementById('clienteEstado').value = cliente.estado || '';
        document.getElementById('clienteCep').value = cliente.cep || '';

        const modal = new bootstrap.Modal(document.getElementById('modalCliente'));
        modal.show();
    } catch (error) {
        console.error('Erro ao carregar cliente:', error);
        alert('Erro ao carregar dados do cliente. Verifique o console (F12).');
    }
}

// Salvar cliente (criar ou atualizar)
async function salvarCliente() {
    const cliente = {
        nome: document.getElementById('clienteNome').value.trim(),
        email: document.getElementById('clienteEmail').value.trim(),
        telefone: document.getElementById('clienteTelefone').value.trim(),
        cpfCnpj: document.getElementById('clienteCpfCnpj').value.trim(),
        endereco: document.getElementById('clienteEndereco').value.trim(),
        cidade: document.getElementById('clienteCidade').value.trim(),
        estado: document.getElementById('clienteEstado').value.trim(),
        cep: document.getElementById('clienteCep').value.trim()
    };

    if (!cliente.nome || !cliente.email) {
        alert('Por favor, preencha os campos obrigatórios (Nome e Email)');
        return;
    }

    try {
        // CORREÇÃO: Usar concatenação (+) em vez de template literal `${}`
        // O Qute não entende ${clienteEditando} pois é uma variável JavaScript
        const url = clienteEditando ?
            '/clientes/api/' + clienteEditando : '/clientes/api';

        const method = clienteEditando ? 'PUT' : 'POST';

        const response = await fetchAutenticado(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(cliente)
        });

        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (response.ok) {
            const modal = bootstrap.Modal.getInstance(document.getElementById('modalCliente'));
            modal.hide();
            carregarClientes();
            alert(clienteEditando ? 'Cliente atualizado com sucesso!' : 'Cliente criado com sucesso!');
        } else {
            const errorData = await response.json();
            alert('Erro ao salvar cliente: ' + (errorData.error || response.statusText));
        }
    } catch (error) {
        console.error('Erro ao salvar cliente:', error);
        alert('Erro ao salvar cliente. Verifique o console.');
    }
}

// Excluir cliente
async function excluirCliente(id) {
    if (!confirm('Tem certeza que deseja excluir este cliente? Isso não será possível se houver processos vinculados.')) {
        return;
    }

    try {
        // CORREÇÃO: Use concatenação aqui também
        const url = '/clientes/api/' + id;

        const response = await fetchAutenticado(url, {
            method: 'DELETE'
        });

        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (response.ok) {
            carregarClientes();
            alert('Cliente excluído com sucesso!');
        } else {
            const errorData = await response.json();
            alert('Erro ao excluir cliente: ' + (errorData.error || response.statusText));
        }
    } catch (error) {
        console.error('Erro ao excluir cliente:', error);
        alert('Erro ao excluir cliente');
    }
}