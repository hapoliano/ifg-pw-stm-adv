package org.atty.stm.util;

import org.atty.stm.model.*;
import org.atty.stm.model.dto.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapperUtils {

    // -----------------------------------------------------
    // USUÁRIO
    // -----------------------------------------------------
    public static UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;

        UsuarioDTO dto = new UsuarioDTO();

        dto.setId(usuario.id);
        dto.setEmail(usuario.email);
        dto.setNome(usuario.nome);
        dto.setPerfil(usuario.perfil);
        dto.setTelefone(usuario.telefone);
        dto.setEndereco(usuario.endereco);
        dto.setCidade(usuario.cidade);
        dto.setOab(usuario.oab);
        dto.setEspecialidades(usuario.especialidade);
        dto.setFormacao(usuario.formacao);
        dto.setExperiencia(usuario.experiencia);
        dto.setAtivo(usuario.ativo);
        dto.setAprovado(usuario.aprovado);

        dto.setDataCriacao(usuario.dataCriacao);
        dto.setDataAtualizacao(usuario.dataAtualizacao);
        dto.setUltimoAcesso(usuario.ultimoAcesso);

        dto.clearSensitiveData();
        return dto;
    }

    // -----------------------------------------------------
    // CLIENTE
    // -----------------------------------------------------
    public static ClienteDTO toDTO(Cliente cliente) {
        if (cliente == null) return null;

        ClienteDTO dto = new ClienteDTO();

        dto.setId(String.valueOf(cliente.id));
        dto.setNome(cliente.nome);
        dto.setEmail(cliente.email);
        dto.setCpfCnpj(cliente.cpfCnpj);
        dto.setTelefone(cliente.telefone);
        dto.setEndereco(cliente.endereco);
        dto.setCidade(cliente.cidade);
        dto.setEstado(cliente.estado);
        dto.setCep(cliente.cep);

        return dto;
    }

    // -----------------------------------------------------
    // PROCESSO
    // -----------------------------------------------------
    public static ProcessoDTO toDTO(Processo p) {
        if (p == null) return null;

        ProcessoDTO dto = new ProcessoDTO();

        dto.setId(p.id);
        dto.setNumeroProcesso(p.numeroProcesso);
        dto.setTitulo(p.titulo);
        dto.setDescricao(p.descricao);

        dto.setClienteId(p.cliente != null ? p.cliente.id : null);
        dto.setCliente(p.cliente != null ? p.cliente.nome : null);

        dto.setAdvogadoResponsavelId(
                p.advogadoResponsavel != null ? p.advogadoResponsavel.id : null
        );
        dto.setAdvogadoResponsavel(
                p.advogadoResponsavel != null ? p.advogadoResponsavel.nome : null
        );

        dto.setStatus(p.status != null ? p.status.name() : null);
        dto.setTipo(p.tipo);
        dto.setArea(p.area);
        dto.setTribunal(p.tribunal);
        dto.setVara(p.vara);
        dto.setComarca(p.comarca);
        dto.setPrioridade(p.prioridade);

        dto.setValorCausa(p.valorCausa);
        dto.setValorCondenacao(p.valorCondenacao);
        dto.setObservacoes(p.observacoes);

        dto.setDataAbertura(p.dataAbertura != null ? p.dataAbertura.toString() : null);
        dto.setDataDistribuicao(p.dataDistribuicao != null ? p.dataDistribuicao.toString() : null);
        dto.setDataConclusao(p.dataConclusao != null ? p.dataConclusao.toString() : null);
        dto.setPrazoFinal(p.prazoFinal != null ? p.prazoFinal.toString() : null);

        dto.setDataCriacao(p.dataCriacao != null ? p.dataCriacao.toString() : null);
        dto.setDataAtualizacao(p.dataAtualizacao != null ? p.dataAtualizacao.toString() : null);

        return dto;
    }


    // -----------------------------------------------------
    // EVENTO
    // -----------------------------------------------------
    public static EventoDTO toDTO(Evento e) {
        if (e == null) return null;

        EventoDTO dto = new EventoDTO();

        dto.setId(e.id);
        dto.setTitle(e.titulo);
        dto.setDescription(e.descricao);

        // Conversão de datas
        dto.setStart(e.dataInicio != null ? e.dataInicio.toString() : null);
        dto.setEnd(e.dataFim != null ? e.dataFim.toString() : null);

        dto.setColor(e.cor);
        dto.setProcessoId(e.processo != null ? e.processo.id : null);

        // --- ADICIONE ESTAS DUAS LINHAS ---
        dto.setTipo(e.tipo);
        dto.setAllDay(e.diaInteiro);
        // ----------------------------------

        return dto;
    }

    // -----------------------------------------------------
    // FILES
    // -----------------------------------------------------
    public static FilesDTO toDTO(Files file) {
        if (file == null) return null;

        FilesDTO dto = new FilesDTO();

        dto.setId(file.id);
        dto.setNome(file.nomeArquivo);
        dto.setTipo(file.tipoArquivo);
        dto.setTamanho(file.tamanho);

        // URL → vem do campo caminhoArquivo
        dto.setUrl(file.caminhoArquivo);

        dto.setDataUpload(
                file.dataUpload != null
                        ? file.dataUpload.toString()
                        : null
        );

        dto.setProcessoId(file.processo != null ? file.processo.id : null);

        return dto;
    }

    // -----------------------------------------------------
    // AUDITORIA (CORRIGIDO)
    // -----------------------------------------------------
    public static AuditoriaDTO toDTO(Auditoria auditoria) {
        if (auditoria == null) return null;
        // CORREÇÃO: Usar o método fromEntity do DTO para mapeamento completo e formatação de data
        return AuditoriaDTO.fromEntity(auditoria);
    }


    // -----------------------------------------------------
    // LISTAS (GENÉRICO)
    // -----------------------------------------------------
    public static <E, D> List<D> toDTOList(List<E> entities, Class<D> dtoClass) {
        if (entities == null) return List.of();

        return entities.stream()
                .map(entity -> {
                    if (entity instanceof Usuario u) return (D) toDTO(u);
                    if (entity instanceof Cliente c) return (D) toDTO(c);
                    if (entity instanceof Processo p) return (D) toDTO(p);
                    if (entity instanceof Evento ev) return (D) toDTO(ev);
                    if (entity instanceof Files f) return (D) toDTO(f);
                    if (entity instanceof Auditoria a) return (D) AuditoriaDTO.fromEntity(a); // CORREÇÃO: Usar fromEntity
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}