package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.atty.stm.model.Notification;
import org.atty.stm.model.Usuario;
import org.atty.stm.repository.NotificationRepository;
import org.atty.stm.util.MapperUtils;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class NotificationService {

    @Inject
    NotificationRepository repository;

    @Inject
    AuditoriaService auditoriaService;

    public List<org.atty.stm.model.dto.NotificationDTO> listarPorUsuario(Long usuarioId) {
        return MapperUtils.toDTOList(repository.findByUsuarioId(usuarioId), org.atty.stm.model.dto.NotificationDTO.class);
    }

    @Transactional
    public Notification criar(Notification n, Usuario autor, String ip, String ua) {
        n.dataCriacao = LocalDateTime.now();
        repository.persist(n);
        auditoriaService.registrarAcaoSimples(autor, "CRIACAO", "NOTIFICATION", n.id, "Notificação criada", ip, ua);
        return n;
    }

    @Transactional
    public boolean marcarComoLida(Long id, Usuario usuario, String ip, String ua) {
        Notification n = repository.findById(id);
        if (n == null) return false;
        n.lida = true;
        repository.persist(n);
        auditoriaService.registrarAcaoSimples(usuario, "ATUALIZACAO", "NOTIFICATION", id, "Marcar notificação lida", ip, ua);
        return true;
    }
}
