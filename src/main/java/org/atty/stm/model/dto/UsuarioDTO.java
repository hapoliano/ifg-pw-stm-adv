package org.atty.stm.model.dto;

import java.time.LocalDateTime;

public class UsuarioDTO {
    private Long id;
    private String email;
    private String nome;
    private String senha;
    private String perfil; // MASTER, ADVOGADO, CLIENTE

    // Profissionais
    private String oab;
    private String especialidades;
    private String formacao;
    private String experiencia;

    // Contato
    private String telefone;
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;

    // Status
    private Boolean ativo;
    private Boolean aprovado;

    // Auditoria
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;
    private LocalDateTime ultimoAcesso;

    // Construtores
    public UsuarioDTO() {
    }

    public UsuarioDTO(String nome, String email, String senha, String perfil) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
        this.ativo = true;
        this.aprovado = "MASTER".equals(perfil); // Masters são aprovados automaticamente
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }

    public String getOab() { return oab; }
    public void setOab(String oab) { this.oab = oab; }

    public String getEspecialidades() { return especialidades; }
    public void setEspecialidades(String especialidades) { this.especialidades = especialidades; }

    public String getFormacao() { return formacao; }
    public void setFormacao(String formacao) { this.formacao = formacao; }

    public String getExperiencia() { return experiencia; }
    public void setExperiencia(String experiencia) { this.experiencia = experiencia; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Boolean getAprovado() { return aprovado; }
    public void setAprovado(Boolean aprovado) { this.aprovado = aprovado; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(LocalDateTime dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }

    public LocalDateTime getUltimoAcesso() { return ultimoAcesso; }
    public void setUltimoAcesso(LocalDateTime ultimoAcesso) { this.ultimoAcesso = ultimoAcesso; }

    // Métodos auxiliares para verificar tipo
    public boolean isAdvogado() {
        return "ADVOGADO".equals(perfil);
    }

    public boolean isCliente() {
        return "CLIENTE".equals(perfil);
    }

    public boolean isMaster() {
        return "MASTER".equals(perfil);
    }

    // Métodos auxiliares para validação
    public boolean isValidForCreation() {
        return nome != null && !nome.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                senha != null && !senha.trim().isEmpty() &&
                perfil != null && !perfil.trim().isEmpty();
    }

    public boolean isValidForUpdate() {
        return id != null &&
                nome != null && !nome.trim().isEmpty() &&
                email != null && !email.trim().isEmpty() &&
                perfil != null && !perfil.trim().isEmpty();
    }

    // Método para limpar dados sensíveis antes de enviar para o cliente
    public void clearSensitiveData() {
        this.senha = null;
    }

    @Override
    public String toString() {
        return "UsuarioDTO{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                ", perfil='" + perfil + '\'' +
                ", ativo=" + ativo +
                ", aprovado=" + aprovado +
                '}';
    }
}