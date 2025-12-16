package org.atty.stm.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.atty.stm.model.Usuario;

import java.util.List;

@ApplicationScoped
public class UsuarioRepository implements PanacheRepository<Usuario> {

    // -------------------------------------------------------------
    // MÉTODOS PRINCIPAIS (SEUS ORIGINAIS)
    // -------------------------------------------------------------

    // Buscar qualquer usuário pelo email (case insensitive)
    public Usuario buscarPorEmail(String email) {
        if (email == null) return null;
        return find("LOWER(email) = ?1", email.toLowerCase().trim()).firstResult();
    }

    // Buscar usuário ativo pelo email
    public Usuario buscarPorEmailAtivo(String email) {
        if (email == null) return null;
        return find("LOWER(email) = ?1 AND ativo = true", email.toLowerCase().trim()).firstResult();
    }

    // Verificar se email já existe
    public boolean existeEmail(String email) {
        if (email == null) return false;
        return count("LOWER(email) = ?1", email.toLowerCase().trim()) > 0;
    }

    // Contagem por perfil (ativo)
    public long countByPerfil(String perfil) {
        return count("perfil = ?1 AND ativo = true", perfil);
    }

    // Advogados pendentes
    public List<Usuario> findAdvogadosPendentes() {
        return list("perfil = 'ADVOGADO' AND aprovado = false AND ativo = true");
    }

    public long countAdvogadosPendentes() {
        return count("perfil = 'ADVOGADO' AND aprovado = false AND ativo = true");
    }

    // Buscar por ID apenas se ativo
    public Usuario findAtivoById(Long id) {
        return find("id = ?1 AND ativo = true", id).firstResult();
    }

    // Alias moderno de buscarPorEmail()
    public Usuario findByEmail(String email) {
        return buscarPorEmail(email);
    }

    // Alias moderno de buscarPorEmailAtivo()
    public Usuario findActiveByEmail(String email) {
        return buscarPorEmailAtivo(email);
    }

    // -------------------------------------------------------------
    // NOVO MÉTODO (NECESSÁRIO PARA O DASHBOARD MASTER)
    // -------------------------------------------------------------
    /**
     * Conta o total de usuários cadastrados no sistema (ativos e inativos).
     */
    public long countAll() {
        return count();
    }

}