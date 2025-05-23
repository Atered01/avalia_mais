package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.avalia.bancodedados.BancoDeDados;
import com.example.avalia.bancodedados.DatabaseContract;
import com.example.avalia.Usuario;


// NOME DA CLASSE ALTERADO PARA UsuarioController
public class UsuarioController {
    private SQLiteDatabase database;
    private com.example.avalia.bancodedados.BancoDeDados dbHelper; // MUDANÇA: Agora usa BancoDeDados (o SQLiteOpenHelper)
    private static final String TAG_LOG = "UsuarioController"; // Tag de Log atualizada

    public UsuarioController(Context context) {
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

    public long adicionarUsuario(String nomeCompleto, String email, String senhaNaoHasheada, String dataNascimento, String cpf) {
        ContentValues values = new ContentValues();
        // MUDANÇA: Chama o método estático de BancoDeDados
        String senhaHasheada = BancoDeDados.hashPassword(senhaNaoHasheada);
        if (senhaHasheada == null) {
            Log.e(TAG_LOG, "Falha ao gerar hash da senha para: " + email);
            return -1;
        }

        values.put(com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO, nomeCompleto);
        values.put(com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_EMAIL, email.toLowerCase());
        values.put(com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_SENHA_HASH, senhaHasheada);
        // MUDANÇA: Chama o método estático de BancoDeDados
        values.put(com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_DATA_CADASTRO, BancoDeDados.getCurrentDateTime());
        values.put(com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO, dataNascimento);
        values.put(com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_CPF, cpf);

        long newRowId = -1;
        try {
            newRowId = database.insertOrThrow(DatabaseContract.UserEntry.TABLE_NAME, null, values);
            Log.d(TAG_LOG, "Usuário adicionado com ID: " + newRowId + " Email: " + email);
        } catch (android.database.sqlite.SQLiteConstraintException e) {
            Log.e(TAG_LOG, "Erro ao adicionar usuário (email duplicado?): " + email, e);
        }
        return newRowId;
    }

    public boolean verificarEmailExistente(String email) {
        String[] projection = {DatabaseContract.UserEntry._ID};
        String selection = DatabaseContract.UserEntry.COLUMN_NAME_EMAIL + " = ?";
        String[] selectionArgs = {email.toLowerCase()};

        Cursor cursor = null;
        try {
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
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public Usuario verificarLogin(String email, String senhaNaoHasheada) {
        // MUDANÇA: Chama o método estático de BancoDeDados
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
                DatabaseContract.UserEntry.COLUMN_NAME_CPF
        };
        String selection = DatabaseContract.UserEntry.COLUMN_NAME_EMAIL + " = ? AND " +
                DatabaseContract.UserEntry.COLUMN_NAME_SENHA_HASH + " = ?";
        String[] selectionArgs = {email.toLowerCase(), senhaHasheada};

        Cursor cursor = null;
        Usuario usuario = null;
        try {
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
                usuario = new Usuario(id, nome, emailDb, dataNasc, cpfUser);
                Log.d(TAG_LOG, "Login bem-sucedido para: " + email);
            } else {
                Log.d(TAG_LOG, "Falha no login para: " + email);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return usuario;
    }
}