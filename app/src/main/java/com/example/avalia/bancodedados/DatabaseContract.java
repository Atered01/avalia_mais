package com.example.avalia.bancodedados;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class MissaoEntry implements BaseColumns {
        public static final String TABLE_NAME = "missoes";
        public static final String COLUMN_NAME_ID_ORIGINAL = "id_original"; // ID da missão como definido em getListaDeMissoesPadrao
        public static final String COLUMN_NAME_DESCRICAO = "descricao";
        public static final String COLUMN_NAME_PONTOS = "pontos";
        public static final String COLUMN_NAME_CONCLUIDA = "concluida"; // Esta coluna pode ser depreciada ou usada para um status global se necessário
        public static final String COLUMN_NAME_AREA_CONHECIMENTO = "area_conhecimento";
        public static final String COLUMN_NAME_ICON_RESOURCE_ID = "icon_resource_id";
    }

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";
        public static final String COLUMN_NAME_NOME_COMPLETO = "nome_completo";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_SENHA_HASH = "senha_hash";
        public static final String COLUMN_NAME_DATA_CADASTRO = "data_cadastro";
        public static final String COLUMN_NAME_DATA_NASCIMENTO = "data_nascimento";
        public static final String COLUMN_NAME_CPF = "cpf";
        public static final String COLUMN_NAME_PONTUACAO_TOTAL = "pontuacao_total";
    }

    // NOVA TABELA PARA RASTREAR MISSÕES CONCLUÍDAS POR USUÁRIO
    public static class UsuarioMissaoEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuario_missoes";
        public static final String COLUMN_NAME_ID_USUARIO = "id_usuario"; // Foreign Key para UserEntry._ID
        public static final String COLUMN_NAME_ID_MISSAO_ORIGINAL = "id_missao_original"; // Foreign Key para MissaoEntry.COLUMN_NAME_ID_ORIGINAL
        public static final String COLUMN_NAME_DATA_CONCLUSAO = "data_conclusao"; // Data/Hora que a missão foi concluída

        // Para garantir que um usuário não possa concluir a mesma missão múltiplas vezes (a menos que seja permitido)
        // Pode-se adicionar uma constraint UNIQUE(id_usuario, id_missao_original)
    }

    public static class ProvaEntry implements BaseColumns {
        public static final String TABLE_NAME = "provas";
        public static final String COLUMN_NOME = "nome_prova";
        // Poderíamos adicionar COLUMN_TEMPO_LIMITE_MINUTOS INTEGER, se necessário
    }

    public static class QuestaoEntry implements BaseColumns {
        public static final String TABLE_NAME = "questoes";
        public static final String COLUMN_PROVA_ID = "prova_id"; // Chave estrangeira para ProvaEntry._ID
        public static final String COLUMN_ENUNCIADO = "enunciado";
        public static final String COLUMN_ALTERNATIVA_A = "alternativa_a";
        public static final String COLUMN_ALTERNATIVA_B = "alternativa_b";
        public static final String COLUMN_ALTERNATIVA_C = "alternativa_c";
        public static final String COLUMN_ALTERNATIVA_D = "alternativa_d";
        public static final String COLUMN_ALTERNATIVA_E = "alternativa_e";
        public static final String COLUMN_RESPOSTA_CORRETA = "resposta_correta"; // CHAR(1) -> 'A', 'B', 'C', 'D', 'E'
    }

    public static class ResultadoProvaEntry implements BaseColumns {
        public static final String TABLE_NAME = "resultados_provas";
        public static final String COLUMN_USUARIO_ID = "usuario_id"; // Chave estrangeira para UsuarioEntry._ID
        public static final String COLUMN_PROVA_ID = "prova_id";     // Chave estrangeira para ProvaEntry._ID
        public static final String COLUMN_ACERTOS = "acertos";
        public static final String COLUMN_ERROS = "erros";
        public static final String COLUMN_TEMPO_GASTO_MS = "tempo_gasto_ms";
        public static final String COLUMN_DATA_REALIZACAO = "data_realizacao"; // TEXT
    }

    // Comandos SQL para criar as tabelas (atualize com as novas)
