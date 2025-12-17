const alertDiv = document.getElementById('alert-message');
const formCadastro = document.getElementById('formCadastro');
const tipoUsuarioSelect = document.getElementById('tipoUsuario');
const campoOab = document.getElementById('campoOab');
const inputOab = document.getElementById('oab');

function updateAlert(type, message) {
    if (!alertDiv) return;
    alertDiv.textContent = message;
    alertDiv.classList.remove('alert-success', 'alert-danger');
    if (type === 'success') alertDiv.classList.add('alert-success');
    else if (type === 'error') alertDiv.classList.add('alert-danger');
    alertDiv.style.display = 'block';
}

if(tipoUsuarioSelect) {
    tipoUsuarioSelect.addEventListener('change', function() {
        if (this.value === 'ADVOGADO') {
            campoOab.style.display = 'block';
            inputOab.required = true;
        } else {
            campoOab.style.display = 'none';
            inputOab.required = false;
            inputOab.value = '';
        }
    });
}

if(formCadastro) {
    formCadastro.addEventListener('submit', async function (e) {
        e.preventDefault();
        alertDiv.style.display = 'none';

        if (!this.checkValidity()) {
            e.stopPropagation();
            this.classList.add('was-validated');
            updateAlert('error', 'Por favor, preencha todos os campos obrigatórios.');
            return;
        }

        const nome = document.getElementById('nome').value;
        const email = document.getElementById('email').value;
        const senha = document.getElementById('senha').value;
        const confirmarSenha = document.getElementById('confirmarSenha').value;
        const tipoUsuario = document.getElementById('tipoUsuario').value;
        const telefone = document.getElementById('telefone').value;
        const oab = document.getElementById('oab').value;

        if (senha.length < 8) {
            updateAlert('error', "A senha deve ter no mínimo 8 caracteres.");
            return;
        }

        if (senha !== confirmarSenha) {
            updateAlert('error', "As senhas não coincidem. Por favor, verifique.");
            return;
        }

        const dados = {
            nome: nome,
            email: email,
            senha: senha,
            confirmarSenha: confirmarSenha,
            tipoUsuario: tipoUsuario,
            telefone: telefone,
            oab: oab
        };

        try {
            const response = await fetch('/cadastro/cadastrar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dados)
            });

            const result = await response.json();

            if (response.ok && result.success) {
                updateAlert('success', result.mensagem);
                setTimeout(() => window.location.href = result.redirectUrl, 1500);
            } else {
                updateAlert('error', "Erro: " + (result.mensagem || "Verifique os dados."));
            }
        } catch (error) {
            console.error('Erro de conexão:', error);
            updateAlert('error', "Erro de conexão com o servidor.");
        }
    });
}