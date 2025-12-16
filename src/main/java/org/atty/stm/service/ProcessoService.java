package org.atty.stm.service;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

// IMPORTAÇÕES CORRETAS
import org.atty.stm.dto.ProcessoDTO; // Usa o seu DTO existente
import org.atty.stm.dto.StatusUpdateDTO; // <--- NOVO IMPORT CORRETO
// Assumindo que você tem essas classes no pacote exception (prática recomendada)
import org.atty.stm.exception.NotFoundException;
import org.atty.stm.exception.ForbiddenException;
import org.atty.stm.model.ProcessoStatus; // Enum de Status

// Assumindo a existência dessas classes nos seus pacotes model e repository
import org.atty.stm.model.Cliente;
import org.atty.stm.model.Processo;
import org.atty.stm.model.Usuario;
import org.atty.stm.repository.ClienteRepository;
import org.atty.stm.repository.UsuarioRepository;
import org.atty.stm.repository.ProcessoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


// ====================================
// SERVICE CLASS
// ====================================

@ApplicationScoped
public class ProcessoService {

    @Inject
    AuthService authService;

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    UsuarioRepository usuarioRepository;

    @Inject
    ProcessoRepository processoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ====================================
    // MÉTODOS DE MAPEAR
    // ====================================

    /** Mapeia a Entity Processo para o DTO (para retornar ao Controller/Cliente) */
    private ProcessoDTO mapToDTO(Processo processo) {
        if (processo == null) return null;
        ProcessoDTO dto = new ProcessoDTO();

        // Mapeamento de IDs
        dto.setId(processo.id);
        dto.setClienteId(processo.cliente != null ? processo.cliente.id : null);
        dto.setAdvogadoResponsavelId(processo.advogadoResponsavel != null ? processo.advogadoResponsavel.id : null);

        // Mapeamento de campos básicos
        dto.setNumeroProcesso(processo.numeroProcesso);
        dto.setTitulo(processo.titulo);
        dto.setDescricao(processo.descricao);

        // Converte o Enum ProcessoStatus (Model) para String (DTO)
        dto.setStatus(processo.status.toString());

        // Mapeamento de detalhes
        dto.setTipo(processo.tipo);
        dto.setArea(processo.area);
        dto.setTribunal(processo.tribunal);
        dto.setVara(processo.vara);
        dto.setComarca(processo.comarca);
        dto.setPrioridade(processo.prioridade);
        dto.setValorCausa(processo.valorCausa);
        dto.setValorCondenacao(processo.valorCondenacao);
        dto.setObservacoes(processo.observacoes);

        // Mapeamento de envolvidos (Nomes para visualização no DTO)
        dto.setCliente(processo.cliente != null ? processo.cliente.nome : "Não Definido");
        dto.setAdvogadoResponsavel(processo.advogadoResponsavel != null ? processo.advogadoResponsavel.nome : "N/A");

        // Mapeamento de datas (Convertendo LocalDate/LocalDateTime para String no formato "dd/MM/yyyy")
        dto.setDataAbertura(processo.dataAbertura != null ? processo.dataAbertura.format(DATE_FORMATTER) : null);
        dto.setDataDistribuicao(processo.dataDistribuicao != null ? processo.dataDistribuicao.format(DATE_FORMATTER) : null);
        dto.setDataConclusao(processo.dataConclusao != null ? processo.dataConclusao.format(DATE_FORMATTER) : null);
        dto.setPrazoFinal(processo.prazoFinal != null ? processo.prazoFinal.format(DATE_FORMATTER) : null);

        // Campos de data de criação e atualização (com hora)
        dto.setDataCriacao(processo.dataCriacao != null ? processo.dataCriacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null);
        dto.setDataAtualizacao(processo.dataAtualizacao != null ? processo.dataAtualizacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null);

        return dto;
    }

