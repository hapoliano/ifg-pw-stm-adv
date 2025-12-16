package org.atty.stm.model.dto;

public class ClienteDTO {
    private String id;
    private String nome;
    private String email;
    private String telefone;
    private String cpfCnpj;
    private String endereco;
    private String cidade;
    private String estado;
    private String cep;
    private String status;
    private String dataCriacao;
    private String advogadoResponsavel;

    // Getters e Setters para todos os campos
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getAdvogadoResponsavel() { return advogadoResponsavel; }
    public void setAdvogadoResponsavel(String advogadoResponsavel) { this.advogadoResponsavel = advogadoResponsavel; }
}