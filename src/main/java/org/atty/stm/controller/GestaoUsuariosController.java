package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.Location;
import io.quarkus.qute.TemplateInstance;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.service.UsuarioService;
import org.atty.stm.service.AdvogadoVerificacaoService;
import org.atty.stm.model.Usuario;
import org.atty.stm.model.dto.UsuarioDTO;

import java.util.Map;

@Path("/gestao-usuarios")
@RolesAllowed("MASTER")
@RequestScoped
public class GestaoUsuariosController extends ControllerBase {

    @Inject
    @Location("usuarios.html")
    Template gestaoUsuariosTemplate;

    @Inject
    UsuarioService usuarioService;

    @Inject
    AdvogadoVerificacaoService advogadoVerificacaoService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getGestaoUsuariosPage() {
        Usuario usuario = getUsuarioEntity();
        return gestaoUsuariosTemplate.data("usuario", usuario);
    }

    // --- MÉTODOS QUE ESTAVAM FALTANDO ---

    @GET
    @Path("/api/estatisticas")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEstatisticas() {
        // Usa o serviço de usuário para pegar os totais
        return Response.ok(usuarioService.getEstatisticasDashboard()).build();
    }

    @GET
    @Path("/api/advogados/pendentes")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarPendentes() {
        // Usa o serviço de verificação para listar pendentes
        return Response.ok(advogadoVerificacaoService.listarPendentes()).build();
    }

    @GET
    @Path("/api/todos")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listarTodos() {
        return Response.ok(usuarioService.listarTodos()).build();
    }

    @PUT
    @Path("/api/{id}/toggle-ativo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleAtivo(@PathParam("id") Long id) {
        Usuario master = getUsuarioEntity();
        return Response.ok(usuarioService.toggleAtivo(id, master, getIpAddress(), getUserAgent())).build();
    }

    // --- FIM DOS MÉTODOS QUE FALTAVAM ---

    @PUT
    @Path("/api/advogados/aprovar/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response aprovarAdvogado(@PathParam("id") Long id) {
        Usuario master = getUsuarioEntity();
        UsuarioDTO usuario = advogadoVerificacaoService.aprovar(id, master, getIpAddress(), getUserAgent());
        return Response.ok(usuario).build();
    }

    @POST
    @Path("/api/advogados/rejeitar/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejeitarAdvogado(@PathParam("id") Long id, Map<String, String> data) {
        Usuario master = getUsuarioEntity();
        String comentario = data.get("comentario");
        UsuarioDTO usuario = advogadoVerificacaoService.rejeitar(id, master, comentario, getIpAddress(), getUserAgent());
        return Response.ok(usuario).build();
    }
}