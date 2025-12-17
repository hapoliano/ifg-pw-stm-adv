// Variáveis Globais do Módulo
let eventoModal;
let calendar;
const formatadorData = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' });

// Função principal chamada pelo HTML
function inicializarAgenda(eventosCarregados) {
    // 1. Configuração Sidebar e Footer
    configurarLayout();

    // 2. Configuração Modal Bootstrap
    const modalEl = document.getElementById('modalNovoEvento');
    if (modalEl) {
        eventoModal = new bootstrap.Modal(modalEl);
        modalEl.addEventListener('hidden.bs.modal', resetarFormulario);
    }

    // 3. Tratamento dos dados (Data nula, etc)
    const eventosCorrigidos = eventosCarregados.map(e => ({
        ...e,
        end: (e.end && e.end !== "null" && e.end.trim() !== "") ? e.end : null,
        description: e.description || ""
    }));

    // 4. Inicializar FullCalendar
    const calendarEl = document.getElementById('calendar');
    if (calendarEl) {
        calendar = new FullCalendar.Calendar(calendarEl, {
            initialView: 'dayGridMonth',
            locale: 'pt-br',
            headerToolbar: {
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek'
            },
            events: eventosCorrigidos,
            editable: true,
            selectable: true,
            eventTimeFormat: { hour: '2-digit', minute: '2-digit', meridiem: false },
            select: function(info) { abrirModalNovo(info.startStr, info.endStr, info.allDay); },
            eventClick: function(info) { abrirEdicaoEvento(info.event, eventosCorrigidos); }
        });
        calendar.render();
    }

    // 5. Estatísticas e Lista Lateral
    atualizarEstatisticas(eventosCorrigidos);
    preencherProximosEventos(eventosCorrigidos);

    // 6. Listeners de Botões
    configurarBotoes();
}

// --- Funções Auxiliares de Configuração ---

function configurarLayout() {
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
}

function configurarBotoes() {
    // Botão Salvar
    const formEv = document.getElementById("formEvento");
    if(formEv) {
        formEv.addEventListener("submit", salvarEvento);
    }

    // Botão Deletar (agora via JS, não onclick inline)
    const btnDel = document.getElementById("btnDeletarEvento");
    if(btnDel) {
        btnDel.addEventListener("click", deletarEvento);
    }
}

function resetarFormulario() {
    const form = document.getElementById('formEvento');
    if(form) form.reset();
    document.getElementById('eventoId').value = "";
    document.getElementById('modalNovoEventoLabel').textContent = "Criar Novo Compromisso";
    const btnDel = document.getElementById('btnDeletarEvento');
    if(btnDel) btnDel.style.display = 'none';
}

// --- Lógica do Calendário ---

function formatFullCalendarDate(d) {
    if (!d || d === "null") return "";
    // Se for data ISO completa, pega apenas YYYY-MM-DDTHH:mm
    return d.substring(0, 16);
}

function abrirModalNovo(start, end, allDay) {
    if(!eventoModal) return;

    resetarFormulario();

    document.getElementById('corEvento').value = "#007bff";
    document.getElementById('dataInicio').value = formatFullCalendarDate(start);
    document.getElementById('dataFim').value = formatFullCalendarDate(end);

    const chk = document.getElementById('allDay');
    if(chk) chk.checked = allDay;

    eventoModal.show();
}

function abrirEdicaoEvento(eventObj, listaEventos) {
    if(!eventoModal) return;

    // Busca o objeto original para ter acesso à descrição completa se o FullCalendar tiver truncado
    const dadosCompletos = listaEventos.find(e => e.id == eventObj.id) ||
        { description: eventObj.extendedProps.description, tipo: eventObj.extendedProps.tipo };

    document.getElementById('modalNovoEventoLabel').textContent = "Editar: " + eventObj.title;
    document.getElementById('eventoId').value = eventObj.id;
    document.getElementById('tituloEvento').value = eventObj.title;
    document.getElementById('descricaoEvento').value = dadosCompletos.description || "";

    // Tratamento de datas para o input datetime-local
    const startStr = eventObj.start ? eventObj.start.toISOString() : "";
    const endStr = eventObj.end ? eventObj.end.toISOString() : "";

    document.getElementById('dataInicio').value = formatFullCalendarDate(startStr);
    document.getElementById('dataFim').value = formatFullCalendarDate(endStr);
    document.getElementById('corEvento').value = eventObj.backgroundColor || "#007bff";

    const chk = document.getElementById('allDay');
    if(chk) chk.checked = eventObj.allDay;

    const btnDel = document.getElementById('btnDeletarEvento');
    if(btnDel) btnDel.style.display = 'inline-block';

    eventoModal.show();
}

