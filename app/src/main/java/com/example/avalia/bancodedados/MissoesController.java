package com.example.avalia.bancodedados; // Verifique se este é o pacote correto

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.avalia.missoes.Missao;

import java.util.ArrayList;
import java.util.List;

public class MissoesController {
    private SQLiteDatabase database;
    private BancoDeDados dbHelper; // Usa a classe BancoDeDados (seu SQLiteOpenHelper)
    private static final String TAG_LOG = "MissoesController";

    public MissoesController(Context context) {
        dbHelper = new BancoDeDados(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        Log.d(TAG_LOG, "Banco de dados aberto para escrita.");
    }

    public void close() {
        if (database != null && database.isOpen()) { // Adiciona verificação
            dbHelper.close(); // O SQLiteOpenHelper gerencia o fechamento real do SQLiteDatabase
            Log.d(TAG_LOG, "Banco de dados fechado.");
        }
    }

    public List<Missao> getMissoesPorArea(String areaConhecimento) {
        List<Missao> listaMissoes = new ArrayList<>();
        String[] projection = {
                DatabaseContract.MissaoEntry._ID,
                DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL,
                DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO,
                DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS,
                DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA,
                DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO,
                DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID // ADICIONAR À PROJEÇÃO
        };

        String selection = DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO + " = ?";
        String[] selectionArgs = {areaConhecimento};

        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseContract.MissaoEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            while (cursor.moveToNext()) {
                long dbId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry._ID));
                int idOriginal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL));
                String descricao = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO));
                int pontos = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS));
                boolean concluida = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA)) == 1;
                String area = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO));
                // LENDO O ICON RESOURCE ID DO CURSOR
                int iconId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID));

                // USANDO O CONSTRUTOR DE Missao COM 7 ARGUMENTOS
                listaMissoes.add(new Missao(dbId, idOriginal, descricao, pontos, concluida, area, iconId));
            }
        } catch (IllegalArgumentException e) {
            // Este catch é para o caso de getColumnIndexOrThrow falhar, como no seu erro original.
            Log.e(TAG_LOG, "Erro ao ler coluna do cursor: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG_LOG, "Carregadas " + listaMissoes.size() + " missões para a área: " + areaConhecimento);
        return listaMissoes;
    }

    public int updateStatusMissao(long dbIdMissao, boolean concluida) {
        // ... (sem mudanças neste metodo para o bug atual) ...
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA, concluida ? 1 : 0);

        String selection = DatabaseContract.MissaoEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(dbIdMissao)};

        int count = database.update(
                DatabaseContract.MissaoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        Log.d(TAG_LOG, "Missão " + dbIdMissao + " atualizada para concluida=" + concluida + ". Linhas afetadas: " + count);
        return count;
    }
}