    /** Mapeia o DTO para a Entity Processo (para persistir no DB) */
    private void mapToEntity(ProcessoDTO dto, Processo entity) {
        // Mapeamento de campos básicos
        entity.numeroProcesso = dto.getNumeroProcesso();
        entity.titulo = dto.getTitulo();
        entity.descricao = dto.getDescricao();

        // Mapeamento de status e conversão de String (DTO) para Enum (Entity)
        if (dto.getStatus() != null && !dto.getStatus().isEmpty()) {
            try {
                // ToUpperCase() para garantir que a conversão funcione independentemente da caixa do JSON
                entity.status = ProcessoStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status de processo inválido: " + dto.getStatus() + ". Válidos: " + java.util.Arrays.toString(ProcessoStatus.values()));
            }
        } else if (entity.id == null) {
            entity.status = ProcessoStatus.EM_ANDAMENTO; // Default se for um novo processo
        }

        // Mapeamento de datas (Convertendo String "dd/MM/yyyy" para LocalDate)
        try {
            entity.dataAbertura = dto.getDataAbertura() != null && !dto.getDataAbertura().isEmpty() ? LocalDate.parse(dto.getDataAbertura(), DATE_FORMATTER) : null;
            entity.dataDistribuicao = dto.getDataDistribuicao() != null && !dto.getDataDistribuicao().isEmpty() ? LocalDate.parse(dto.getDataDistribuicao(), DATE_FORMATTER) : null;
            entity.prazoFinal = dto.getPrazoFinal() != null && !dto.getPrazoFinal().isEmpty() ? LocalDate.parse(dto.getPrazoFinal(), DATE_FORMATTER) : null;
            entity.dataConclusao = dto.getDataConclusao() != null && !dto.getDataConclusao().isEmpty() ? LocalDate.parse(dto.getDataConclusao(), DATE_FORMATTER) : null;
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato de data inválido. Use dd/MM/yyyy.");
        }

        // Mapeamento de valores e detalhes
        entity.valorCausa = dto.getValorCausa();
        entity.valorCondenacao = dto.getValorCondenacao();
        entity.observacoes = dto.getObservacoes();
        entity.tipo = dto.getTipo();
        entity.area = dto.getArea();
        entity.tribunal = dto.getTribunal();
        entity.vara = dto.getVara();
        entity.comarca = dto.getComarca();
        entity.prioridade = dto.getPrioridade();

        // Atualização de data
        entity.dataAtualizacao = LocalDateTime.now();

        // 1. Mapear Cliente
        if (dto.getClienteId() != null) {
            Cliente cliente = (Cliente) clienteRepository.findById(dto.getClienteId());
            if (cliente == null) {
                throw new NotFoundException("Cliente com ID " + dto.getClienteId() + " não encontrado.");
            }
            entity.cliente = cliente;
        } else {
            throw new IllegalArgumentException("O processo deve estar vinculado a um cliente.");
        }

        // 2. Mapear Advogado Responsável
        if (dto.getAdvogadoResponsavelId() != null) {
            Usuario advogado = (Usuario) usuarioRepository.findById(dto.getAdvogadoResponsavelId());
            if (advogado == null || (!"ADVOGADO".equals(advogado.perfil) && !"MASTER".equals(advogado.perfil))) {
                throw new NotFoundException("Advogado com ID " + dto.getAdvogadoResponsavelId() + " não encontrado ou perfil inválido.");
            }
            entity.advogadoResponsavel = advogado;
        } else {
            entity.advogadoResponsavel = null;
        }
    }

    // Método de segurança
    public boolean temPermissaoParaProcesso(Usuario usuario, Processo processo) {
        if ("MASTER".equals(usuario.perfil)) {
            return true;
        }
        if ("ADVOGADO".equals(usuario.perfil) && processo.advogadoResponsavel != null) {
            return processo.advogadoResponsavel.id.equals(usuario.id);
        }
        if ("CLIENTE".equals(usuario.perfil) && processo.cliente != null && processo.cliente.usuario != null) {
            return processo.cliente.usuario.id.equals(usuario.id);
        }
        return false;
    }

    // ====================================
    // MÉTODOS CRUD (BUSINESS LOGIC)
    // ====================================

