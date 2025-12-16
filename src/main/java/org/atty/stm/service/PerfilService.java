package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.atty.stm.model.Usuario;
import org.atty.stm.repository.UsuarioRepository;
import java.time.LocalDateTime;

@ApplicationScoped
public class PerfilService {

    @Inject
    UsuarioRepository usuarioRepository;

    // Novo método para buscar o usuário completo usando o email do contexto de segurança
    public Usuario buscarUsuarioCompletoPorEmail(String email){
        return usuarioRepository.buscarPorEmail(email);
    }

    @Transactional
    public boolean atualizarPerfil(Long id, Usuario usuarioAtualizado){
        Usuario u = usuarioRepository.findById(id); // Busca pelo ID do contexto
        if(u == null) return false;

        // Atualiza campos de contato e pessoais
        u.nome = usuarioAtualizado.nome;
        u.email = usuarioAtualizado.email;
        u.telefone = usuarioAtualizado.telefone;
        u.endereco = usuarioAtualizado.endereco;
        u.cidade = usuarioAtualizado.cidade;

        // Atualiza campos profissionais APENAS se for advogado
        if (u.isAdvogado()) {
            u.oab = usuarioAtualizado.oab;
            u.especialidade = usuarioAtualizado.especialidade;
            u.formacao = usuarioAtualizado.formacao;
            u.experiencia = usuarioAtualizado.experiencia;
        }

        u.dataAtualizacao = LocalDateTime.now();
        // Não precisa de persist, pois é @Transactional e a entidade está attached.
        return true;
    }

    @Transactional
    public boolean alterarSenha(Long id, String novaSenha){
        Usuario u = usuarioRepository.findById(id);
        if(u == null) return false;

        // ⚠️ ATENÇÃO: AQUI DEVE SER INSERIDA A LÓGICA DE HASH DE SENHA
        u.senha = novaSenha; // SUBSTITUIR ESTA LINHA PELO HASH SEGURO

        u.dataAtualizacao = LocalDateTime.now();
        return true;
    }
}