package org.atty.stm.model.dto;

import org.atty.stm.model.Auditoria;
import java.time.format.DateTimeFormatter;

public class AuditoriaDTO {
    // Campos que o HTML espera (mapeados do Model)
    public String dataHora;
    public String usuario;  // Mapeia de Auditoria.usuarioNome
    public String acao;
    public String entidade;

    // NOVO: ID primário do registro de Auditoria (Auditoria.id)
    public Long registroId;

    // RENOMEADO: ID da entidade que foi auditada (Auditoria.entidadeId)
    public Long entidadeId;

    public String descricao;

    // Campos auxiliares
    public String ipAddress;
    public Long usuarioId;
    public String userAgent; // CAMPO ADICIONALMENTE INCLUÍDO NO FROMENTITY ANTES

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /** Mapeia a Entidade Auditoria para o DTO de visualização, adaptando os nomes. */
    public static AuditoriaDTO fromEntity(Auditoria entity) {
        AuditoriaDTO dto = new AuditoriaDTO();

        dto.dataHora = entity.dataHora != null ? entity.dataHora.format(FORMATTER) : null;
        dto.usuario = entity.usuarioNome;
        dto.acao = entity.acao;
        dto.entidade = entity.entidade;

        // CORREÇÃO DE NOME E MAPEAMENTO
        dto.registroId = entity.id;
        dto.entidadeId = entity.entidadeId;

        dto.descricao = entity.descricao;

        dto.usuarioId = entity.usuarioId;
        dto.ipAddress = entity.ipAddress;
        dto.userAgent = entity.userAgent;

        return dto;
    }
}