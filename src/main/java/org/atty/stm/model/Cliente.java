package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clientes")
public class Cliente extends PanacheEntityBase {

    @Id
    @SequenceGenerator(
            name = "clientesSeq",
            sequenceName = "clientes_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "clientesSeq")
    public Long id;

    @Column(nullable = false)
    public String nome;

    @Column(unique = true, nullable = false)
    public String email;

    public String telefone;

    @Column(name = "cpf_cnpj", unique = true)
    public String cpfCnpj;

    public String endereco;
    public String cidade;
    public String estado;
    public String cep;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advogado_responsavel_id")
    public Usuario advogadoResponsavel;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Processo> processos;

    @Column(name = "data_criacao")
    public LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "data_atualizacao")
    public LocalDateTime dataAtualizacao = LocalDateTime.now();

    public String status = "ATIVO";

    // Métodos estáticos
    public static Cliente findById(Long id) {
        return find("id", id).firstResult();
    }

    public static List<Cliente> listAll() {
        return list("ORDER BY nome");
    }

    public static Cliente findByUsuarioId(Long usuarioId) {
        return find("usuario.id = ?1", usuarioId).firstResult();
    }

    public static List<Cliente> findByAdvogadoId(Long advogadoId) {
        return find("advogadoResponsavel.id = ?1 ORDER BY nome", advogadoId).list();
    }

    public static Cliente findByEmail(String email) {
        return find("LOWER(email) = ?1", email.toLowerCase()).firstResult();
    }

    public static long countByAdvogado(Long advogadoId) {
        return count("advogadoResponsavel.id = ?1", advogadoId);
    }

    public static List<Cliente> findAtivosByAdvogado(Long advogadoId) {
        return find("advogadoResponsavel.id = ?1 AND status = 'ATIVO' ORDER BY nome", advogadoId).list();
    }

    // Método para persistir
    public void persist() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        PanacheEntityBase.persist(this);
    }
}