package org.atty.stm.controller;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.ProcessoDTO;
import org.atty.stm.model.dto.StatusUpdateDTO; // Importação correta
import org.atty.stm.model.Usuario;
import org.atty.stm.service.ProcessoService;

import java.net.URI;

@Path("/processos/{id}")
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"})
public class ProcessoDetalheController extends ControllerBase {

    @Inject
    @Location("processodetalhe.html")
    Template processoDetalhe;

    @Inject
    ProcessoService processoService;

    // 1. RENDERIZAÇÃO DA PÁGINA DE DETALHE (GET /processodetalhe/{id})
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDetalhePage(@PathParam("id") Long id) {
        Usuario usuario = getUsuarioEntity();

        if (usuario == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        // Busca o processo garantindo que o usuário tem permissão para vê-lo
        ProcessoDTO processo = processoService.buscarPorId(id, usuario);

        if (processo == null) {
            // Se não achou ou não tem permissão, volta para a lista
            return Response.seeOther(URI.create("/processos")).build();
        }

        TemplateInstance instance = processoDetalhe
                .data("usuario", usuario)
                .data("processo", processo);

        // Bloqueia o cache
        return Response.ok(instance)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build();
    }

    // 2. API PARA MUDAR STATUS (PUT /processodetalhe/{id}/status)
    @PUT
    @Path("/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"MASTER", "ADVOGADO"})
    public Response atualizarStatus(@PathParam("id") Long id, StatusUpdateDTO statusDto) {
        //                                                     ^-- Variável de instância
        Usuario usuario = getUsuarioEntity();
        try {
            // AQUI, você deve usar a variável de instância: statusDto
            processoService.atualizarStatus(id, statusDto, usuario);
            return Response.noContent().build();
        } catch (Exception e) {
            return handleServiceException(e);
        }
    }

    // 3. API PARA EXCLUIR PROCESSO (DELETE /processodetalhe/{id})
    @DELETE
    @Path("/{id}")
    @RolesAllowed({"MASTER", "ADVOGADO"})
    public Response excluirProcesso(@PathParam("id") Long id) {
        Usuario usuario = getUsuarioEntity();
        try {
            processoService.excluirProcesso(id, usuario);
            return Response.noContent().build();
        } catch (Exception e) {
            return handleServiceException(e);
        }
    }
}