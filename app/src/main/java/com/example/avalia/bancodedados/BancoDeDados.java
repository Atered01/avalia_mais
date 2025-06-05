package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.avalia.missoes.Missao; // Presumo que você tenha essa classe Missao

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BancoDeDados extends SQLiteOpenHelper {

    // INCREMENTE A VERSÃO DO BANCO DE DADOS POR CAUSA DAS NOVAS TABELAS DE PROVAS
    public static final int DATABASE_VERSION = 8; // <<<<<<< ATUALIZADO DE 7 PARA 8
    public static final String DATABASE_NAME = "MissoesApp.db"; // Mantendo o seu nome de BD
    private static final String TAG_LOG = "BancoDeDados";

    // Comandos SQL existentes (mantidos como no seu arquivo)
    private static final String SQL_CREATE_MISSOES_TABLE =
            "CREATE TABLE " + DatabaseContract.MissaoEntry.TABLE_NAME + " (" +
                    DatabaseContract.MissaoEntry._ID + " INTEGER PRIMARY KEY," + // AUTOINCREMENT pode ser útil aqui também
                    DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL + " INTEGER UNIQUE," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO + " TEXT," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS + " INTEGER," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA + " INTEGER," +
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

    private static final String SQL_CREATE_USUARIO_MISSOES_TABLE =
            "CREATE TABLE " + DatabaseContract.UsuarioMissaoEntry.TABLE_NAME + " (" +
                    DatabaseContract.UsuarioMissaoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + " INTEGER NOT NULL," +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + " INTEGER NOT NULL," +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_DATA_CONCLUSAO + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + ") REFERENCES " +
                    DatabaseContract.UserEntry.TABLE_NAME + "(" + DatabaseContract.UserEntry._ID + ")," +
                    "FOREIGN KEY(" + DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + ") REFERENCES " +
                    DatabaseContract.MissaoEntry.TABLE_NAME + "(" + DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL + ")," + // Ajuste se MissaoEntry._ID for a FK
                    "UNIQUE(" + DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_USUARIO + "," +
                    DatabaseContract.UsuarioMissaoEntry.COLUMN_NAME_ID_MISSAO_ORIGINAL + "))";

    // Comandos SQL para as NOVAS tabelas (vindos do seu DatabaseContract.java)
    // Nota: Se os comandos SQL CREATE já estão no DatabaseContract, você pode referenciá-los diretamente.
    // Ex: DatabaseContract.SQL_CREATE_PROVAS
    // Para clareza, vou copiá-los aqui, mas o ideal é que estejam apenas em um lugar (DatabaseContract).
    // Se você já os tem no DatabaseContract como `static final String`, use DatabaseContract.NOME_DO_COMANDO

    // Reutilizando os comandos que você já colocou no DatabaseContract.java
    // Não é necessário redeclará-los aqui se eles já são public static final no DatabaseContract.


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
        db.execSQL(SQL_CREATE_USUARIO_MISSOES_TABLE);
        Log.i(TAG_LOG, "Tabela UsuarioMissoes criada.");

        // CRIAR NOVAS TABELAS DE PROVAS
        db.execSQL(DatabaseContract.SQL_CREATE_PROVAS); // Usando o comando do seu DatabaseContract
        Log.i(TAG_LOG, "Tabela Provas criada.");
        db.execSQL(DatabaseContract.SQL_CREATE_QUESTOES); // Usando o comando do seu DatabaseContract
        Log.i(TAG_LOG, "Tabela Questoes criada.");
        db.execSQL(DatabaseContract.SQL_CREATE_RESULTADOS_PROVAS); // Usando o comando do seu DatabaseContract
        Log.i(TAG_LOG, "Tabela ResultadosProvas criada.");


        if (isTableEmpty(db, DatabaseContract.MissaoEntry.TABLE_NAME)) {
            inicializarMissoesPadrao(db);
        }
        // Você pode querer adicionar um método similar para inicializar provas/questões padrão aqui
        // exemplo: inicializarProvasPadrao(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG_LOG, "onUpgrade: Atualizando banco de dados da versão " + oldVersion + " para " + newVersion);

        // Lógica de upgrade incremental existente
        if (oldVersion < 2) {
            Log.d(TAG_LOG, "Upgrade de v" + oldVersion + ": Criando tabela Usuarios.");
            if (!tableExists(db, DatabaseContract.UserEntry.TABLE_NAME)) { // Adicionar verificação
                db.execSQL(SQL_CREATE_USUARIOS_TABLE);
            }
        }
        if (oldVersion < 3) {
            if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO)) {
                db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " ADD COLUMN " + DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO + " TEXT");
            }
            if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, DatabaseContract.UserEntry.COLUMN_NAME_CPF)) {
                db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " ADD COLUMN " + DatabaseContract.UserEntry.COLUMN_NAME_CPF + " TEXT");
            }
        }
        if (oldVersion < 4) {
            if (!columnExists(db, DatabaseContract.MissaoEntry.TABLE_NAME, DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID)) {
                db.execSQL("ALTER TABLE " + DatabaseContract.MissaoEntry.TABLE_NAME + " ADD COLUMN " + DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID + " INTEGER DEFAULT 0");
            }
        }
        if (oldVersion < 5) {
            if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL)) {
                db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " ADD COLUMN " + DatabaseContract.UserEntry.COLUMN_NAME_PONTUACAO_TOTAL + " INTEGER DEFAULT 0");
            }
        }
        if (oldVersion < 6) {
            Log.d(TAG_LOG, "Upgrade de v" + oldVersion + ": Criando tabela UsuarioMissoes.");
            if (!tableExists(db, DatabaseContract.UsuarioMissaoEntry.TABLE_NAME)) {
                db.execSQL(SQL_CREATE_USUARIO_MISSOES_TABLE);
                Log.i(TAG_LOG, "Tabela UsuarioMissoes criada durante o upgrade.");
            }
        }
        if (oldVersion < 7) {
            if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, "nova_coluna")) { // "nova_coluna" era um exemplo seu
                // db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " ADD COLUMN nova_coluna TEXT");
                // Log.i(TAG_LOG, "Coluna nova_coluna adicionada à tabela Usuarios.");
            }
        }

        // NOVO: Lógica de upgrade para a versão 8 (adicionar tabelas de Provas)
        if (oldVersion < 8) {
            Log.d(TAG_LOG, "Upgrade de v" + oldVersion + " para v8: Criando tabelas de Provas.");
            if (!tableExists(db, DatabaseContract.ProvaEntry.TABLE_NAME)) {
                db.execSQL(DatabaseContract.SQL_CREATE_PROVAS); // Usando o comando do seu DatabaseContract
                Log.i(TAG_LOG, "Tabela Provas criada durante o upgrade.");
            }
            if (!tableExists(db, DatabaseContract.QuestaoEntry.TABLE_NAME)) {
                db.execSQL(DatabaseContract.SQL_CREATE_QUESTOES); // Usando o comando do seu DatabaseContract
                Log.i(TAG_LOG, "Tabela Questoes criada durante o upgrade.");
            }
            if (!tableExists(db, DatabaseContract.ResultadoProvaEntry.TABLE_NAME)) {
                db.execSQL(DatabaseContract.SQL_CREATE_RESULTADOS_PROVAS); // Usando o comando do seu DatabaseContract
                Log.i(TAG_LOG, "Tabela ResultadosProvas criada durante o upgrade.");
            }
        }
    }

    // ... (métodos tableExists, columnExists, hashPassword, bytesToHex, getCurrentDateTime, isTableEmpty, inicializarMissoesPadrao, getListaDeMissoesPadrao como no seu arquivo) ...
    // Esses métodos auxiliares que você já tem são ótimos!

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
                if (nameColumnIndex == -1) {
                    cursor.close(); // Fechar cursor aqui também
                    return false;
                }
                while (cursor.moveToNext()) {
                    if (columnName.equals(cursor.getString(nameColumnIndex))) {
                        cursor.close(); // Fechar cursor assim que encontrar
                        return true;
                    }
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) { // Verificar se já não foi fechado
                cursor.close();
            }
        }
        return false;
    }

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
            if (cursor.moveToFirst()) { // Adicionar verificação antes de getInt
                isEmpty = (cursor.getInt(0) == 0);
            }
            cursor.close();
        }
        return isEmpty;
    }

    private void inicializarMissoesPadrao(SQLiteDatabase db) {
        Log.d(TAG_LOG, "Inicializando missões padrão...");
        List<Missao> missoesPadrao = getListaDeMissoesPadrao(); // Método que você já possui
        for (Missao missao : missoesPadrao) {
            ContentValues values = new ContentValues();
            // Assumindo que sua classe Missao tem os getters correspondentes
            // e que DatabaseContract.MissaoEntry tem as constantes corretas
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

    // Você já tem este método, apenas garantindo que ele está aqui para o contexto
    private List<Missao> getListaDeMissoesPadrao() {
        List<Missao> missoes = new ArrayList<>();
        int idCounter = 1;
        int placeholderIconId = 0; // Use um ID de recurso drawable real se tiver

        String areaMat = "Matemática e suas Tecnologias";
        missoes.add(new Missao(idCounter++, "Resolver 15 exercícios de Análise Combinatória.", 15, areaMat, placeholderIconId));
        // ... resto das suas missões padrão
        String areaHum = "Ciências Humanas e suas Tecnologias";
        missoes.add(new Missao(idCounter++, "Ler sobre os principais eventos da Guerra Fria.", 15, areaHum, placeholderIconId));
        String areaLing = "Linguagens, Códigos e suas Tecnologias";
        missoes.add(new Missao(idCounter++, "Interpretar 5 textos literários curtos.", 15, areaLing, placeholderIconId));
        String areaNat = "Ciências da Natureza e suas Tecnologias";
        missoes.add(new Missao(idCounter++, "Revisar os conceitos de Leis de Newton.", 15, areaNat, placeholderIconId));
        // Adicione mais missões conforme sua lista original
        return missoes;
    }
}