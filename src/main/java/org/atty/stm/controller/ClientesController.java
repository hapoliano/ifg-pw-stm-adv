package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.ClienteDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.service.ClienteService;
import java.util.List;
import java.util.Collections;

@Path("/clientes")
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"})
@RequestScoped
public class ClientesController extends org.atty.stm.controller.ControllerBase {

    @Inject
    @Location("clientes.html")
    Template clientes;

    @Inject
    ClienteService clienteService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getClientesPage() {
        Usuario usuario = getUsuarioEntity();
        TemplateInstance template = clientes.data("usuario", usuario);
        return Response.ok(template).build();
    }

    // Endpoint API para listar clientes simplificado (Para Selects)
    @GET
    @Path("/api/simples")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClientesSimples() {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            // Reutiliza a lógica de listagem existente
            // Se precisar de algo mais leve no futuro, crie um método específico no Service
            List<ClienteDTO> lista = clienteService.listarClientes(usuario);
            return Response.ok(lista).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("error", "Erro ao listar clientes."))
                    .build();
        }
    }

    // Endpoint API para listar clientes
    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllClientes() {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try {
            List<ClienteDTO> lista = clienteService.listarClientes(usuario);
            return Response.ok(lista).build();
        } catch (Exception e) {
            e.printStackTrace(); // Ajuda a ver o erro no terminal
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("error", "Erro ao listar clientes: " + e.getMessage()))
                    .build();
        }
    }

    // Endpoint API para buscar cliente por ID
    @GET
    @Path("/api/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClienteById(@PathParam("id") Long id) {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        ClienteDTO cliente = clienteService.buscarClientePorId(id, usuario);
        if (cliente == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(cliente).build();
    }

    // Endpoint API para criar cliente
    @POST
    @Path("/api")
    @RolesAllowed({"MASTER", "ADVOGADO"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response criarCliente(ClienteDTO dto, @HeaderParam("X-Forwarded-For") String ip, @HeaderParam("User-Agent") String ua) {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            ClienteDTO novoCliente = clienteService.criarCliente(dto, usuario, ip, ua);
            return Response.status(Response.Status.CREATED).entity(novoCliente).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("error", e.getMessage())).build();
        }
    }

    // Endpoint API para atualizar cliente
    @PUT
    @Path("/api/{id}")
    @RolesAllowed({"MASTER", "ADVOGADO"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response atualizarCliente(@PathParam("id") Long id, ClienteDTO dto) {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            ClienteDTO clienteAtualizado = clienteService.atualizarCliente(id, dto, usuario);
            return Response.ok(clienteAtualizado).build();
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("error", e.getMessage())).build();
        }
    }

    // Endpoint API para deletar cliente
    @DELETE
    @Path("/api/{id}")
    @RolesAllowed({"MASTER", "ADVOGADO"})
    public Response deletarCliente(@PathParam("id") Long id, @HeaderParam("X-Forwarded-For") String ip, @HeaderParam("User-Agent") String ua) {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        try {
            if (clienteService.deletarCliente(id, usuario, ip, ua)) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (RuntimeException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Collections.singletonMap("error", e.getMessage())).build();
        }
    }
}