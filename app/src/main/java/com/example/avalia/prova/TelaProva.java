package com.example.avalia.prova; // Ajuste este pacote para o seu

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avalia.R;
// Importe a ACTIVITY que vai exibir o resultado:
import com.example.avalia.usuario.GerenciadorDeSessao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TelaProva extends AppCompatActivity {

    private static final String TAG = "TelaProva";
    private static final long TEMPO_PROVA_MILISSEGUNDOS = 60 * 60 * 1000; // 60 minutos

    private TextView textViewNomeDaProva, textViewContadorQuestao, textViewCronometro, textViewEnunciadoQuestao;
    private RadioGroup radioGroupAlternativas;
    private RadioButton radioButtonAlternativaA, radioButtonAlternativaB, radioButtonAlternativaC, radioButtonAlternativaD, radioButtonAlternativaE;
    private Button buttonProximaFinalizar;

    private ProvaController provaController;
    private GerenciadorDeSessao gerenciadorDeSessao;

    private List<Questao> listaQuestoes;
    private int idProvaSelecionada;
    private String nomeProvaSelecionada;
    private int questaoAtualIndex = 0;
    private CountDownTimer countDownTimer;
    private long tempoRestanteMilisegundos = TEMPO_PROVA_MILISSEGUNDOS;

    private HashMap<Integer, Character> respostasUsuario;

    private static final String KEY_QUESTAO_ATUAL_INDEX = "questaoAtualIndex";
    private static final String KEY_TEMPO_RESTANTE = "tempoRestante";
    private static final String KEY_RESPOSTAS_USUARIO = "respostasUsuario";
    private static final String KEY_LISTA_QUESTOES = "listaQuestoes";
    private static final String KEY_ID_PROVA = "idProva";
    private static final String KEY_NOME_PROVA = "nomeProva";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_prova);

        inicializarViews();
        provaController = new ProvaController(this);
        gerenciadorDeSessao = new GerenciadorDeSessao(this);
        listaQuestoes = new ArrayList<>();
        respostasUsuario = new HashMap<>();

        Intent intent = getIntent();
        if (savedInstanceState == null) {
            if (intent != null && intent.hasExtra("ID_PROVA_SELECIONADA")) {
                idProvaSelecionada = intent.getIntExtra("ID_PROVA_SELECIONADA", -1);
                nomeProvaSelecionada = intent.getStringExtra("NOME_PROVA_SELECIONADA");

                if (idProvaSelecionada != -1) {
                    textViewNomeDaProva.setText(nomeProvaSelecionada != null ? nomeProvaSelecionada : "Prova");
                    carregarQuestoesDaProva();
                } else {
                    erroAoCarregarProva("ID da prova inválido.");
                }
            } else {
                erroAoCarregarProva("Não foi possível obter os dados da prova.");
            }
        } else {
            tempoRestanteMilisegundos = savedInstanceState.getLong(KEY_TEMPO_RESTANTE);
            questaoAtualIndex = savedInstanceState.getInt(KEY_QUESTAO_ATUAL_INDEX);
            if (savedInstanceState.containsKey(KEY_RESPOSTAS_USUARIO)) {
                respostasUsuario = (HashMap<Integer, Character>) savedInstanceState.getSerializable(KEY_RESPOSTAS_USUARIO);
            }
            if (savedInstanceState.containsKey(KEY_LISTA_QUESTOES)) {
                listaQuestoes = (ArrayList<Questao>) savedInstanceState.getSerializable(KEY_LISTA_QUESTOES);
            }
            idProvaSelecionada = savedInstanceState.getInt(KEY_ID_PROVA);
            nomeProvaSelecionada = savedInstanceState.getString(KEY_NOME_PROVA);
            textViewNomeDaProva.setText(nomeProvaSelecionada != null ? nomeProvaSelecionada : "Prova");

            if (listaQuestoes != null && !listaQuestoes.isEmpty()) {
                exibirQuestaoAtual();
            } else {
                Log.w(TAG, "Lista de questões vazia após restaurar estado, tentando recarregar.");
                if (idProvaSelecionada != -1) carregarQuestoesDaProva();
                else erroAoCarregarProva("Não foi possível restaurar dados da prova.");
            }
        }
        configurarListeners();
    }

    private void inicializarViews() {
        textViewNomeDaProva = findViewById(R.id.textViewNomeDaProva);
        textViewContadorQuestao = findViewById(R.id.textViewContadorQuestao);
        textViewCronometro = findViewById(R.id.textViewCronometro);
        textViewEnunciadoQuestao = findViewById(R.id.textViewEnunciadoQuestao);
        radioGroupAlternativas = findViewById(R.id.radioGroupAlternativas);
        radioButtonAlternativaA = findViewById(R.id.radioButtonAlternativaA);
        radioButtonAlternativaB = findViewById(R.id.radioButtonAlternativaB);
        radioButtonAlternativaC = findViewById(R.id.radioButtonAlternativaC);
        radioButtonAlternativaD = findViewById(R.id.radioButtonAlternativaD);
        radioButtonAlternativaE = findViewById(R.id.radioButtonAlternativaE);
        buttonProximaFinalizar = findViewById(R.id.buttonProximaFinalizar);
    }

    private void configurarListeners() {
        buttonProximaFinalizar.setOnClickListener(v -> processarCliqueProximaFinalizar());
    }

    private void carregarQuestoesDaProva() {
        Log.d(TAG, "Carregando questões para a prova ID: " + idProvaSelecionada);
        List<Questao> questoesBuscadas = provaController.getQuestoesDaProva(idProvaSelecionada, 10);

        if (questoesBuscadas != null && !questoesBuscadas.isEmpty()) {
            listaQuestoes.clear();
            listaQuestoes.addAll(questoesBuscadas);
            Log.d(TAG, listaQuestoes.size() + " questões carregadas.");

            if (respostasUsuario.isEmpty()) {
                questaoAtualIndex = 0;
            }
            exibirQuestaoAtual();
        } else {
            erroAoCarregarProva("Não foram encontradas questões para esta prova.");
        }
    }

    private void exibirQuestaoAtual() {
        if (listaQuestoes == null || listaQuestoes.isEmpty() || questaoAtualIndex < 0 || questaoAtualIndex >= listaQuestoes.size()) {
            Log.e(TAG, "Tentativa de exibir questão com lista vazia ou índice inválido. Índice: " + questaoAtualIndex + ", Tamanho: " + (listaQuestoes != null ? listaQuestoes.size() : "null"));
            if (listaQuestoes != null && !listaQuestoes.isEmpty() && questaoAtualIndex >= listaQuestoes.size()){
                finalizarProvaLogica();
            } else {
                erroAoCarregarProva("Erro ao exibir questão.");
            }
            return;
        }

        Questao questao = listaQuestoes.get(questaoAtualIndex);
        textViewEnunciadoQuestao.setText(questao.getEnunciado());
        radioButtonAlternativaA.setText("A) " + questao.getAlternativaA());
        radioButtonAlternativaB.setText("B) " + questao.getAlternativaB());
        radioButtonAlternativaC.setText("C) " + questao.getAlternativaC());
        radioButtonAlternativaD.setText("D) " + questao.getAlternativaD());

        if (questao.getAlternativaE() != null && !questao.getAlternativaE().trim().isEmpty()) {
            radioButtonAlternativaE.setText("E) " + questao.getAlternativaE());
            radioButtonAlternativaE.setVisibility(View.VISIBLE);
        } else {
            radioButtonAlternativaE.setText("");
            radioButtonAlternativaE.setVisibility(View.GONE);
        }

        radioGroupAlternativas.clearCheck();

        if (respostasUsuario != null && respostasUsuario.containsKey(questao.getId())) {
            char respostaSalva = respostasUsuario.get(questao.getId());
            switch (respostaSalva) {
                case 'A': radioGroupAlternativas.check(R.id.radioButtonAlternativaA); break;
                case 'B': radioGroupAlternativas.check(R.id.radioButtonAlternativaB); break;
                case 'C': radioGroupAlternativas.check(R.id.radioButtonAlternativaC); break;
                case 'D': radioGroupAlternativas.check(R.id.radioButtonAlternativaD); break;
                case 'E': if(radioButtonAlternativaE.getVisibility() == View.VISIBLE) radioGroupAlternativas.check(R.id.radioButtonAlternativaE); break;
            }
        }

        textViewContadorQuestao.setText(String.format(Locale.getDefault(), "Questão: %d/%d", questaoAtualIndex + 1, listaQuestoes.size()));

        if (questaoAtualIndex == listaQuestoes.size() - 1) {
            buttonProximaFinalizar.setText("Finalizar Prova");
        } else {
            buttonProximaFinalizar.setText("Próxima Questão");
        }
    }

    private void processarCliqueProximaFinalizar() {
        if (listaQuestoes.isEmpty()) return;

        salvarRespostaAtual();

        if (buttonProximaFinalizar.getText().toString().equals("Finalizar Prova")) {
            finalizarProvaLogica();
        } else {
            questaoAtualIndex++;
            if (questaoAtualIndex < listaQuestoes.size()) {
                exibirQuestaoAtual();
            } else {
                finalizarProvaLogica();
            }
        }
    }

    private void salvarRespostaAtual() {
        if (listaQuestoes.isEmpty() || questaoAtualIndex < 0 || questaoAtualIndex >= listaQuestoes.size()){
            return;
        }
        int radioButtonID = radioGroupAlternativas.getCheckedRadioButtonId();
        Questao questao = listaQuestoes.get(questaoAtualIndex);

        if (radioButtonID != -1) {
            char alternativaSelecionada = ' ';
            if (radioButtonID == R.id.radioButtonAlternativaA) alternativaSelecionada = 'A';
            else if (radioButtonID == R.id.radioButtonAlternativaB) alternativaSelecionada = 'B';
            else if (radioButtonID == R.id.radioButtonAlternativaC) alternativaSelecionada = 'C';
            else if (radioButtonID == R.id.radioButtonAlternativaD) alternativaSelecionada = 'D';
            else if (radioButtonID == R.id.radioButtonAlternativaE) alternativaSelecionada = 'E';

            if (alternativaSelecionada != ' ') {
                respostasUsuario.put(questao.getId(), alternativaSelecionada);
                Log.d(TAG, "Resposta salva para questão ID " + questao.getId() + ": " + alternativaSelecionada);
            }
        } else {
            Log.d(TAG, "Nenhuma resposta selecionada para questão ID " + questao.getId());
        }
    }

    private void finalizarProvaLogica() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.d(TAG, "Finalizando prova...");

        int acertos = 0;
        int erros = 0;

        if (listaQuestoes == null || listaQuestoes.isEmpty()) {
            Toast.makeText(this, "Nenhuma questão foi carregada. Não é possível finalizar.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Questao questao : listaQuestoes) {
            Character respostaDoUsuario = respostasUsuario.get(questao.getId());
            if (respostaDoUsuario != null) {
                if (respostaDoUsuario == questao.getRespostaCorreta()) {
                    acertos++;
                } else {
                    erros++;
                }
            } else {
                erros++;
            }
        }

        Log.d(TAG, "Resultado - Acertos: " + acertos + ", Erros: " + erros);
        long idUsuario = gerenciadorDeSessao.getUsuarioId();
        if (idUsuario <= 0) {
            Toast.makeText(this, "Erro: Usuário não identificado. Resultado não salvo.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        long tempoGasto = TEMPO_PROVA_MILISSEGUNDOS - tempoRestanteMilisegundos;
        ResultadoProva resultado = new ResultadoProva((int)idUsuario, idProvaSelecionada, acertos, erros, tempoGasto, "");
        provaController.salvarResultadoProva(resultado);

        // A Activity que exibe o resultado
        Intent intentResultado = new Intent(TelaProva.this, TelaResultadoProva.class); // <<--- NOME DA ACTIVITY DE RESULTADO
        intentResultado.putExtra("TOTAL_QUESTOES", listaQuestoes.size());
        intentResultado.putExtra("ACERTOS", acertos);
        intentResultado.putExtra("ERROS", erros);
        intentResultado.putExtra("NOME_PROVA", nomeProvaSelecionada);
        startActivity(intentResultado);
        finish();
    }

    private void iniciarCronometro(long milisegundosIniciais) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.d(TAG, "Iniciando cronômetro com: " + milisegundosIniciais + " ms");
        countDownTimer = new CountDownTimer(milisegundosIniciais, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tempoRestanteMilisegundos = millisUntilFinished;
                String tempoFormatado = String.format(Locale.getDefault(), "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                );
                textViewCronometro.setText("Tempo: " + tempoFormatado);
            }

            @Override
            public void onFinish() {
                tempoRestanteMilisegundos = 0;
                textViewCronometro.setText("Tempo: 00:00");
                if (!isFinishing() && !isDestroyed()) {
                    Toast.makeText(TelaProva.this, "Tempo esgotado! A prova foi cancelada.", Toast.LENGTH_LONG).show();
                    salvarRespostaAtual();
                    finish();
                }
            }
        }.start();
    }

    private void erroAoCarregarProva(String mensagem) {
        Log.e(TAG, mensagem);
        if (!isFinishing() && !isDestroyed()) {
            Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (listaQuestoes != null && !listaQuestoes.isEmpty() && questaoAtualIndex >= 0 && questaoAtualIndex < listaQuestoes.size()) {
            salvarRespostaAtual();
        }
        outState.putInt(KEY_QUESTAO_ATUAL_INDEX, questaoAtualIndex);
        outState.putLong(KEY_TEMPO_RESTANTE, tempoRestanteMilisegundos);
        if (respostasUsuario != null) {
            outState.putSerializable(KEY_RESPOSTAS_USUARIO, respostasUsuario);
        }
        if (listaQuestoes != null && listaQuestoes instanceof ArrayList) {
            outState.putSerializable(KEY_LISTA_QUESTOES, (ArrayList<Questao>) listaQuestoes);
        }
        outState.putInt(KEY_ID_PROVA, idProvaSelecionada);
        outState.putString(KEY_NOME_PROVA, nomeProvaSelecionada);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.d(TAG, "onSaveInstanceState: Estado salvo. Tempo restante: " + tempoRestanteMilisegundos);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: tempoRestanteMilisegundos = " + tempoRestanteMilisegundos + ", Questões carregadas: " + (listaQuestoes != null && !listaQuestoes.isEmpty()));
        if (listaQuestoes != null && !listaQuestoes.isEmpty() && tempoRestanteMilisegundos > 0) {
            iniciarCronometro(tempoRestanteMilisegundos);
        } else if (listaQuestoes != null && !listaQuestoes.isEmpty() && tempoRestanteMilisegundos <= 0) {
            textViewCronometro.setText("Tempo: 00:00");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            Log.d(TAG, "onPause: Cronômetro pausado. Tempo restante: " + tempoRestanteMilisegundos);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Log.d(TAG, "onDestroy: TelaProva destruída.");
    }
}