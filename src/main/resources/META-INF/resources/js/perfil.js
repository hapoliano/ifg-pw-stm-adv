// Funções de Gerenciamento do Perfil (Inalteradas)
function toggleEdit(){
    document.getElementById('formEdit').classList.toggle('hidden');
}

function updateView(payload) {
    document.getElementById('viewNome').textContent = payload.nome;
    document.getElementById('viewTelefone').textContent = payload.telefone || 'N/A';
    document.getElementById('viewEndereco').textContent = payload.endereco || 'N/A';
    document.getElementById('viewCidade').textContent = payload.cidade || 'N/A';

    if (document.getElementById('viewOAB')) {
        document.getElementById('viewOAB').textContent = payload.oab || 'N/A';
        document.getElementById('viewEspecialidade').textContent = payload.especialidade || 'N/A';
        document.getElementById('viewFormacao').textContent = payload.formacao || 'N/A';
        document.getElementById('viewExperiencia').textContent = payload.experiencia || 'N/A';
    }
}

document.getElementById('formPerfil').addEventListener('submit', async e=>{
    e.preventDefault();

    const payload = {
        nome: document.getElementById('editNome').value,
        // O email é enviado, mas o backend deve ignorar e usar o do token para a busca
        email: document.getElementById('editEmail').value,
        telefone: document.getElementById('editTelefone').value,
        endereco: document.getElementById('editEndereco').value,
        cidade: document.getElementById('editCidade').value,
    };

    if (document.getElementById('editOAB')) {
        payload.oab = document.getElementById('editOAB').value;
        payload.especialidade = document.getElementById('editEspecialidade').value;
        payload.formacao = document.getElementById('editFormacao').value;
        payload.experiencia = document.getElementById('editExperiencia').value;
    }

    const res = await fetch(`/perfil/editar`,{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify(payload)
    });

    if(res.ok) {
        alert('Perfil atualizado com sucesso!');
        updateView(payload);
        toggleEdit();
    }
    else {
        alert('Erro ao atualizar perfil. Verifique os dados ou tente novamente.');
    }
});

document.getElementById('formSenha').addEventListener('submit', async e=>{
    e.preventDefault();

    const novaSenha = document.getElementById('novaSenha').value;
    const confirmar = document.getElementById('confirmarSenha').value;

    if(novaSenha.length < 8) {
        alert('A nova senha deve ter no mínimo 8 caracteres.');
        return;
    }

    if(novaSenha !== confirmar) {
        alert('As senhas não conferem');
        return;
    }

    const res = await fetch(`/perfil/senha`,{
        method:'POST',
        headers:{'Content-Type':'application/json'},
        body: JSON.stringify(novaSenha)
    });

    if(res.ok) {
        alert('Senha alterada com sucesso! Você precisará logar novamente na próxima sessão.');
        document.getElementById('novaSenha').value = '';
        document.getElementById('confirmarSenha').value = '';
    }
    else {
        alert('Erro ao alterar senha. Por favor, tente novamente.');
    }
});

// Função de Logout (Copiada do Dashboard)
function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;
    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => window.location.href = '/login');
}

document.addEventListener('DOMContentLoaded', function() {
    // Configuração de Sidebar Responsiva (Copiada do Dashboard)
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
});