// --- Estatísticas e Side List ---

function preencherProximosEventos(eventos) {
    const listaEl = document.getElementById('proximosEventosList');
    if(!listaEl) return;

    const agora = new Date();
    const futuros = eventos
        .filter(e => new Date(e.start) >= agora)
        .sort((a, b) => new Date(a.start) - new Date(b.start))
        .slice(0, 5);

    listaEl.innerHTML = '';
    if (futuros.length === 0) {
        listaEl.innerHTML = '<p class="text-muted small text-center mt-3">Nenhum evento futuro.</p>';
        return;
    }

    futuros.forEach(e => {
        const dataHora = formatadorData.format(new Date(e.start));
        const item = document.createElement('div');
        item.className = 'evento-item';
        item.style.borderLeftColor = e.color || '#3498db';
        item.innerHTML = `<strong class="d-block text-truncate">${e.title}</strong><small><i class="bi bi-clock me-1"></i> ${dataHora}</small>`;
        listaEl.appendChild(item);
    });
}

function atualizarEstatisticas(eventos) {
    const elHoje = document.getElementById("compromissosHoje");
    if(!elHoje) return;

    const hoje = new Date();
    hoje.setHours(0,0,0,0);
    const semana = new Date(hoje.getTime() + 7*86400000);

    const qtdHoje = eventos.filter(e => new Date(e.start).toDateString() === hoje.toDateString()).length;
    const qtdSem = eventos.filter(e => { const d=new Date(e.start); return d>=hoje && d<=semana; }).length;
    const qtdAud = eventos.filter(e => (e.title||"").toLowerCase().includes("audiência") && new Date(e.start)>new Date()).length;

    document.getElementById("compromissosHoje").textContent = qtdHoje;
    document.getElementById("compromissosSemana").textContent = qtdSem;
    document.getElementById("audiencias").textContent = qtdAud;
}

// --- AJAX Requests ---

function deletarEvento() {
    const id = document.getElementById('eventoId').value;
    if (!id || !confirm('Excluir este compromisso?')) return;

    fetch(`/agenda/evento/${id}`, { method: 'DELETE' })
        .then(r => {
            if(r.status===204) {
                alert("Excluído!");
                eventoModal.hide();
                window.location.reload();
            } else {
                alert("Erro ao excluir. Status: "+r.status);
            }
        })
        .catch(e => alert("Erro de rede: "+e));
}

function salvarEvento(e) {
    e.preventDefault();
    const btn = document.getElementById("btnSalvarEvento");
    const form = e.target;
    const isEdit = document.getElementById('eventoId').value !== "";
    const originalText = btn.innerHTML;

    btn.disabled = true;
    btn.innerHTML = "Salvando...";

    const dados = {
        id: isEdit ? parseInt(form.elements.id.value) : null,
        title: form.elements.titulo.value,
        description: form.elements.descricao.value,
        start: form.elements.dataInicio.value,
        end: form.elements.dataFim.value || null,
        color: form.elements.cor.value,
        allDay: form.elements.diaInteiro.checked,
        tipo: "OUTROS"
    };

    fetch('/agenda/evento', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(dados)
    })
        .then(r => {
            if(!r.ok) throw new Error("HTTP "+r.status);
            return r.json();
        })
        .then(() => {
            alert("Salvo com sucesso!");
            eventoModal.hide();
            window.location.reload();
        })
        .catch(err => {
            console.error(err);
            alert("Erro ao salvar: " + err.message);
        })
        .finally(() => {
            btn.disabled = false;
            btn.innerHTML = originalText;
        });
}