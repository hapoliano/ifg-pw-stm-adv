package org.atty.stm.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.atty.stm.model.Files;

import java.util.List;

@ApplicationScoped
public class FilesRepository implements PanacheRepository<Files> {

    public List<Files> findByProcessoId(Long processoId) {
        return list("processo.id = ?1 ORDER BY dataUpload DESC", processoId);
    }
}
