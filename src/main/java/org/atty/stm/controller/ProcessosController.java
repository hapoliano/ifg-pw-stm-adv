package org.atty.stm.controller;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped; // Importante para o bug de sessão
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.ProcessoDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.service.ProcessoService;
import java.util.List;

@Path("/processos")
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"})
@RequestScoped
public class ProcessosController extends ControllerBase {

    @Inject
    @Location("processos.html")
    Template processosTemplate;

    @Inject
    ProcessoService processoService;

    // 1. RENDERIZAÇÃO DA PÁGINA (GET /processos)
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getProcessosPage() {
        Usuario usuario = getUsuarioEntity();
        return processosTemplate.data("usuario", usuario);
    }

    // 2. API PARA LISTAR PROCESSOS (GET /processos/api)
    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllProcessos() {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<ProcessoDTO> lista = processoService.listarTodos(usuario);
        return Response.ok(lista).build();
    }

    // 3. API PARA BUSCAR UM ÚNICO PROCESSO (Faltava este endpoint!)
    // O botão de Editar chama esse método para preencher o formulário
    @GET
    @Path("/api/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProcessoById(@PathParam("id") Long id) {
        Usuario usuario = getUsuarioEntity();
        try {
            ProcessoDTO dto = processoService.buscarPorId(id, usuario);
            return Response.ok(dto).build();
        } catch (Exception e) {
            return handleServiceException(e);
        }
    }

    // 4. API PARA CRIAR PROCESSO (POST /processos/api)
    @POST
    @Path("/api")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"MASTER", "ADVOGADO"})
    public Response criarProcesso(ProcessoDTO dto) {
        Usuario usuario = getUsuarioEntity();
        try {
            ProcessoDTO novo = processoService.criarProcesso(dto, usuario);
            return Response.status(Response.Status.CREATED).entity(novo).build();
        } catch (Exception e) {
            return handleServiceException(e);
        }
    }

    // 5. API PARA EDITAR PROCESSO (PUT /processos/api/{id})
    @PUT
    @Path("/api/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"MASTER", "ADVOGADO"})
    public Response atualizarProcesso(@PathParam("id") Long id, ProcessoDTO dto) {
        Usuario usuario = getUsuarioEntity();
        try {
            ProcessoDTO atualizado = processoService.atualizarProcesso(id, dto, usuario);
            return Response.ok(atualizado).build();
        } catch (Exception e) {
            return handleServiceException(e);
        }
    }
}