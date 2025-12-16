package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.ClienteDTO;
import org.atty.stm.service.ClienteService;
import java.util.List;

@Path("/clientes")
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"}) // Cliente também pode ver SEUS dados de cliente
public class ClientesController extends org.atty.stm.controller.ControllerBase { // <-- Estende ControllerBase

    @Inject
    @Location("clientes.html")
    Template clientes;
    @Inject
    ClienteService clienteService; // Deve ser criado

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getClientesPage() {
        // Renderiza o template com os dados do usuário
        TemplateInstance template = clientes.data("usuario", getUsuarioEntity());

        // Retorna com cabeçalhos que impedem o cache
        return Response.ok(template)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build();
    }

    // Endpoint API para listar clientes
    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllClientes() {
        // Lógica de Negócio: O Service deve filtrar clientes por advogado logado se não for MASTER
        List<ClienteDTO> lista = clienteService.listarClientes(getUsuarioEntity()); // CORRIGIDO: Agora chama o service real
        return Response.ok(lista).build();
    }

    // Endpoint API para buscar cliente por ID
    @GET
    @Path("/api/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClienteById(@PathParam("id") Long id) {
        ClienteDTO cliente = clienteService.buscarClientePorId(id, getUsuarioEntity());
        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(cliente).build();
    }

    // Endpoint API para criar cliente
    @POST
    @Path("/api")
    @RolesAllowed({"MASTER", "ADVOGADO"}) // Somente Master/Advogado podem criar
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response criarCliente(ClienteDTO dto, @HeaderParam("X-Forwarded-For") String ip, @HeaderParam("User-Agent") String ua) {
        try {
            ClienteDTO novoCliente = clienteService.criarCliente(dto, getUsuarioEntity(), ip, ua);
            return Response.status(Response.Status.CREATED).entity(novoCliente).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Collections.singletonMap("error", e.getMessage())).build();
        }
    }

    // Endpoint API para atualizar cliente
    @PUT
    @Path("/api/{id}")
    @RolesAllowed({"MASTER", "ADVOGADO"}) // Somente Master/Advogado podem atualizar
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizarCliente(@PathParam("id") Long id, ClienteDTO dto) {
        try {
            ClienteDTO clienteAtualizado = clienteService.atualizarCliente(id, dto, getUsuarioEntity());
            return Response.ok(clienteAtualizado).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Collections.singletonMap("error", e.getMessage())).build();
        }
    }

    // Endpoint API para deletar cliente
    @DELETE
    @Path("/api/{id}")
    @RolesAllowed({"MASTER", "ADVOGADO"}) // Somente Master/Advogado podem deletar
    public Response deletarCliente(@PathParam("id") Long id, @HeaderParam("X-Forwarded-For") String ip, @HeaderParam("User-Agent") String ua) {
        try {
            if (clienteService.deletarCliente(id, getUsuarioEntity(), ip, ua)) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Collections.singletonMap("error", e.getMessage())).build();
        }
    }
}