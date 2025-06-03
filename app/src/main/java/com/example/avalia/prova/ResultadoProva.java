package com.example.avalia.prova;

import java.io.Serializable;

public class ResultadoProva  implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private int usuarioId;
    private int provaId;
    private int acertos;
    private int erros;
    private long tempoGastoMs; // Tempo em milissegundos
    private String dataRealizacao; // Data no formato YYYY-MM-DD HH:MM:SS

    public ResultadoProva(int usuarioId, int provaId, int acertos, int erros, long tempoGastoMs, String dataRealizacao) {
        this.usuarioId = usuarioId;
        this.provaId = provaId;
        this.acertos = acertos;
        this.erros = erros;
        this.tempoGastoMs = tempoGastoMs;
        this.dataRealizacao = dataRealizacao;
    }

    public ResultadoProva(int id, int usuarioId, int provaId, int acertos, int erros, long tempoGastoMs, String dataRealizacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.provaId = provaId;
        this.acertos = acertos;
        this.erros = erros;
        this.tempoGastoMs = tempoGastoMs;
        this.dataRealizacao = dataRealizacao;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }
    public int getProvaId() { return provaId; }
    public void setProvaId(int provaId) { this.provaId = provaId; }
    public int getAcertos() { return acertos; }
    public void setAcertos(int acertos) { this.acertos = acertos; }
    public int getErros() { return erros; }
    public void setErros(int erros) { this.erros = erros; }
    public long getTempoGastoMs() { return tempoGastoMs; }
    public void setTempoGastoMs(long tempoGastoMs) { this.tempoGastoMs = tempoGastoMs; }
    public String getDataRealizacao() { return dataRealizacao; }
    public void setDataRealizacao(String dataRealizacao) { this.dataRealizacao = dataRealizacao; }
}