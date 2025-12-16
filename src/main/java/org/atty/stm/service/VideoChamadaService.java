package org.atty.stm.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.atty.stm.exception.ServiceException; // Import necessário para a exceção base
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Path("/video/api")
@RolesAllowed({"MASTER","ADVOGADO","CLIENTE"})
@ApplicationScoped
public class VideoChamadaService {

    @ConfigProperty(name = "openvidu.url")
    String OPENVIDU_URL;

    @ConfigProperty(name = "openvidu.secret")
    String OPENVIDU_SECRET;

    // Injeção de dependências para o logger (boas práticas)
    // private static final Logger LOG = Logger.getLogger(VideoChamadaService.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    // DTO de entrada para a requisição de token
    // Records são ideais para DTOs em Java 14+
    record TokenRequest(String sessionId, String userName) {}

    /**
     * Endpoint que recebe o ID da sessão e o nome do usuário e retorna um token OpenVidu.
     * Este método garante que a sessão OpenVidu existe antes de gerar o token.
     */
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,String> gerarToken(TokenRequest request) {
        try {
            // 1. Obtém a SessionId (Cria se não existir no OpenVidu)
            String finalSessionId = obterSessao(request.sessionId());

            // 2. Gera o Token para a sessão, injetando o nome do cliente
            String token = gerarTokenOpenVidu(finalSessionId, request.userName());

            return Map.of("token", token, "sessionId", finalSessionId);

        } catch (IOException e) {
            // Se houver qualquer falha na comunicação com a API do OpenVidu,
            // lançamos ServiceException para que o ControllerBase a capture e retorne 500.
            // LOG.error("Erro ao comunicar com OpenVidu:", e);
            throw new ServiceException("Falha na comunicação com o servidor de vídeo (OpenVidu).", e);
        }
    }

    /**
     * Tenta obter uma sessão existente no OpenVidu ou cria uma nova com o ID fornecido
     * se ela não for encontrada (status 404).
     */
    private String obterSessao(String sessionId) throws IOException {
        // 1. Tenta obter a sessão (GET /api/sessions/{sessionId})
        String getUrl = OPENVIDU_URL + "/api/sessions/" + sessionId;
        HttpURLConnection conGet = (HttpURLConnection) new URL(getUrl).openConnection();
        conGet.setRequestMethod("GET");
        conGet.setRequestProperty("Authorization", "Basic " + base64Auth());

        int getResponseCode = conGet.getResponseCode();

        if (getResponseCode == 200) {
            // Sessão já existe
            return sessionId;
        } else if (getResponseCode == 404) {
            // Sessão não existe, cria uma nova usando o sessionId fornecido
            return criarSessaoOpenVidu(sessionId);
        } else {
            // Outro erro HTTP
            String error = lerResposta(conGet.getErrorStream());
            throw new IOException("Erro ao verificar sessão OpenVidu. Código: " + getResponseCode + ". Resposta: " + error);
        }
    }

    /**
     * Cria uma nova sessão com o ID especificado (customSessionId).
     */
    private String criarSessaoOpenVidu(String sessionId) throws IOException {
        String url = OPENVIDU_URL + "/api/sessions";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Basic " + base64Auth());
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        // Define o ID personalizado
        String body = "{\"customSessionId\": \"" + sessionId + "\"}";

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes());
        }

        int responseCode = con.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            String error = lerResposta(con.getErrorStream());
            throw new IOException("Erro ao criar sessão OpenVidu. Código: " + responseCode + ". Resposta: " + error);
        }

        String response = lerResposta(con.getInputStream());
        JsonNode node = objectMapper.readTree(response);
        return node.get("id").asText(); // Retorna o ID da sessão criada
    }

    /**
     * Gera o token para a sessão e injeta o nome do usuário no campo 'data' da conexão.
     */
    private String gerarTokenOpenVidu(String sessionId, String userName) throws IOException {
        String url = OPENVIDU_URL + "/api/tokens";
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Authorization", "Basic " + base64Auth());
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        // Formata o payload para incluir o sessionId e o clientData
        // O clientData é usado pelo frontend para exibir o nome do participante.
        String body = String.format("{\"session\":\"%s\", \"data\":\"{\\\"clientData\\\":\\\"%s\\\"}\"}", sessionId, userName);

        try (OutputStream os = con.getOutputStream()) {
            os.write(body.getBytes());
        }

        int responseCode = con.getResponseCode();
        if (responseCode != 200 && responseCode != 201) {
            String error = lerResposta(con.getErrorStream());
            throw new IOException("Erro ao gerar token OpenVidu. Código: " + responseCode + ". Resposta: " + error);
        }

        String response = lerResposta(con.getInputStream());
        JsonNode node = objectMapper.readTree(response);
        return node.get("token").asText();
    }

    /**
     * Cria a string de autenticação Basic para a API do OpenVidu.
     */
    private String base64Auth() {
        return Base64.getEncoder().encodeToString(("OPENVIDUAPP:" + OPENVIDU_SECRET).getBytes());
    }

    /**
     * Lê o corpo da resposta de uma conexão HTTP.
     */
    private String lerResposta(InputStream inputStream) throws IOException {
        if (inputStream == null) return "";
        try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }
}