package com.example.avalia.prova;

public class Prova {
    private int id;
    private String nome;
    // Poderíamos adicionar outros campos como 'descricao', 'areaConhecimento', etc. no futuro.

    public Prova(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    // Construtor sem ID, útil para quando o banco de dados gera o ID automaticamente
    public Prova(String nome) {
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // É útil ter o toString para debugar e para usar em Spinners/ListViews simples
    @Override
    public String toString() {
        return nome; // Ou "Prova: " + nome + " (ID: " + id + ")"
    }
}