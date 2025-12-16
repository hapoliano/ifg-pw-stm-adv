package org.atty.stm.dto;

public class CadastroDTO {

    private String nome;
    private String email;
    private String senha;
    private String confirmarSenha; // CRÍTICO: Reintroduzido para validação
    private String tipoUsuario;

    // Dados Adicionais
    private String telefone;
    private String oab;

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getConfirmarSenha() { return confirmarSenha; }
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }

    public String getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(String tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getOab() { return oab; }
    public void setOab(String oab) { this.oab = oab; }


}