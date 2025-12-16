package org.atty.stm.controller;

import org.atty.stm.model.dto.AuditoriaDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.service.AuditoriaService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.util.List;

@Path("/auditoria")
@RolesAllowed({"MASTER"})
public class AuditoriaController extends org.atty.stm.controller.ControllerBase {

    @Inject
    @Location("auditoria.html")
    Template auditoria;

    @Inject
    AuditoriaService auditoriaService;

    // RENDERIZAÇÃO DA PÁGINA (GET /auditoria)
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getPage() {
        Usuario usuario = getUsuarioEntity();

        if (usuario == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        // CORREÇÃO: O nome correto do método no Service é listarLogsRecentes
        var logs = auditoriaService.listarLogsRecentes(50);

        TemplateInstance instance = auditoria
                .data("usuario", usuario)
                .data("logs", logs);

        return Response.ok(instance)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build();
    }

    // ENDPOINT JSON para AJAX (GET /auditoria/logs)
    @GET
    @Path("/logs")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLogs() {
        try {
            List<AuditoriaDTO> logs = auditoriaService.listarLogsRecentes(100);
            return Response.ok(logs).build();
        } catch (Exception e) {
            System.err.println("Erro ao buscar logs de auditoria: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}