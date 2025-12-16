package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.atty.stm.model.Usuario;
import org.atty.stm.repository.UsuarioRepository;
import org.atty.stm.util.PasswordUtils;
import org.atty.stm.util.TokenUtils;

import java.time.LocalDateTime;
import java.util.Set;

@ApplicationScoped
public class AuthService {

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    AuditoriaService auditoriaService;

    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @Transactional
    public String authenticate(String email, String senha, String ip, String ua) {

        Usuario usuario = usuarioRepository.buscarPorEmailAtivo(email);

        if (usuario == null) {
            auditoriaService.registrarAcaoSimples(
                    null, "LOGIN_FALHA", "USUARIO", null,
                    "Tentativa de login com email inexistente: " + email,
                    ip, ua
            );
            throw new RuntimeException("Credenciais inválidas.");
        }

        if (!usuario.aprovado) {
            auditoriaService.registrarAcaoSimples(
                    usuario, "LOGIN_FALHA", "USUARIO", usuario.id,
                    "Tentativa de login: usuário não aprovado.",
                    ip, ua
            );
            throw new RuntimeException("Usuário não aprovado.");
        }

        String senhaDB = usuario.senha;
        boolean autenticado = false;

        // --- CASO 1: senha já está em formato Bcrypt ---
        if (senhaDB.startsWith("$2")) {
            autenticado = PasswordUtils.verify(senha, senhaDB);
        }

        // --- CASO 2: senha antiga (texto puro) ---
        else {
            autenticado = senha.equals(senhaDB);
            if (autenticado) {
                // Atualiza automaticamente para Bcrypt na primeira autenticação
                usuario.senha = PasswordUtils.hash(senha);
                usuarioRepository.persist(usuario);
            }
        }

        if (!autenticado) {
            auditoriaService.registrarAcaoSimples(
                    usuario, "LOGIN_FALHA", "USUARIO", usuario.id,
                    "Senha inválida para o email: " + email,
                    ip, ua
            );
            throw new RuntimeException("Credenciais inválidas.");
        }

        // Atualiza último acesso
        usuario.ultimoAcesso = LocalDateTime.now();
        usuarioRepository.persist(usuario);

        auditoriaService.registrarAcaoSimples(
                usuario, "LOGIN_SUCESSO", "USUARIO", usuario.id,
                "Usuário logado com sucesso.",
                ip, ua
        );

        try {
            Set<String> roles = Set.of(usuario.perfil);

            return TokenUtils.generateJwt(
                    usuario.email,
                    usuario.id.toString(),
                    usuario.nome,
                    roles,
                    issuer,
                    3600
            );

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar token JWT: " + e.getMessage(), e);
        }
    }
}
