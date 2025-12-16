package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase; // MUDOU
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification extends PanacheEntityBase { // MUDOU

    @Id
    @SequenceGenerator(
            name = "notificationsSeq",
            sequenceName = "notifications_seq",
            allocationSize = 50 // <-- A CORREÇÃO
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notificationsSeq")
    public Long id; // <-- ID DEFINIDO MANUALMENTE

    @Column(nullable = false)
    public String mensagem;
    public boolean lida = false;
    public String link;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;

    @Column(name = "data_criacao", nullable = false)
    public LocalDateTime dataCriacao = LocalDateTime.now();
}