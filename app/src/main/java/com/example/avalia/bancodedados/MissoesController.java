package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


import com.example.avalia.bancodedados.DatabaseContract;
import com.example.avalia.Missao;

import java.util.ArrayList;
import java.util.List;


public class MissoesController {
    private SQLiteDatabase database;
    private com.example.avalia.bancodedados.BancoDeDados dbHelper; // MUDANÇA: Agora usa BancoDeDados (o SQLiteOpenHelper)
    private static final String TAG_LOG = "MissoesController"; // Tag de Log atualizada

    public MissoesController(Context context) {
        dbHelper = new com.example.avalia.bancodedados.BancoDeDados(context); // MUDANÇA: Instancia BancoDeDados
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        Log.d(TAG_LOG, "Banco de dados aberto para escrita.");
    }

    public void close() {
        dbHelper.close();
        Log.d(TAG_LOG, "Banco de dados fechado.");
    }

    public List<Missao> getMissoesPorArea(String areaConhecimento) {
        List<Missao> listaMissoes = new ArrayList<>();
        String[] projection = {
                DatabaseContract.MissaoEntry._ID,
                DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL,
                DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO,
                DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS,
                DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA,
                DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO
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
                int iconId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID));
                listaMissoes.add(new Missao(dbId, idOriginal, descricao, pontos, concluida, area, iconId));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG_LOG, "Carregadas " + listaMissoes.size() + " missões para a área: " + areaConhecimento);
        return listaMissoes;
    }

    public int updateStatusMissao(long dbIdMissao, boolean concluida) {
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