package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase; // MUDOU
import jakarta.persistence.*;

@Entity
@Table(name = "processo_participantes")
public class ProcessoParticipante extends PanacheEntityBase { // MUDOU

    @Id
    @SequenceGenerator(
            name = "procParticipantesSeq",
            sequenceName = "processo_participantes_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "procParticipantesSeq")
    public Long id; // <-- ID DEFINIDO MANUALMENTE

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    public Processo processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;

    @Column(name = "papel_no_processo")
    public String papelNoProcesso;
}