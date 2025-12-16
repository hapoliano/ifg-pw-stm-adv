package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.atty.stm.dto.DashboardDTO;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Evento;
import org.atty.stm.model.ProcessoStatus; // Importação adicionada para usar o Enum
import org.atty.stm.repository.ClienteRepository;
import org.atty.stm.repository.ProcessoRepository;
import org.atty.stm.repository.EventoRepository;
import org.atty.stm.repository.UsuarioRepository;

import java.util.List;

@ApplicationScoped
public class DashboardMasterService {

    @Inject ClienteRepository clienteRepository;
    @Inject ProcessoRepository processoRepository;
    @Inject EventoRepository eventoRepository;
    @Inject UsuarioRepository usuarioRepository;

    public DashboardDTO getDashboardMaster() {
        DashboardDTO dto = new DashboardDTO();
        dto.setNomeUsuario("MASTER");

        // 1. Processos (geral, MASTER)
        dto.setTotalProcessos((int) processoRepository.countAll());

        // Processos Ativos (global)
        // CORREÇÃO: Usa o novo método countAtivos do repositório, que é tipado
        long processosAtivos = processoRepository.countAtivos();
        dto.setProcessosAtivos((int) processosAtivos);

        // 2. Clientes (geral, MASTER)
        dto.setTotalClientes((int) clienteRepository.countAllAtivos());

        // 3. Compromissos (geral, MASTER)
        dto.setCompromissosHoje(eventoRepository.countEventosHoje(null));

        return dto;
    }

    // Métodos Auxiliares (Para os dados que o DashboardDTO não cobre no front-end Master)
    public long contarTotalUsuarios() {
        return usuarioRepository.countAll();
    }
    public long contarUsuariosAtivos() {
        return usuarioRepository.count("ativo = true");
    }
    public long contarAdvogadosPendentes() {
        return usuarioRepository.countAdvogadosPendentes();
    }

    // Métodos de listagem e contagem existentes
    public List<Processo> getProcessosRecentes(int limit) {
        // CORREÇÃO: Usa o novo método listRecentes do repositório
        return processoRepository.listRecentes(limit);
    }

    public List<Evento> getProximosCompromissos(int limit) {
        return eventoRepository.listProximos(limit);
    }

    // MUDANÇA CRÍTICA: Faz a conversão de String para Enum no Service antes de chamar o Repository
    public long countProcessosPorStatus(String status) {
        if (status == null || status.isEmpty()) {
            return 0;
        }
        try {
            // Converte a string (ex: "EM_ANDAMENTO") para o Enum ProcessoStatus
            ProcessoStatus enumStatus = ProcessoStatus.valueOf(status.toUpperCase());

            // Chama o método tipado do repositório
            return processoRepository.countByStatus(enumStatus);

        } catch (IllegalArgumentException e) {
            // Tratar status inválido (opcionalmente logar ou retornar 0)
            System.err.println("Status de processo inválido fornecido: " + status);
            return 0;
        }
    }
}