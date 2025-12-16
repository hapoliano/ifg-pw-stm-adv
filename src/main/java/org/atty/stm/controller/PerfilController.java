package org.atty.stm.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.inject.Inject;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import org.atty.stm.model.Usuario;
import org.atty.stm.service.PerfilService;
import jakarta.annotation.security.RolesAllowed;

@Path("/perfil")
@RolesAllowed({"MASTER", "ADVOGADO", "CLIENTE"}) // Protege a rota
public class PerfilController extends ControllerBase {

    @Inject
    @Location("perfil.html")
    Template perfilTemplate;

    @Inject
    PerfilService perfilService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getPerfil() {
        // Usa o email do contexto de segurança do ControllerBase
        Usuario usuario = perfilService.buscarUsuarioCompletoPorEmail(this.userEmail);

        if (usuario == null) {
            return Response.seeOther(java.net.URI.create("/login")).build();
        }

        TemplateInstance instance = perfilTemplate.data("usuario", usuario);

        return Response.ok(instance)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build();
    }

    @POST
    @Path("/editar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editarPerfil(Usuario usuarioAtualizado) {
        // Usa o ID do contexto de segurança do ControllerBase para garantir que o usuário
        // só possa editar o seu próprio perfil.
        boolean ok = perfilService.atualizarPerfil(this.userId, usuarioAtualizado);

        if(ok) return Response.ok().build();
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    @POST
    @Path("/senha")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response alterarSenha(String novaSenha) {
        // Usa o ID do contexto de segurança do ControllerBase
        boolean ok = perfilService.alterarSenha(this.userId, novaSenha);

        if(ok) return Response.ok().build();
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}