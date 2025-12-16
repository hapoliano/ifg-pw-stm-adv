package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.UsuarioDTO;
import org.atty.stm.model.AdvogadoVerificacao;
import org.atty.stm.model.Usuario;
import org.atty.stm.repository.AdvogadoVerificacaoRepository;
import org.atty.stm.util.MapperUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdvogadoVerificacaoService {

    @Inject
    AdvogadoVerificacaoRepository repository;
    @Inject
    AuditoriaService auditoriaService;
    @Inject
    UsuarioService usuarioService;

    // Listar advogados pendentes
    public List<UsuarioDTO> listarPendentes() {
        List<AdvogadoVerificacao> verificacoes = repository.findPendentes();

        return verificacoes.stream()
                .map(v -> v.advogado)
                .map(MapperUtils::toDTO)
                .collect(Collectors.toList());
    }

    // Aprovar verificação de advogado (CORRIGIDO: Busca por ID do Advogado)
    @Transactional
    public UsuarioDTO aprovar(Long advogadoId, Usuario admin, String ip, String ua) {
        // CORREÇÃO: Usa findByAdvogadoId em vez de findById
        AdvogadoVerificacao v = repository.findByAdvogadoId(advogadoId);

        // Se não achar pelo ID do advogado, tenta achar pelo ID da verificação (fallback)
        if (v == null) {
            v = repository.findById(advogadoId);
        }

        if (v == null) {
            throw new WebApplicationException("Verificação não encontrada para o advogado ID: " + advogadoId, Response.Status.NOT_FOUND);
        }

        // Se já estiver aprovado ou rejeitado, avisa
        if (!"PENDENTE".equals(v.status)) {
            throw new WebApplicationException("Esta verificação já foi processada (" + v.status + ")", Response.Status.BAD_REQUEST);
        }

        v.status = "APROVADO";
        v.dataVerificacao = LocalDateTime.now();
        repository.persist(v);

        Long usuarioId = v.advogado.id;
        // Delega a aprovação do usuário para o UsuarioService
        UsuarioDTO usuarioAtualizado = usuarioService.aprovarUsuario(usuarioId, admin, ip, ua);

        auditoriaService.registrarAcaoSimples(
                admin, "APROVACAO_VERIFICACAO", "ADVOGADO_VERIFICACAO", v.id,
                "Verificação aprovada para o usuário ID: " + usuarioId, ip, ua
        );
        return usuarioAtualizado;
    }

    // Rejeitar verificação de advogado (CORRIGIDO: Busca por ID do Advogado)
    @Transactional
    public UsuarioDTO rejeitar(Long advogadoId, Usuario admin, String comentario, String ip, String ua) {
        // CORREÇÃO: Usa findByAdvogadoId em vez de findById
        AdvogadoVerificacao v = repository.findByAdvogadoId(advogadoId);

        if (v == null) {
            v = repository.findById(advogadoId);
        }

        if (v == null) {
            throw new WebApplicationException("Verificação não encontrada para o advogado ID: " + advogadoId, Response.Status.NOT_FOUND);
        }

        v.status = "REJEITADO";
        v.comentariosAdmin = comentario;
        v.dataVerificacao = LocalDateTime.now();
        repository.persist(v);

        auditoriaService.registrarAcaoSimples(
                admin, "REJEICAO_VERIFICACAO", "ADVOGADO_VERIFICACAO", v.id,
                "Verificação rejeitada para o usuário ID: " + v.advogado.id, ip, ua
        );

        return MapperUtils.toDTO(v.advogado);
    }
}