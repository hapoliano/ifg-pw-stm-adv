package org.atty.stm.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.atty.stm.model.AdvogadoVerificacao;
import java.util.List;

@ApplicationScoped
// Usa PanacheRepositoryBase pois a entidade tem ID definido manualmente, não PanacheEntity
public class AdvogadoVerificacaoRepository implements PanacheRepositoryBase<AdvogadoVerificacao, Long> {

    public List<AdvogadoVerificacao> findPendentes() {
        return list("status = 'PENDENTE'");
    }

    public AdvogadoVerificacao findByAdvogadoId(Long advogadoId) {
        return find("advogado.id = ?1", advogadoId).firstResult();
    }
}