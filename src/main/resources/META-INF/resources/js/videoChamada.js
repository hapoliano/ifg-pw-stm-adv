// Funções de Logout e Sidebar
function fazerLogout() {
    if (!confirm('Deseja realmente sair do sistema?')) return;
    fetch('/login/logout', { method: 'POST' })
        .then(() => window.location.href = '/login')
        .catch(() => window.location.href = '/login');
}

// Configuração de Sidebar Mobile
document.addEventListener('DOMContentLoaded', function() {
    const sidebar = document.getElementById('sidebarNav');
    const overlay = document.querySelector('.sidebar-overlay');

    // A lógica de ativação/desativação do collapse da sidebar deve estar em sidebar.css ou em um script global.
    // Já que você está unificando, a inicialização de ano e o overlay é o suficiente aqui.
    if (sidebar && !overlay) {
        // Se o overlay não existir (o que deve acontecer se for a primeira inclusão do sidebar.html),
        // a lógica de clique do botão é tratada pelo Bootstrap em conjunto com o CSS da sidebar.
        // Para garantir que o overlay do mobile feche o menu, vamos adicioná-lo ao DOM se não existir.
        const newOverlay = document.createElement('div');
        newOverlay.className = 'sidebar-overlay d-lg-none';
        newOverlay.setAttribute('data-bs-toggle', 'collapse');
        newOverlay.setAttribute('data-bs-target', '#sidebarNav');
        document.body.appendChild(newOverlay);

        sidebar.addEventListener('show.bs.collapse', () => newOverlay.style.display = 'block');
        sidebar.addEventListener('hide.bs.collapse', () => newOverlay.style.display = 'none');
        newOverlay.addEventListener('click', () => {
            const bsCollapse = bootstrap.Collapse.getInstance(sidebar);
            if (bsCollapse) bsCollapse.hide();
        });
    }


    // Ano do footer
    document.getElementById('footerYear').textContent = new Date().getFullYear();
});


// ----------------------------------------------------
// LÓGICA DE VIDEOCHAMADA (OpenVidu)
// ----------------------------------------------------
const OV = new OpenVidu();
let session = null;
let publisher = null;
let micOn = true;
let cameraOn = true;

// Dados injetados pelo Qute
const USER_NAME = "{usuario.nome}";
// Mantido o USER_ID na SESSION_ID para garantir unicidade, se necessário.
const SESSION_ID = "Sessao_{usuario.id}";

document.getElementById('session-title').textContent = "Sala " + SESSION_ID;

document.getElementById('join-btn').addEventListener('click', joinSession);
document.getElementById('leave-btn').addEventListener('click', leaveSession);

// 1. Chamada ao Backend para obter Token
async function getToken() {
    // Assume que o endpoint de token é /video/api/token
    try {
        const res = await fetch('/video/api/token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ sessionId: SESSION_ID, userName: USER_NAME })
        });

        if (!res.ok) {
            // Tenta ler como JSON, se falhar, lê como texto
            const text = await res.text();
            let errorMsg = 'Erro desconhecido no servidor.';
            try {
                const json = JSON.parse(text);
                errorMsg = json.error || json.message || errorMsg;
            } catch (e) {
                console.warn("Resposta de erro não é JSON:", text);
                errorMsg = "Erro interno no servidor (Verifique os logs do Quarkus).";
            }
            throw new Error(errorMsg);
        }

        const data = await res.json();
        return data.token;
    } catch (err) {
        // Repassa o erro para o catch do joinSession
        throw err;
    }
}