// ... (definições de MissaoEntry, UserEntry, UsuarioMissaoEntry, ProvaEntry, QuestaoEntry, ResultadoProvaEntry permanecem como estão) ...

    // NOVO: Comando SQL para criar a tabela Provas
    static final String SQL_CREATE_PROVAS =
            "CREATE TABLE " + ProvaEntry.TABLE_NAME + " (" +
                    ProvaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ProvaEntry.COLUMN_NOME + " TEXT NOT NULL)";

    // NOVO: Comando SQL para criar a tabela Questoes
    static final String SQL_CREATE_QUESTOES =
            "CREATE TABLE " + QuestaoEntry.TABLE_NAME + " (" +
                    QuestaoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    QuestaoEntry.COLUMN_PROVA_ID + " INTEGER NOT NULL," +
                    QuestaoEntry.COLUMN_ENUNCIADO + " TEXT NOT NULL," +
                    QuestaoEntry.COLUMN_ALTERNATIVA_A + " TEXT," +
                    QuestaoEntry.COLUMN_ALTERNATIVA_B + " TEXT," +
                    QuestaoEntry.COLUMN_ALTERNATIVA_C + " TEXT," +
                    QuestaoEntry.COLUMN_ALTERNATIVA_D + " TEXT," +
                    QuestaoEntry.COLUMN_ALTERNATIVA_E + " TEXT," +
                    QuestaoEntry.COLUMN_RESPOSTA_CORRETA + " TEXT NOT NULL," +
                    "FOREIGN KEY(" + QuestaoEntry.COLUMN_PROVA_ID + ") REFERENCES " +
                    ProvaEntry.TABLE_NAME + "(" + ProvaEntry._ID + "))";

    // NOVO: Comando SQL para criar a tabela ResultadosProvas (COM A CORREÇÃO)
    static final String SQL_CREATE_RESULTADOS_PROVAS =
            "CREATE TABLE " + ResultadoProvaEntry.TABLE_NAME + " (" +
                    ResultadoProvaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ResultadoProvaEntry.COLUMN_USUARIO_ID + " INTEGER NOT NULL," +
                    ResultadoProvaEntry.COLUMN_PROVA_ID + " INTEGER NOT NULL," +
                    ResultadoProvaEntry.COLUMN_ACERTOS + " INTEGER NOT NULL," +
                    ResultadoProvaEntry.COLUMN_ERROS + " INTEGER NOT NULL," +
                    ResultadoProvaEntry.COLUMN_TEMPO_GASTO_MS + " INTEGER," +
                    ResultadoProvaEntry.COLUMN_DATA_REALIZACAO + " TEXT," +
                    "FOREIGN KEY(" + ResultadoProvaEntry.COLUMN_USUARIO_ID + ") REFERENCES " +
                    UserEntry.TABLE_NAME + "(" + UserEntry._ID + ")," + // Corrigido aqui
                    "FOREIGN KEY(" + ResultadoProvaEntry.COLUMN_PROVA_ID + ") REFERENCES " +
                    ProvaEntry.TABLE_NAME + "(" + ProvaEntry._ID + "))";


    // Comandos SQL para deletar as tabelas NOVAS (os de usuários e missões foram removidos)
    // NOVOS
    static final String SQL_DELETE_PROVAS =
            "DROP TABLE IF EXISTS " + ProvaEntry.TABLE_NAME;
    static final String SQL_DELETE_QUESTOES =
            "DROP TABLE IF EXISTS " + QuestaoEntry.TABLE_NAME;
    static final String SQL_DELETE_RESULTADOS_PROVAS =
            "DROP TABLE IF EXISTS " + ResultadoProvaEntry.TABLE_NAME;
}

// } // Fechamento da classe DatabaseContract (já estava lá)


