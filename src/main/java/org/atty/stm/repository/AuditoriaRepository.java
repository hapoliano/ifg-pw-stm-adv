package org.atty.stm.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.atty.stm.model.Auditoria;
import java.util.List;

@ApplicationScoped
public class AuditoriaRepository implements PanacheRepository<Auditoria> {

    public List<Auditoria> findRecentLogs(int limit) {
        return find("ORDER BY dataHora DESC").page(0, limit).list();
    }
}