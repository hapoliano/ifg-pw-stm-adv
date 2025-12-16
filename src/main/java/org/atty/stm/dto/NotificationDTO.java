package org.atty.stm.dto;

public class NotificationDTO {

    private Long id;
    private Long usuarioId;
    private String titulo;
    private String mensagem;
    private String tipo;
    private boolean lido;
    private String dataCriacao;

    // --------------------------------------------------
    // GETTERS & SETTERS PADRÃO
    // --------------------------------------------------

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isLido() {
        return lido;
    }
    public void setLido(boolean lido) {
        this.lido = lido;
    }

    public String getDataCriacao() {
        return dataCriacao;
    }
    public void setDataCriacao(String dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}
