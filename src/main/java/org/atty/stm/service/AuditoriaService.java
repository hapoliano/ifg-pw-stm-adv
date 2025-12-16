package org.atty.stm.service;

import org.atty.stm.model.dto.AuditoriaDTO;
import org.atty.stm.model.Auditoria;
import org.atty.stm.model.Usuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.atty.stm.repository.AuditoriaRepository;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de Negócios (Business Logic) para gerenciar o registro e a consulta
 * do histórico de Auditoria do sistema.
 */
@ApplicationScoped
public class AuditoriaService {

    @Inject
    AuditoriaRepository auditoriaRepository;

    // --- Lógica de Registro (CREATE) ---

    /**
     * Método principal e genérico para registrar qualquer ação no sistema.
     * Deve ser chamado pelos outros Services (ex: UsuarioService, ProcessoService).
     *
     * @param usuarioId ID do usuário que executou a ação (pode ser null).
     * @param usuarioNome Nome/Email do usuário.
     * @param acao Ação realizada (ex: LOGIN, CREATE_USER, UPDATE_PROCESSO).
     * @param entidade Entidade afetada (ex: USUARIO, PROCESSO).
     * @param entidadeId ID da entidade afetada (pode ser null).
     * @param descricao Detalhe da ação.
     * @param ipAddress Endereço IP de origem.
     * @param userAgent User-Agent do navegador/aplicativo.
     */
    @Transactional
    public void registrarAcao(Long usuarioId, String usuarioNome, String acao,
                              String entidade, Long entidadeId, String descricao,
                              String ipAddress, String userAgent) {

        Auditoria auditoria = new Auditoria(
                usuarioId, usuarioNome, acao, entidade, entidadeId,
                descricao, ipAddress, userAgent
        );

        // Delega a persistência ao Repository (DAO)
        auditoriaRepository.persist(auditoria);
    }

    // --- Métodos de Conveniência (para uso específico) ---

    /**
     * Registra um login bem-sucedido.
     */
    public void registrarLoginSucesso(Usuario usuario, String ipAddress, String userAgent) {
        registrarAcao(usuario.getId(), usuario.getNome(), "LOGIN", "USUARIO", usuario.getId(),
                "Login bem-sucedido", ipAddress, userAgent);
    }

    /**
     * Registra uma tentativa de login falha.
     */
    public void registrarLoginFalho(String email, String ipAddress, String userAgent) {
        // ID e Nome ficam nulos (ou usam o email) em tentativa falha
        registrarAcao(null, email, "LOGIN_FALHO", "USUARIO", null,
                "Tentativa de login falhou para: " + email, ipAddress, userAgent);
    }

    /**
     * Registra uma ação simples com base no usuário logado.
     */
    @Transactional
    public void registrarAcaoSimples(Usuario usuario, String acao, String entidade, Long entidadeId,
                                     String descricao, String ipAddress, String userAgent) {
        registrarAcao(usuario.getId(), usuario.getNome(), acao, entidade, entidadeId, descricao, ipAddress, userAgent);
    }

    // --- Lógica de Negócio de Busca (READ) ---

    /**
     * Lista os logs de auditoria mais recentes, convertidos em DTOs.
     *
     * @param limit O número máximo de logs.
     * @return Lista de AuditoriaDTO, ordenada por data decrescente.
     */
    public List<AuditoriaDTO> listarLogsRecentes(int limit) {
        // 1. Delega a busca ao Repository
        List<Auditoria> logs = auditoriaRepository.findRecentLogs(limit);

        // 2. Converte a lista de entidades para DTOs usando Stream e o método estático do DTO
        return logs.stream()
                .map(AuditoriaDTO::fromEntity)
                .collect(Collectors.toList());
    }
}