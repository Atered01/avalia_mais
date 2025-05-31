package com.example.avalia.usuario;

public class Usuario {
    private long id;
    private String nomeCompleto;
    private String email;
    private String dataNascimento;
    private String cpf;
    private int pontuacaoTotal;


    // Construtor atualizado para incluir dataNascimento e cpf
    public Usuario(long id, String nomeCompleto, String email, String dataNascimento, String cpf, int pontuacaoTotal) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
        this.pontuacaoTotal = pontuacaoTotal;
    }

    // Getters
    public long getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getEmail() { return email; }
    public String getDataNascimento() { return dataNascimento; }
    public String getCpf() { return cpf; }
    public int getPontuacaoTotal() { return pontuacaoTotal; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public void setEmail(String email) { this.email = email; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setPontuacaoTotal(int pontuacaoTotal) { this.pontuacaoTotal = pontuacaoTotal; }
}