package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.DashboardDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Evento;
import org.atty.stm.service.DashboardService;

import java.util.List;

@Path("/dashboard")
// Adiciona o Produces JSON para os endpoints REST, mas o HTML será sobrescrito abaixo
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class DashboardController extends ControllerBase {

    @Inject
    DashboardService dashboardService;

    // 1. INJEÇÃO DO TEMPLATE (Faltando no código anterior)
    @Inject
    @Location("dashboardAdvogado.html")
    Template dashboardTemplate;

    // -------------------------------------------------------------------------
    // ENDPOINT JSON (Chamado pelo JavaScript para atualização assíncrona)
    // -------------------------------------------------------------------------
    @GET
    public Response getDashboard() {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null || !usuario.isAdvogado()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        DashboardDTO dto = dashboardService.getDashboardAdvogado(usuario);
        return Response.ok(dto).build();
    }

    // -------------------------------------------------------------------------
    // ENDPOINT HTML (Chamado pelo navegador para a primeira renderização)
    // -------------------------------------------------------------------------
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboardPage() {
        Usuario usuario = getUsuarioEntity();

        // Se o usuário não existe, redireciona ou lança exceção
        if (usuario == null) {
            // Dependendo da sua arquitetura, pode ser um throw ou um redirect
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        DashboardDTO estatisticas = dashboardService.getDashboardAdvogado(usuario);

        // Mapeia os dados do DTO para os nomes esperados pelo Template Qute!
        return dashboardTemplate
                .data("usuario", usuario)
                .data("meusProcessos", estatisticas.getTotalProcessos())
                .data("processosAtivos", estatisticas.getProcessosAtivos())
                .data("meusClientes", estatisticas.getTotalClientes())
                .data("compromissosHoje", estatisticas.getCompromissosHoje());
    }

    // -------------------------------------------------------------------------
    // OUTROS ENDPOINTS EXISTENTES
    // -------------------------------------------------------------------------

    @GET
    @Path("/processos-recentes")
    public Response getProcessosRecentes(@QueryParam("limit") @DefaultValue("5") int limit) {
        Usuario usuario = getUsuarioEntity();
        List<Processo> processos = dashboardService.getProcessosRecentes(usuario, limit);
        return Response.ok(processos).build();
    }

    @GET
    @Path("/proximos-compromissos")
    public Response getProximosCompromissos(@QueryParam("limit") @DefaultValue("5") int limit) {
        Usuario usuario = getUsuarioEntity();
        List<Evento> eventos = dashboardService.getProximosCompromissos(usuario, limit);
        return Response.ok(eventos).build();
    }

    @GET
    @Path("/count-status/{status}")
    public Response countPorStatus(@PathParam("status") String status) {
        Usuario usuario = getUsuarioEntity();
        long count = dashboardService.countProcessosPorStatus(status, usuario);
        return Response.ok(count).build();
    }
}