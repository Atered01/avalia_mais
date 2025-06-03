package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException; // Mantido, pois open() pode lançá-la
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException; // open() no dbHelper pode lançar
import android.util.Log;

import com.example.avalia.missoes.Missao;

import java.util.ArrayList;
import java.util.List;

public class MissoesController {
    private SQLiteDatabase database;
    private BancoDeDados dbHelper;
    private static final String TAG = "MissoesController";
    // private Context context; // Se precisar do contexto para outras coisas

    public MissoesController(Context context) {
        // this.context = context.getApplicationContext();
        dbHelper = new BancoDeDados(context.getApplicationContext());
    }

    public void open() throws SQLException {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d(TAG, "Banco de dados aberto para MissoesController.");
        } catch (SQLiteException e) { // SQLiteException é uma boa captura aqui
            Log.e(TAG, "Erro ao abrir o banco de dados em MissoesController: ", e);
            throw new SQLException("Falha ao abrir banco de dados para MissoesController", e);
        }
    }

    // Método close() revisado para fechar apenas a 'database'
    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
            Log.d(TAG, "Instância SQLiteDatabase fechada em MissoesController.");
        }
        // Não fechar dbHelper.close() aqui para permitir que seja compartilhado
    }

    // isOpen() permanece útil se você quiser verificar externamente,
    // mas os métodos internos agora não dependem tanto dele ser chamado de fora.
    public boolean isOpen() {
        return database != null && database.isOpen();
    }

    public List<Missao> getMissoesPorAreaParaUsuario(String areaConhecimento, long idUsuarioLogado) {
        List<Missao> listaMissoes = new ArrayList<>();
        Cursor cursor = null;
        // Projeção como você tinha
        String[] projection = {
                DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL,
                DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO,
                DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS,
                DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO,
                DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID
        };
        String selection = DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO + " = ?";
        String[] selectionArgs = {areaConhecimento};

        try {
            open(); // Abre a conexão para esta operação

            cursor = database.query(
                    DatabaseContract.MissaoEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) { // Adicionado null check para cursor
                do {
                    int idOriginalIndex = cursor.getColumnIndex(DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL);
                    int descIndex = cursor.getColumnIndex(DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO);
                    int pontosIndex = cursor.getColumnIndex(DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS);
                    int areaIndex = cursor.getColumnIndex(DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO);
                    int iconIdIndex = cursor.getColumnIndex(DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID);

                    if (idOriginalIndex != -1 && descIndex != -1 && pontosIndex != -1 && areaIndex != -1 && iconIdIndex != -1) {
                        int idOriginal = cursor.getInt(idOriginalIndex);
                        String descricao = cursor.getString(descIndex);
                        int pontos = cursor.getInt(pontosIndex);
                        String area = cursor.getString(areaIndex);
                        int iconId = cursor.getInt(iconIdIndex);

                        Missao missao = new Missao(idOriginal, descricao, pontos, area, iconId);

                        if (idUsuarioLogado > 0) { // Verifica se o ID do usuário é válido
                            // Chamada para o método que também gerencia seu open/close
                            boolean concluidaPeloUsuario = isMissaoConcluidaPeloUsuario(idUsuarioLogado, idOriginal);
                            missao.setConcluida(concluidaPeloUsuario);
                        }
                        listaMissoes.add(missao);
                    } else {
                        Log.w(TAG, "Índice de coluna não encontrado ao buscar missões por área.");
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao buscar missões por área para usuário: " + areaConhecimento, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Fecha a conexão aberta para esta operação
        }
        Log.d(TAG, "Carregadas " + listaMissoes.size() + " missões para a área: " + areaConhecimento + " (Usuário: " + idUsuarioLogado + ")");
        return listaMissoes;
    }

    public boolean marcarMissaoComoConcluidaPeloUsuario(long idUsuario, int idMissaoOriginal) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO, idUsuario);
        values.put(DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL, idMissaoOriginal);
        values.put(DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_DATA_CONCLUSAO, BancoDeDados.getCurrentDateTime());
        long result = -1;

        try {
            open(); // Abre a conexão para esta operação
            result = database.insertWithOnConflict(
                    DatabaseContract.UsuarioMissaoEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
            );
            if (result != -1) {
                Log.i(TAG, "Missão " + idMissaoOriginal + " marcada como concluída para usuário " + idUsuario);
                return true;
            } else {
                Log.w(TAG, "Missão " + idMissaoOriginal + " já estava marcada ou erro ao marcar para usuário " + idUsuario);
                // Se já existia, CONFLICT_IGNORE retorna -1. Podemos verificar se está lá.
                return isMissaoConcluidaPeloUsuario(idUsuario, idMissaoOriginal); // Confirma
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao marcar missão " + idMissaoOriginal + " como concluída para usuário " + idUsuario, e);
            return false;
        } finally {
            close(); // Fecha a conexão aberta para esta operação
        }
    }

    public boolean desmarcarMissaoComoConcluidaPeloUsuario(long idUsuario, int idMissaoOriginal) {
        String whereClause = DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + " = ? AND " +
                DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + " = ?";
        String[] whereArgs = {String.valueOf(idUsuario), String.valueOf(idMissaoOriginal)};
        int rowsDeleted = 0;

        try {
            open(); // Abre a conexão para esta operação
            rowsDeleted = database.delete(
                    DatabaseContract.UsuarioMissaoEntry.TABLE_NAME,
                    whereClause,
                    whereArgs
            );
            if (rowsDeleted > 0) {
                Log.i(TAG, "Missão " + idMissaoOriginal + " desmarcada para usuário " + idUsuario);
                return true;
            } else {
                Log.w(TAG, "Missão " + idMissaoOriginal + " não estava marcada ou erro ao desmarcar para usuário " + idUsuario);
                return false; // Se não deletou nada, é porque não existia a combinação
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desmarcar missão " + idMissaoOriginal + " para usuário " + idUsuario, e);
            return false;
        } finally {
            close(); // Fecha a conexão aberta para esta operação
        }
    }

    public boolean isMissaoConcluidaPeloUsuario(long idUsuario, int idMissaoOriginal) {
        Cursor cursor = null;
        boolean concluida = false;
        try {
            open(); // Abre a conexão para esta operação
            String selection = DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + " = ? AND " +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + " = ?";
            String[] selectionArgs = {String.valueOf(idUsuario), String.valueOf(idMissaoOriginal)};

            cursor = database.query(
                    DatabaseContract.UsuarioMissaoEntry.TABLE_NAME,
                    new String[]{DatabaseContract.UsuarioMissaoEntry._ID},
                    selection, selectionArgs, null, null, null, "1"
            );
            concluida = (cursor != null && cursor.getCount() > 0); // Adicionado null check para cursor
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar se missão " + idMissaoOriginal + " foi concluída pelo usuário " + idUsuario, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Fecha a conexão aberta para esta operação
        }
        return concluida;
    }

    public int getNumeroTotalDeMissoesNoSistema() {
        Cursor cursor = null;
        int count = 0;
        try {
            open(); // Abre a conexão para esta operação
            cursor = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseContract.MissaoEntry.TABLE_NAME, null);
            if (cursor != null && cursor.moveToFirst()) { // Adicionado null check para cursor
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar todas as missões do sistema", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Fecha a conexão aberta para esta operação
        }
        return count;
    }

    public int getNumeroMissoesConcluidasPeloUsuario(long idUsuario) {
        Cursor cursor = null;
        int count = 0;
        try {
            open(); // Abre a conexão para esta operação
            String selection = DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + " = ?";
            String[] selectionArgs = {String.valueOf(idUsuario)};
            cursor = database.query(
                    DatabaseContract.UsuarioMissaoEntry.TABLE_NAME,
                    new String[]{"COUNT(" + DatabaseContract.UsuarioMissaoEntry._ID + ")"},
                    selection, selectionArgs, null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) { // Adicionado null check para cursor
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar missões concluídas pelo usuário " + idUsuario, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Fecha a conexão aberta para esta operação
        }
        return count;
    }

    // Este método não acessa o banco diretamente, mas chama outros que agora gerenciam open/close.
    // Portanto, não precisa de open/close aqui.
    public int getNumeroMissoesPendentesParaUsuario(long idUsuario) {
        int totalMissoesNoSistema = getNumeroTotalDeMissoesNoSistema(); // Já gerencia open/close
        int missoesConcluidasPeloUsuario = getNumeroMissoesConcluidasPeloUsuario(idUsuario); // Já gerencia open/close
        int pendentes = totalMissoesNoSistema - missoesConcluidasPeloUsuario;
        Log.d(TAG, "Missões pendentes para usuário " + idUsuario + ": " + pendentes +
                " (Total: " + totalMissoesNoSistema + ", Concluídas: " + missoesConcluidasPeloUsuario + ")");
        return Math.max(0, pendentes);
    }

    // O método updateStatusMissao (global) foi comentado por você,
    // então não o incluirei aqui a menos que você queira refatorá-lo também.
}