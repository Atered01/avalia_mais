package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.avalia.usuario.Usuario;

import java.util.ArrayList;
import java.util.List;


// NOME DA CLASSE ALTERADO PARA UsuarioController
public class UsuarioController {
    private SQLiteDatabase database;
    private BancoDeDados dbHelper;
    private static final String TAG_LOG = "UsuarioController";

    public UsuarioController(Context context) {
        dbHelper = new BancoDeDados(context);
    }

    public void open() throws SQLException { // Adicionado throws SQLException para consistência
        try {
            database = dbHelper.getWritableDatabase();
            Log.d(TAG_LOG, "Banco de dados aberto para escrita.");
        } catch (SQLiteException e) {
            Log.e(TAG_LOG, "Erro ao abrir o banco de dados em UsuarioController: ", e);
            throw e; // Relança a exceção para ser tratada por quem chamou
        }
    }

    public boolean isOpen() { // Método que havíamos discutido para TelaHome
        return database != null && database.isOpen();
    }

    public void close() {
        if (database != null && database.isOpen()) {
            database.close();
            Log.d(TAG_LOG, "Instância SQLiteDatabase fechada.");
        }
    }

    public long adicionarUsuario(String nomeCompleto, String email, String senhaNaoHasheada, String dataNascimento, String cpf) {
        // ... seu método adicionarUsuario (como antes)
        ContentValues values = new ContentValues();
        String senhaHasheada = BancoDeDados.hashPassword(senhaNaoHasheada);
        if (senhaHasheada == null) {
            Log.e(TAG_LOG, "Falha ao gerar hash da senha para: " + email);
            return -1;
        }

        values.put(DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO, nomeCompleto);
        values.put(DatabaseContract.UserEntry.COLUMN_NAME_EMAIL, email.toLowerCase());
        values.put(DatabaseContract.UserEntry.COLUMN_NAME_SENHA_HASH, senhaHasheada);
        values.put(DatabaseContract.UserEntry.COLUMN_NAME_DATA_CADASTRO, BancoDeDados.getCurrentDateTime());
        values.put(DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO, dataNascimento);
        values.put(DatabaseContract.UserEntry.COLUMN_NAME_CPF, cpf);
        values.put(DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL, 0);

        long newRowId = -1;
        try {
            if (!isOpen()) open();
            newRowId = database.insertOrThrow(DatabaseContract.UserEntry.TABLE_NAME, null, values);
            Log.d(TAG_LOG, "Usuário adicionado com ID: " + newRowId + " Email: " + email);
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Log.e(TAG_LOG, "Erro ao adicionar usuário (email duplicado?): " + email, e);
        } catch (SQLException e) {
            Log.e(TAG_LOG, "Erro de SQL ao adicionar usuário: " + email, e);
        }
        return newRowId;
    }

