package com.example.avalia.chatbot;

public class MensagemChat {
    public static final int TIPO_USUARIO = 0;
    public static final int TIPO_BOT = 1;

    private String texto;
    private int tipoMensagem;
    private long timestamp; // Opcional: para ordenar ou exibir a hora da mensagem

    public MensagemChat(String texto, int tipoMensagem) {
        this.texto = texto;
        this.tipoMensagem = tipoMensagem;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTexto() {
        return texto;
    }

    public int getTipoMensagem() {
        return tipoMensagem;
    }

    public long getTimestamp() {
        return timestamp;
    }
}