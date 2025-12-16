package org.atty.stm.dto;

public class DashboardDTO {
    private String nomeUsuario;
    private int totalProcessos;
    private int processosAtivos;
    private int totalClientes;
    private int compromissosHoje;

    // Getters e Setters
    public String getNomeUsuario() {
        return nomeUsuario;
    }
    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public int getTotalProcessos() {
        return totalProcessos;
    }
    public void setTotalProcessos(int totalProcessos) {
        this.totalProcessos = totalProcessos;
    }

    public int getProcessosAtivos() {
        return processosAtivos;
    }
    public void setProcessosAtivos(int processosAtivos) {
        this.processosAtivos = processosAtivos;
    }

    public int getTotalClientes() {
        return totalClientes;
    }
    public void setTotalClientes(int totalClientes) {
        this.totalClientes = totalClientes;
    }

    public int getCompromissosHoje() {
        return compromissosHoje;
    }
    public void setCompromissosHoje(int compromissosHoje) {
        this.compromissosHoje = compromissosHoje;
    }
}