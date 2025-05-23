package com.example.avalia;

public class Usuario {
    private long id; // _ID do banco de dados SQLite
    private String nomeCompleto;
    private String email;
    private String dataNascimento; // Campo para data de nascimento
    private String cpf;            // Campo para CPF

    // Construtor atualizado para incluir dataNascimento e cpf
    public Usuario(long id, String nomeCompleto, String email, String dataNascimento, String cpf) {
        this.id = id;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.dataNascimento = dataNascimento;
        this.cpf = cpf;
    }

    // Getters
    public long getId() { return id; }
    public String getNomeCompleto() { return nomeCompleto; }
    public String getEmail() { return email; }
    public String getDataNascimento() { return dataNascimento; }
    public String getCpf() { return cpf; }

    // Setters (se necessário no futuro)
    public void setId(long id) { this.id = id; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }
    public void setEmail(String email) { this.email = email; }
    public void setDataNascimento(String dataNascimento) { this.dataNascimento = dataNascimento; }
    public void setCpf(String cpf) { this.cpf = cpf; }
}