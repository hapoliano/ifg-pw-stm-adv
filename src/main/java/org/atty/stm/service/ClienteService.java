package org.atty.stm.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.atty.stm.model.dto.ClienteDTO;
import org.atty.stm.model.Cliente;
import org.atty.stm.model.Usuario;
import org.atty.stm.model.Processo;
import org.atty.stm.repository.ClienteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ClienteService {

    @Inject
    ClienteRepository clienteRepository;

    @Inject
    AuditoriaService auditoriaService;

    @Transactional
    public List<ClienteDTO> listarClientes(Usuario usuario) {
        List<Cliente> clientes;

        if (usuario == null) return List.of();

        if ("MASTER".equals(usuario.perfil)) {
            // Traz tudo com fetch
            clientes = clienteRepository.listarTodosComAdvogado();
        } else if ("ADVOGADO".equals(usuario.perfil)) {
            // Traz do advogado com fetch
            clientes = clienteRepository.listarPorAdvogadoComFetch(usuario.id);
        } else {
            Cliente cliente = clienteRepository.findByUsuarioId(usuario.id);
            clientes = cliente != null ? List.of(cliente) : List.of();
        }

        return clientes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ClienteDTO buscarClientePorId(Long id, Usuario usuario) {
        Cliente cliente = clienteRepository.findByIdComFetch(id);

        if (cliente == null) return null;
        if (usuario != null && !temPermissao(usuario, cliente)) return null;

        return toDTO(cliente);
    }

    // --- Criar novo cliente ---
    @Transactional
    public ClienteDTO criarCliente(ClienteDTO dto, Usuario usuarioCriador, String ip, String ua) {
        validarCamposObrigatorios(dto);

        Cliente existente = clienteRepository.findByEmail(dto.getEmail());
        if (existente != null) throw new RuntimeException("Email já cadastrado");

        Cliente cliente = new Cliente();
        cliente.nome = dto.getNome().trim();
        cliente.email = dto.getEmail().trim().toLowerCase();
        cliente.telefone = dto.getTelefone() != null ? dto.getTelefone().trim() : null;
        cliente.cpfCnpj = dto.getCpfCnpj() != null ? dto.getCpfCnpj().trim() : null;
        cliente.endereco = dto.getEndereco() != null ? dto.getEndereco().trim() : null;
        cliente.cidade = dto.getCidade() != null ? dto.getCidade().trim() : null;
        cliente.estado = dto.getEstado() != null ? dto.getEstado().trim() : null;
        cliente.cep = dto.getCep() != null ? dto.getCep().trim() : null;
        cliente.status = "ATIVO";
        cliente.dataCriacao = LocalDateTime.now();
        cliente.dataAtualizacao = LocalDateTime.now();

        if (usuarioCriador != null && "ADVOGADO".equals(usuarioCriador.perfil)) {
            cliente.advogadoResponsavel = usuarioCriador;
        }

        clienteRepository.persist(cliente);
        auditoriaService.registrarAcaoSimples(usuarioCriador, "CRIACAO", "CLIENTE",
                cliente.id, "Cliente criado: " + cliente.email, ip, ua);

        return toDTO(cliente);
    }

    // --- Atualizar cliente ---
    @Transactional
    public ClienteDTO atualizarCliente(Long id, ClienteDTO dto, Usuario usuario) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null) throw new RuntimeException("Cliente não encontrado");
        if (usuario != null && !temPermissao(usuario, cliente)) throw new RuntimeException("Sem permissão para atualizar este cliente");

        validarCamposObrigatorios(dto);

        Cliente existente = clienteRepository.findByEmail(dto.getEmail());
        if (existente != null && !existente.id.equals(cliente.id)) {
            throw new RuntimeException("Email já cadastrado");
        }

        cliente.nome = dto.getNome().trim();
        cliente.email = dto.getEmail().trim().toLowerCase();
        cliente.telefone = dto.getTelefone() != null ? dto.getTelefone().trim() : null;
        cliente.cpfCnpj = dto.getCpfCnpj() != null ? dto.getCpfCnpj().trim() : null;
        cliente.endereco = dto.getEndereco() != null ? dto.getEndereco().trim() : null;
        cliente.cidade = dto.getCidade() != null ? dto.getCidade().trim() : null;
        cliente.estado = dto.getEstado() != null ? dto.getEstado().trim() : null;
        cliente.cep = dto.getCep() != null ? dto.getCep().trim() : null;
        cliente.dataAtualizacao = LocalDateTime.now();

        clienteRepository.persist(cliente);

        return toDTO(cliente);
    }

    // --- Deletar cliente ---
    @Transactional
    public boolean deletarCliente(Long id, Usuario usuario, String ip, String ua) {
        Cliente cliente = clienteRepository.findById(id);
        if (cliente == null) return false;
        if (usuario != null && !temPermissao(usuario, cliente)) throw new RuntimeException("Sem permissão para deletar este cliente");

        long processosCount = Processo.count("cliente.id = ?1", id);
        if (processosCount > 0) throw new RuntimeException("Não é possível excluir cliente com processos vinculados");

        boolean deleted = clienteRepository.deleteById(id);
        if (deleted) {
            auditoriaService.registrarAcaoSimples(usuario, "DELECAO", "CLIENTE", id, "Cliente deletado", ip, ua);
        }
        return deleted;
    }

    // --- Métodos auxiliares ---
    private boolean temPermissao(Usuario usuario, Cliente cliente) {
        if (usuario == null) return false;
        switch (usuario.perfil) {
            case "MASTER": return true;
            case "ADVOGADO": return cliente.advogadoResponsavel != null && cliente.advogadoResponsavel.id.equals(usuario.id);
            case "CLIENTE": return cliente.usuario != null && cliente.usuario.id.equals(usuario.id);
            default: return false;
        }
    }

    private void validarCamposObrigatorios(ClienteDTO dto) {
        if (dto.getNome() == null || dto.getNome().trim().isEmpty() ||
                dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Nome e email são obrigatórios");
        }
    }

    private ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.id != null ? cliente.id.toString() : null);
        dto.setNome(cliente.nome);
        dto.setEmail(cliente.email);
        dto.setTelefone(cliente.telefone);
        dto.setCpfCnpj(cliente.cpfCnpj);
        dto.setEndereco(cliente.endereco);
        dto.setCidade(cliente.cidade);
        dto.setEstado(cliente.estado);
        dto.setCep(cliente.cep);
        dto.setStatus(cliente.status);
        dto.setDataCriacao(cliente.dataCriacao != null ? cliente.dataCriacao.toString() : "");

        if (cliente.advogadoResponsavel != null) {
            dto.setAdvogadoResponsavel(cliente.advogadoResponsavel.nome);
        }

        return dto;
    }
}