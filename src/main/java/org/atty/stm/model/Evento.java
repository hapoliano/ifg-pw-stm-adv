package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "eventos")
public class Evento extends PanacheEntityBase {

    @Id
    @SequenceGenerator(
            name = "eventosSeq",
            sequenceName = "eventos_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "eventosSeq")
    public Long id;

    @Column(nullable = false)
    public String titulo;

    @Column(columnDefinition = "TEXT")
    public String descricao;

    @Column(name = "data_inicio", nullable = false)
    public LocalDateTime dataInicio;

    @Column(name = "data_fim")
    public LocalDateTime dataFim;

    public String tipo;

    @Column(name = "cor")
    public String cor;

    // NOVO CAMPO: Se é evento de dia inteiro (allDay do DTO)
    @Column(name = "dia_inteiro")
    public boolean diaInteiro = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id")
    public Processo processo;

    @Column(name = "data_criacao")
    public LocalDateTime dataCriacao = LocalDateTime.now();

    // Métodos utilitários
    public static List<Evento> findByUsuarioId(Long usuarioId) {
        return find("usuario.id = ?1 ORDER BY dataInicio", usuarioId).list();
    }

    public static List<Evento> findEventosHoje(Long usuarioId) {
        LocalDateTime hoje = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime amanha = hoje.plusDays(1);

        return find("usuario.id = ?1 AND dataInicio >= ?2 AND dataInicio < ?3 ORDER BY dataInicio",
                usuarioId, hoje, amanha).list();
    }
}