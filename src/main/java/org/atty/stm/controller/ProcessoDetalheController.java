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

@Path("/processodetalhe")
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"})
public class ProcessoDetalheController extends ControllerBase {

    @Inject
    @Location("processodetalhe.html")
    Template processodetalheTemplate;

    @Inject
    ProcessoService processoService;

    // 1. RENDERIZAÇÃO DA PÁGINA DE DETALHE (GET /processodetalhe/{id})
    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getProcessoDetalhe(@PathParam("id") Long id) {
        Usuario usuario = getUsuarioEntity();

        try {
            ProcessoDTO processo = processoService.buscarPorId(id, usuario);
            return processodetalheTemplate
                    .data("processo", processo)
                    .data("usuario", usuario);

        } catch (Exception e) {
            // Se não encontrar ou acesso negado, renderiza uma página de erro (ou redireciona)
            // Aqui, apenas logamos e retornamos um template básico de erro para simplificar.
            e.printStackTrace();
            throw new WebApplicationException("Erro ao carregar detalhe do processo: " + e.getMessage(),
                    Response.Status.NOT_FOUND);
        }
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