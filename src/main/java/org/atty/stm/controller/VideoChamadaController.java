package org.atty.stm.controller;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/video-chamada") // Corrigido para /video-chamada, como está na sidebar
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"})
public class VideoChamadaController extends ControllerBase {

    @Inject
    @Location("videoChamada.html")
    Template videoChamada;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response get() {
        TemplateInstance instance = videoChamada.data("usuario", getUsuarioEntity());

        return Response.ok(instance)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build();
    }
}