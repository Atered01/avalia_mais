package com.example.avalia.chatbot;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton; // Mudado de Button para ImageButton se você usou o XML sugerido
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import para Toolbar
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // Import para RecyclerView

// ... (seus imports da API Gemini e Guava como antes)
import com.example.avalia.BuildConfig;
import com.example.avalia.R;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Candidate;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.FinishReason;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ChatBot extends AppCompatActivity { // Renomeei para convenção

    private static final String TAG = "ChatBotActivity";

    // Componentes da UI atualizados para RecyclerView
    private RecyclerView recyclerViewChat;
    private EditText perguntaEditText; // Mantido
    private ImageButton enviarButton;   // Mudado para ImageButton se você alterou no XML
    // private Button enviarButton;    // Se você manteve como Button
    private ProgressBar progressBarIA;

    private ChatAdapter chatAdapter; // Nosso novo adapter
    private List<MensagemChat> listaMensagensChat; // Lista de objetos MensagemChat
    private ArrayList<String> descricoesMissoes;

    private GenerativeModel gm;
    private Executor executor = Executors.newSingleThreadExecutor(); // Para a API Gemini

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Removido pois vamos usar Toolbar padrão
        setContentView(R.layout.activity_chat_bot); // Seu layout XML principal do chatbot

        Toolbar toolbar = findViewById(R.id.toolbarChatbot);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Removido o ViewCompat.setOnApplyWindowInsetsListener se o padding já está tratado pelo layout

        perguntaEditText = findViewById(R.id.perguntaEditText);
        enviarButton = findViewById(R.id.enviarButton); // Se for ImageButton, o ID é o mesmo
        progressBarIA = findViewById(R.id.progressBarIA);
        if (progressBarIA != null) progressBarIA.setVisibility(View.GONE);

        // Configuração do RecyclerView
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        listaMensagensChat = new ArrayList<>();
        chatAdapter = new ChatAdapter(listaMensagensChat);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        // layoutManager.setStackFromEnd(true); // Descomente se quiser que a lista comece de baixo e role para cima
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        descricoesMissoes = getIntent().getStringArrayListExtra("lista_descricoes_missoes");
        if (descricoesMissoes == null) {
            descricoesMissoes = new ArrayList<>();
        }

        // Inicialização do Gemini (como antes)
        String apiKey = BuildConfig.GEMINI_API_KEY;
        if (apiKey == null || apiKey.equals("NO_API_KEY") || apiKey.isEmpty()) {
            Log.e(TAG, "Chave API Gemini não configurada.");
            Toast.makeText(this, "Chave API não configurada!", Toast.LENGTH_LONG).show();
            if (enviarButton != null) enviarButton.setEnabled(false);
        } else {
            try {
                GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
                List<SafetySetting> safetySettings = new ArrayList<>();
                safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE));
                // ... (outras safety settings)
                gm = new GenerativeModel("gemini-1.5-flash-latest", apiKey, configBuilder.build(), safetySettings);
                Log.i(TAG, "GenerativeModel inicializado.");
            } catch (Exception e) {
                Log.e(TAG, "Erro inicializando GenerativeModel: ", e);
                Toast.makeText(this, "Erro ao inicializar IA.", Toast.LENGTH_LONG).show();
                if (enviarButton != null) enviarButton.setEnabled(false);
            }
        }

        if (enviarButton != null) {
            enviarButton.setOnClickListener(v -> enviarMensagemUsuarioEProcessarComIA());
        }

        adicionarMensagemBot("Olá! Sou o Avalia+, seu assistente de estudos. Como posso te ajudar?");
    }

    private void enviarMensagemUsuarioEProcessarComIA() {
        String textoMensagem = perguntaEditText.getText().toString().trim();
        if (TextUtils.isEmpty(textoMensagem)) return;
        if (gm == null || (enviarButton != null && !enviarButton.isEnabled())) {
            Toast.makeText(this, "Assistente IA não está pronto.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Usuário enviou: " + textoMensagem);
        // Adiciona mensagem do usuário ao adapter
        MensagemChat msgUsuario = new MensagemChat(textoMensagem, MensagemChat.TIPO_USUARIO);
        chatAdapter.adicionarMensagem(msgUsuario);
        if (chatAdapter.getItemCount() > 0) {
            recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
        perguntaEditText.setText("");

        if (progressBarIA != null) progressBarIA.setVisibility(View.VISIBLE);
        if (enviarButton != null) enviarButton.setEnabled(false);

        processarPerguntaUsuarioComIA(textoMensagem);
    }

    private void processarPerguntaUsuarioComIA(String pergunta) {
        // ... (Lógica de construção do prompt e chamada à API Gemini como antes) ...
        // ... (Usando GenerativeModelFutures.from(gm) etc.) ...

        // No onSuccess do Futures.addCallback:
        // ...
        // runOnUiThread(() -> {
        //     adicionarMensagemBot(finalRespostaDoBot); // Usa o método do adapter
        //     if (progressBarIA != null) progressBarIA.setVisibility(View.GONE);
        //     if (buttonEnviarMensagem != null) buttonEnviarMensagem.setEnabled(true);
        // });
        // ...

        // No onFailure do Futures.addCallback:
        // ...
        // runOnUiThread(() -> {
        //     adicionarMensagemBot("Erro ao contatar IA...");
        //     if (progressBarIA != null) progressBarIA.setVisibility(View.GONE);
        //     if (buttonEnviarMensagem != null) buttonEnviarMensagem.setEnabled(true);
        // });
        // ...

        // Vou colar a parte do processarPerguntaUsuarioComIA completa para clareza:
        if (gm == null) { /* ... (código como antes) ... */ return;}

        StringBuilder promptContext = new StringBuilder("Você é um assistente de estudos para o ENEM chamado Avalia+, ...");
        // ... (construção completa do prompt como na sua versão anterior) ...
        if (descricoesMissoes != null && !descricoesMissoes.isEmpty()) {
            promptContext.append("Contexto das missões atuais do usuário (se relevante para a pergunta):\n");
            for (String desc : descricoesMissoes) {
                promptContext.append("- ").append(desc).append("\n");
            }
        }
        promptContext.append("\nUsuário: ").append(pergunta).append("\nAssistente Avalia+:");

        Content content = new Content.Builder().addText(promptContext.toString()).build();
        GenerativeModelFutures futuresClient = GenerativeModelFutures.from(gm);
        ListenableFuture<GenerateContentResponse> future = futuresClient.generateContent(content);

        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                String respostaBot = "Desculpe, não consegui processar uma resposta clara neste momento.";
                String finishReasonString = "DESCONHECIDO";
                try {
                    if (result != null) {
                        String textFromResult = result.getText();
                        if (textFromResult != null && !textFromResult.isEmpty()) {
                            respostaBot = textFromResult;
                        } else {
                            // ... (lógica de fallback com FinishReason como antes) ...
                            if (result.getCandidates() != null && !result.getCandidates().isEmpty()) {
                                Candidate firstCandidate = result.getCandidates().get(0);
                                if (firstCandidate != null) {
                                    if (firstCandidate.getFinishReason() != null) {
                                        finishReasonString = firstCandidate.getFinishReason().toString();
                                        if (firstCandidate.getFinishReason() == FinishReason.SAFETY) {
                                            respostaBot = "Minha configuração de segurança me impede de responder a essa pergunta.";
                                        } // ... outros FinishReason ...
                                    }
                                    // Fallback para partes do conteúdo
                                    if ((textFromResult == null || textFromResult.isEmpty()) && respostaBot.startsWith("Desculpe")) {
                                        if (firstCandidate.getContent() != null && firstCandidate.getContent().getParts() != null && !firstCandidate.getContent().getParts().isEmpty() &&
                                                firstCandidate.getContent().getParts().get(0).toString() != null && !firstCandidate.getContent().getParts().get(0).toString().isEmpty()) {
                                            respostaBot = firstCandidate.getContent().getParts().get(0).toString();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao processar resposta da IA: ", e);
                }
                final String finalResposta = respostaBot;
                runOnUiThread(() -> {
                    adicionarMensagemBot(finalResposta);
                    if (progressBarIA != null) progressBarIA.setVisibility(View.GONE);
                    if (enviarButton != null) enviarButton.setEnabled(true);
                });
            }
            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e(TAG, "Falha ao gerar conteúdo do Gemini: ", t);
                runOnUiThread(() -> {
                    adicionarMensagemBot("Bot: Desculpe, ocorreu um erro. (" + t.getMessage() + ")");
                    if (progressBarIA != null) progressBarIA.setVisibility(View.GONE);
                    if (enviarButton != null) enviarButton.setEnabled(true);
                });
            }
        }, ContextCompat.getMainExecutor(ChatBot.this)); // Usando ContextCompat.getMainExecutor
    }


    // ATUALIZADO: Este método agora usa o ChatAdapter
    private void adicionarMensagemBot(String texto) {
        Log.d(TAG, "Bot respondeu: " + texto);
        MensagemChat mensagemBot = new MensagemChat(texto, MensagemChat.TIPO_BOT);
        chatAdapter.adicionarMensagem(mensagemBot);
        if (chatAdapter.getItemCount() > 0) {
            recyclerViewChat.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        }
    }
    // Não precisamos mais do método adicionarMensagemAoChat(String mensagem, boolean ehUsuario)
    // que adicionava TextViews diretamente, pois o adapter cuida disso.

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Ou onBackPressedDispatcher.onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) executor).shutdownNow();
        }
        Log.d(TAG, "ChatbotActivity destruída.");
    }
}