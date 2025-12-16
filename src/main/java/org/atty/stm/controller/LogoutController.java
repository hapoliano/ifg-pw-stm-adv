package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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
    public Response logout(@CookieParam("session_token") String token) {
        try {
            // A lógica de logout está no LoginController
            return Response.ok()
                    .entity("{\"success\": true, \"message\": \"Logout realizado com sucesso\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"success\": false, \"error\": \"Erro no logout\"}")
                    .build();
        }
    }
}