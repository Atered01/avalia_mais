package com.example.avalia.usuario;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class GerenciadorDeSessao { // Nome da classe está correto
    private static final String PREFS_NAME = "AppLoginPrefs";
    private static final String PREF_USER_LOGGED_IN_ID = "userLoggedInId";
    private static final String PREF_USER_EMAIL = "userEmail";
    private static final String PREF_USER_NOME = "userNome";
    private static final String PREF_USER_FOTO_URI = "userFotoUri"; // Chave para URI da foto
    private static final String PREF_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_CAMINHO_FOTO_PERFIL = "caminhoFotoPerfil";


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    // CORREÇÃO AQUI: O nome do construtor deve ser igual ao nome da classe
    public GerenciadorDeSessao(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void criarSessaoLogin(long id, String email, String nome) {
        editor.putBoolean(PREF_IS_LOGGED_IN, true);
        editor.putLong(PREF_USER_LOGGED_IN_ID, id);
        editor.putString(PREF_USER_EMAIL, email);
        editor.putString(PREF_USER_NOME, nome);
        editor.apply();
        Log.i("GerenciadorDeSessao", "Sessão criada para usuário ID: " + id);
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(PREF_IS_LOGGED_IN, false);
    }

    public long getUsuarioId() {
        return sharedPreferences.getLong(PREF_USER_LOGGED_IN_ID, -1);
    }

    public String getUsuarioEmail() {
        return sharedPreferences.getString(PREF_USER_EMAIL, null);
    }

    public String getUsuarioNome() {
        return sharedPreferences.getString(PREF_USER_NOME, null);
    }


    public void salvarCaminhoFotoPerfil(String caminhoArquivo) {
        editor.putString(KEY_CAMINHO_FOTO_PERFIL, caminhoArquivo);
        editor.apply();
    }

    public String getCaminhoFotoPerfil() {
        return sharedPreferences.getString(KEY_CAMINHO_FOTO_PERFIL, null);
    }
    public void salvarUriFotoPerfil(String uri) {
        editor.putString(PREF_USER_FOTO_URI + "_" + getUsuarioId(), uri);
        editor.apply();
    }

    public String getUriFotoPerfil() {
        return sharedPreferences.getString(PREF_USER_FOTO_URI + "_" + getUsuarioId(), null);
    }

    public void logoutUser() {
        long userId = getUsuarioId();
        String fotoKey = PREF_USER_FOTO_URI + "_" + userId;

        editor.clear();
        editor.putBoolean(PREF_IS_LOGGED_IN, false); // Garante que isLoggedIn retorne false
        editor.commit();
        Log.i("GerenciadorDeSessao", "Usuário deslogado e sessão limpa. Chave da foto possivelmente removida: " + fotoKey);
    }
}