package org.atty.stm.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.atty.stm.model.Notification;

import java.util.List;

@ApplicationScoped
public class NotificationRepository implements PanacheRepository<Notification> {

    public List<Notification> findByUsuarioId(Long usuarioId) {
        return list("usuario.id = ?1 ORDER BY dataCriacao DESC", usuarioId);
    }
}