// 2. Conectar à Sessão (Lógica OpenVidu...)
async function joinSession() {
    if (session) return;

    document.getElementById('join-btn').disabled = true;

    try {
        const token = await getToken();

        session = OV.initSession();

        // Setup Listeners
        session.on('streamCreated', event => {
            const subscriber = session.subscribe(event.stream, undefined);
            // A stream connection data contém o nome do usuário enviado na conexão
            const clientData = JSON.parse(event.stream.connection.data).clientData;
            addVideo(subscriber.stream.streamId, clientData, subscriber);
            addParticipant(subscriber.stream.streamId, clientData);
        });

        session.on('streamDestroyed', event => {
            removeVideo(event.stream.streamId);
            removeParticipant(event.stream.streamId);
        });

        session.on('sessionDisconnected', event => {
            if (event.reason !== 'disconnect') {
                console.warn('Sessão desconectada por motivo inesperado:', event.reason);
            }
            leaveSession();
        });

        // Listener de Chat (Signal)
        session.on('signal:chat', event => {
            const data = JSON.parse(event.data);
            // Evita que a própria mensagem seja duplicada (já é adicionada em sendMessage)
            if (data.user !== USER_NAME) {
                addChatMessage(data.user, data.message);
            }
        });

        await session.connect(token, { clientData: USER_NAME });

        // Inicializa Publisher (seu vídeo)
        publisher = OV.initPublisher(undefined, {
            audioSource: undefined, videoSource: undefined, publishAudio: true, publishVideo: true, mirror: false
        });

        session.publish(publisher);
        addVideo(publisher.stream.streamId, USER_NAME + " (Você)", publisher);
        addParticipant(publisher.stream.streamId, USER_NAME + " (Você)");

        updateControls(true);
        document.getElementById('join-btn').style.display = 'none';
        document.getElementById('leave-btn').style.display = 'inline-block';

    } catch (error) {
        console.error('Erro ao conectar à sessão:', error.message);
        alert('Falha ao conectar: ' + error.message);
        updateControls(false);
        document.getElementById('join-btn').style.display = 'inline-block';
        document.getElementById('leave-btn').style.display = 'none';
    }
}

function leaveSession() {
    if (session) { session.disconnect(); }
    session = null;
    publisher = null;
    document.getElementById('video-grid').innerHTML = '<div class="col-12 text-center my-auto p-5 text-white"><p class="opacity-7">Aguardando conexão...</p></div>';
    document.getElementById('participantList').innerHTML = '<li class="list-group-item text-muted text-center"><small>Ninguém conectado</small></li>';
    document.getElementById('chatMessages').innerHTML = '<div class="text-center text-muted"><small>Nenhuma mensagem ainda</small></div>';
    updateControls(false);
    document.getElementById('join-btn').style.display = 'inline-block';
    document.getElementById('join-btn').disabled = false;
    document.getElementById('leave-btn').style.display = 'none';
}

function toggleMic() {
    if (!publisher) return;
    micOn = !micOn;
    publisher.publishAudio(micOn);
    document.getElementById('btnMic').innerHTML = micOn ? '<i class="bi bi-mic"></i>' : '<i class="bi bi-mic-mute"></i>';
    document.getElementById('btnMic').classList.toggle('btn-secondary', micOn);
    document.getElementById('btnMic').classList.toggle('btn-warning', !micOn);
}

function toggleCamera() {
    if (!publisher) return;
    cameraOn = !cameraOn;
    publisher.publishVideo(cameraOn);
    document.getElementById('btnCamera').innerHTML = cameraOn ? '<i class="bi bi-camera-video"></i>' : '<i class="bi bi-camera-video-off"></i>';
    document.getElementById('btnCamera').classList.toggle('btn-secondary', cameraOn);
    document.getElementById('btnCamera').classList.toggle('btn-warning', !cameraOn);
}

function shareScreen() {
    alert('Compartilhamento de tela em desenvolvimento. Requer lógica mais complexa no OpenVidu.');
}

function sendMessage() {
    const chatInput = document.getElementById('chatInput');
    const message = chatInput.value.trim();

    if (message && session) {
        const signalOptions = {
            data: JSON.stringify({ user: USER_NAME, message: message }),
            type: 'chat',
            to: [], // Envia para todos na sessão
        };
        session.signal(signalOptions);
        chatInput.value = '';
        addChatMessage("Você", message);
    }
}

