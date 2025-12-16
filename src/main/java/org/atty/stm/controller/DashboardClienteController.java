package org.atty.stm.controller;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.qute.Location;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.atty.stm.model.dto.DashboardDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Evento;
import org.atty.stm.service.DashboardClienteService;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Path("/dashboard/cliente")
// NOTA: O Produces(MediaType.APPLICATION_JSON) só vale para os endpoints que não especificarem outro
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DashboardClienteController extends ControllerBase {

    @Inject
    DashboardClienteService dashboardService;

    // 1. INJEÇÃO DO TEMPLATE (Faltando)
    @Inject
    @Location("dashboardCliente.html")
    Template dashboardTemplate;

    // Estrutura MOCK para o Advogado (O ideal é que isso venha do Service)
    public static class AdvogadoInfo {
        public String nome = "Dr. João Silva";
        public String especialidade = "Cível e Trabalhista";
        public String oab = "12345-MG";
        public String telefone = "(31) 98765-4321";
    }

    // -------------------------------------------------------------------------
    // ENDPOINT JSON (Chamado pelo JavaScript para atualização assíncrona)
    // -------------------------------------------------------------------------
    @GET
    public Response getDashboard() {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null || !usuario.isCliente()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        DashboardDTO dto = dashboardService.getDashboardCliente(usuario);

        // Combina DTO com métricas adicionais (necessário para os 4 cards do front-end)
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("totalProcessos", dto.getTotalProcessos());
        responseData.put("processosAtivos", dto.getProcessosAtivos());
        responseData.put("proximasAudiencias", dto.getCompromissosHoje()); // Usa CompromissosHoje do DTO
        responseData.put("mensagensNaoLidas", 0); // HARDCODED: Assumindo 0 se não houver serviço

        return Response.ok(responseData).build();
    }

    // -------------------------------------------------------------------------
    // ENDPOINT HTML (Faltando: Chamado pelo navegador para a primeira renderização)
    // -------------------------------------------------------------------------
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboardPage() {
        Usuario usuario = getUsuarioEntity();
        if (usuario == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }

        DashboardDTO estatisticas = dashboardService.getDashboardCliente(usuario);
        // Lista de Processos Recentes para a tabela/lista
        List<Processo> processosRecentes = dashboardService.getProcessosRecentes(usuario, 5);

        return dashboardTemplate
                .data("usuario", usuario)
                .data("totalProcessos", estatisticas.getTotalProcessos())
                .data("processosAtivos", estatisticas.getProcessosAtivos())
                .data("proximasAudiencias", estatisticas.getCompromissosHoje()) // Mapeia para o card
                .data("mensagensNaoLidas", 0) // HARDCODED (sem service)
                .data("processosLista", processosRecentes)
                .data("advogadoInfo", new AdvogadoInfo()); // MOCK (sem service)
    }

    // -------------------------------------------------------------------------
    // OUTROS ENDPOINTS EXISTENTES (Inalterados)
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