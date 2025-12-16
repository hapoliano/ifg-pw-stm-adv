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

import java.util.List;
import org.atty.stm.model.dto.EventoDTO;
import org.atty.stm.service.EventoService;
import org.atty.stm.model.Usuario;

@Path("/agenda")
@RequestScoped
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"})
@Produces(MediaType.TEXT_HTML)
public class AgendaController extends org.atty.stm.controller.ControllerBase {

    @Inject
    @Location("agenda.html")
    Template agenda;

    @Inject
    EventoService eventoService;

    // ROTA HTML (Para carregar a página)
    @GET
    public TemplateInstance getAgendaPage() {
        Usuario usuarioLogado = getUsuarioEntity();

        List<EventoDTO> eventos = usuarioLogado != null
                ? eventoService.listarEventosUsuario(usuarioLogado.id)
                : List.of();

        return agenda
                .data("eventos", eventos)
                .data("usuario", usuarioLogado);
    }

    // ENDPOINT PARA SALVAR/CRIAR/ATUALIZAR EVENTO (POST)
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/evento")
    public Response salvarEvento(EventoDTO dto) {
        Usuario usuarioLogado = getUsuarioEntity();
        if (usuarioLogado == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        // 🟢 USANDO MÉTODOS HERDADOS DO ControllerBase
        String ip = getIpAddress();
        String ua = getUserAgent();

        if (dto.getId() == null) {
            // Criação
            EventoDTO novoEvento = eventoService.criarEvento(dto, usuarioLogado, ip, ua);
            return Response.status(Response.Status.CREATED).entity(novoEvento).build();
        } else {
            // Atualização
            EventoDTO eventoAtualizado = eventoService.atualizarEvento(dto, usuarioLogado, ip, ua);
            // Retornamos 403 Forbidden se o evento não for encontrado ou se o usuário não tiver permissão
            if (eventoAtualizado == null) return Response.status(Response.Status.FORBIDDEN).build();

            return Response.ok(eventoAtualizado).build();
        }
    }

    // ENDPOINT PARA DELETAR EVENTO (DELETE)
    @DELETE
    @Path("/evento/{id}")
    public Response deletarEvento(@PathParam("id") Long id) {
        Usuario usuarioLogado = getUsuarioEntity();
        if (usuarioLogado == null) return Response.status(Response.Status.UNAUTHORIZED).build();

        // 🟢 USANDO MÉTODOS HERDADOS DO ControllerBase
        String ip = getIpAddress();
        String ua = getUserAgent();

        if (eventoService.deletarEvento(id, usuarioLogado, ip, ua)) {
            return Response.noContent().build(); // 204 No Content
        }
        return Response.status(Response.Status.FORBIDDEN).build(); // 403 Forbidden
    }
}