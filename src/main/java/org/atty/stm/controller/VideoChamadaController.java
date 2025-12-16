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

@Path("/video-chamada") // Corrigido para /video-chamada, como está na sidebar
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"})
public class VideoChamadaController extends ControllerBase {

    @Inject
    @Location("videoChamada.html")
    Template videoChamada;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        // Garante que o usuário tem o ID e nome para a lógica do JS
        return videoChamada.data("usuario", getUsuarioEntity());
    }
}