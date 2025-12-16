package org.atty.stm.controller;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.RequestScoped; // IMPORTANTE: Necessário para evitar vazamento de dados entre usuários
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.atty.stm.service.AuthService;

import java.util.Map;

@Path("/login")
@RequestScoped // <--- A CORREÇÃO PRINCIPAL ESTÁ AQUI
public class LoginController extends ControllerBase {

    @Inject
    @Location("login.html")
    Template loginTemplate;

    @Inject
    AuthService authService;

    // Nota: UriInfo e SecurityContext já existem na ControllerBase,
    // mas mantivemos aqui caso você prefira usar os locais.
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
            e.printStackTrace(); // Bom para debugar no console
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("success", false, "mensagem", "Erro interno no servidor."))
                    .build();
        }
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getLogin() {
        // Se o usuário já estiver logado (cookie válido), redireciona para a dashboard correta
        if (securityContext.getUserPrincipal() != null) {
            return redirecionarPorPerfil();
        }

        return Response.ok(loginTemplate.data("contextPath", uriInfo.getBaseUri())).build();
    }

    /**
     * Método auxiliar para enviar cada perfil para sua tela correta.
     * Isso evita que um Master caia na tela de Cliente ou vice-versa.
     */
    private Response redirecionarPorPerfil() {
        String destino = "/dashboard"; // Default (Advogado)

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
        // Garante a destruição do cookie antigo
        NewCookie expiredCookie = new NewCookie.Builder("token")
                .value("")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(false) // Mude para true em produção com HTTPS
                .build();

        return Response.status(Response.Status.SEE_OTHER)
                .location(uriInfo.getBaseUriBuilder().path("/login").build())
                .cookie(expiredCookie)
                .build();
    }
}