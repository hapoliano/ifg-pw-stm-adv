package org.atty.stm.controller;

import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.atty.stm.exception.ForbiddenException;
import org.atty.stm.exception.NotFoundException;
import org.atty.stm.model.Usuario;
import org.atty.stm.repository.UsuarioRepository;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Set;

/**
 * Classe base para todos os Controllers protegidos que injeta o usuário autenticado
 * e trata exceções comuns da camada Service.
 */
public abstract class ControllerBase {

    @Inject
    JsonWebToken jwt;

    @Inject
    UsuarioRepository usuarioRepository;

    @Context
    protected HttpHeaders headers;

    @Context
    protected UriInfo uriInfo;

    @Context
    protected SecurityContext securityContext;

    protected String userEmail;
    protected String userName;
    protected Set<String> userRoles;
    protected String userPerfil;
    protected Long userId;

    private Usuario usuarioEntity;

    private static final Logger LOG = Logger.getLogger(ControllerBase.class);

    @PostConstruct
    void init() {
        // A checagem if (jwt != null && jwt.getSubject() != null) está correta para inicialização
        if (jwt != null && jwt.getSubject() != null) {
            this.userEmail = jwt.getSubject();
            this.userName = jwt.getName() != null ? jwt.getName() : userEmail;
            this.userRoles = jwt.getGroups();

            // Lógica para definir o Perfil: assume o primeiro Role como Perfil
            this.userPerfil = userRoles != null && !userRoles.isEmpty() ?
                    userRoles.iterator().next().toUpperCase() : "CLIENTE";

            String userIdString = jwt.getClaim("upn");
            try {
                if (userIdString != null) {
                    this.userId = Long.parseLong(userIdString);
                }
            } catch (NumberFormatException e) {
                // CORREÇÃO DE LOG: Se a claim 'upn' não for um número, logamos
                LOG.errorf("Claim 'upn' do JWT não é um número: %s. Usando e-mail: %s", userIdString, this.userEmail);
                // NOTA: Se o ID não puder ser obtido, a busca por entidade pode falhar.
                // Assumimos que o ID é a principal forma de busca.
            }

            LOG.debugf("User authenticated: %s (%s) id=%s", this.userEmail, this.userPerfil, this.userId);
        }
    }

    /** Obtém a entidade completa do usuário logado do banco de dados (busca por ID otimizada) */
    protected Usuario getUsuarioEntity() {
        if (this.usuarioEntity != null) {
            return this.usuarioEntity; // Retorna em cache
        }

        if (this.userId == null) {
            LOG.warn("Não foi possível obter o ID do usuário. Retornando null.");
            return null;
        }

        // Busca a entidade pelo ID
        // NOTA: 'usuarioRepository' deve ser um PanacheRepository ou similar com findById(Long)
        this.usuarioEntity = usuarioRepository.findById(this.userId);

        if (this.usuarioEntity == null) {
            LOG.errorf("Entidade Usuario com ID %d não encontrada no banco de dados.", this.userId);
        }

        return this.usuarioEntity;
    }

    /**
     * Trata exceções lançadas pela camada Service, mapeando-as para respostas HTTP adequadas.
     * @param e A exceção lançada.
     * @return Um objeto Response com o status e corpo de erro corretos.
     */
    protected Response handleServiceException(Exception e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Ocorreu um erro interno.";

        if (e instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", errorMessage))
                    .build();
        } else if (e instanceof ForbiddenException) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", errorMessage))
                    .build();
        } else if (e instanceof IllegalArgumentException) {
            // Exceções de validação de dados (ex: status inválido no enum)
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", errorMessage))
                    .build();
        } else {
            // Qualquer outra exceção (Internal Server Error)
            LOG.error("Erro interno no Controller:", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Erro interno no servidor. Consulte o log para detalhes."))
                    .build();
        }
    }

    // Métodos auxiliares
    protected String getIpAddress() {
        String xff = headers.getHeaderString("X-Forwarded-For");
        return xff != null ? xff.split(",")[0].trim() : "127.0.0.1";
    }

    protected String getUserAgent() {
        return headers.getHeaderString("User-Agent");
    }

    protected boolean isMaster() { return "MASTER".equals(this.userPerfil); }
    protected boolean isAdvogado() { return "ADVOGADO".equals(this.userPerfil); }
    protected boolean isCliente() { return "CLIENTE".equals(this.userPerfil); }
}