package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "advogado_verificacoes")
public class AdvogadoVerificacao extends PanacheEntityBase {
    @Id
    @SequenceGenerator(
            name = "advVerificacoesSeq",
            sequenceName = "advogado_verificacoes_seq",
            allocationSize = 50 // <-- A CORREÇÃO
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "advVerificacoesSeq")
    public Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advogado_id", nullable = false, unique=true)
    public Usuario advogado;

    public String oab;
    public String status;
    @Column(name = "comentarios_admin", columnDefinition = "TEXT")
    public String comentariosAdmin;

    @Column(name = "data_solicitacao", nullable = false)
    public LocalDateTime dataSolicitacao = LocalDateTime.now();

    @Column(name = "data_verificacao")
    public LocalDateTime dataVerificacao;
}