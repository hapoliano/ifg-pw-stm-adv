package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.atty.stm.model.enums.ProcessoStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "processos")
public class Processo extends PanacheEntityBase {

    // --- ID e Geração de Chave Primária ---
    @Id
    @SequenceGenerator(
            name = "processosSeq",
            sequenceName = "processos_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "processosSeq")
    public Long id;

    // --- Detalhes Principais ---
    @Column(name = "numero_processo", unique = true, nullable = false)
    public String numeroProcesso;

    public String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    public String descricao;

    // CORREÇÃO: Usa o Enum ProcessoStatus importado
    @Enumerated(EnumType.STRING)
    public ProcessoStatus status = ProcessoStatus.EM_ANDAMENTO;

    public String tipo;
    public String area;
    public String tribunal;
    public String vara;
    public String comarca;
    public String prioridade = "MEDIA";

    // --- Datas e Prazos ---
    @Column(name = "data_abertura")
    public LocalDate dataAbertura;

    @Column(name = "data_distribuicao")
    public LocalDate dataDistribuicao;

    @Column(name = "data_conclusao")
    public LocalDate dataConclusao;

    @Column(name = "prazo_final")
    public LocalDate prazoFinal;

    @Column(name = "data_criacao", nullable = false)
    public LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao", nullable = false)
    public LocalDateTime dataAtualizacao = LocalDateTime.now();

    // --- Valores ---
    @Column(name = "valor_causa")
    public Double valorCausa;

    @Column(name = "valor_condenacao")
    public Double valorCondenacao;

    public String observacoes;

    // --- Relacionamentos (Assumindo Usuario, Cliente, etc. no mesmo pacote) ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    public Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advogado_responsavel_id")
    public Usuario advogadoResponsavel;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<ProcessoParticipante> participantes;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Evento> eventos;

    @OneToMany(mappedBy = "processo", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Files> arquivos;

    // --- Métodos Panache ---
    public static List<Processo> findByClienteId(Long clienteId) {
        return find("cliente.id = ?1 ORDER BY dataCriacao DESC", clienteId).list();
    }
    public static long countByStatus(ProcessoStatus status) {
        return count("status = ?1", status);
    }
    public boolean isAtrasado() {
        return prazoFinal != null && prazoFinal.isBefore(LocalDate.now());
    }
}