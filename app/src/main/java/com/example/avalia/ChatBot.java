package com.example.avalia;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity; // Para alinhar mensagens
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Para cores
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Imports da SDK do Gemini
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Candidate;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.FinishReason;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig; // Se for usar config
import com.google.ai.client.generativeai.type.HarmCategory;   // Se for usar safety settings
import com.google.ai.client.generativeai.type.SafetySetting; // Se for usar safety settings
import com.google.ai.client.generativeai.type.BlockThreshold; // Se for usar safety settings


// Imports para Futures do Guava
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList; // Para a lista de descrições de missões
import java.util.List;    // Para safetySettings
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBot extends AppCompatActivity { // Convenção: ChatBotActivity

    private EditText perguntaEditText;
    private Button enviarButton;
    private LinearLayout chatLayout;
    private ScrollView scrollView;

    private GenerativeModelFutures generativeModelFutures; // Renomeado para evitar confusão com a classe
    private GenerativeModel gm; // Guardar a instância do GenerativeModel
    private Executor mainExecutor;

    private Executor executor = Executors.newSingleThreadExecutor();

    // Puxe do BuildConfig como configuramos anteriormente!
    // private static final String API_KEY = "SUA_CHAVE_API_AQUI_VIA_BUILDCONFIG";
    private static final String GEMINI_MODEL = "gemini-1.5-flash-latest";

    private static final String TAG = "ChatBotGemini";
    private ArrayList<String> descricoesMissoes; // Para o contexto das missões

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_bot); // Seu layout XML

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaChatBot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        perguntaEditText = findViewById(R.id.perguntaEditText);
        enviarButton = findViewById(R.id.enviarButton);
        chatLayout = findViewById(R.id.chatLayout);
        scrollView = findViewById(R.id.scrollView);
        mainExecutor = ContextCompat.getMainExecutor(this); // Forma padrão de obter o executor da UI thread

        // Receber descrições das missões (se vierem de outra activity)
        descricoesMissoes = getIntent().getStringArrayListExtra("lista_descricoes_missoes");
        if (descricoesMissoes == null) {
            descricoesMissoes = new ArrayList<>();
        }

        // Inicializa o modelo GenerativeModel do Gemini
        String apiKey = BuildConfig.GEMINI_API_KEY; // Puxa do BuildConfig
        if (apiKey == null || apiKey.equals("NO_API_KEY") || apiKey.isEmpty()) {
            Log.e(TAG, "onCreate: Chave da API do Gemini não configurada no BuildConfig. Verifique local.properties e build.gradle");
            Toast.makeText(this, "Chave da API não configurada!", Toast.LENGTH_LONG).show();
            adicionarMensagemAoChat("Bot: Erro - Chave da API não configurada.", false);
            if (enviarButton != null) enviarButton.setEnabled(false);
            return;
        }

        try {
            GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
            // Adicione configurações se desejar:
            // configBuilder.setTemperature(0.7f);
            // configBuilder.setMaxOutputTokens(1000);

            List<SafetySetting> safetySettings = new ArrayList<>();
            safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE));
            safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE));
            safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE));
            safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE));

            gm = new GenerativeModel( // Inicializa a variável de instância 'gm'
                    GEMINI_MODEL,
                    apiKey,
                    configBuilder.build(),
                    safetySettings
            );
            // Cria a instância de GenerativeModelFutures usando o 'gm' inicializado
            generativeModelFutures = GenerativeModelFutures.from(gm);
            Log.i(TAG, "GenerativeModel e GenerativeModelFutures inicializados com sucesso.");

        } catch (Exception e) {
            Log.e(TAG, "Erro ao inicializar o GenerativeModel: ", e);
            Toast.makeText(this, "Erro ao inicializar Gemini: " + e.getMessage(), Toast.LENGTH_LONG).show();
            adicionarMensagemAoChat("Bot: Erro ao inicializar Gemini.", false);
            if (enviarButton != null) enviarButton.setEnabled(false);
            return;
        }

        if (enviarButton != null) {
            enviarButton.setOnClickListener(v -> processarMensagem());
        }

        adicionarMensagemAoChat("Bot: Olá! Como posso te ajudar hoje?", false);
    }

    private void processarMensagem() {
        String perguntaUsuario = perguntaEditText.getText().toString().trim();

        if (TextUtils.isEmpty(perguntaUsuario)) {
            Toast.makeText(this, "Digite uma pergunta.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (generativeModelFutures == null) {
            Toast.makeText(this, "Assistente IA não está pronto.", Toast.LENGTH_SHORT).show();
            return;
        }

        adicionarMensagemAoChat("Você: " + perguntaUsuario, true);
        perguntaEditText.setText("");

        TextView tvBotDigitando = adicionarMensagemAoChat("Bot: Pensando...", false);
        if (enviarButton != null) enviarButton.setEnabled(false);

        // Construir o prompt
        StringBuilder promptParaIA = new StringBuilder("Você é o Avalia+, um assistente de estudos para o ENEM. ");
        promptParaIA.append("Responda de forma amigável, educativa e concisa.\n");
        if (descricoesMissoes != null && !descricoesMissoes.isEmpty()) {
            promptParaIA.append("Contexto das missões atuais do usuário (se relevante para a pergunta):\n");
            for (String desc : descricoesMissoes) {
                promptParaIA.append("- ").append(desc).append("\n");
            }
        }
        promptParaIA.append("\nUsuário: ").append(perguntaUsuario).append("\nAssistente Avalia+:");


        Content promptContent = new Content.Builder().addText(promptParaIA.toString()).build();

        ListenableFuture<GenerateContentResponse> responseFuture = generativeModelFutures.generateContent(promptContent);
        Futures.addCallback(responseFuture, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String respostaBot = "Desculpe, não consegui encontrar uma resposta para isso.";
                String finishReasonString = "DESCONHECIDO";

                if (result != null) {
                    String textFromResult = result.getText(); // Tentativa principal
                    if (textFromResult != null && !textFromResult.isEmpty()) {
                        respostaBot = textFromResult;
                        Log.i(TAG, "Resposta Gemini (getText): " + respostaBot);
                    } else {
                        if (result.getCandidates() != null && !result.getCandidates().isEmpty()) {
                            Candidate firstCandidate = result.getCandidates().get(0);
                            if (firstCandidate != null) {
                                if (firstCandidate.getFinishReason() != null) {
                                    finishReasonString = firstCandidate.getFinishReason().toString();
                                }
                                if (firstCandidate.getFinishReason() == FinishReason.SAFETY) {
                                    respostaBot = "Não posso responder a isso devido às configurações de segurança.";
                                } else if (firstCandidate.getFinishReason() == FinishReason.RECITATION) {
                                    respostaBot = "A resposta parece ser muito similar a conteúdo protegido.";
                                } else if (textFromResult == null || textFromResult.isEmpty()){ // Se getText() falhou e não foi SAFETY/RECITATION
                                    Log.w(TAG, "getText() nulo/vazio. FinishReason: " + finishReasonString);
                                    // Tentar pegar das partes como último recurso
                                    if (firstCandidate.getContent() != null && firstCandidate.getContent().getParts() != null && !firstCandidate.getContent().getParts().isEmpty() &&
                                            firstCandidate.getContent().getParts().get(0).toString() != null && !firstCandidate.getContent().getParts().get(0).toString().isEmpty()) {
                                        respostaBot = firstCandidate.getContent().getParts().get(0).toString();
                                        Log.i(TAG, "Resposta Gemini (candidate.part): " + respostaBot);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "GenerateContentResponse (result) é nulo.");
                }

                final String finalResposta = respostaBot;
                mainExecutor.execute(() -> {
                    if (tvBotDigitando != null) {
                        tvBotDigitando.setText("Bot: " + finalResposta);
                    } else {
                        adicionarMensagemAoChat("Bot: " + finalResposta, false);
                    }
                    if (enviarButton != null) enviarButton.setEnabled(true);
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e(TAG, "Falha ao gerar conteúdo do Gemini: ", t);
                String mensagemErro = "Bot: Desculpe, ocorreu um erro. (" + t.getMessage() + ")";
                mainExecutor.execute(() -> {
                    if (tvBotDigitando != null) {
                        tvBotDigitando.setText(mensagemErro);
                    } else {
                        adicionarMensagemAoChat(mensagemErro, false);
                    }
                    if (enviarButton != null) enviarButton.setEnabled(true);
                    scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                });
            }
        }, mainExecutor);
    }

    private TextView adicionarMensagemAoChat(String mensagem, boolean ehUsuario) {
        TextView mensagemTextView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 8;
        params.bottomMargin = 8;

        if (ehUsuario) {
            mensagemTextView.setBackgroundResource(R.drawable.bg_mensagem_usuario); // Crie este drawable
            mensagemTextView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            params.gravity = Gravity.END;
            params.leftMargin = 60; // Adiciona margem à esquerda para empurrar para a direita
        } else {
            mensagemTextView.setBackgroundResource(R.drawable.bg_mensagem_bot); // Crie este drawable
            mensagemTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            params.gravity = Gravity.START;
            params.rightMargin = 60; // Adiciona margem à direita para empurrar para a esquerda
        }
        mensagemTextView.setLayoutParams(params);
        mensagemTextView.setText(mensagem);
        mensagemTextView.setTextSize(16f);
        mensagemTextView.setPadding(24, 16, 24, 16); // Ajuste o padding

        chatLayout.addView(mensagemTextView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        return mensagemTextView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // gm não tem um método close() explícito.
        // O ExecutorService pode ser desligado se você quiser ser muito rigoroso.
        if (executor instanceof java.util.concurrent.ExecutorService) { // Usa a variável 'executor' (minúsculo)
            Log.d(TAG, "Desligando o ExecutorService...");
            ((java.util.concurrent.ExecutorService) executor).shutdownNow(); // Usa a variável 'executor' (minúsculo)
        }
        Log.d(TAG, "ChatbotActivity destruída.");
    }
}