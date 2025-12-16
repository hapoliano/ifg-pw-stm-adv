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
    @Location("gestaoUsuarios.html")
    Template gestaoUsuariosTemplate;

    @Inject
    UsuarioService usuarioService;

    @Inject
    AdvogadoVerificacaoService advogadoVerificacaoService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getGestaoUsuariosPage() {
        Usuario usuario = getUsuarioEntity(); // Recupera usuário logado (do ControllerBase)

        // 3. Renderiza o template enviando o objeto usuario
        return gestaoUsuariosTemplate.data("usuario", usuario);
    }

    // ... (Métodos de renderização, /api/todos, /api/advogados/pendentes, /api/{id}/toggle-ativo, /api/estatisticas - SEM MUDANÇAS)

    // 5. API: APROVAR ADVOGADO (AGORA PURO)
    @PUT
    @Path("/api/advogados/aprovar/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response aprovarAdvogado(@PathParam("id") Long id) {
        Usuario master = getUsuarioEntity();
        // Chama o Service. Se houver erro de negócio, o Service lança WebApplicationException,
        // que é automaticamente transformada em resposta HTTP (e.g., 404) pelo Quarkus.
        UsuarioDTO usuario = advogadoVerificacaoService.aprovar(id, master, getIpAddress(), getUserAgent());
        // Retorna o DTO no sucesso (200 OK)
        return Response.ok(usuario).build();
    }

    // 6. API: REJEITAR ADVOGADO (AGORA PURO)
    @POST
    @Path("/api/advogados/rejeitar/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejeitarAdvogado(@PathParam("id") Long id, Map<String, String> data) {
        Usuario master = getUsuarioEntity();
        String comentario = data.get("comentario");

        // Chama o Service. Lógica de erro no Service.
        UsuarioDTO usuario = advogadoVerificacaoService.rejeitar(id, master, comentario, getIpAddress(), getUserAgent());
        // Retorna o DTO no sucesso (200 OK)
        return Response.ok(usuario).build();
    }
}