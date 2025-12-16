    package org.atty.stm.service;

    import io.quarkus.elytron.security.common.BcryptUtil;
    import io.quarkus.panache.common.Sort;
    import jakarta.enterprise.context.ApplicationScoped;
    import jakarta.inject.Inject;
    import jakarta.transaction.Transactional;
    import jakarta.ws.rs.WebApplicationException;
    import jakarta.ws.rs.core.Response;
    import org.atty.stm.dto.CadastroDTO;
    import org.atty.stm.dto.UsuarioDTO;
    import org.atty.stm.model.Usuario;
    import org.atty.stm.repository.UsuarioRepository;
    import org.atty.stm.util.MapperUtils;

    import java.time.LocalDateTime;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;

    @ApplicationScoped
    public class UsuarioService {

        @Inject
        UsuarioRepository usuarioRepository;

        @Inject
        AuditoriaService auditoriaService;

        // --- Lógica de Cadastro (BO) ---
        @Transactional
        public Usuario criarUsuario(CadastroDTO dto) {
            if (!dto.getSenha().equals(dto.getConfirmarSenha())) {
                throw new WebApplicationException("As senhas não conferem.", Response.Status.BAD_REQUEST);
            }

            if (usuarioRepository.existeEmail(dto.getEmail())) {
                throw new WebApplicationException("Este e-mail já está em uso.", Response.Status.CONFLICT);
            }

            if ("ADVOGADO".equalsIgnoreCase(dto.getTipoUsuario()) && (dto.getOab() == null || dto.getOab().isBlank())) {
                throw new WebApplicationException("O número da OAB é obrigatório para advogados.", Response.Status.BAD_REQUEST);
            }

            Usuario novoUsuario = new Usuario();
            novoUsuario.nome = dto.getNome();
            novoUsuario.email = dto.getEmail().toLowerCase();
            novoUsuario.senha = BcryptUtil.bcryptHash(dto.getSenha());
            novoUsuario.perfil = dto.getTipoUsuario().toUpperCase();
            novoUsuario.oab = dto.getOab();
            novoUsuario.telefone = dto.getTelefone();
            // Clientes e Masters são aprovados automaticamente. Advogados NÃO.
            novoUsuario.aprovado = !("ADVOGADO".equals(novoUsuario.perfil));
            novoUsuario.ativo = true; // Todo usuário é ativo por padrão ao criar (a menos que seja rejeitado)

            usuarioRepository.persist(novoUsuario);
            return novoUsuario;
        }

        // --- Lógica de Aprovação (BO) ---
        @Transactional
        public UsuarioDTO aprovarUsuario(Long id, Usuario master, String ip, String ua) {
            Usuario usuario = usuarioRepository.findById(id);
            if (usuario == null) throw new WebApplicationException("Usuário não encontrado", Response.Status.NOT_FOUND);

            // Esta verificação se aplica a qualquer usuário que precisa de aprovação (atualmente, Advogados)
            if (usuario.aprovado) {
                throw new WebApplicationException("Usuário já está aprovado.", Response.Status.BAD_REQUEST);
            }

            usuario.aprovado = true;
            usuario.ativo = true; // Garante que o usuário aprovado esteja ativo
            usuario.dataAtualizacao = LocalDateTime.now();
            usuarioRepository.persist(usuario);

            auditoriaService.registrarAcaoSimples(master, "APROVACAO", "USUARIO", usuario.id,
                    "Usuário aprovado: " + usuario.email, ip, ua);

            return MapperUtils.toDTO(usuario);
        }

        // NOVO MÉTODO: Listar todos os usuários para a aba 'Gerenciamento Geral' do Master
        public List<UsuarioDTO> listarTodosMaster() {
            // Ordena por nome para facilitar a visualização
            List<Usuario> usuarios = usuarioRepository.listAll(Sort.by("nome"));
            return MapperUtils.toDTOList(usuarios, UsuarioDTO.class);
        }

        // --- Lógica do Dashboard (BO) ---
        public Map<String, Long> getEstatisticasDashboard() {
            Map<String, Long> estatisticas = new HashMap<>();

            long totalUsuarios = usuarioRepository.count();
            long totalAdvogados = usuarioRepository.countByPerfil("ADVOGADO");
            long totalClientes = usuarioRepository.countByPerfil("CLIENTE");
            // CORREÇÃO: Conta pendentes apenas se forem ativos, para garantir que rejeitados/inativados não entrem
            long advogadosPendentes = usuarioRepository.count("perfil = 'ADVOGADO' AND aprovado = false AND ativo = true");
            long advogadosAprovados = totalAdvogados - advogadosPendentes; // Cálculo simples

            estatisticas.put("totalUsuarios", totalUsuarios);
            estatisticas.put("totalAdvogados", totalAdvogados);
            estatisticas.put("totalClientes", totalClientes);
            estatisticas.put("advogadosPendentes", advogadosPendentes);
            estatisticas.put("advogadosAprovados", advogadosAprovados);

            return estatisticas;
        }

        // --- Métodos herdados do antigo ---
        public List<UsuarioDTO> listarTodos() {
            return MapperUtils.toDTOList(usuarioRepository.listAll(), UsuarioDTO.class);
        }

        @Transactional
        public UsuarioDTO toggleAtivo(Long id, Usuario admin, String ip, String ua) {
            Usuario u = usuarioRepository.findById(id);
            if (u == null) throw new WebApplicationException("Usuário não encontrado", Response.Status.NOT_FOUND);
            u.ativo = !u.ativo;
            u.dataAtualizacao = LocalDateTime.now();
            usuarioRepository.persist(u);

            auditoriaService.registrarAcaoSimples(admin, "TOGGLE_ATIVO", "USUARIO", u.id,
                    "Toggle ativo para: " + u.email + " (Novo status: " + (u.ativo ? "Ativo" : "Inativo") + ")", ip, ua);
            return MapperUtils.toDTO(u);
        }
    }