function addChatMessage(user, message) {
    const chatBox = document.getElementById('chatMessages');
    const isSelf = user === USER_NAME || user === "Você";
    const msgClass = isSelf ? 'text-end' : 'text-start';
    const userText = isSelf ? '' : `<small class="fw-bold">${user}:</small>`;

    // Remove a mensagem inicial de "Nenhuma mensagem ainda"
    const initialMessage = chatBox.querySelector('.text-muted');
    if (initialMessage) { initialMessage.parentElement.remove(); }

    const newMsg = document.createElement('div');
    newMsg.className = `p-1 ${msgClass}`;
    // Uso de 'bg-primary' e 'bg-light' para diferenciar as mensagens no chat
    newMsg.innerHTML = `${userText} <p class="mb-0 ${isSelf ? 'bg-primary text-white p-2 d-inline-block rounded' : 'bg-light p-2 d-inline-block rounded'}">${message}</p>`;

    // Se a primeira mensagem estiver lá
    if (chatBox.children[0] && chatBox.children[0].textContent.includes('Nenhuma mensagem ainda')) {
        chatBox.innerHTML = '';
    }

    chatBox.appendChild(newMsg);
    chatBox.scrollTop = chatBox.scrollHeight;
}

// Funções de manipulação de vídeo e participantes (mantidas)
function addVideo(streamId, userData, streamManager) {
    const initialMessage = document.querySelector('#video-grid .text-white');
    if (initialMessage) { initialMessage.parentElement.remove(); }

    const videoId = 'video-' + streamId;
    let videoBox = document.getElementById(videoId);

    if (!videoBox) {
        videoBox = document.createElement('div');
        videoBox.className = 'video-box';
        videoBox.id = videoId;
        videoBox.setAttribute('data-stream-id', streamId);
        document.getElementById('video-grid').appendChild(videoBox);
    }

    // Adiciona o elemento de vídeo. streamManager é Publisher ou Subscriber.
    streamManager.addVideoElement(videoBox.id);

    const label = document.createElement('div');
    label.className = 'video-label';
    label.textContent = userData;
    videoBox.appendChild(label);
}

function removeVideo(streamId) {
    const element = document.querySelector(`.video-box[data-stream-id='${streamId}']`);
    if (element) { element.remove(); }
    if (document.getElementById('video-grid').children.length === 0) {
        document.getElementById('video-grid').innerHTML = '<div class="col-12 text-center my-auto p-5 text-white"><p class="opacity-7">Aguardando conexão...</p></div>';
    }
}

function addParticipant(streamId, name) {
    const list = document.getElementById('participantList');
    const initialItem = list.querySelector('.text-muted');
    if (initialItem) initialItem.parentElement.remove();

    const listItem = document.createElement('li');
    listItem.className = 'list-group-item d-flex justify-content-between align-items-center';
    listItem.id = 'participant-' + streamId;
    listItem.innerHTML = `<i class="bi bi-person-circle me-2"></i> ${name}`;
    list.appendChild(listItem);
}

function removeParticipant(connectionId) {
    const item = document.getElementById('participant-' + connectionId);
    if (item) { item.remove(); }
    if (document.getElementById('participantList').children.length === 0) {
        document.getElementById('participantList').innerHTML = '<li class="list-group-item text-muted text-center"><small>Ninguém conectado</small></li>';
    }
}

function updateControls(isConnected) {
    document.getElementById('leave-btn').disabled = !isConnected;
    document.getElementById('btnMic').disabled = !isConnected;
    document.getElementById('btnCamera').disabled = !isConnected;
    document.getElementById('btnScreen').disabled = !isConnected;
    micOn = true; cameraOn = true;
    document.getElementById('btnMic').innerHTML = '<i class="bi bi-mic"></i>';
    document.getElementById('btnCamera').innerHTML = '<i class="bi bi-camera-video"></i>';
    document.getElementById('btnMic').className = 'btn btn-secondary me-2';
    document.getElementById('btnCamera').className = 'btn btn-secondary me-2';
}

function copyLink() {
    const link = document.getElementById('inviteLink');
    link.select();
    link.setSelectionRange(0, 99999);
    document.execCommand("copy");
    alert("Link copiado: " + link.value);
}