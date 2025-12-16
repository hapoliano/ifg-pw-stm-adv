package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase; // MUDOU
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
public class Files extends PanacheEntityBase { // MUDOU

    @Id
    @SequenceGenerator(
            name = "filesSeq",
            sequenceName = "files_seq",
            allocationSize = 50 // <-- A CORREÇÃO
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "filesSeq")
    public Long id; // <-- ID DEFINIDO MANUALMENTE

    @Column(name = "nome_arquivo", nullable = false)
    public String nomeArquivo;
    @Column(name = "caminho_arquivo", nullable = false)
    public String caminhoArquivo;
    @Column(name = "tipo_arquivo")
    public String tipoArquivo;
    public long tamanho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    public Processo processo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    public Usuario usuario;

    @Column(name = "data_upload", nullable = false)
    public LocalDateTime dataUpload = LocalDateTime.now();
}