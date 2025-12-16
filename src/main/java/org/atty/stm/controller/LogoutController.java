package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

@Path("/logout")
public class LogoutController {

    @Inject
    @Location("logout.html")
    Template logout;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance logoutPage() {
        return logout.instance();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout() {
        try {
            // CRÍTICO: Criar um cookie com o mesmo nome "token" e maxAge(0) para forçar a remoção
            NewCookie expiredCookie = new NewCookie.Builder("token")
                    .value("")
                    .path("/")
                    .maxAge(0)
                    .httpOnly(true)
                    .secure(false) // Defina como true se estiver usando HTTPS em produção
                    .build();

            return Response.ok()
                    .entity("{\"success\": true, \"message\": \"Logout realizado com sucesso\"}")
                    .cookie(expiredCookie) // Envia o comando para o navegador deletar o cookie
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"success\": false, \"error\": \"Erro no logout\"}")
                    .build();
        }
    }
}