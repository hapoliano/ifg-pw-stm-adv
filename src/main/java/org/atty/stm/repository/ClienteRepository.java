    package org.atty.stm.repository;

    import jakarta.enterprise.context.ApplicationScoped;
    import org.atty.stm.model.Cliente;
    import io.quarkus.hibernate.orm.panache.PanacheRepository;

    import java.util.List;

    @ApplicationScoped
    public class ClienteRepository implements PanacheRepository<Cliente> {

        // Total de clientes ativos
        public long countAllAtivos() {
            return count("status = 'ATIVO'");
        }

        // Total de clientes de um advogado específico
        public long countByAdvogado(Long advogadoId) {
            if (advogadoId == null) return 0;
            return count("advogadoResponsavel.id = ?1", advogadoId);
        }

        // Lista todos os clientes vinculados a um advogado específico.
        public List<Cliente> findByAdvogadoId(Long advogadoId) {
            if (advogadoId == null) return List.of();
            return list("advogadoResponsavel.id = ?1", advogadoId);
        }

        //NOVO: Busca um cliente pelo ID do seu usuário vinculado.

        public Cliente findByUsuarioId(Long usuarioId) {
            if (usuarioId == null) return null;
            // Assume que existe um campo 'usuario' no modelo Cliente que referencia a tabela Usuario
            return find("usuario.id = ?1", usuarioId).firstResult();
        }

        // Busca um cliente pelo email (case insensitive)
        public Cliente findByEmail(String email) {
            if (email == null) return null;
            return find("LOWER(email) = ?1", email.toLowerCase().trim()).firstResult();
        }
    }