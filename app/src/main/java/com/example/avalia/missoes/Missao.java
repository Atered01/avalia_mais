package com.example.avalia.missoes; // Certifique-se de que o pacote está correto

public class Missao {
    private long dbId; // _ID do banco de dados SQLite
    private int idOriginal; // ID que usamos para identificar unicamente a missão (ex: da lista hardcoded)
    private String descricao;
    private int pontos;
    private boolean concluida;
    private String areaConhecimento;
    private int iconResourceId; // NOVO CAMPO: ID do recurso do drawable para o ícone da missão

    // Construtor completo: para criar um objeto Missao a partir dos dados do banco
    // ou quando todos os dados, incluindo o ícone, são conhecidos.
    public Missao(long dbId, int idOriginal, String descricao, int pontos, boolean concluida, String areaConhecimento, int iconResourceId) {
        this.dbId = dbId;
        this.idOriginal = idOriginal;
        this.descricao = descricao;
        this.pontos = pontos;
        this.concluida = concluida;
        this.areaConhecimento = areaConhecimento;
        this.iconResourceId = iconResourceId; // Atribui o ID do ícone
    }

    // Construtor simplificado: para criar novas missões para pré-popular o banco
    // (sem o dbId inicialmente, será gerado pelo DB, e o ícone é fornecido)
    public Missao(int idOriginal, String descricao, int pontos, String areaConhecimento, int iconResourceId) {
        this.idOriginal = idOriginal;
        this.descricao = descricao;
        this.pontos = pontos;
        this.areaConhecimento = areaConhecimento;
        this.iconResourceId = iconResourceId; // Atribui o ID do ícone
        this.concluida = false; // Padrão: missão começa como não concluída
        this.dbId = -1;         // Indica que ainda não foi salvo no DB ou ID do DB não é relevante neste ponto
    }


    // Getters
    public long getDbId() {
        return dbId;
    }

    public int getIdOriginal() {
        return idOriginal;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getPontos() {
        return pontos;
    }

    public boolean isConcluida() {
        return concluida;
    }

    public String getAreaConhecimento() {
        return areaConhecimento;
    }

    public int getIconResourceId() { // NOVO GETTER
        return iconResourceId;
    }

    // Setters
    public void setDbId(long dbId) {
        this.dbId = dbId;
    }

    public void setConcluida(boolean concluida) {
        this.concluida = concluida;
    }

    public void setAreaConhecimento(String areaConhecimento) {
        this.areaConhecimento = areaConhecimento;
    }

    public void setIconResourceId(int iconResourceId) { // NOVO SETTER
        this.iconResourceId = iconResourceId;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

    // (idOriginal geralmente não tem setter pois é um identificador fixo)
}