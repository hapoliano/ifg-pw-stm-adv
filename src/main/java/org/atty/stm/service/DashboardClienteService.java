package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.atty.stm.model.dto.DashboardDTO;
import org.atty.stm.model.enums.ProcessoStatus;
import org.atty.stm.model.Usuario;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Evento;
import org.atty.stm.repository.ProcessoRepository;
import org.atty.stm.repository.EventoRepository;

import java.util.List;

@ApplicationScoped
public class DashboardClienteService {

    @Inject
    ProcessoRepository processoRepository;

    @Inject
    EventoRepository eventoRepository;

    public DashboardDTO getDashboardCliente(Usuario usuario) {
        DashboardDTO dto = new DashboardDTO();
        dto.setNomeUsuario(usuario.getNome());

        // Cliente só vê ele mesmo
        dto.setTotalClientes(1);

        // Total de Processos do Cliente (agora usa o método countByCliente do repositório)
        long totalProcessos = processoRepository.countByCliente(usuario.getId());
        dto.setTotalProcessos((int) totalProcessos);

        // Processos Ativos do Cliente:
        // CORREÇÃO: Usa o método de Panache simplificado se countAtivosByCliente não existir
        // Caso seu ProcessoRepository possua countAtivosByCliente, use-o
        long processosAtivos = processoRepository.count(
                "cliente.usuario.id = ?1 AND status <> ?2",
                usuario.getId(),
                ProcessoStatus.CONCLUIDO // Usa o Enum na query Panache
        );
        dto.setProcessosAtivos((int) processosAtivos);

        dto.setCompromissosHoje(eventoRepository.countEventosHoje(usuario.getId()));

        return dto;
    }

    public List<Processo> getProcessosRecentes(Usuario usuario, int limit) {
        // CORREÇÃO: Usa o novo método listByClienteRecentes do repositório
        return processoRepository.listByClienteRecentes(usuario.getId(), limit);
    }

    public List<Evento> getProximosCompromissos(Usuario usuario, int limit) {
        return eventoRepository.listProximosByUsuario(usuario.getId(), limit);
    }

    // CORREÇÃO CRÍTICA: Realiza a conversão de String para ProcessoStatus
    public long countProcessosPorStatus(String status, Usuario usuario) {
        if (status == null || status.isEmpty() || usuario == null) {
            return 0;
        }

        try {
            // Converte a string (ex: "EM_ANDAMENTO") para o Enum ProcessoStatus
            ProcessoStatus enumStatus = ProcessoStatus.valueOf(status.toUpperCase());

            // Chama o método tipado do repositório
            // Nota: Este método no repositório usa o ID do usuário (cliente.usuario.id) para filtrar.
            return processoRepository.countByStatusAndCliente(enumStatus, usuario.getId());

        } catch (IllegalArgumentException e) {
            // Se o status for inválido, retorna 0
            System.err.println("Status de processo inválido fornecido no Dashboard Cliente: " + status);
            return 0;
        }
    }
}