package org.atty.stm.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.dto.UsuarioDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.service.UsuarioService;

import java.util.List;
import java.util.Map;

@Path("/usuarios")
@RolesAllowed({"MASTER", "ADVOGADO"})
public class UsuarioController {

    @Inject
    UsuarioService usuarioService;

    // -----------------------------
    // API: LISTAR TODOS USUÁRIOS
    // -----------------------------
    @GET
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarUsuarios() {
        List<UsuarioDTO> lista = usuarioService.listarTodos();
        return Response.ok(lista).build();
    }

    // -----------------------------
    // API: BUSCAR USUÁRIO POR ID
    // -----------------------------
    @GET
    @Path("/api/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response buscarPorId(@PathParam("id") Long id) {
        UsuarioDTO usuario = usuarioService.listarTodos()
                .stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (usuario == null) return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(usuario).build();
    }

    // -----------------------------
    // API: TOGGLE ATIVO (MASTER/ADMIN)
    // -----------------------------
    @PUT
    @Path("/api/{id}/ativo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleAtivo(@PathParam("id") Long id) {
        Usuario admin = getUsuarioLogado(); // implemente a forma de pegar o usuário logado
        UsuarioDTO usuarioAtualizado = usuarioService.toggleAtivo(id, admin, "0.0.0.0", "user-agent-placeholder");
        return Response.ok(usuarioAtualizado).build();
    }

    // -----------------------------
    // API: APROVAR ADVOGADO (MASTER)
    // -----------------------------
    @PUT
    @Path("/api/{id}/aprovar")
    @Produces(MediaType.APPLICATION_JSON)
    public Response aprovarAdvogado(@PathParam("id") Long id) {
        Usuario master = getUsuarioLogado();
        UsuarioDTO usuario = usuarioService.aprovarUsuario(id, master, "0.0.0.0", "user-agent-placeholder");
        return Response.ok(usuario).build();
    }

    // -----------------------------
    // API: ESTATÍSTICAS DE USUÁRIOS
    // -----------------------------
    @GET
    @Path("/api/estatisticas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEstatisticas() {
        Map<String, Long> stats = usuarioService.getEstatisticasDashboard();
        return Response.ok(stats).build();
    }

    // -----------------------------
    // MÉTODO AUXILIAR
    // -----------------------------
    private Usuario getUsuarioLogado() {
        // TODO: implementar a forma de pegar o usuário logado a partir do contexto de segurança
        return new Usuario(); // placeholder
    }
}
