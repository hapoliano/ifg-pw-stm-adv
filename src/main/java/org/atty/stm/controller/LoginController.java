package org.atty.stm.controller;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*; // UriInfo, NewCookie, SecurityContext
import org.atty.stm.service.AuthService;

import java.util.Map;

@Path("/login")
public class LoginController extends ControllerBase {

    @Inject
    @Location("login.html")
    Template loginTemplate;

    @Inject
    AuthService authService;

    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    /**
     * POST /login/logar
     * Recebe JSON { email, senha } e delega para AuthService.authenticate.
     * Retorna header 'token' contendo o JWT (o filtro transforma em cookie).
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
            // usa helpers da ControllerBase para auditoria (IP / UA)
            String ip = getIpAddress();
            String ua = getUserAgent();

            // chama o método que autentica e gera o JWT
            String jwt = authService.authenticate(email, senha, ip, ua);

            // Retorna token no header 'token' — seu JWTResponseFilter criará o cookie
            return Response.ok(Map.of("success", true, "mensagem", "Login efetuado com sucesso."))
                    .header("token", jwt)
                    .build();

        } catch (RuntimeException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("success", false, "mensagem", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("success", false, "mensagem", "Erro interno no servidor."))
                    .build();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getLogin() {
        if (securityContext.getUserPrincipal() != null) {
            return Response.status(Response.Status.SEE_OTHER)
                    .location(uriInfo.getBaseUriBuilder().path("/dashboard").build())
                    .build();
        }

        return Response.ok(loginTemplate.data("contextPath", uriInfo.getBaseUri())).build();
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

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logoutPost() {
        // 1. Cria um cookie com o MESMO nome usado no login ("token"), mas com maxAge(0)
        NewCookie expiredCookie = new NewCookie.Builder("token")
                .value("")             // Valor vazio
                .path("/")             // O mesmo path do cookie original
                .maxAge(0)             // 0 = Expira imediatamente (deleta)
                .httpOnly(true)
                .secure(false)         // Mantenha false para dev local (http), true para prod (https)
                .build();

        // 2. Retorna sucesso e anexa o cookie expirado na resposta
        return Response.ok(Map.of("success", true, "mensagem", "Logout realizado."))
                .cookie(expiredCookie) // Isso é o que faz o navegador apagar o login
                .build();
    }
}
