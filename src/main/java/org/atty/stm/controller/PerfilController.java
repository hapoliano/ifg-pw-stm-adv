package org.atty.stm.controller;

import jakarta.enterprise.context.RequestScoped;
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
@RequestScoped
public class PerfilController extends ControllerBase {

    @Inject
    @Location("perfil.html")
    Template perfilTemplate;

    @Inject
    PerfilService perfilService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getPerfil() {
        // Usa o email do contexto de segurança do ControllerBase
        Usuario usuario = perfilService.buscarUsuarioCompletoPorEmail(this.userEmail);

        if(usuario == null){
            // Se o usuário não for encontrado (erro de sincronização DB/JWT), redireciona.
            throw new WebApplicationException(Response.seeOther(java.net.URI.create("/login")).build());
        }
        // Os dados do usuário logado são passados para o template
        return perfilTemplate.data("usuario", usuario);
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