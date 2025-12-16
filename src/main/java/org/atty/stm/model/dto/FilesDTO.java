package org.atty.stm.model.dto;

public class FilesDTO {

    private Long id;
    private String nome;
    private String tipo;
    private Long tamanho;
    private String url;
    private String dataUpload;
    private Long processoId;

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getTamanho() { return tamanho; }
    public void setTamanho(Long tamanho) { this.tamanho = tamanho; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getDataUpload() { return dataUpload; }
    public void setDataUpload(String dataUpload) { this.dataUpload = dataUpload; }

    public Long getProcessoId() { return processoId; }
    public void setProcessoId(Long processoId) { this.processoId = processoId; }
}
