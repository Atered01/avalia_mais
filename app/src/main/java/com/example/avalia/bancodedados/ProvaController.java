package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException; // Para o método open()
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.avalia.prova.Prova;
import com.example.avalia.prova.Questao;
import com.example.avalia.prova.ResultadoProva;
// import com.example.avalia.bancodedados.UsuarioController; // Já deve estar no mesmo pacote

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProvaController {

    private static final String TAG_LOG = "ProvaController";
    private BancoDeDados dbHelper;
    private SQLiteDatabase database;
    private Context context;

    public ProvaController(Context context) {
        this.context = context.getApplicationContext();
        dbHelper = new BancoDeDados(this.context);
    }

    // Abre a conexão com o banco de dados
    private void open() throws SQLException {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d(TAG_LOG, "Banco de dados aberto para ProvaController.");
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao abrir o banco de dados em ProvaController: ", e);
            // Relança como SQLException para que o chamador possa tratar, se necessário
            throw new SQLException("Falha ao abrir banco de dados para ProvaController", e);
        }
    }

    // Fecha apenas a instância SQLiteDatabase, não o dbHelper
    private void close() {
        if (database != null && database.isOpen()) {
            database.close();
            Log.d(TAG_LOG, "Instância SQLiteDatabase fechada em ProvaController.");
        }
    }

    // --- MÉTODOS PARA PROVAS ---

    public long inserirProva(Prova prova) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ProvaEntry.COLUMN_NOME, prova.getNome());
        long id = -1;
        try {
            open(); // Abre a conexão
            id = database.insert(DatabaseContract.ProvaEntry.TABLE_NAME, null, values);
            if (id != -1) {
                Log.i(TAG_LOG, "Prova inserida com ID: " + id + " Nome: " + prova.getNome());
            } else {
                Log.e(TAG_LOG, "Falha ao inserir prova: " + prova.getNome());
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao inserir prova: " + prova.getNome(), e);
        } finally {
            close(); // Fecha a conexão
        }
        return id;
    }

    public List<Prova> getTodasProvas() {
        List<Prova> provas = new ArrayList<>();
        Cursor cursor = null;
        try {
            open(); // Abre a conexão
            cursor = database.query(
                    DatabaseContract.ProvaEntry.TABLE_NAME, null, null, null, null, null,
                    DatabaseContract.ProvaEntry.COLUMN_NOME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(DatabaseContract.ProvaEntry._ID);
                    int nomeIndex = cursor.getColumnIndex(DatabaseContract.ProvaEntry.COLUMN_NOME);
                    if (idIndex != -1 && nomeIndex != -1) {
                        provas.add(new Prova(cursor.getInt(idIndex), cursor.getString(nomeIndex)));
                    }
                } while (cursor.moveToNext());
            } else {
                Log.i(TAG_LOG, "Nenhuma prova encontrada.");
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao buscar todas as provas.", e);
        } finally {
            if (cursor != null) cursor.close();
            close(); // Fecha a conexão
        }
        return provas;
    }

    public Prova getProvaPorId(int provaId) {
        Prova prova = null;
        Cursor cursor = null;
        try {
            open(); // Abre a conexão
            cursor = database.query(
                    DatabaseContract.ProvaEntry.TABLE_NAME, null,
                    DatabaseContract.ProvaEntry._ID + " = ?", new String[]{String.valueOf(provaId)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int nomeIndex = cursor.getColumnIndex(DatabaseContract.ProvaEntry.COLUMN_NOME);
                if (nomeIndex != -1) {
                    prova = new Prova(provaId, cursor.getString(nomeIndex));
                }
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao buscar prova por ID: " + provaId, e);
        } finally {
            if (cursor != null) cursor.close();
            close(); // Fecha a conexão
        }
        return prova;
    }

    // --- MÉTODOS PARA QUESTÕES ---

    public long inserirQuestao(Questao questao) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.QuestaoEntry.COLUMN_PROVA_ID, questao.getProvaId());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ENUNCIADO, questao.getEnunciado());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_A, questao.getAlternativaA());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_B, questao.getAlternativaB());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_C, questao.getAlternativaC());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_D, questao.getAlternativaD());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_E, questao.getAlternativaE());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_RESPOSTA_CORRETA, String.valueOf(questao.getRespostaCorreta()));
        long id = -1;
        try {
            open(); // Abre a conexão
            id = database.insert(DatabaseContract.QuestaoEntry.TABLE_NAME, null, values);
            if (id != -1) {
                Log.i(TAG_LOG, "Questão inserida ID: " + id + " para Prova ID: " + questao.getProvaId());
            } else {
                Log.e(TAG_LOG, "Falha ao inserir questão para Prova ID: " + questao.getProvaId());
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao inserir questão para Prova ID: " + questao.getProvaId(), e);
        } finally {
            close(); // Fecha a conexão
        }
        return id;
    }

    public List<Questao> getQuestoesDaProva(int provaId, int limite) {
        List<Questao> questoes = new ArrayList<>();
        Cursor cursor = null;
        try {
            open(); // Abre a conexão
            cursor = database.query(
                    DatabaseContract.QuestaoEntry.TABLE_NAME, null,
                    DatabaseContract.QuestaoEntry.COLUMN_PROVA_ID + " = ?", new String[]{String.valueOf(provaId)},
                    null, null, "RANDOM()", String.valueOf(limite)
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry._ID);
                    int enunciadoIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ENUNCIADO);
                    int altAIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_A);
                    int altBIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_B);
                    int altCIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_C);
                    int altDIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_D);
                    int altEIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_E);
                    int respCorretaIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_RESPOSTA_CORRETA);

                    if (idIndex != -1 && enunciadoIndex != -1 && altAIndex != -1 && /* ... todos os outros ... */ respCorretaIndex != -1) {
                        String respCorretaStr = cursor.getString(respCorretaIndex);
                        questoes.add(new Questao(
                                cursor.getInt(idIndex), provaId, cursor.getString(enunciadoIndex),
                                cursor.getString(altAIndex), cursor.getString(altBIndex), cursor.getString(altCIndex),
                                cursor.getString(altDIndex), cursor.getString(altEIndex),
                                (!respCorretaStr.isEmpty()) ? respCorretaStr.charAt(0) : ' '
                        ));
                    }
                } while (cursor.moveToNext());
                Log.i(TAG_LOG, "Recuperadas " + questoes.size() + " questões para prova ID: " + provaId);
            } else {
                Log.i(TAG_LOG, "Nenhuma questão encontrada para prova ID: " + provaId);
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao buscar questões da prova ID: " + provaId, e);
        } finally {
            if (cursor != null) cursor.close();
            close(); // Fecha a conexão
        }
        return questoes;
    }

    // --- MÉTODOS PARA RESULTADOS ---
    public long salvarResultadoProva(ResultadoProva resultado) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_USUARIO_ID, resultado.getUsuarioId());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_PROVA_ID, resultado.getProvaId());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_ACERTOS, resultado.getAcertos());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_ERROS, resultado.getErros());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_TEMPO_GASTO_MS, resultado.getTempoGastoMs());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_DATA_REALIZACAO, sdf.format(new Date()));
        long idResultado = -1;

        try {
            open(); // Abre a conexão
            idResultado = database.insert(DatabaseContract.ResultadoProvaEntry.TABLE_NAME, null, values);
            if (idResultado != -1) {
                Log.i(TAG_LOG, "Resultado da prova salvo ID: " + idResultado + " para Usuário ID: " + resultado.getUsuarioId());
                int pontosGanhos = resultado.getAcertos() * 10; // Exemplo
                if (pontosGanhos > 0 && resultado.getUsuarioId() > 0) {
                    UsuarioController usuarioController = new UsuarioController(context);
                    // O UsuarioController.atualizarPontuacaoUsuario idealmente também seguiria
                    // o padrão de abrir/fechar sua própria conexão 'database'
                    boolean sucessoPontuacao = usuarioController.atualizarPontuacaoUsuario(resultado.getUsuarioId(), pontosGanhos);
                    if (sucessoPontuacao) {
                        Log.i(TAG_LOG, "Pontuação do Usuário ID " + resultado.getUsuarioId() + " atualizada. Adicionados: " + pontosGanhos);
                    } else {
                        Log.e(TAG_LOG, "Falha ao atualizar pontuação para Usuário ID " + resultado.getUsuarioId());
                    }
                }
            } else {
                Log.e(TAG_LOG, "Falha ao salvar resultado da prova para Usuário ID: " + resultado.getUsuarioId());
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao salvar resultado da prova para Usuário ID: " + resultado.getUsuarioId(), e);
        } finally {
            close(); // Fecha a conexão
        }
        return idResultado;
    }

    // --- MÉTODO PARA POPULAR DADOS INICIAIS (EXEMPLO) ---
    public void popularDadosIniciaisSeVazio() {
        Cursor cursorProvas = null;
        boolean provasVazias = true;
        try {
            open(); // Abre a conexão principal para este método
            cursorProvas = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseContract.ProvaEntry.TABLE_NAME, null);
            if (cursorProvas != null && cursorProvas.moveToFirst()) {
                provasVazias = (cursorProvas.getInt(0) == 0);
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao verificar se tabela de provas está vazia.", e);
            // Mesmo com erro, tentar fechar o DB se foi aberto
            close(); // Fecha em caso de exceção aqui
            return; // Retorna para não prosseguir se houve erro na verificação
        } finally {
            if (cursorProvas != null) {
                cursorProvas.close();
            }
            // Não fechar o 'database' aqui ainda se 'provasVazias' for true e sem erro, pois será usado abaixo.
            // O 'close()' final do método cuidará disso.
        }

        if (provasVazias) {
            Log.i(TAG_LOG, "Tabela de provas vazia. Populando com dados iniciais...");
            try {
                // Prova de Matemática
                Prova provaMat = new Prova("Matemática e suas Tecnologias");
                long idProvaMat = inserirProvaSemAbrirFechar(provaMat);
                if (idProvaMat != -1) {
                    // ... (inserir as 10 questões de matemática como antes) ...
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Quanto é 2 + 2?", "3", "4", "5", "6", "2", 'B'));
                    // ... (mais 9 questões) ...
                    Log.i(TAG_LOG, "Prova de Matemática e questões inseridas.");
                }

                // Prova de Linguagens
                Prova provaLing = new Prova("Linguagens, Códigos e suas Tecnologias");
                long idProvaLing = inserirProvaSemAbrirFechar(provaLing);
                if (idProvaLing != -1) {
                    // ... (inserir as 10 questões de linguagens como antes) ...
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "Qual o antônimo de 'rápido'?", "Veloz", "Lento", "Ágil", "Curto", "Forte", 'B'));
                    // ... (mais 9 questões) ...
                    Log.i(TAG_LOG, "Prova de Linguagens e questões inseridas.");
                }
                Log.i(TAG_LOG, "Dados iniciais de provas e questões populados.");
            } catch (Exception e) {
                Log.e(TAG_LOG, "Erro ao popular dados iniciais de provas e questões.", e);
            } finally {
                close(); // Fecha a conexão que foi aberta no início do método.
            }
        } else {
            Log.i(TAG_LOG, "Tabela de provas não está vazia. Nenhum dado inicial foi adicionado.");
            close(); // Fecha a conexão se não estava vazia e a conexão foi aberta para a verificação.
        }
    }

    // Métodos auxiliares (já operam em 'database' aberta pelo chamador)
    private long inserirProvaSemAbrirFechar(Prova prova) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ProvaEntry.COLUMN_NOME, prova.getNome());
        return database.insert(DatabaseContract.ProvaEntry.TABLE_NAME, null, values);
    }

    private long inserirQuestaoSemAbrirFechar(Questao questao) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.QuestaoEntry.COLUMN_PROVA_ID, questao.getProvaId());
        // ... (resto das colunas da questão) ...
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ENUNCIADO, questao.getEnunciado());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_A, questao.getAlternativaA());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_B, questao.getAlternativaB());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_C, questao.getAlternativaC());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_D, questao.getAlternativaD());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_E, questao.getAlternativaE());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_RESPOSTA_CORRETA, String.valueOf(questao.getRespostaCorreta()));
        return database.insert(DatabaseContract.QuestaoEntry.TABLE_NAME, null, values);
    }
}