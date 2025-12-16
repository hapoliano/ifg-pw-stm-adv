package org.atty.stm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.atty.stm.model.Processo;
import org.atty.stm.model.ProcessoStatus; // Importação Crucial
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;

@ApplicationScoped
public class ProcessoRepository implements PanacheRepository<Processo> {

    // --------------------------------------------------
    // BUSCAS POR ID E NÚMERO
    // --------------------------------------------------

    public Processo findById(Long id) {
        return find("id", id).firstResult();
    }

    public Processo findByNumero(String numero) {
        if (numero == null || numero.isEmpty()) return null;
        return find("numeroProcesso = ?1", numero).firstResult();
    }

    // --------------------------------------------------
    // CONTADORES
    // --------------------------------------------------

    public long countAll() {
        return count();
    }

    // Conta processos ativos (status diferente de CONCLUIDO)
    public long countAtivos() {
        return count("status <> ?1", ProcessoStatus.CONCLUIDO);
    }

    // Conta processos pelo ID do usuário cliente
    public long countByCliente(Long usuarioClienteId) {
        if (usuarioClienteId == null) return 0;
        return count("cliente.usuario.id = ?1", usuarioClienteId);
    }

    // Conta processos pelo ID do advogado responsável
    public long countByAdvogado(Long advogadoId) {
        if (advogadoId == null) return 0;
        return count("advogadoResponsavel.id = ?1", advogadoId);
    }

    // Recebe o Enum ProcessoStatus
    public long countByStatus(ProcessoStatus status) {
        if (status == null) return 0;
        return count("status = ?1", status);
    }

    // Recebe o Enum ProcessoStatus
    public long countByStatusAndAdvogado(ProcessoStatus status, Long advogadoId) {
        if (status == null || advogadoId == null) return 0;
        return count("status = ?1 and advogadoResponsavel.id = ?2", status, advogadoId);
    }

    // Recebe o Enum ProcessoStatus
    public long countByStatusAndCliente(ProcessoStatus status, Long usuarioClienteId) {
        if (status == null || usuarioClienteId == null) return 0;
        return count("status = ?1 and cliente.usuario.id = ?2", status, usuarioClienteId);
    }

    // --------------------------------------------------
    // LISTAGENS
    // --------------------------------------------------

    public List<Processo> listAllOrderByData() {
        return list("order by dataCriacao desc");
    }

    // Listagem de processos mais recentes (geral)
    public List<Processo> listRecentes(int limit) {
        return find("order by dataCriacao desc").page(0, limit).list();
    }

    // MÉTODO NOVO REQUERIDO: Listagem de processos recentes por Advogado
    public List<Processo> listByAdvogadoRecentes(Long advogadoId, int limit) {
        if (advogadoId == null) return List.of();
        return find("advogadoResponsavel.id = ?1 order by dataCriacao desc", advogadoId)
                .page(0, limit)
                .list();
    }

    // Listagem de processos recentes por Cliente
    public List<Processo> listByClienteRecentes(Long usuarioClienteId, int limit) {
        if (usuarioClienteId == null) return List.of();
        return find("cliente.usuario.id = ?1 order by dataCriacao desc", usuarioClienteId)
                .page(0, limit)
                .list();
    }

    // Recebe o Enum ProcessoStatus
    public List<Processo> listByStatus(ProcessoStatus status, int limit) {
        if (status == null) return List.of();
        return list("status = ?1 order by dataCriacao desc", status)
                .stream().limit(limit).toList();
    }

    // Recebe o Enum ProcessoStatus
    public List<Processo> listByStatusAndAdvogado(ProcessoStatus status, Long advogadoId, int limit) {
        if (status == null || advogadoId == null) return List.of();
        return list("status = ?1 and advogadoResponsavel.id = ?2 order by dataCriacao desc", status, advogadoId)
                .stream().limit(limit).toList();
    }

    public List<Processo> findByParticipanteId(Long usuarioId) {
        if (usuarioId == null) return List.of();
        return list("advogadoResponsavel.id = ?1 or cliente.usuario.id = ?1", usuarioId);
    }
}