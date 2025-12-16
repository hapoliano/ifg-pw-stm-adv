package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.atty.stm.dto.UsuarioDTO;
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

    // MÉTODO REINSERIDO: Listar advogados pendentes
    public List<UsuarioDTO> listarPendentes() {
        List<AdvogadoVerificacao> verificacoes = repository.findPendentes();

        return verificacoes.stream()
                // Mapeia do objeto de Verificação para o objeto de Usuário (advogado)
                .map(v -> v.advogado)
                // Converte a entidade Usuário para o DTO
                .map(MapperUtils::toDTO)
                .collect(Collectors.toList());
    }

    // Aprovar verificação de advogado
    @Transactional
    public UsuarioDTO aprovar(Long id, Usuario admin, String ip, String ua) {
        AdvogadoVerificacao v = repository.findById(id);
        if (v == null) throw new WebApplicationException("Verificação não encontrada", Response.Status.NOT_FOUND);

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

    // Rejeitar verificação de advogado
    @Transactional
    public UsuarioDTO rejeitar(Long id, Usuario admin, String comentario, String ip, String ua) {
        AdvogadoVerificacao v = repository.findById(id);
        if (v == null) throw new WebApplicationException("Verificação não encontrada", Response.Status.NOT_FOUND);

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