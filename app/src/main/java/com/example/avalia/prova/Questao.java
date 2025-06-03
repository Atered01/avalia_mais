package com.example.avalia.prova;

import java.io.Serializable;
import java.util.List;

public class Questao implements Serializable {
    private int id;
    private int provaId; // Chave estrangeira para a tabela Prova
    private String enunciado;
    private String alternativaA;
    private String alternativaB;
    private String alternativaC;
    private String alternativaD;
    private String alternativaE;
    private char respostaCorreta; // 'A', 'B', 'C', 'D', ou 'E'
    private static final long serialVersionUID = 1L;

    public Questao(int id, int provaId, String enunciado, String alternativaA, String alternativaB, String alternativaC, String alternativaD, String alternativaE, char respostaCorreta) {
        this.id = id;
        this.provaId = provaId;
        this.enunciado = enunciado;
        this.alternativaA = alternativaA;
        this.alternativaB = alternativaB;
        this.alternativaC = alternativaC;
        this.alternativaD = alternativaD;
        this.alternativaE = alternativaE;
        this.respostaCorreta = respostaCorreta;
    }

    // Construtor sem ID
    public Questao(int provaId, String enunciado, String alternativaA, String alternativaB, String alternativaC, String alternativaD, String alternativaE, char respostaCorreta) {
        this.provaId = provaId;
        this.enunciado = enunciado;
        this.alternativaA = alternativaA;
        this.alternativaB = alternativaB;
        this.alternativaC = alternativaC;
        this.alternativaD = alternativaD;
        this.alternativaE = alternativaE;
        this.respostaCorreta = respostaCorreta;
    }


    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProvaId() {
        return provaId;
    }

    public void setProvaId(int provaId) {
        this.provaId = provaId;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public String getAlternativaA() {
        return alternativaA;
    }

    public void setAlternativaA(String alternativaA) {
        this.alternativaA = alternativaA;
    }

    public String getAlternativaB() {
        return alternativaB;
    }

    public void setAlternativaB(String alternativaB) {
        this.alternativaB = alternativaB;
    }

    public String getAlternativaC() {
        return alternativaC;
    }

    public void setAlternativaC(String alternativaC) {
        this.alternativaC = alternativaC;
    }

    public String getAlternativaD() {
        return alternativaD;
    }

    public void setAlternativaD(String alternativaD) {
        this.alternativaD = alternativaD;
    }

    public String getAlternativaE() {
        return alternativaE;
    }

    public void setAlternativaE(String alternativaE) {
        this.alternativaE = alternativaE;
    }

    public char getRespostaCorreta() {
        return respostaCorreta;
    }

    public void setRespostaCorreta(char respostaCorreta) {
        this.respostaCorreta = respostaCorreta;
    }
}