    public boolean verificarEmailExistente(String email) {
        // ... seu método verificarEmailExistente (como antes)
        String[] projection = {DatabaseContract.UserEntry._ID};
        String selection = DatabaseContract.UserEntry.COLUMN_NAME_EMAIL + " = ?";
        String[] selectionArgs = {email.toLowerCase()};
        Cursor cursor = null;
        try {
            if (!isOpen()) open();
            cursor = database.query(
                    DatabaseContract.UserEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );
            boolean existe = cursor.getCount() > 0;
            Log.d(TAG_LOG, "Email '" + email + "' existe? " + existe);
            return existe;
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao verificar email existente: " + email, e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Usuario verificarLogin(String email, String senhaNaoHasheada) {
        // ... seu método verificarLogin (como antes)
        String senhaHasheada = BancoDeDados.hashPassword(senhaNaoHasheada);
        if (senhaHasheada == null) {
            Log.e(TAG_LOG, "Falha ao gerar hash da senha para login: " + email);
            return null;
        }

        String[] projection = {
                DatabaseContract.UserEntry._ID,
                DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO,
                DatabaseContract.UserEntry.COLUMN_NAME_EMAIL,
                DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO,
                DatabaseContract.UserEntry.COLUMN_NAME_CPF,
                DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL
        };
        String selection = DatabaseContract.UserEntry.COLUMN_NAME_EMAIL + " = ? AND " +
                DatabaseContract.UserEntry.COLUMN_NAME_SENHA_HASH + " = ?";
        String[] selectionArgs = {email.toLowerCase(), senhaHasheada};

        Cursor cursor = null;
        Usuario usuario = null;
        try {
            if (!isOpen()) open();
            cursor = database.query(
                    DatabaseContract.UserEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry._ID));
                String nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO));
                String emailDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_EMAIL));
                String dataNasc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO));
                String cpfUser = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_CPF));
                int pontuacao = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL));
                usuario = new Usuario(id, nome, emailDb, dataNasc, cpfUser, pontuacao);
                Log.d(TAG_LOG, "Login bem-sucedido para: " + email);
            } else {
                Log.d(TAG_LOG, "Falha no login para: " + email);
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao verificar login para: " + email, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return usuario;
    }

    // MÉTODO NOVO (ou que deveria existir aqui)
    /**
     * Busca um usuário no banco de dados pelo seu ID.
     * @param usuarioId O ID do usuário a ser buscado.
     * @return Um objeto Usuario se encontrado, ou null caso contrário.
     */
    public Usuario getUsuarioPorId(long usuarioId) {
        if (usuarioId <= 0) { // IDs geralmente são positivos
            Log.w(TAG_LOG, "Tentativa de buscar usuário com ID inválido: " + usuarioId);
            return null;
        }

        String[] projection = {
                DatabaseContract.UserEntry._ID,
                DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO,
                DatabaseContract.UserEntry.COLUMN_NAME_EMAIL,
                DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO,
                DatabaseContract.UserEntry.COLUMN_NAME_CPF,
                DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL
        };
        String selection = DatabaseContract.UserEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(usuarioId)};

        Cursor cursor = null;
        Usuario usuario = null;

        try {
            open(); // Abre a conexão para esta operação

            cursor = database.query(
                    DatabaseContract.UserEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) { // Adicionado null check para cursor
                // Mapeamento seguro dos índices de coluna
                int idIndex = cursor.getColumnIndex(DatabaseContract.UserEntry._ID);
                int nomeIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO);
                int emailIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_EMAIL);
                int dataNascIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO);
                int cpfIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_CPF);
                int pontuacaoIndex = cursor.getColumnIndex(DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL);

                // Verificar se todos os índices são válidos
                if (idIndex != -1 && nomeIndex != -1 && emailIndex != -1 && dataNascIndex != -1 &&
                        cpfIndex != -1 && pontuacaoIndex != -1) {

                    long id = cursor.getLong(idIndex);
                    String nome = cursor.getString(nomeIndex);
                    String emailDb = cursor.getString(emailIndex);
                    String dataNasc = cursor.getString(dataNascIndex); // Pode ser null
                    String cpfUser = cursor.getString(cpfIndex);       // Pode ser null
                    int pontuacao = cursor.getInt(pontuacaoIndex);

                    // Assumindo que seu construtor Usuario pode lidar com dataNasc e cpfUser nulos se for o caso
                    usuario = new Usuario(id, nome, emailDb, dataNasc, cpfUser, pontuacao);
                    Log.d(TAG_LOG, "Usuário encontrado por ID: " + usuarioId + ", Nome: " + nome);
                } else {
                    Log.e(TAG_LOG, "Um ou mais índices de coluna não foram encontrados ao buscar usuário por ID: " + usuarioId);
                }
            } else {
                Log.w(TAG_LOG, "Nenhum usuário encontrado com o ID: " + usuarioId);
            }
        } catch (Exception e) { // Captura Exception genérica para robustez
            Log.e(TAG_LOG, "Erro ao buscar usuário por ID: " + usuarioId, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            close(); // Fecha a conexão SQLiteDatabase aberta para esta operação
        }
        return usuario;
    }
    public boolean atualizarPontuacaoUsuario(long usuarioId, int pontosParaAdicionar) {
        // ... seu método atualizarPontuacaoUsuario (como antes, mas com verificação de isOpen()) ...
        if (!isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG_LOG, "Erro ao abrir banco para atualizar pontuação", e);
                return false;
            }
        }

        Cursor cursor = null;
        int pontuacaoAtual = 0;
        try {
            cursor = database.query(DatabaseContract.UserEntry.TABLE_NAME,
                    new String[]{DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL},
                    DatabaseContract.UserEntry._ID + " = ?",
                    new String[]{String.valueOf(usuarioId)},
                    null, null, null);
            if (cursor.moveToFirst()) {
                pontuacaoAtual = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL));
            } else {
                Log.e(TAG_LOG, "Usuário com ID " + usuarioId + " não encontrado para atualizar pontuação.");
                return false;
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL, pontuacaoAtual + pontosParaAdicionar);

        String selection = DatabaseContract.UserEntry._ID + " = ?";
        String[] selectionArgs = {String.valueOf(usuarioId)};
        int count = 0;
        try {
            count = database.update(DatabaseContract.UserEntry.TABLE_NAME, values, selection, selectionArgs);
            if (count > 0) {
                Log.i(TAG_LOG, "Pontuação do usuário " + usuarioId + " atualizada. Adicionado: " + pontosParaAdicionar + ". Nova total: " + (pontuacaoAtual + pontosParaAdicionar));
            } else {
                Log.e(TAG_LOG, "Falha ao atualizar pontuação do usuário " + usuarioId);
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao atualizar pontuação do usuário " + usuarioId, e);
            return false;
        }
        return count > 0;
    }

    public List<Usuario> getUsuariosParaRanking(int limite) {
        // ... seu método getUsuariosParaRanking (como antes, mas com verificação de isOpen()) ...
        List<Usuario> listaUsuarios = new ArrayList<>();
        if (!isOpen()) {
            try {
                open();
            } catch (SQLException e) {
                Log.e(TAG_LOG, "Erro ao abrir banco para buscar ranking", e);
                return listaUsuarios;
            }
        }

        String[] projection = {
                DatabaseContract.UserEntry._ID,
                DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO,
                DatabaseContract.UserEntry.COLUMN_NAME_EMAIL,
                DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO,
                DatabaseContract.UserEntry.COLUMN_NAME_CPF,
                DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL
        };
        String orderBy = DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL + " DESC";
        String limitClause = (limite > 0) ? String.valueOf(limite) : null;
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseContract.UserEntry.TABLE_NAME, projection, null, null, null, null, orderBy, limitClause
            );
            while (cursor.moveToNext()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry._ID));
                String nome = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO));
                String emailDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_EMAIL));
                String dataNasc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO));
                String cpfUser = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_CPF));
                int pontuacao = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL));
                listaUsuarios.add(new Usuario(id, nome, emailDb, dataNasc, cpfUser, pontuacao));
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao carregar usuários para ranking", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        Log.d(TAG_LOG, "Carregados " + listaUsuarios.size() + " usuários para o ranking.");
        return listaUsuarios;
    }
}