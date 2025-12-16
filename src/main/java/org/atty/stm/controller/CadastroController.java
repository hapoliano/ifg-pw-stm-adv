package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.Location;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.CadastroDTO;
import org.atty.stm.service.UsuarioService;

import java.util.Map;

@Path("/cadastro")
public class CadastroController extends org.atty.stm.controller.ControllerBase {

    @Inject
    @Location("cadastro.html")
    Template cadastroTemplate;

    @Inject
    UsuarioService usuarioService;

    // Página de cadastro
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getCadastroPage() {
        return cadastroTemplate.instance()
                .data("nomeUsuario", this.userName)
                .data("perfilUsuario", this.userPerfil);
    }

    // Endpoint API de cadastro
    @POST
    @Path("/cadastrar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fazerCadastro(CadastroDTO dto) {
        try {
            var usuario = usuarioService.criarUsuario(dto);
            String mensagem = ("ADVOGADO".equals(usuario.perfil) && !usuario.aprovado) ?
                    "Cadastro realizado! Aguarde aprovação." : "Cadastro realizado com sucesso!";
            return Response.ok(Map.of(
                    "success", true,
                    "mensagem", mensagem,
                    "redirectUrl", "/login"
            )).build();

        } catch (jakarta.ws.rs.WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("success", false, "mensagem", e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("success", false, "mensagem", "Erro interno"))
                    .build();
        }
    }
}
