package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.NewCookie; // Importante para redirecionamentos se precisar
import org.atty.stm.model.dto.DashboardDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Evento;
import org.atty.stm.service.DashboardService;

import java.util.List;
import java.net.URI; // Para redirecionamento

@Path("/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardController extends ControllerBase {

    @Inject
    DashboardService dashboardService;

    @Inject
    @Location("dashboardAdvogado.html")
    Template dashboardTemplate;

    // --- Endpoint JSON (Mantido igual) ---
    @GET
    public Response getDashboard() {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null || !usuario.isAdvogado()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        DashboardDTO dto = dashboardService.getDashboardAdvogado(usuario);
        return Response.ok(dto).build();
    }

    // --- Endpoint HTML (CORRIGIDO) ---
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboardPage() {
        Usuario usuario = getUsuarioEntity();

        if (usuario == null) {
            // Redireciona para login se não estiver logado
            return Response.seeOther(URI.create("/login")).build();
        }

        // SEGURANÇA: Se for MASTER tentando acessar /dashboard, manda para /dashboard/master
        if ("MASTER".equals(usuario.getPerfil())) {
            return Response.seeOther(URI.create("/dashboard/master")).build();
        }

        DashboardDTO estatisticas = dashboardService.getDashboardAdvogado(usuario);

        // Renderiza o template
        TemplateInstance instance = dashboardTemplate
                .data("usuario", usuario)
                .data("meusProcessos", estatisticas.getTotalProcessos())
                .data("processosAtivos", estatisticas.getProcessosAtivos())
                .data("meusClientes", estatisticas.getTotalClientes())
                .data("compromissosHoje", estatisticas.getCompromissosHoje());

        // Retorna com cabeçalhos ANTI-CACHE
        return Response.ok(instance)
                .header("Cache-Control", "no-cache, no-store, must-revalidate") // HTTP 1.1
                .header("Pragma", "no-cache") // HTTP 1.0
                .header("Expires", "0") // Proxies
                .build();
    }

    // ... (Mantenha os outros endpoints como getProcessosRecentes iguais) ...
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