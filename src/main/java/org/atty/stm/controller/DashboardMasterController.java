package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.DashboardDTO;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Evento;
import org.atty.stm.model.Usuario; // Adicionado para uso no Audit
import org.atty.stm.service.DashboardMasterService;
import org.atty.stm.service.AuditoriaService; // <<< NOVO

import java.net.URI;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Path("/dashboard/master")
@RolesAllowed("MASTER")
public class DashboardMasterController extends ControllerBase {

    @Inject
    @Location("dashboardMaster.html")
    Template dashboardTemplate;

    @Inject
    DashboardMasterService dashboardService;

    @Inject // <<< INJEÇÃO DO SERVIÇO DE AUDITORIA
    AuditoriaService auditoriaService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboard() {
        // 1. Pega os dados básicos que cabem no DTO
        DashboardDTO dto = dashboardService.getDashboardMaster();

        // 2. Pega os dados adicionais que só o Master precisa
        long totalUsuarios = dashboardService.contarTotalUsuarios();
        long usuariosAtivos = dashboardService.contarUsuariosAtivos();
        long advogadosPendentes = dashboardService.contarAdvogadosPendentes();

        // 3. Combina DTO e dados adicionais em um Map para o JSON
        Map<String, Object> responseData = new HashMap<>();

        // Dados do DTO
        responseData.put("nomeUsuario", dto.getNomeUsuario());
        responseData.put("totalProcessos", dto.getTotalProcessos());
        responseData.put("processosAtivos", dto.getProcessosAtivos());
        responseData.put("totalClientes", dto.getTotalClientes());
        responseData.put("compromissosHoje", dto.getCompromissosHoje());

        // Dados Auxiliares (que o front-end JavaScript espera)
        responseData.put("totalUsuarios", totalUsuarios);
        responseData.put("usuariosAtivos", usuariosAtivos);
        responseData.put("advogadosPendentes", advogadosPendentes);

        // Retorna o Map completo como JSON
        return Response.ok(responseData).build();
    }

    @GET
    @Path("/processos-recentes")
    public Response getProcessosRecentes(@QueryParam("limit") @DefaultValue("5") int limit) {
        List<Processo> processos = dashboardService.getProcessosRecentes(limit);
        return Response.ok(processos).build();
    }

    @GET
    @Path("/proximos-compromissos")
    public Response getProximosCompromissos(@QueryParam("limit") @DefaultValue("5") int limit) {
        List<Evento> eventos = dashboardService.getProximosCompromissos(limit);
        return Response.ok(eventos).build();
    }

    @GET
    @Path("/count-status/{status}")
    public Response countPorStatus(@PathParam("status") String status) {
        long count = dashboardService.countProcessosPorStatus(status);
        return Response.ok(count).build();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboardPage() {
        Usuario usuario = getUsuarioEntity();

        if (usuario == null) {
            return Response.seeOther(URI.create("/login")).build();
        }

        // Auditoria
        auditoriaService.registrarAcaoSimples(
                usuario, "ACCESS_DASHBOARD", "DASHBOARD_MASTER", null,
                "Acesso à Dashboard Master", getIpAddress(), getUserAgent()
        );

        TemplateInstance instance = dashboardTemplate
                .data("usuario", usuario)
                .data("nomeUsuario", this.userName)
                .data("perfilUsuario", this.userPerfil)
                .data("estatisticas", dashboardService.getDashboardMaster())
                .data("processosRecentes", dashboardService.getProcessosRecentes(5))
                .data("eventosHoje", dashboardService.getProximosCompromissos(5));

        // Retorna com cabeçalhos ANTI-CACHE
        return Response.ok(instance)
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .build();
    }
}