package org.atty.stm.controller;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.panache.common.Sort; // Importante para ordenação
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.ProcessoDTO;
import org.atty.stm.model.dto.StatusUpdateDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.model.Cliente; // Import do Model
import org.atty.stm.repository.ClienteRepository; // Import do Repo
import org.atty.stm.repository.UsuarioRepository; // Import do Repo
import org.atty.stm.service.ProcessoService;

import java.util.List;

@Path("/processodetalhe")
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"})
@RequestScoped
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

            List<Cliente> clientes = List.of();
            List<Usuario> advogados = List.of();

            // Lógica de negócio delegada ao Service
            if ("MASTER".equals(usuario.perfil) || "ADVOGADO".equals(usuario.perfil)) {
                clientes = processoService.listarClientesOpcoes();
                advogados = processoService.listarAdvogadosOpcoes();
            }

            return processodetalheTemplate
                    .data("processo", processo)
                    .data("usuario", usuario)
                    .data("listaClientes", clientes)
                    .data("listaAdvogados", advogados);

        } catch (Exception e) {
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
        Usuario usuario = getUsuarioEntity();
        try {
            processoService.atualizarStatus(id, statusDto, usuario);
            return Response.noContent().build();
        } catch (Exception e) {
            return handleServiceException(e);
        }
    }

    // 3. API PARA ATUALIZAR DADOS COMPLETOS (PUT /processodetalhe/{id}) - NOVO MÉTODO
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"MASTER", "ADVOGADO"})
    public Response atualizarDados(@PathParam("id") Long id, ProcessoDTO processoDto) {
        Usuario usuario = getUsuarioEntity();
        try {
            // Reutiliza o método atualizarProcesso do Service que já valida e mapeia tudo
            processoService.atualizarProcesso(id, processoDto, usuario);
            return Response.ok().build(); // Retorna 200 OK
        } catch (Exception e) {
            return handleServiceException(e);
        }
    }

    // 4. API PARA EXCLUIR PROCESSO (DELETE /processodetalhe/{id})
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