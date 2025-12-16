package org.atty.stm.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.atty.stm.model.Cliente;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;

@ApplicationScoped
public class ClienteRepository implements PanacheRepository<Cliente> {

    // --- MÉTODOS EXISTENTES ---
    public long countAllAtivos() {
        return count("status = 'ATIVO'");
    }

    public long countByAdvogado(Long advogadoId) {
        if (advogadoId == null) return 0;
        return count("advogadoResponsavel.id = ?1", advogadoId);
    }

    public Cliente findByUsuarioId(Long usuarioId) {
        if (usuarioId == null) return null;
        return find("usuario.id = ?1", usuarioId).firstResult();
    }

    public Cliente findByEmail(String email) {
        if (email == null) return null;
        return find("LOWER(email) = ?1", email.toLowerCase().trim()).firstResult();
    }

    // --- NOVOS MÉTODOS COM JOIN FETCH (SOLUÇÃO DO BUG) ---

    // Traz TODOS os clientes e já carrega os dados do Advogado junto
    public List<Cliente> listarTodosComAdvogado() {
        return list("SELECT c FROM Cliente c LEFT JOIN FETCH c.advogadoResponsavel ORDER BY c.nome");
    }

    // Traz clientes de um advogado específico, também carregando os dados
    public List<Cliente> listarPorAdvogadoComFetch(Long advogadoId) {
        return list("SELECT c FROM Cliente c LEFT JOIN FETCH c.advogadoResponsavel WHERE c.advogadoResponsavel.id = ?1 ORDER BY c.nome", advogadoId);
    }

    // Busca um único cliente trazendo o advogado junto para evitar erro no toDTO
    public Cliente findByIdComFetch(Long id) {
        return find("SELECT c FROM Cliente c LEFT JOIN FETCH c.advogadoResponsavel WHERE c.id = ?1", id).firstResult();
    }
}