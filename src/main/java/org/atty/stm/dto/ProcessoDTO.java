package org.atty.stm.dto;

public class ProcessoDTO {

    private Long id;

    private String numeroProcesso;
    private String titulo;
    private String descricao;

    private Long clienteId;
    private String cliente;

    private Long advogadoResponsavelId;
    private String advogadoResponsavel;

    private String status;
    private String tipo;
    private String area;
    private String tribunal;
    private String vara;
    private String comarca;

    private String prioridade;

    private Double valorCausa;
    private Double valorCondenacao;

    private String observacoes;

    private String dataAbertura;
    private String dataDistribuicao;
    private String dataConclusao;
    private String prazoFinal;

    private String dataCriacao;
    private String dataAtualizacao;

    // ---------------------------
    // GETTERS E SETTERS
    // ---------------------------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumeroProcesso() { return numeroProcesso; }
    public void setNumeroProcesso(String numeroProcesso) { this.numeroProcesso = numeroProcesso; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public Long getAdvogadoResponsavelId() { return advogadoResponsavelId; }
    public void setAdvogadoResponsavelId(Long advogadoResponsavelId) { this.advogadoResponsavelId = advogadoResponsavelId; }

    public String getAdvogadoResponsavel() { return advogadoResponsavel; }
    public void setAdvogadoResponsavel(String advogadoResponsavel) { this.advogadoResponsavel = advogadoResponsavel; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getTribunal() { return tribunal; }
    public void setTribunal(String tribunal) { this.tribunal = tribunal; }

    public String getVara() { return vara; }
    public void setVara(String vara) { this.vara = vara; }

    public String getComarca() { return comarca; }
    public void setComarca(String comarca) { this.comarca = comarca; }

    public String getPrioridade() { return prioridade; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }

    public Double getValorCausa() { return valorCausa; }
    public void setValorCausa(Double valorCausa) { this.valorCausa = valorCausa; }

    public Double getValorCondenacao() { return valorCondenacao; }
    public void setValorCondenacao(Double valorCondenacao) { this.valorCondenacao = valorCondenacao; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(String dataAbertura) { this.dataAbertura = dataAbertura; }

    public String getDataDistribuicao() { return dataDistribuicao; }
    public void setDataDistribuicao(String dataDistribuicao) { this.dataDistribuicao = dataDistribuicao; }

    public String getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(String dataConclusao) { this.dataConclusao = dataConclusao; }

    public String getPrazoFinal() { return prazoFinal; }
    public void setPrazoFinal(String prazoFinal) { this.prazoFinal = prazoFinal; }

    public String getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(String dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getDataAtualizacao() { return dataAtualizacao; }
    public void setDataAtualizacao(String dataAtualizacao) { this.dataAtualizacao = dataAtualizacao; }
}