    public List<ProcessoDTO> listarTodos(Usuario usuario) {
        PanacheQuery<Processo> query;
        if ("MASTER".equals(usuario.perfil)) {
            query = Processo.findAll();
        } else if ("ADVOGADO".equals(usuario.perfil)) {
            query = Processo.find("advogadoResponsavel.id", usuario.id);
        } else if ("CLIENTE".equals(usuario.perfil)) {
            query = Processo.find("cliente.usuario.id", usuario.id);
        } else {
            return List.of();
        }

        return query.list().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProcessoDTO buscarPorId(Long id, Usuario usuario) {
        Processo processo = processoRepository.findById(id);
        if (processo == null) {
            throw new NotFoundException("Processo não encontrado.");
        }
        if (!temPermissaoParaProcesso(usuario, processo)) {
            throw new ForbiddenException("Acesso negado ao processo.");
        }
        return mapToDTO(processo);
    }

    @Transactional
    public ProcessoDTO criarProcesso(ProcessoDTO dto, Usuario usuario) {
        if (!("MASTER".equals(usuario.perfil) || "ADVOGADO".equals(usuario.perfil))) {
            throw new ForbiddenException("Permissão negada para criar processo.");
        }

        if (processoRepository.findByNumero(dto.getNumeroProcesso()) != null) {
            throw new IllegalArgumentException("Já existe um processo com o número: " + dto.getNumeroProcesso());
        }

        Processo novoProcesso = new Processo();
        mapToEntity(dto, novoProcesso);

        if (novoProcesso.advogadoResponsavel == null && "ADVOGADO".equals(usuario.perfil)) {
            novoProcesso.advogadoResponsavel = usuario;
        }

        novoProcesso.persist();
        return mapToDTO(novoProcesso);
    }

    @Transactional
    public ProcessoDTO atualizarProcesso(Long id, ProcessoDTO dto, Usuario usuario) {
        Processo processo = processoRepository.findById(id);
        if (processo == null) {
            throw new NotFoundException("Processo não encontrado para atualização.");
        }
        if (!temPermissaoParaProcesso(usuario, processo)) {
            throw new ForbiddenException("Permissão negada para atualizar o processo.");
        }

        mapToEntity(dto, processo);
        processo.persistAndFlush();
        return mapToDTO(processo);
    }

    @Transactional
    public ProcessoDTO atualizarStatus(Long id, StatusUpdateDTO statusDto, Usuario usuario) {
        Processo processo = processoRepository.findById(id);
        if (processo == null) {
            throw new NotFoundException("Processo não encontrado para mudança de status.");
        }
        if (!temPermissaoParaProcesso(usuario, processo)) {
            throw new ForbiddenException("Permissão negada para mudar status do processo.");
        }

        try {
            // Conversão segura e correta para o ProcessoStatus
            ProcessoStatus novoStatus = ProcessoStatus.valueOf(statusDto.status.toUpperCase());
            processo.status = novoStatus;

            if (novoStatus == ProcessoStatus.CONCLUIDO && processo.dataConclusao == null) {
                processo.dataConclusao = LocalDate.now();
            } else if (novoStatus != ProcessoStatus.CONCLUIDO) {
                processo.dataConclusao = null;
            }

            processo.dataAtualizacao = LocalDateTime.now();

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + statusDto.status + ". Status válidos: " + java.util.Arrays.toString(ProcessoStatus.values()));
        }

        processo.persistAndFlush();
        return mapToDTO(processo);
    }

    @Transactional
    public void excluirProcesso(Long id, Usuario usuario) {
        Processo processo = processoRepository.findById(id);
        if (processo == null) {
            throw new NotFoundException("Processo não encontrado para exclusão.");
        }
        if (!("MASTER".equals(usuario.perfil) ||
                ("ADVOGADO".equals(usuario.perfil) && temPermissaoParaProcesso(usuario, processo)))) {
            throw new ForbiddenException("Permissão negada para excluir o processo.");
        }

        processo.delete();
    }
}