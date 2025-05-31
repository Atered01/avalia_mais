package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.avalia.missoes.Missao; // Certifique-se que o import da sua classe Missao está correto

import java.util.ArrayList;
import java.util.List;

public class MissoesController {
    private SQLiteDatabase database;
    private BancoDeDados dbHelper;
    private static final String TAG = "MissoesController";

    public MissoesController(Context context) {
        dbHelper = new BancoDeDados(context);
    }

    public void open() throws SQLiteException { // SQLException é mais genérico que SQLiteException
        try {
            database = dbHelper.getWritableDatabase();
            Log.d(TAG, "Banco de dados aberto para MissoesController.");
        } catch (Exception e) { // Captura Exception mais genérica para robustez
            Log.e(TAG, "Erro ao abrir o banco de dados em MissoesController: ", e);
            // Relançar como uma exceção específica do app ou tratar aqui
            throw new SQLException("Falha ao abrir banco de dados para MissoesController", e);
        }
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
            Log.d(TAG, "Banco de dados fechado para MissoesController.");
        }
        if (database != null && database.isOpen()) {
            database.close(); // Garante que a instância do database também seja fechada
            Log.d(TAG, "Instância SQLiteDatabase fechada em MissoesController.");
        }
    }

    public boolean isOpen() {
        return database != null && database.isOpen();
    }

    /**
     * Busca missões por área de conhecimento.
     * Para cada missão, verifica se o usuário especificado já a concluiu.
     * @param areaConhecimento A área de conhecimento para filtrar as missões.
     * @param idUsuarioLogado O ID do usuário logado, para verificar o status de conclusão.
     * Passe -1 ou um valor inválido se não quiser verificar por usuário.
     * @return Lista de missões da área, com o status de conclusão definido para o usuário.
     */
    public List<Missao> getMissoesPorAreaParaUsuario(String areaConhecimento, long idUsuarioLogado) {
        List<Missao> listaMissoes = new ArrayList<>();
        String[] projection = {
                // DatabaseContract.MissaoEntry._ID, // O _ID da tabela missoes
                DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL,
                DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO,
                DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS,
                // DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA, // Status global, pode ser útil para outros fins
                DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO,
                DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID
        };

        String selection = DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO + " = ?";
        String[] selectionArgs = {areaConhecimento};
        Cursor cursor = null;

        try {
            if (!isOpen()) open(); // Garante que o BD esteja aberto

            cursor = database.query(
                    DatabaseContract.MissaoEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            while (cursor.moveToNext()) {
                int idOriginal = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL));
                String descricao = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO));
                int pontos = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS));
                String area = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO));
                int iconId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID));

                // Cria a missão. O status 'concluida' aqui será específico do usuário.
                // Assumindo que o construtor de Missao que você usa é (int idOriginal, String descricao, int pontos, String area, int iconId)
                // e que Missao tem um setter como setConcluida(boolean) ou um construtor que aceita.
                // Vamos usar um construtor Missao(idOriginal, descricao, pontos, areaConhecimento, iconResourceId)
                // e depois definir se foi concluída pelo usuário.
                Missao missao = new Missao(idOriginal, descricao, pontos, area, iconId); // Ajuste o construtor conforme sua classe Missao

                // Verifica se esta missão foi concluída pelo usuário logado
                if (idUsuarioLogado != -1) { // -1 pode indicar que não há usuário logado ou não queremos checar
                    boolean concluidaPeloUsuario = isMissaoConcluidaPeloUsuario(idUsuarioLogado, idOriginal);
                    missao.setConcluida(concluidaPeloUsuario); // Assumindo que sua classe Missao tem um método setConcluida(boolean)
                    // ou que você pode passar isso no construtor.
                }
                listaMissoes.add(missao);
            }
        } catch (Exception e) { // Captura Exception mais genérica
            Log.e(TAG, "Erro ao buscar missões por área para usuário: " + areaConhecimento, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "Carregadas " + listaMissoes.size() + " missões para a área: " + areaConhecimento + " (Usuário: " + idUsuarioLogado + ")");
        return listaMissoes;
    }

    /**
     * Marca uma missão como concluída por um usuário específico na tabela usuario_missoes.
     * @param idUsuario O ID do usuário.
     * @param idMissaoOriginal O ID original da missão (da tabela missoes).
     * @return true se a inserção foi bem-sucedida, false caso contrário.
     */
    public boolean marcarMissaoComoConcluidaPeloUsuario(long idUsuario, int idMissaoOriginal) {
        if (!isOpen()) open();

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO, idUsuario);
        values.put(DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL, idMissaoOriginal);
        values.put(DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_DATA_CONCLUSAO, BancoDeDados.getCurrentDateTime());

        try {
            long result = database.insertWithOnConflict(
                    DatabaseContract.UsuarioMissaoEntry.TABLE_NAME,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE // Ignora se o par (id_usuario, id_missao_original) já existe
            );
            if (result != -1) {
                Log.i(TAG, "Missão " + idMissaoOriginal + " marcada como concluída para usuário " + idUsuario);
                return true;
            } else {
                Log.w(TAG, "Missão " + idMissaoOriginal + " já estava marcada como concluída para usuário " + idUsuario + " ou erro.");
                // Se CONFLICT_IGNORE, result é -1 se a linha já existe ou outro erro.
                // Para saber se já existia, você pode tentar um select antes ou verificar se o erro é de constraint.
                return isMissaoConcluidaPeloUsuario(idUsuario, idMissaoOriginal); // Confirma se realmente está lá
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao marcar missão " + idMissaoOriginal + " como concluída para usuário " + idUsuario, e);
            return false;
        }
    }

    public boolean desmarcarMissaoComoConcluidaPeloUsuario(long idUsuario, int idMissaoOriginal) {
        if (!isOpen()) open();

        String whereClause = DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + " = ? AND " +
                DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + " = ?";
        String[] whereArgs = {String.valueOf(idUsuario), String.valueOf(idMissaoOriginal)};

        try {
            int rowsDeleted = database.delete(
                    DatabaseContract.UsuarioMissaoEntry.TABLE_NAME,
                    whereClause,
                    whereArgs
            );
            if (rowsDeleted > 0) {
                Log.i(TAG, "Missão " + idMissaoOriginal + " desmarcada para usuário " + idUsuario);
                return true;
            } else {
                Log.w(TAG, "Missão " + idMissaoOriginal + " não estava marcada como concluída para usuário " + idUsuario + " ou erro ao desmarcar.");
                // Se a missão não estava marcada, a operação de delete não afeta linhas, mas não é um erro.
                return true; // Consideramos sucesso se não estava lá ou foi removida.
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao desmarcar missão " + idMissaoOriginal + " para usuário " + idUsuario, e);
            return false;
        }
    }

    /**
     * Verifica se uma missão específica foi concluída por um usuário.
     * @param idUsuario O ID do usuário.
     * @param idMissaoOriginal O ID original da missão.
     * @return true se a missão foi concluída pelo usuário, false caso contrário.
     */
    public boolean isMissaoConcluidaPeloUsuario(long idUsuario, int idMissaoOriginal) {
        if (!isOpen()) open();
        Cursor cursor = null;
        try {
            String selection = DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + " = ? AND " +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + " = ?";
            String[] selectionArgs = {String.valueOf(idUsuario), String.valueOf(idMissaoOriginal)};

            cursor = database.query(
                    DatabaseContract.UsuarioMissaoEntry.TABLE_NAME,
                    new String[]{DatabaseContract.UsuarioMissaoEntry._ID}, // Apenas verificar existência
                    selection, selectionArgs, null, null, null, "1" // Limita a 1 resultado
            );
            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar se missão " +idMissaoOriginal+ " foi concluída pelo usuário " + idUsuario, e);
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    /**
     * Obtém o número total de missões cadastradas no sistema (na tabela missoes).
     * @return O número total de missões.
     */
    public int getNumeroTotalDeMissoesNoSistema() {
        if (!isOpen()) open();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseContract.MissaoEntry.TABLE_NAME, null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar todas as missões do sistema", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    /**
     * Obtém o número de missões que um usuário específico concluiu (da tabela usuario_missoes).
     * @param idUsuario O ID do usuário.
     * @return O número de missões concluídas pelo usuário.
     */
    public int getNumeroMissoesConcluidasPeloUsuario(long idUsuario) {
        if (!isOpen()) open();
        Cursor cursor = null;
        try {
            String selection = DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + " = ?";
            String[] selectionArgs = {String.valueOf(idUsuario)};
            cursor = database.query(
                    DatabaseContract.UsuarioMissaoEntry.TABLE_NAME,
                    new String[]{"COUNT(" + DatabaseContract.UsuarioMissaoEntry._ID + ")"}, // Contar as entradas
                    selection, selectionArgs, null, null, null
            );
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar missões concluídas pelo usuário " + idUsuario, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    /**
     * Calcula o número de missões pendentes para um usuário específico.
     * @param idUsuario O ID do usuário.
     * @return O número de missões pendentes para o usuário.
     */
    public int getNumeroMissoesPendentesParaUsuario(long idUsuario) {
        int totalMissoesNoSistema = getNumeroTotalDeMissoesNoSistema();
        int missoesConcluidasPeloUsuario = getNumeroMissoesConcluidasPeloUsuario(idUsuario);
        int pendentes = totalMissoesNoSistema - missoesConcluidasPeloUsuario;
        Log.d(TAG, "Missões pendentes para usuário " + idUsuario + ": " + pendentes +
                " (Total: " + totalMissoesNoSistema + ", Concluídas: " + missoesConcluidasPeloUsuario + ")");
        return Math.max(0, pendentes); // Garante que não seja negativo
    }



    /*
    DAR A OPÇÃO DOS DOIS

     * O método abaixo, updateStatusMissao, que estava no seu MissoesController original,
     * atualiza um status GLOBAL da missão na tabela 'missoes'.
     * Se o seu objetivo é marcar uma missão como concluída APENAS para um usuário,
     * você deve usar o `marcarMissaoComoConcluidaPeloUsuario`.
     * Mantenha este método se você tem um caso de uso para marcar uma missão como globalmente
     * concluída/indisponível para todos. Caso contrário, para evitar confusão,
     * ele pode ser removido ou renomeado para algo como `updateStatusGlobalDaMissao`.
     * Por ora, vou comentá-lo para focar na lógica por usuário.

    public int updateStatusMissao(long dbIdMissao, boolean concluida) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA, concluida ? 1 : 0);

        String selection = DatabaseContract.MissaoEntry._ID + " = ?"; // Cuidado: _ID da tabela missoes, não id_original
        String[] selectionArgs = {String.valueOf(dbIdMissao)};

        int count = database.update(
                DatabaseContract.MissaoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);
        Log.d(TAG_LOG, "Status GLOBAL da Missão (dbID " + dbIdMissao + ") atualizado para concluida=" + concluida + ". Linhas afetadas: " + count);
        return count;
    }
    */
}