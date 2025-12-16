package org.atty.stm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.atty.stm.model.Evento;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class EventoRepository implements PanacheRepository<Evento> {

    // Contagem de eventos de hoje (para advogado ou cliente)
    public int countEventosHoje(Long usuarioId) {
        LocalDate hoje = LocalDate.now();
        if (usuarioId == null) {
            return (int) count("dataInicio >= ?1 and dataInicio < ?2", hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
        }
        return (int) count("usuario.id = ?1 and dataInicio >= ?2 and dataInicio < ?3",
                usuarioId, hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay());
    }

    // Lista próximos eventos (advogado/cliente)
    public List<Evento> listProximosByUsuario(Long usuarioId, int limit) {
        return list("usuario.id = ?1 and dataInicio >= ?2 order by dataInicio asc",
                usuarioId, LocalDate.now().atStartOfDay())
                .stream().limit(limit).toList();
    }

    // Lista próximos eventos de todos (para master)
    public List<Evento> listProximos(int limit) {
        return list("dataInicio >= ?1 order by dataInicio asc", LocalDate.now().atStartOfDay())
                .stream().limit(limit).toList();
    }

    //Busca todos os eventos criados por um usuário específico.
    public List<Evento> findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return List.of();
        // Lista todos os eventos associados ao ID do usuário, ordenados do mais recente para o mais antigo.
        return list("usuario.id = ?1 order by dataInicio desc", usuarioId);
    }
}