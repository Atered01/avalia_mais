package com.example.avalia.prova;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException; // Para o método open()
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.avalia.bancodedados.BancoDeDados;
import com.example.avalia.bancodedados.DatabaseContract;
import com.example.avalia.usuario.UsuarioController;
// import com.example.avalia.usuario.UsuarioController; // Já deve estar no mesmo pacote

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProvaController {

    private static final String TAG_LOG = "ProvaController";
    private BancoDeDados dbHelper;
    private SQLiteDatabase database;
    private Context context;

    public ProvaController(Context context) {
        this.context = context.getApplicationContext();
        dbHelper = new BancoDeDados(this.context);
    }

    // Abre a conexão com o banco de dados
    private void open() throws SQLException {
        try {
            database = dbHelper.getWritableDatabase();
            Log.d(TAG_LOG, "Banco de dados aberto para ProvaController.");
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao abrir o banco de dados em ProvaController: ", e);
            // Relança como SQLException para que o chamador possa tratar, se necessário
            throw new SQLException("Falha ao abrir banco de dados para ProvaController", e);
        }
    }

    // Fecha apenas a instância SQLiteDatabase, não o dbHelper
    private void close() {
        if (database != null && database.isOpen()) {
            database.close();
            Log.d(TAG_LOG, "Instância SQLiteDatabase fechada em ProvaController.");
        }
    }

    // --- MÉTODOS PARA PROVAS ---

    public long inserirProva(Prova prova) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ProvaEntry.COLUMN_NOME, prova.getNome());
        long id = -1;
        try {
            open(); // Abre a conexão
            id = database.insert(DatabaseContract.ProvaEntry.TABLE_NAME, null, values);
            if (id != -1) {
                Log.i(TAG_LOG, "Prova inserida com ID: " + id + " Nome: " + prova.getNome());
            } else {
                Log.e(TAG_LOG, "Falha ao inserir prova: " + prova.getNome());
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao inserir prova: " + prova.getNome(), e);
        } finally {
            close(); // Fecha a conexão
        }
        return id;
    }

    public List<Prova> getTodasProvas() {
        List<Prova> provas = new ArrayList<>();
        Cursor cursor = null;
        try {
            open(); // Abre a conexão
            cursor = database.query(
                    DatabaseContract.ProvaEntry.TABLE_NAME, null, null, null, null, null,
                    DatabaseContract.ProvaEntry.COLUMN_NOME + " ASC"
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(DatabaseContract.ProvaEntry._ID);
                    int nomeIndex = cursor.getColumnIndex(DatabaseContract.ProvaEntry.COLUMN_NOME);
                    if (idIndex != -1 && nomeIndex != -1) {
                        provas.add(new Prova(cursor.getInt(idIndex), cursor.getString(nomeIndex)));
                    }
                } while (cursor.moveToNext());
            } else {
                Log.i(TAG_LOG, "Nenhuma prova encontrada.");
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao buscar todas as provas.", e);
        } finally {
            if (cursor != null) cursor.close();
            close(); // Fecha a conexão
        }
        return provas;
    }

    public Prova getProvaPorId(int provaId) {
        Prova prova = null;
        Cursor cursor = null;
        try {
            open(); // Abre a conexão
            cursor = database.query(
                    DatabaseContract.ProvaEntry.TABLE_NAME, null,
                    DatabaseContract.ProvaEntry._ID + " = ?", new String[]{String.valueOf(provaId)},
                    null, null, null
            );

            if (cursor != null && cursor.moveToFirst()) {
                int nomeIndex = cursor.getColumnIndex(DatabaseContract.ProvaEntry.COLUMN_NOME);
                if (nomeIndex != -1) {
                    prova = new Prova(provaId, cursor.getString(nomeIndex));
                }
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao buscar prova por ID: " + provaId, e);
        } finally {
            if (cursor != null) cursor.close();
            close(); // Fecha a conexão
        }
        return prova;
    }

    // --- MÉTODOS PARA QUESTÕES ---

    public long inserirQuestao(Questao questao) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.QuestaoEntry.COLUMN_PROVA_ID, questao.getProvaId());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ENUNCIADO, questao.getEnunciado());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_A, questao.getAlternativaA());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_B, questao.getAlternativaB());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_C, questao.getAlternativaC());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_D, questao.getAlternativaD());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_E, questao.getAlternativaE());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_RESPOSTA_CORRETA, String.valueOf(questao.getRespostaCorreta()));
        long id = -1;
        try {
            open(); // Abre a conexão
            id = database.insert(DatabaseContract.QuestaoEntry.TABLE_NAME, null, values);
            if (id != -1) {
                Log.i(TAG_LOG, "Questão inserida ID: " + id + " para Prova ID: " + questao.getProvaId());
            } else {
                Log.e(TAG_LOG, "Falha ao inserir questão para Prova ID: " + questao.getProvaId());
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao inserir questão para Prova ID: " + questao.getProvaId(), e);
        } finally {
            close(); // Fecha a conexão
        }
        return id;
    }

    public List<Questao> getQuestoesDaProva(int provaId, int limite) {
        List<Questao> questoes = new ArrayList<>();
        Cursor cursor = null;
        try {
            open(); // Abre a conexão
            cursor = database.query(
                    DatabaseContract.QuestaoEntry.TABLE_NAME, null,
                    DatabaseContract.QuestaoEntry.COLUMN_PROVA_ID + " = ?", new String[]{String.valueOf(provaId)},
                    null, null, "RANDOM()", String.valueOf(limite)
            );

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int idIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry._ID);
                    int enunciadoIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ENUNCIADO);
                    int altAIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_A);
                    int altBIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_B);
                    int altCIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_C);
                    int altDIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_D);
                    int altEIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_E);
                    int respCorretaIndex = cursor.getColumnIndex(DatabaseContract.QuestaoEntry.COLUMN_RESPOSTA_CORRETA);

                    if (idIndex != -1 && enunciadoIndex != -1 && altAIndex != -1 && /* ... todos os outros ... */ respCorretaIndex != -1) {
                        String respCorretaStr = cursor.getString(respCorretaIndex);
                        questoes.add(new Questao(
                                cursor.getInt(idIndex), provaId, cursor.getString(enunciadoIndex),
                                cursor.getString(altAIndex), cursor.getString(altBIndex), cursor.getString(altCIndex),
                                cursor.getString(altDIndex), cursor.getString(altEIndex),
                                (!respCorretaStr.isEmpty()) ? respCorretaStr.charAt(0) : ' '
                        ));
                    }
                } while (cursor.moveToNext());
                Log.i(TAG_LOG, "Recuperadas " + questoes.size() + " questões para prova ID: " + provaId);
            } else {
                Log.i(TAG_LOG, "Nenhuma questão encontrada para prova ID: " + provaId);
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao buscar questões da prova ID: " + provaId, e);
        } finally {
            if (cursor != null) cursor.close();
            close(); // Fecha a conexão
        }
        return questoes;
    }

    // --- MÉTODOS PARA RESULTADOS ---
    public long salvarResultadoProva(ResultadoProva resultado) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_USUARIO_ID, resultado.getUsuarioId());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_PROVA_ID, resultado.getProvaId());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_ACERTOS, resultado.getAcertos());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_ERROS, resultado.getErros());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_TEMPO_GASTO_MS, resultado.getTempoGastoMs());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        values.put(DatabaseContract.ResultadoProvaEntry.COLUMN_DATA_REALIZACAO, sdf.format(new Date()));
        long idResultado = -1;

        try {
            open(); // Abre a conexão
            idResultado = database.insert(DatabaseContract.ResultadoProvaEntry.TABLE_NAME, null, values);
            if (idResultado != -1) {
                Log.i(TAG_LOG, "Resultado da prova salvo ID: " + idResultado + " para Usuário ID: " + resultado.getUsuarioId());
                int pontosGanhos = resultado.getAcertos() * 10; // Exemplo
                if (pontosGanhos > 0 && resultado.getUsuarioId() > 0) {
                    UsuarioController usuarioController = new UsuarioController(context);
                    // O UsuarioController.atualizarPontuacaoUsuario idealmente também seguiria
                    // o padrão de abrir/fechar sua própria conexão 'database'
                    boolean sucessoPontuacao = usuarioController.atualizarPontuacaoUsuario(resultado.getUsuarioId(), pontosGanhos);
                    if (sucessoPontuacao) {
                        Log.i(TAG_LOG, "Pontuação do Usuário ID " + resultado.getUsuarioId() + " atualizada. Adicionados: " + pontosGanhos);
                    } else {
                        Log.e(TAG_LOG, "Falha ao atualizar pontuação para Usuário ID " + resultado.getUsuarioId());
                    }
                }
            } else {
                Log.e(TAG_LOG, "Falha ao salvar resultado da prova para Usuário ID: " + resultado.getUsuarioId());
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao salvar resultado da prova para Usuário ID: " + resultado.getUsuarioId(), e);
        } finally {
            close(); // Fecha a conexão
        }
        return idResultado;
    }

    // --- MÉTODO PARA POPULAR DADOS INICIAIS (EXEMPLO) ---
    public void popularDadosIniciaisSeVazio() {
        Cursor cursorProvas = null;
        boolean provasVazias = true;
        try {
            open(); // Abre a conexão principal para este método
            cursorProvas = database.rawQuery("SELECT COUNT(*) FROM " + DatabaseContract.ProvaEntry.TABLE_NAME, null);
            if (cursorProvas != null && cursorProvas.moveToFirst()) {
                provasVazias = (cursorProvas.getInt(0) == 0);
            }
        } catch (Exception e) {
            Log.e(TAG_LOG, "Erro ao verificar se tabela de provas está vazia.", e);
            // Mesmo com erro, tentar fechar o DB se foi aberto
            close(); // Fecha em caso de exceção aqui
            return; // Retorna para não prosseguir se houve erro na verificação
        } finally {
            if (cursorProvas != null) {
                cursorProvas.close();
            }
            // Não fechar o 'database' aqui ainda se 'provasVazias' for true e sem erro, pois será usado abaixo.
            // O 'close()' final do método cuidará disso.
        }

        if (provasVazias) {
            Log.i(TAG_LOG, "Tabela de provas vazia. Populando com dados iniciais...");
            try {
                // Prova de Matemática
                Prova provaMat = new Prova("Matemática e suas Tecnologias");
                long idProvaMat = inserirProvaSemAbrirFechar(provaMat);
                if (idProvaMat != -1) {
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Uma loja de eletrônicos vendeu 15 smartphones e 10 tablets em um dia. Se cada smartphone custa R$ 1.200 e cada tablet R$ 850, qual foi o faturamento total com esses itens nesse dia?", "R$ 23.500", "R$ 25.000", "R$ 26.500", "R$ 20.500", "R$ 28.000", 'C'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Um terreno retangular tem 360 m² de área. Se um de seus lados mede 12 metros, qual o perímetro desse terreno?", "60 m", "72 m", "84 m", "96 m", "108 m", 'B')); // Lado = 30, Perímetro = 2*(12+30) = 84
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "João aplicou R$ 5.000 a uma taxa de juros simples de 2% ao mês. Qual será o montante após 6 meses de aplicação?", "R$ 5.500", "R$ 5.600", "R$ 6.000", "R$ 5.800", "R$ 6.200", 'B')); // J = 5000*0.02*6 = 600. M = 5600
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Em uma pesquisa com 800 estudantes sobre preferência de atividades extracurriculares, 45% escolheram esportes, 30% artes e o restante optou por clubes de leitura. Quantos estudantes optaram por clubes de leitura?", "200", "180", "240", "250", "150", 'A')); // 100-45-30 = 25%. 0.25*800 = 200
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Um carro consome, em média, 1 litro de gasolina para percorrer 14 km. Se o tanque do carro tem capacidade para 42 litros, qual a autonomia máxima do carro com o tanque cheio?", "588 km", "560 km", "602 km", "504 km", "590 km", 'A')); // 14 * 42 = 588
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Uma pesquisa sobre o tempo diário dedicado à leitura mostrou que, de 5 amigos, os tempos (em minutos) foram: 30, 45, 60, 45, 70. Qual a mediana desses tempos?", "45 min", "50 min", "52 min", "60 min", "48 min", 'A')); // Ordenado: 30, 45, 45, 60, 70. Mediana = 45
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Se o preço de uma camiseta aumentou de R$ 40,00 para R$ 50,00, qual foi o percentual de aumento?", "20%", "10%", "25%", "15%", "30%", 'C')); // ( (50-40)/40 ) * 100 = 25%
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Numa urna há 5 bolas azuis, 3 bolas vermelhas e 2 bolas verdes. Retirando-se uma bola ao acaso, qual a probabilidade de ela ser vermelha?", "3/10", "1/3", "2/5", "1/5", "3/7", 'A')); // 3 vermelhas / 10 total
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "Um mapa está na escala 1:500.000. Se a distância real entre duas cidades é de 75 km, qual será a distância entre elas no mapa, em centímetros?", "10 cm", "15 cm", "20 cm", "25 cm", "5 cm", 'B')); // 75km = 7.500.000 cm. 7.500.000 / 500.000 = 15 cm
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaMat, "O gráfico de uma função do tipo y = ax + b é uma reta. Se a > 0, a função é classificada como:", "Constante", "Decrescente", "Crescente", "Exponencial", "Quadrática", 'C'));
                    Log.i(TAG_LOG, "Prova de Matemática (ENEM) e 10 questões inseridas.");
                    Log.i(TAG_LOG, "Prova de Matemática e questões inseridas.");
                }

                // Prova de Linguagens
                Prova provaLing = new Prova("Linguagens, Códigos e suas Tecnologias");
                long idProvaLing = inserirProvaSemAbrirFechar(provaLing);
                if (idProvaLing != -1) {
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "Leia o trecho: 'A persistência é o caminho do êxito.' (Charles Chaplin). A palavra 'êxito', no contexto, significa:", "Início", "Fracasso", "Sucesso", "Meio", "Dificuldade", 'C'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "Na frase 'O debate sobre inteligência artificial avança rapidamente, mas *muitos temem* suas implicações éticas.', a expressão destacada indica uma relação de:", "Causa", "Consequência", "Adversidade", "Finalidade", "Condição", 'C'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "Identifique a figura de linguagem predominante no verso: 'Rios te correrão dos olhos, se chorares!' (Olavo Bilac)", "Metáfora", "Comparação", "Hipérbole", "Eufemismo", "Ironia", 'C'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "A norma culta da língua portuguesa prescreve o uso do pronome oblíquo átono antes do verbo (próclise) quando há, entre outros casos, a presença de palavras atrativas. Qual das alternativas abaixo exemplifica corretamente a próclise devido a uma palavra negativa?", "Entregaram-me o pacote.", "Nunca se arrependa do bem que fizer.", "Ele apresentar-se-á amanhã.", "Far-lhe-ei uma visita.", "Os alunos esforçaram-se muito.", 'B'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "No contexto da comunicação digital, o termo 'phishing' refere-se a uma prática que visa principalmente:", "Compartilhar notícias falsas para ganho político.", "Criar perfis falsos para interagir em redes sociais.", "Obter dados pessoais e financeiros de forma fraudulenta.", "Promover produtos ou serviços de maneira enganosa.", "Utilizar softwares para minerar criptomoedas.", 'C'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "Qual das seguintes características NÃO é típica do gênero textual 'notícia'?", "Objetividade na apresentação dos fatos.", "Uso predominante da linguagem denotativa.", "Presença de um narrador-personagem.", "Estrutura com título, lide e corpo do texto.", "Veiculação em jornais, revistas ou portais online.", 'C'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "A variação linguística que ocorre devido às diferenças regionais (geográficas) é chamada de:", "Variação histórica.", "Variação social ou diastrática.", "Variação situacional ou diafásica.", "Variação diatópica ou regional.", "Variação estilística.", 'D'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "Leia o trecho: 'A tecnologia, *se por um lado* conecta o mundo, *por outro* pode aprofundar o isolamento individual.' As expressões destacadas estabelecem uma relação de:", "Adição", "Conclusão", "Explicação", "Contraste/Oposição", "Alternância", 'D'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "Na oração 'Vende-se uma casa com urgência', o 'se' é classificado como:", "Partícula expletiva ou de realce.", "Pronome reflexivo.", "Índice de indeterminação do sujeito.", "Conjunção subordinativa condicional.", "Partícula apassivadora (pronome apassivador).", 'E'));
                    inserirQuestaoSemAbrirFechar(new Questao((int)idProvaLing, "O Modernismo no Brasil, especialmente em sua primeira fase (1922-1930), caracterizou-se pela:", "Valorização do academicismo e do rigor formal.", "Busca pela ruptura com padrões estéticos tradicionais e pela experimentação.", "Retomada dos ideais românticos e da expressão sentimental exacerbada.", "Influência do Parnasianismo e do Simbolismo europeus.", "Crítica social voltada exclusivamente para as elites agrárias.", 'B'));
                    Log.i(TAG_LOG, "Prova de Linguagens e questões inseridas.");
                }
                Log.i(TAG_LOG, "Dados iniciais de provas e questões populados.");
            } catch (Exception e) {
                Log.e(TAG_LOG, "Erro ao popular dados iniciais de provas e questões.", e);
            } finally {
                close(); // Fecha a conexão que foi aberta no início do método.
            }
        } else {
            Log.i(TAG_LOG, "Tabela de provas não está vazia. Nenhum dado inicial foi adicionado.");
            close(); // Fecha a conexão se não estava vazia e a conexão foi aberta para a verificação.
        }
    }

    // Métodos auxiliares (já operam em 'database' aberta pelo chamador)
    private long inserirProvaSemAbrirFechar(Prova prova) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.ProvaEntry.COLUMN_NOME, prova.getNome());
        return database.insert(DatabaseContract.ProvaEntry.TABLE_NAME, null, values);
    }

    private long inserirQuestaoSemAbrirFechar(Questao questao) {
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.QuestaoEntry.COLUMN_PROVA_ID, questao.getProvaId());
        // ... (resto das colunas da questão) ...
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ENUNCIADO, questao.getEnunciado());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_A, questao.getAlternativaA());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_B, questao.getAlternativaB());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_C, questao.getAlternativaC());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_D, questao.getAlternativaD());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_ALTERNATIVA_E, questao.getAlternativaE());
        values.put(DatabaseContract.QuestaoEntry.COLUMN_RESPOSTA_CORRETA, String.valueOf(questao.getRespostaCorreta()));
        return database.insert(DatabaseContract.QuestaoEntry.TABLE_NAME, null, values);
    }
}