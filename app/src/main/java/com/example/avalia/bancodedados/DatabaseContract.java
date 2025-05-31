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
}