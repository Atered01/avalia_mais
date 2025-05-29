package com.example.avalia.bancodedados;

import android.provider.BaseColumns;

public final class DatabaseContract {

    private DatabaseContract() {}

    public static class MissaoEntry implements BaseColumns {
        public static final String TABLE_NAME = "missoes";
        public static final String COLUMN_NAME_ID_ORIGINAL = "id_original";
        public static final String COLUMN_NAME_DESCRICAO = "descricao";
        public static final String COLUMN_NAME_PONTOS = "pontos";
        public static final String COLUMN_NAME_CONCLUIDA = "concluida";
        public static final String COLUMN_NAME_AREA_CONHECIMENTO = "area_conhecimento";
        public static final String COLUMN_NAME_ICON_RESOURCE_ID = "icon_resource_id"; // INTEGER
    }

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "usuarios";
        public static final String COLUMN_NAME_NOME_COMPLETO = "nome_completo";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_SENHA_HASH = "senha_hash";
        public static final String COLUMN_NAME_DATA_CADASTRO = "data_cadastro";
        public static final String COLUMN_NAME_DATA_NASCIMENTO = "data_nascimento";
        public static final String COLUMN_NAME_CPF = "cpf";
    }
}