package org.atty.stm.controller;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.atty.stm.service.AuthService;
import org.atty.stm.repository.UsuarioRepository; // <--- NOVO IMPORT
import org.atty.stm.model.Usuario;             // <--- NOVO IMPORT

import java.util.Map;

@Path("/login")
@RequestScoped
public class LoginController extends ControllerBase {

    @Inject
    @Location("login.html")
    Template loginTemplate;

    @Inject
    AuthService authService;

    @Inject
    UsuarioRepository usuarioRepository; // <--- INJEÇÃO NECESSÁRIA PARA VER O PERFIL

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    /**
     * POST /login/logar
     * Recebe JSON { email, senha } e delega para AuthService.authenticate.
     * Retorna header 'token' contendo o JWT e no corpo a URL de redirecionamento.
     */
    @POST
    @Path("/logar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response logar(Map<String, String> body) {

        String email = body.get("email");
        String senha = body.get("senha");

        if (email == null || senha == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("success", false, "mensagem", "Email e senha são obrigatórios."))
                    .build();
        }

        try {
            String ip = getIpAddress();
            String ua = getUserAgent();

            // 1. Autentica
            String jwt = authService.authenticate(email, senha, ip, ua);

            // 2. Busca o usuário para decidir o redirecionamento
            Usuario usuario = usuarioRepository.buscarPorEmail(email);

            // 3. Define a rota baseada no perfil
            String redirectUrl = "/dashboard"; // Padrão (Advogado)

            if (usuario != null) {
                if ("MASTER".equals(usuario.perfil)) {
                    redirectUrl = "/dashboard/master";
                } else if ("CLIENTE".equals(usuario.perfil)) {
                    redirectUrl = "/dashboard/cliente";
                }
            }

            // 4. Retorna com a URL correta
            return Response.ok(Map.of(
                            "success", true,
                            "mensagem", "Login efetuado com sucesso.",
                            "redirectUrl", redirectUrl // <--- O Frontend vai usar isso
                    ))
                    .header("token", jwt)
                    .build();

        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("success", false, "mensagem", e.getMessage()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("success", false, "mensagem", "Erro interno no servidor."))
                    .build();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getLogin() {
        if (securityContext.getUserPrincipal() != null) {
            return redirecionarPorPerfil();
        }
        return Response.ok(loginTemplate.data("contextPath", uriInfo.getBaseUri())).build();
    }

    private Response redirecionarPorPerfil() {
        String destino = "/dashboard";

        if (securityContext.isUserInRole("MASTER")) {
            destino = "/dashboard/master";
        } else if (securityContext.isUserInRole("CLIENTE")) {
            destino = "/dashboard/cliente";
        }

        return Response.status(Response.Status.SEE_OTHER)
                .location(uriInfo.getBaseUriBuilder().path(destino).build())
                .build();
    }

    @GET
    @Path("/logout")
    public Response logout() {
        NewCookie expiredCookie = new NewCookie.Builder("token")
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false)
                .build();

        return Response.status(Response.Status.SEE_OTHER)
                .location(uriInfo.getBaseUriBuilder().path("/login").build())
                .cookie(expiredCookie)
                .build();
    }
}