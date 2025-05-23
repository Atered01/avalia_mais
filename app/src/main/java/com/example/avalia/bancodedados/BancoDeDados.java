package com.example.avalia.bancodedados;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.avalia.bancodedados.DatabaseContract;
import com.example.avalia.Missao;
import com.example.avalia.R;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// NOME DA CLASSE ALTERADO PARA BancoDeDados
public class BancoDeDados extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "MissoesApp.db";
    private static final String TAG_LOG = "BancoDeDados"; // Tag de Log atualizada

    // SQL para criar a tabela de Missões
    private static final String SQL_CREATE_MISSOES_TABLE =
            "CREATE TABLE " + com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.TABLE_NAME + " (" +
                    com.example.avalia.bancodedados.DatabaseContract.MissaoEntry._ID + " INTEGER PRIMARY KEY," +
                    com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL + " INTEGER UNIQUE," +
                    com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO + " TEXT," +
                    com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS + " INTEGER," +
                    com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA + " INTEGER," +
                    com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO + " TEXT," +
                    com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_ICON_RESOURCE_ID + " INTEGER)";

    // SQL para criar a tabela de Usuários
    private static final String SQL_CREATE_USUARIOS_TABLE =
            "CREATE TABLE " + com.example.avalia.bancodedados.DatabaseContract.UserEntry.TABLE_NAME + " (" +
                    com.example.avalia.bancodedados.DatabaseContract.UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_NOME_COMPLETO + " TEXT NOT NULL," +
                    com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_EMAIL + " TEXT UNIQUE NOT NULL," +
                    com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_SENHA_HASH + " TEXT NOT NULL," +
                    com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_DATA_CADASTRO + " TEXT," +
                    com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO + " TEXT," +
                    com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_CPF + " TEXT)";

    // Construtor com o nome da classe atualizado
    public BancoDeDados(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG_LOG, "onCreate: Criando tabelas...");
        db.execSQL(SQL_CREATE_MISSOES_TABLE);
        Log.d(TAG_LOG, "Tabela Missoes criada.");
        db.execSQL(SQL_CREATE_USUARIOS_TABLE);
        Log.d(TAG_LOG, "Tabela Usuarios criada.");

        if (isTableEmpty(db, com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.TABLE_NAME)) {
            inicializarMissoesPadrao(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG_LOG, "onUpgrade: Atualizando banco de dados da versão " + oldVersion + " para " + newVersion);
        if (oldVersion < 2) {
            Log.d(TAG_LOG, "Versão antiga < 2, criando tabela Usuarios completa.");
            db.execSQL(SQL_CREATE_USUARIOS_TABLE);
        }
        if (oldVersion < 3 && oldVersion >= 2) {
            Log.d(TAG_LOG, "Versão antiga era 2, adicionando colunas data_nascimento e cpf à tabela Usuarios.");
            try {
                db.execSQL("ALTER TABLE " + com.example.avalia.bancodedados.DatabaseContract.UserEntry.TABLE_NAME +
                        " ADD COLUMN " + com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_DATA_NASCIMENTO + " TEXT");
                db.execSQL("ALTER TABLE " + com.example.avalia.bancodedados.DatabaseContract.UserEntry.TABLE_NAME +
                        " ADD COLUMN " + com.example.avalia.bancodedados.DatabaseContract.UserEntry.COLUMN_NAME_CPF + " TEXT");
                Log.d(TAG_LOG, "Colunas data_nascimento e cpf adicionadas com sucesso.");
            } catch (Exception e) {
                Log.e(TAG_LOG, "Erro ao adicionar colunas data_nascimento/cpf: " + e.getMessage());
            }
        }
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
            values.put(com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_ID_ORIGINAL, missao.getIdOriginal());
            values.put(com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_DESCRICAO, missao.getDescricao());
            values.put(com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_PONTOS, missao.getPontos());
            values.put(com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_CONCLUIDA, missao.isConcluida() ? 1 : 0);
            values.put(com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.COLUMN_NAME_AREA_CONHECIMENTO, missao.getAreaConhecimento());

            long resultado = db.insertWithOnConflict(com.example.avalia.bancodedados.DatabaseContract.MissaoEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            if (resultado != -1) {
                Log.d(TAG_LOG, "Missão padrão inserida: " + missao.getDescricao());
            }
        }
    }

    private List<Missao> getListaDeMissoesPadrao() {
        List<Missao> missoes = new ArrayList<>();
        int idCounter = 1;
        int placeholderIconId = R.drawable.ic_task_placeholder; // Defina o ID do seu ícone placeholder aqui
        // Certifique-se que ic_task_placeholder.xml existe em res/drawable

        String areaMat = "Matemática e suas Tecnologias";
        // Adicionando o placeholderIconId como o quinto argumento
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