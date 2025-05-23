package com.example.avalia.bancodedados; // Ou com.example.avalia.bancodedados se você moveu

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.avalia.Missao;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BancoDeDados extends SQLiteOpenHelper {
    // ATENÇÃO: Incrementar a versão do banco de dados para forçar o onUpgrade.
    // Se a última versão funcional era 3 (após adicionar CPF/DataNasc aos usuários),
    // esta DEVE ser 4.
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "MissoesApp.db";
    private static final String TAG_LOG = "BancoDeDados";

    // SQL PARA CRIAR A TABELA DE MISSÕES ATUALIZADA
    private static final String SQL_CREATE_MISSOES_TABLE =
            "CREATE TABLE " + DatabaseContract.MissaoEntry.TABLE_NAME + " (" +
                    DatabaseContract.MissaoEntry._ID + " INTEGER PRIMARY KEY," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL + " INTEGER UNIQUE," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO + " TEXT," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS + " INTEGER," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA + " INTEGER," +
                    DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO + " TEXT," + // Vírgula aqui
                    DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID + " INTEGER DEFAULT 0)"; // NOVA COLUNA com DEFAULT

    // SQL para criar a tabela de Usuários
    private static final String SQL_CREATE_USUARIOS_TABLE =
            "CREATE TABLE " + DatabaseContract.UserEntry.TABLE_NAME + " (" +
                    DatabaseContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO + " TEXT NOT NULL," +
                    DatabaseContract.UserEntry.COLUMN_NAME_EMAIL + " TEXT UNIQUE NOT NULL," +
                    DatabaseContract.UserEntry.COLUMN_NAME_SENHA_HASH + " TEXT NOT NULL," +
                    DatabaseContract.UserEntry.COLUMN_NAME_DATA_CADASTRO + " TEXT," +
                    DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO + " TEXT," +
                    DatabaseContract.UserEntry.COLUMN_NAME_CPF + " TEXT)";

    public BancoDeDados(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG_LOG, "onCreate: Criando tabelas...");
        db.execSQL(SQL_CREATE_MISSOES_TABLE); // Cria a tabela Missoes com a nova coluna
        Log.i(TAG_LOG, "Tabela Missoes criada com SQL: " + SQL_CREATE_MISSOES_TABLE);
        db.execSQL(SQL_CREATE_USUARIOS_TABLE);
        Log.i(TAG_LOG, "Tabela Usuarios criada com SQL: " + SQL_CREATE_USUARIOS_TABLE);

        // Popula com missões padrão ao criar o BD pela primeira vez
        if (isTableEmpty(db, DatabaseContract.MissaoEntry.TABLE_NAME)) {
            inicializarMissoesPadrao(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG_LOG, "onUpgrade: Atualizando banco de dados da versão " + oldVersion + " para " + newVersion);

        // Adiciona a tabela Usuarios se ela não existia (veio de versão < 2)
        if (oldVersion < 2) {
            Log.d(TAG_LOG, "Upgrade de v" + oldVersion + ": Criando tabela Usuarios.");
            db.execSQL(SQL_CREATE_USUARIOS_TABLE);
        }

        // Adiciona colunas data_nascimento e cpf à tabela Usuarios se veio de versão < 3
        if (oldVersion < 3) {
            Log.d(TAG_LOG, "Upgrade de v" + oldVersion + ": Adicionando colunas data_nascimento e cpf à tabela Usuarios (se necessário).");
            try {
                // Tenta adicionar apenas se a versão for especificamente 2, ou se for <3 e a tabela já existir
                if (oldVersion == 2 || (oldVersion < 2 && tableExists(db, DatabaseContract.UserEntry.TABLE_NAME))) {
                    if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO)) {
                        db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME +
                                " ADD COLUMN " + DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO + " TEXT");
                        Log.i(TAG_LOG, "Coluna data_nascimento adicionada.");
                    }
                    if (!columnExists(db, DatabaseContract.UserEntry.TABLE_NAME, DatabaseContract.UserEntry.COLUMN_NAME_CPF)) {
                        db.execSQL("ALTER TABLE " + DatabaseContract.UserEntry.TABLE_NAME +
                                " ADD COLUMN " + DatabaseContract.UserEntry.COLUMN_NAME_CPF + " TEXT");
                        Log.i(TAG_LOG, "Coluna cpf adicionada.");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG_LOG, "Erro ao adicionar colunas data_nasc/cpf em Usuarios: " + e.getMessage());
            }
        }

        // Adiciona coluna icon_resource_id à tabela Missoes se veio de versão < 4
        if (oldVersion < 4) {
            Log.d(TAG_LOG, "Upgrade de v" + oldVersion + ": Adicionando coluna icon_resource_id à tabela Missoes (se necessário).");
            try {
                if (!columnExists(db, DatabaseContract.MissaoEntry.TABLE_NAME, DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID)) {
                    db.execSQL("ALTER TABLE " + DatabaseContract.MissaoEntry.TABLE_NAME +
                            " ADD COLUMN " + DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID + " INTEGER DEFAULT 0");
                    Log.i(TAG_LOG, "Coluna icon_resource_id adicionada à tabela Missoes.");
                } else {
                    Log.i(TAG_LOG, "Coluna icon_resource_id já existe na tabela Missoes.");
                }
            } catch (Exception e) {
                Log.e(TAG_LOG, "Erro ao adicionar coluna icon_resource_id: " + e.getMessage());
            }
        }
    }

    // Método auxiliar para verificar se uma tabela existe
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

    // Método auxiliar para verificar se uma coluna existe em uma tabela
    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = null;
        try {
            // PRAGMA table_info retorna uma linha por coluna da tabela
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null) {
                int nameColumnIndex = cursor.getColumnIndex("name");
                if (nameColumnIndex == -1) { // Coluna 'name' não encontrada na pragma, algo está errado
                    return false;
                }
                while (cursor.moveToNext()) {
                    if (columnName.equals(cursor.getString(nameColumnIndex))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao verificar se coluna existe: " + columnName + " em " + tableName, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return false;
    }


    // --- Métodos Utilitários ---
    public static String hashPassword(String password) {
        // ... (código como antes) ...
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
        // ... (código como antes) ...
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
        // ... (código como antes) ...
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date());
    }

    private boolean isTableEmpty(SQLiteDatabase db, String tableName) {
        // ... (código como antes) ...
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        boolean isEmpty = true;
        if (cursor != null) {
            cursor.moveToFirst();
            isEmpty = (cursor.getInt(0) == 0);
            cursor.close();
        }
        return isEmpty;
    }

    // ATUALIZADO para incluir o iconResourceId no ContentValues
    private void inicializarMissoesPadrao(SQLiteDatabase db) {
        Log.d(TAG_LOG, "Inicializando missões padrão...");
        List<Missao> missoesPadrao = getListaDeMissoesPadrao(); // Este método já deve passar o iconId
        for (Missao missao : missoesPadrao) {
            ContentValues values = new ContentValues();
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL, missao.getIdOriginal());
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO, missao.getDescricao());
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS, missao.getPontos());
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA, missao.isConcluida() ? 1 : 0);
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO, missao.getAreaConhecimento());
            // SALVANDO O ICON RESOURCE ID
            values.put(DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID, missao.getIconResourceId());

            long resultado = db.insertWithOnConflict(DatabaseContract.MissaoEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (resultado != -1) {
                Log.d(TAG_LOG, "Missão padrão inserida: " + missao.getDescricao());
            } else {
                Log.w(TAG_LOG, "Falha ao inserir missão padrão (ou já existe): " + missao.getDescricao());
            }
        }
    }

    // GARANTA QUE ESTE MÉTODO ESTÁ PASSANDO O ICON ID CORRETAMENTE
    private List<Missao> getListaDeMissoesPadrao() {
        List<Missao> missoes = new ArrayList<>();
        int idCounter = 1;
        // Defina um ID de drawable real para seu placeholder, ou use 0.
        // Ex: int placeholderIconId = R.drawable.ic_task_placeholder;
        // Se você não tiver um drawable específico ainda, use 0.
        // O MissoesAdapter tem um fallback para R.drawable.ic_task_placeholder
        int placeholderIconId = 0; // Ou R.drawable.seu_icone_padrao_para_missoes

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