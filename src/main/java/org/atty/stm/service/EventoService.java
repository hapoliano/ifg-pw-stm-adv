package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.atty.stm.dto.EventoDTO;
import org.atty.stm.model.Evento;
import org.atty.stm.model.Usuario;
import org.atty.stm.repository.EventoRepository;
import org.atty.stm.repository.ProcessoRepository;
import org.atty.stm.util.MapperUtils;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class EventoService {

    @Inject EventoRepository eventoRepository;
    @Inject ProcessoRepository processoRepository;
    @Inject AuditoriaService auditoriaService;

    // ===========================================
    // LISTAGEM (retirado do código antigo)
    // ===========================================
    public List<EventoDTO> listarEventosUsuario(Long usuarioId) {
        return MapperUtils.toDTOList(
                eventoRepository.findByUsuarioId(usuarioId),
                EventoDTO.class
        );
    }

    // ===========================================
    // CRIAR EVENTO (versão nova — já ok)
    // ===========================================
    @Transactional
    public EventoDTO criarEvento(EventoDTO dto, Usuario usuarioCriador, String ip, String ua) {

        Evento e = new Evento();

        e.titulo      = dto.getTitle();
        e.descricao   = dto.getDescription();

        e.dataInicio  = dto.getStart() != null
                ? LocalDateTime.parse(dto.getStart())
                : LocalDateTime.now();

        e.dataFim     = (dto.getEnd() != null && !dto.getEnd().isEmpty())
                ? LocalDateTime.parse(dto.getEnd())
                : null;

        e.tipo        = dto.getTipo();
        e.cor         = dto.getColor();
        e.diaInteiro  = dto.isAllDay(); // correto

        e.usuario     = usuarioCriador;

        if (dto.getProcessoId() != null) {
            e.processo = processoRepository.findById(dto.getProcessoId());
        }

        e.dataCriacao = LocalDateTime.now();

        eventoRepository.persist(e);

        auditoriaService.registrarAcaoSimples(
                usuarioCriador, "CRIACAO", "EVENTO", e.id,
                "Evento criado: " + e.titulo, ip, ua
        );

        return MapperUtils.toDTO(e);
    }

    // ===========================================
    // ATUALIZAR EVENTO (novo — mesclado)
    // ===========================================
    @Transactional
    public EventoDTO atualizarEvento(EventoDTO dto, Usuario usuarioLogado, String ip, String ua) {

        if (dto.getId() == null) return null;

        Evento e = eventoRepository.findById(dto.getId());
        if (e == null) return null;

        // Permissão — o criador é o único que pode editar
        if (!e.usuario.id.equals(usuarioLogado.id)) {
            return null; // ou lance exceção de permissão
        }

        e.titulo      = dto.getTitle();
        e.descricao   = dto.getDescription();

        e.dataInicio  = dto.getStart() != null
                ? LocalDateTime.parse(dto.getStart())
                : e.dataInicio;

        e.dataFim     = (dto.getEnd() != null && !dto.getEnd().isEmpty())
                ? LocalDateTime.parse(dto.getEnd())
                : null;

        e.tipo        = dto.getTipo();
        e.cor         = dto.getColor();
        e.diaInteiro  = dto.isAllDay();

        // Panache: persist() aceita entidades gerenciadas, mas merge() não é necessário por padrão
        eventoRepository.persist(e);

        auditoriaService.registrarAcaoSimples(
                usuarioLogado, "ATUALIZACAO", "EVENTO", e.id,
                "Evento atualizado: " + e.titulo, ip, ua
        );

        return MapperUtils.toDTO(e);
    }

    // ===========================================
    // DELETAR EVENTO (novo — mesclado)
    // ===========================================
    @Transactional
    public boolean deletarEvento(Long id, Usuario usuario, String ip, String ua) {

        Evento e = eventoRepository.findById(id);
        if (e == null) return false;

        // Permissão — evitar deletar evento de outro usuário
        if (!e.usuario.id.equals(usuario.id)) {
            return false;
        }

        boolean ok = eventoRepository.deleteById(id);

        if (ok) {
            auditoriaService.registrarAcaoSimples(
                    usuario, "DELECAO", "EVENTO", id,
                    "Evento deletado", ip, ua
            );
        }

        return ok;
    }
}
