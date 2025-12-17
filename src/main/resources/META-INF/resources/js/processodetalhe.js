function fetchAutenticado(url, options = {}) {
    return fetch(url, { ...options, credentials: 'include' });
}

document.addEventListener('DOMContentLoaded', function() {
    if(document.getElementById('footerYear')) {
        document.getElementById('footerYear').textContent = new Date().getFullYear();
    }
});

// --- FUNÇÕES AUXILIARES DE DATA ---
// Converte "dd/MM/yyyy" (Java/Visual) para "yyyy-MM-dd" (Input HTML)
function dateToInput(dateStr) {
    if (!dateStr) return '';

    // Se já estiver no formato ISO (yyyy-MM-dd), retorna direto
    if (dateStr.includes('-')) return dateStr;

    // Se estiver no formato BR (dd/MM/yyyy), converte
    const parts = dateStr.split('/');
    if (parts.length === 3) {
        return `${parts[2]}-${parts[1]}-${parts[0]}`;
    }

    return '';
}

// Converte "yyyy-MM-dd" (Input HTML) para "dd/MM/yyyy" (Java API)
function inputToDate(dateVal) {
    if (!dateVal || dateVal.trim() === '') return null;
    return dateVal;
}

// --- MODAL STATUS ---
function abrirModalStatus() {
    const modal = new bootstrap.Modal(document.getElementById('modalStatus'));
    modal.show();
}

async function salvarNovoStatus(processoId) {
    const novoStatus = document.getElementById('novoStatus').value;
    const modalElement = document.getElementById('modalStatus');
    const modal = bootstrap.Modal.getInstance(modalElement);

    try {
        const url = '/processodetalhe/' + processoId + '/status';
        const response = await fetchAutenticado(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: novoStatus })
        });

        if (response.ok) {
            if(modal) modal.hide();
            alert('Status atualizado com sucesso!');
            window.location.reload();
        } else {
            alert('Erro ao atualizar status.');
        }
    } catch (error) {
        console.error(error);
        alert('Erro de comunicação.');
    }
}

// --- MODAL EDIÇÃO (NOVO) ---
function abrirModalEdicao() {
    // Preenche as datas nos inputs (conversão de formato necessária)
    // Os valores originais vêm do template Qute
    const dtAbertura = "{processo.dataAbertura ?: ''}";
    const dtDist = "{processo.dataDistribuicao ?: ''}";
    const dtPrazo = "{processo.prazoFinal ?: ''}";
    const dtConclusao = "{processo.dataConclusao ?: ''}";

    document.getElementById('editDataAbertura').value = dateToInput(dtAbertura);
    document.getElementById('editDataDistribuicao').value = dateToInput(dtDist);
    document.getElementById('editPrazoFinal').value = dateToInput(dtPrazo);
    document.getElementById('editDataConclusao').value = dateToInput(dtConclusao);

    const modal = new bootstrap.Modal(document.getElementById('modalEdicao'));
    modal.show();
}

async function salvarEdicao(processoId) {
    // Coleta os dados do formulário
    const dto = {
        id: processoId,
        numeroProcesso: document.getElementById('editNumero').value,
        titulo: document.getElementById('editTitulo').value,
        descricao: document.getElementById('editDescricao').value,
        clienteId: document.getElementById('editCliente').value,
        advogadoResponsavelId: document.getElementById('editAdvogado').value || null,

        tipo: document.getElementById('editTipo').value,
        area: document.getElementById('editArea').value,
        tribunal: document.getElementById('editTribunal').value,
        vara: document.getElementById('editVara').value,
        comarca: document.getElementById('editComarca').value,
        prioridade: document.getElementById('editPrioridade').value,

        valorCausa: document.getElementById('editValorCausa').value || null,
        valorCondenacao: document.getElementById('editValorCondenacao').value || null,
        observacoes: document.getElementById('editObservacoes').value,

        // Datas convertidas de volta para dd/MM/yyyy
        dataAbertura: inputToDate(document.getElementById('editDataAbertura').value),
        dataDistribuicao: inputToDate(document.getElementById('editDataDistribuicao').value),
        prazoFinal: inputToDate(document.getElementById('editPrazoFinal').value),
        dataConclusao: inputToDate(document.getElementById('editDataConclusao').value)
    };

    // Mantém o status original (ou podemos pegar do campo se adicionarmos)
    dto.status = "{processo.status}";

    try {
        const url = '/processodetalhe/' + processoId;
        const response = await fetchAutenticado(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });

        if (response.status === 401) {
            window.location.href = '/login';
            return;
        }

        if (response.ok) {
            alert('Processo atualizado com sucesso!');
            window.location.reload();
        } else {
            let errorMsg = response.statusText;
            try {
                const data = await response.json();
                if(data.error) errorMsg = data.error;
            } catch(e){}
            alert('Erro ao atualizar: ' + errorMsg);
        }
    } catch (error) {
        console.error('Erro:', error);
        alert('Erro de comunicação ao salvar alterações.');
    }
}

// --- EXCLUIR ---
async function excluirProcesso(processoId) {
    if (!confirm('ATENÇÃO! Tem certeza que deseja excluir este processo?')) return;
    try {
        const url = '/processodetalhe/' + processoId;
        const response = await fetchAutenticado(url, { method: 'DELETE' });
        if (response.ok) {
            alert('Processo excluído!');
            window.location.href = '/processos';
        } else {
            alert('Erro ao excluir.');
        }
    } catch (error) {
        alert('Erro de comunicação.');
    }
}