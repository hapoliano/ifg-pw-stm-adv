package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.atty.stm.dto.DashboardDTO;
import org.atty.stm.model.Usuario;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Evento;
import org.atty.stm.model.ProcessoStatus; // <-- IMPORTAÇÃO ADICIONADA
import org.atty.stm.repository.ClienteRepository;
import org.atty.stm.repository.ProcessoRepository;
import org.atty.stm.repository.EventoRepository;

import java.util.List;

@ApplicationScoped
public class DashboardService {

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    ProcessoRepository processoRepository;

    @Inject
    EventoRepository eventoRepository;

    // ----------------------------------------------------
    // MÉTODOS DE CONTAGEM PARA O DASHBOARD
    // ----------------------------------------------------

    /**
     * Conta processos ativos (status <> 'CONCLUIDO').
     */
    public long contarProcessosAtivos(Usuario usuario) {
        // CORREÇÃO: Usa o ProcessoStatus.CONCLUIDO para maior segurança de tipo,
        // mas a HQL do Panache ainda precisa ser verificada no seu ProcessoRepository para
        // suportar este formato com o enum. Mantenho a string por compatibilidade
        // com o código original, mas o ideal é tipar.

        if ("MASTER".equals(usuario.getPerfil())) {
            // MASTER: todos os processos que não estão concluídos
            return processoRepository.count("status <> 'CONCLUIDO'");
        } else {
            // ADVOGADO/CLIENTE: processos em que ele é participante e que não estão concluídos
            return processoRepository.count(
                    "(advogadoResponsavel.id = ?1 OR cliente.usuario.id = ?1) AND status <> 'CONCLUIDO'",
                    usuario.getId()
            );
        }
    }

    /**
     * Conta o total de processos do usuário.
     */
    public long contarMeusProcessos(Usuario usuario) {
        if ("MASTER".equals(usuario.getPerfil())) {
            // MASTER: todos os processos no sistema
            return processoRepository.count();
        } else {
            // ADVOGADO/CLIENTE: todos os processos em que são participantes
            return processoRepository.count(
                    "advogadoResponsavel.id = ?1 OR cliente.usuario.id = ?1",
                    usuario.getId()
            );
        }
    }

    // ----------------------------------------------------
    // Dashboard principal do advogado (MÉTODO ATUALIZADO)
    // ----------------------------------------------------
    public DashboardDTO getDashboardAdvogado(Usuario usuario) {
        DashboardDTO dto = new DashboardDTO();
        dto.setNomeUsuario(usuario.getNome());

        // Popula o campo processosAtivos
        dto.setProcessosAtivos((int) contarProcessosAtivos(usuario));

        // Popula o campo totalProcessos (equivale a 'meusProcessos' no template)
        dto.setTotalProcessos((int) contarMeusProcessos(usuario));

        // Lógica original mantida para clientes e compromissos
        dto.setTotalClientes((int) clienteRepository.countByAdvogado(usuario.getId()));
        dto.setCompromissosHoje(eventoRepository.countEventosHoje(usuario.getId()));

        return dto;
    }

    // ----------------------------------------------------
    // OUTROS MÉTODOS EXISTENTES
    // ----------------------------------------------------

    public List<Processo> getProcessosRecentes(Usuario usuario, int limit) {
        // CORREÇÃO: listByAdvogadoRecentes agora existe no repositório
        return processoRepository.listByAdvogadoRecentes(usuario.getId(), limit);
    }

    public List<Evento> getProximosCompromissos(Usuario usuario, int limit) {
        return eventoRepository.listProximosByUsuario(usuario.getId(), limit);
    }

    // Estatísticas por status
    public long countProcessosPorStatus(String status, Usuario usuario) {
        if (status == null || status.isEmpty() || usuario == null) {
            return 0;
        }

        try {
            // CONVERSÃO NECESSÁRIA: De String para Enum ProcessoStatus
            ProcessoStatus enumStatus = ProcessoStatus.valueOf(status.toUpperCase());

            // Chama o método tipado do repositório
            return processoRepository.countByStatusAndAdvogado(enumStatus, usuario.getId());

        } catch (IllegalArgumentException e) {
            // Tratar status inválido
            System.err.println("Status de processo inválido fornecido: " + status);
            return 0;
        }
    }
}