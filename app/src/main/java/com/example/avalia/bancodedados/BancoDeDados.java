package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.avalia.missoes.Missao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BancoDeDados extends SQLiteOpenHelper {
    // INCREMENTE A VERSÃO DO BANCO DE DADOS POR CAUSA DA NOVA TABELA
    public static final int DATABASE_VERSION = 7; // <<<<<<< ATUALIZADO DE 5 PARA 6
    public static final String DATABASE_NAME = "MissoesApp.db";
    private static final String TAG_LOG = "BancoDeDados";

    // ... (SQL_CREATE_MISSOES_TABLE e SQL_CREATE_USUARIOS_TABLE como antes) ...
    private static final String SQL_CREATE_MISSOES_TABLE =
            "CREATE TABLE " + DatabaseContract.MissaoEntry.TABLE_NAME + " (" +
                    DatabaseContract.MissaoEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL + " INTEGER UNIQUE," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO + " TEXT," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS + " INTEGER," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA + " INTEGER," + // Pode ser repensado no futuro
                    DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO + " TEXT," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID + " INTEGER DEFAULT 0)";

    private static final String SQL_CREATE_USUARIOS_TABLE =
            "CREATE TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " (" +
                    DatabaseContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO + " TEXT NOT NULL," +
                    DatabaseContract.UserEntry.COLUMN_NAME_EMAIL + " TEXT UNIQUE NOT NULL," +
                    DatabaseContract.UserEntry.COLUMN_NAME_SENHA_HASH + " TEXT NOT NULL," +
                    DatabaseContract.UserEntry.COLUMN_NAME_DATA_CADASTRO + " TEXT," +
                    DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO + " TEXT," +
                    DatabaseContract.UserEntry.COLUMN_NAME_CPF + " TEXT," +
                    DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL + " INTEGER DEFAULT 0)";

    // SQL PARA CRIAR A NOVA TABELA USUARIO_MISSOES
    private static final String SQL_CREATE_USUARIO_MISSOES_TABLE =
            "CREATE TABLE " + DatabaseContract.UsuarioMissaoEntry.TABLE_NAME + " (" +
                    DatabaseContract.UsuarioMissaoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + " INTEGER NOT NULL," +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + " INTEGER NOT NULL," +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_DATA_CONCLUSAO + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + ") REFERENCES " +
                    DatabaseContract.UserEntry.TABLE_NAME + "(" + DatabaseContract.UserEntry._ID + ")," +
                    "FOREIGN KEY(" + DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + ") REFERENCES " +
                    DatabaseContract.MissaoEntry.TABLE_NAME + "(" + DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL + ")," +
                    "UNIQUE(" + DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + "," +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + "))";


    public BancoDeDados(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG_LOG, "onCreate: Criando tabelas...");
        db.execSQL(SQL_CREATE_MISSOES_TABLE);
        Log.i(TAG_LOG, "Tabela Missoes criada.");
        db.execSQL(SQL_CREATE_USUARIOS_TABLE);
        Log.i(TAG_LOG, "Tabela Usuarios criada.");
        db.execSQL(SQL_CREATE_USUARIO_MISSOES_TABLE); // <<<<<<< CRIAR NOVA TABELA
        Log.i(TAG_LOG, "Tabela UsuarioMissoes criada.");

        if (isTableEmpty(db, DatabaseContract.MissaoEntry.TABLE_NAME)) {
            inicializarMissoesPadrao(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG_LOG, "onUpgrade: Atualizando banco de dados da versão " + oldVersion + " para " + newVersion);

        if (oldVersion < 2) {
            Log.d(TAG_LOG, "Upgrade de v" + oldVersion + ": Criando tabela Usuarios.");
            db.execSQL(SQL_CREATE_USUARIOS_TABLE);
        }
        if (oldVersion < 3) {
            // ... lógica para adicionar data_nascimento e cpf (como antes)
            if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO)) {
                db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " ADD COLUMN " + DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO + " TEXT");
            }
            if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, DatabaseContract.UserEntry.COLUMN_NAME_CPF)) {
                db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " ADD COLUMN " + DatabaseContract.UserEntry.COLUMN_NAME_CPF + " TEXT");
            }
        }
        if (oldVersion < 4) {
            // ... lógica para adicionar icon_resource_id (como antes)
            if (!columnExists(db, DatabaseContract.MissaoEntry.TABLE_NAME, DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID)) {
                db.execSQL("ALTER TABLE " + DatabaseContract.MissaoEntry.TABLE_NAME + " ADD COLUMN " + DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID + " INTEGER DEFAULT 0");
            }
        }
        if (oldVersion < 5) {
            // ... lógica para adicionar pontuacao_total (como antes)
            if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL)) {
                db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " ADD COLUMN " + DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL + " INTEGER DEFAULT 0");
            }
        }
        // ADICIONAR NOVA TABELA SE ESTIVER ATUALIZANDO DE UMA VERSÃO ANTERIOR A 6
        if (oldVersion < 6) {
            Log.d(TAG_LOG, "Upgrade de v" + oldVersion + ": Criando tabela UsuarioMissoes.");
            if (!tableExists(db, DatabaseContract.UsuarioMissaoEntry.TABLE_NAME)) {
                db.execSQL(SQL_CREATE_USUARIO_MISSOES_TABLE);
                Log.i(TAG_LOG, "Tabela UsuarioMissoes criada durante o upgrade.");
            }
        }

        if (oldVersion < 7) { // Migração para a versão 7
            if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, "nova_coluna")) {
                try {
                    db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME +
                            " ADD COLUMN nova_coluna TEXT");
                    Log.i(TAG_LOG, "Coluna nova_coluna adicionada à tabela Usuarios.");
                } catch (Exception e) {
                    Log.e(TAG_LOG, "Erro ao adicionar coluna nova_coluna: " + e.getMessage());
                }
            }
        }
    }

    // ... (métodos tableExists, columnExists, hashPassword, bytesToHex, getCurrentDateTime, isTableEmpty, inicializarMissoesPadrao, getListaDeMissoesPadrao como antes) ...
    private boolean tableExists(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = ?", new String[]{tableName});
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                int nameColumnIndex = cursor.getColumnIndex("name");
                if (nameColumnIndex == -1) { return false; }
                while (cursor.moveToNext()) {
                    if (columnName.equals(cursor.getString(nameColumnIndex))) {
                        return true;
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }

    // ... (restante da classe BancoDeDados.java) ...
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG_LOG, "Erro ao hashear senha", e);
            return null;
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static String getCurrentDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private boolean isTableEmpty(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        boolean isEmpty = true;
        if (cursor != null) {
            cursor.moveToFirst();
            isEmpty = (cursor.getInt(0) == 0);
            cursor.close();
        }
        return isEmpty;
    }

    private void inicializarMissoesPadrao(SQLiteDatabase db) {
        Log.d(TAG_LOG, "Inicializando missões padrão...");
        List<Missao> missoesPadrao = getListaDeMissoesPadrao();
        for (Missao missao : missoesPadrao) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL, missao.getIdOriginal());
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO, missao.getDescricao());
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS, missao.getPontos());
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA, missao.isConcluida() ? 1 : 0);
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO, missao.getAreaConhecimento());
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID, missao.getIconResourceId());

            long resultado = db.insertWithOnConflict(DatabaseContract.MissaoEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (resultado != -1) {
                Log.d(TAG_LOG, "Missão padrão inserida: " + missao.getDescricao());
            } else {
                Log.w(TAG_LOG, "Falha ao inserir missão padrão (ou já existe): " + missao.getDescricao());
            }
        }
    }

    private List<Missao> getListaDeMissoesPadrao() {
        List<Missao> missoes = new ArrayList<>();
        int idCounter = 1;
        int placeholderIconId = 0;

        String areaMat = "Matemática e suas Tecnologias";
        missoes.add(new Missao(idCounter++, "Resolver 15 exercícios de Análise Combinatória.", 15, areaMat, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Estudar Funções Trigonométricas (Seno e Cosseno).", 20, areaMat, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Ler o capítulo sobre Geometria Espacial.", 10, areaMat, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Fazer um simulado rápido de 10 questões de Matemática.", 25, areaMat, placeholderIconId));

        String areaHum = "Ciências Humanas e suas Tecnologias";
        missoes.add(new Missao(idCounter++, "Ler sobre os principais eventos da Guerra Fria.", 15, areaHum, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Analisar 3 charges sobre política brasileira atual.", 10, areaHum, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Estudar os conceitos de Globalização e seus impactos.", 20, areaHum, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Revisar os principais filósofos do Iluminismo.", 15, areaHum, placeholderIconId));

        String areaLing = "Linguagens, Códigos e suas Tecnologias";
        missoes.add(new Missao(idCounter++, "Interpretar 5 textos literários curtos.", 15, areaLing, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Escrever uma redação no modelo ENEM (Introdução).", 20, areaLing, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Revisar as figuras de linguagem mais comuns.", 10, areaLing, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Analisar a estrutura de 2 notícias de jornal.", 10, areaLing, placeholderIconId));

        String areaNat = "Ciências da Natureza e suas Tecnologias";
        missoes.add(new Missao(idCounter++, "Revisar os conceitos de Leis de Newton.", 15, areaNat, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Estudar o ciclo do Carbono e do Nitrogênio.", 20, areaNat, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Resolver 10 questões de Estequiometria.", 15, areaNat, placeholderIconId));
        missoes.add(new Missao(idCounter++, "Ler sobre as principais fontes de energia renovável.", 10, areaNat, placeholderIconId));
        return missoes;
    }
}