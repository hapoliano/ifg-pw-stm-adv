package org.atty.stm.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
public class Usuario extends PanacheEntityBase {

    @Id
    @SequenceGenerator(
            name = "usuariosSeq",
            sequenceName = "usuarios_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "usuariosSeq")
    public Long id;

    @Column(unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public String nome;

    @Column(nullable = false)
    public String senha;

    @Column(nullable = false)
    public String perfil; // MASTER, ADVOGADO, CLIENTE

    public String telefone;
    public String endereco;
    public String cidade;

    public String oab;
    public String especialidade;
    @Column(columnDefinition = "TEXT")
    public String formacao;
    @Column(columnDefinition = "TEXT")
    public String experiencia;

    @Column(nullable = false)
    public boolean ativo = true;

    @Column(nullable = false)
    public boolean aprovado = false;

    @Column(name = "datacriacao", nullable = false)
    public LocalDateTime dataCriacao = LocalDateTime.now();

    @Column(name = "dataatualizacao", nullable = false)
    public LocalDateTime dataAtualizacao = LocalDateTime.now();

    @Column(name = "ultimo_acesso")
    public LocalDateTime ultimoAcesso;

    @PrePersist
    void onCreate() {
        this.dataCriacao = LocalDateTime.now();
        this.dataAtualizacao = LocalDateTime.now();
        if ("ADVOGADO".equals(this.perfil)) {
            this.aprovado = false;
        } else {
            this.aprovado = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.dataAtualizacao = LocalDateTime.now();
    }

    // Métodos de compatibilidade (Getters) ADICIONADO getNome()

    public Long getId() {
        return id;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getNome() { // NOVO MÉTODO
        return nome;
    }

    // Métodos de negócio

    public boolean isAdvogado() {
        return "ADVOGADO".equals(this.perfil);
    }

    public boolean isCliente() {
        return "CLIENTE".equals(this.perfil);
    }

    public boolean isMaster() {
        return "MASTER".equals(this.perfil);
    }

    // Métodos estáticos de Panache (Finders)

    public static Usuario findByEmailAndAtivo(String email) {
        if (email == null) return null;
        return find("LOWER(email) = ?1 AND ativo = true AND aprovado = true", email.toLowerCase().trim()).firstResult();
    }

    public static Usuario findByEmail(String email) {
        if (email == null) return null;
        return find("LOWER(email) = ?1", email.toLowerCase().trim()).firstResult();
    }

    public static long countByPerfil(String perfil) {
        return count("perfil = ?1 AND ativo = true", perfil);
    }

    public static List<Usuario> findAdvogadosPendentes() {
        return find("perfil = 'ADVOGADO' AND aprovado = false AND ativo = true").list();
    }
}