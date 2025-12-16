package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "auditoria")
public class Auditoria extends PanacheEntityBase {

    @Id
    @SequenceGenerator(
            name = "auditoriaSeq",
            sequenceName = "auditoria_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auditoriaSeq")
    public Long id;

    @Column(name = "usuario_id")
    public Long usuarioId; // Pode ser nulo para ações não autenticadas (ex: Login Falho)

    @Column(name = "usuario_nome", nullable = false)
    public String usuarioNome; // Pode ser o email (em caso de login falho)

    @Column(nullable = false)
    public String acao; // Ex: LOGIN, CADASTRO_ADVOGADO, PROCESSO_CRIADO

    @Column(nullable = false)
    public String entidade; // Ex: USUARIO, PROCESSO, CLIENTE

    @Column(name = "entidade_id")
    public Long entidadeId; // ID da entidade afetada

    @Column(columnDefinition = "TEXT")
    public String descricao; // Detalhes da ação

    @Column(name = "ip_address")
    public String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    public String userAgent;

    @Column(name = "data_hora", nullable = false)
    public LocalDateTime dataHora;

    // Construtores
    public Auditoria() {
        this.dataHora = LocalDateTime.now();
    }

    public Auditoria(Long usuarioId, String usuarioNome, String acao, String entidade,
                     Long entidadeId, String descricao, String ipAddress, String userAgent) {
        this.usuarioId = usuarioId;
        this.usuarioNome = usuarioNome;
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.descricao = descricao;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.dataHora = LocalDateTime.now(); // Define a data/hora no construtor
    }

    // Métodos Panache de busca
    public static List<Auditoria> findByUsuario(Long usuarioId) {
        return list("usuarioId", usuarioId);
    }

    public static List<Auditoria> findByEntidade(String entidade, Long entidadeId) {
        return find("entidade = ?1 and entidadeId = ?2", entidade, entidadeId).list();
    }

    @PrePersist
    public void prePersist() {
        if (dataHora == null) dataHora = LocalDateTime.now();
    }
}