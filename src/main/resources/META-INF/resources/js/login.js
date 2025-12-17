document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('formLogin');
    const alertDiv = document.getElementById('alert-message');

    function updateAlert(type, message) {
        alertDiv.textContent = message;
        alertDiv.classList.remove('alert-success', 'alert-danger');
        if (type === 'success') {
            alertDiv.classList.add('alert-success');
        } else if (type === 'error') {
            alertDiv.classList.add('alert-danger');
        }
        alertDiv.style.display = 'block';
    }

    if(form) {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            alertDiv.style.display = 'none';

            const email = document.getElementById('email').value;
            const senha = document.getElementById('senha').value;

            try {
                const response = await fetch('/login/logar', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, senha })
                });

                let data = { success: false, mensagem: 'Erro desconhecido.' };

                if (response.headers.get('content-type')?.includes('application/json')) {
                    data = await response.json();
                } else if (response.status === 404) {
                    data.mensagem = 'Erro 404: Rota de login não encontrada.';
                } else {
                    data.mensagem = `Erro de conexão. Status: ${response.status}.`;
                }

                if (response.ok) {
                    updateAlert('success', data.mensagem || 'Login efetuado com sucesso!');
                    setTimeout(() => window.location.href = data.redirectUrl || '/dashboard', 500);
                } else {
                    updateAlert('error', data.mensagem || 'Erro ao efetuar login.');
                }
            } catch (err) {
                console.error('Erro:', err);
                updateAlert('error', 'Não foi possível conectar ao servidor.');
            }
        });
    }
});