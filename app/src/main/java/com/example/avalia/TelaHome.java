package com.example.avalia;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import para Log
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast; // Import para Toast

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avalia.bancodedados.MissoesController; // Import para MissoesController
import com.example.avalia.missoes.Missao;
import com.example.avalia.missoes.tela_missoes;

import java.util.ArrayList;
import java.util.List;

public class TelaHome extends AppCompatActivity {

    private static final String TAG = "TelaHome"; // Tag para logs

    private TextView textWelcome;
    private ImageView userPhoto;
    private TextView textMotivacional;
    private TextView textLicoes;
    private Button buttonHome, buttonLicoes, buttonProvas, buttonIa, buttonRanking;

    // Controller para buscar as missões
    private MissoesController missoesController;

    final String[] areasEnem = {
            "Matemática e suas Tecnologias",
            "Ciências Humanas e suas Tecnologias",
            "Linguagens, Códigos e suas Tecnologias",
            "Ciências da Natureza e suas Tecnologias"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_home);
        Log.d(TAG, "onCreate: Iniciando TelaHome.");

        // Inicializa o MissoesController
        missoesController = new MissoesController(this);
        try {
            missoesController.open();
            Log.d(TAG, "onCreate: Conexão com o banco (MissoesController) aberta.");
        } catch (android.database.SQLException e) {
            Log.e(TAG, "onCreate: Erro ao abrir o banco de dados para MissoesController!", e);
            Toast.makeText(this, "Erro ao conectar com o banco para carregar missões.", Toast.LENGTH_LONG).show();
            // O app pode continuar, mas o chatbot pode não ter contexto de missões
        }


        textWelcome = findViewById(R.id.text_welcome);
        userPhoto = findViewById(R.id.user_photo);
        textMotivacional = findViewById(R.id.text_motivacional);
        textLicoes = findViewById(R.id.text_licoes);

        buttonHome = findViewById(R.id.buttonHome);
        buttonLicoes = findViewById(R.id.buttonLicoes);
        buttonProvas = findViewById(R.id.buttonProvas);
        buttonIa = findViewById(R.id.buttonIa); // Seu botão de IA
        buttonRanking = findViewById(R.id.buttonRanking);

        buttonLicoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botão Lições clicado.");
                // Ação: Mostrar diálogo para ir para tela de missões
                mostrarDialogoSelecaoArea(false); // false indica que é para abrir a tela de missões normal
            }
        });

        // Configurar o listener para o botão "IA"
        buttonIa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botão IA clicado.");
                // Ação: Mostrar diálogo para selecionar área e depois ir para o Chatbot
                mostrarDialogoSelecaoArea(true); // true indica que é para abrir o chatbot após selecionar área
            }
        });

    }

    /**
     * Exibe um diálogo para o usuário selecionar a área do ENEM.
     * @param abrirChatbotAposSelecao Se true, abre o ChatbotActivity após a seleção. Se false, abre a TelaMissoesActivity.
     */
    private void mostrarDialogoSelecaoArea(final boolean abrirChatbotAposSelecao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TelaHome.this);
        String tituloDialogo = abrirChatbotAposSelecao ? "Escolha uma Área para o Chatbot" : "Escolha uma Área para as Missões";
        builder.setTitle(tituloDialogo);

        builder.setItems(areasEnem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String areaSelecionada = areasEnem[which];
                if (abrirChatbotAposSelecao) {
                    Log.d(TAG, "Área selecionada para Chatbot: " + areaSelecionada);
                    abrirChatbotComMissoes(areaSelecionada);
                } else {
                    Log.d(TAG, "Área selecionada para Missões: " + areaSelecionada);
                    abrirTelaMissoes(areaSelecionada);
                }
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            Log.d(TAG, "Diálogo de seleção de área cancelado.");
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void abrirTelaMissoes(String nomeDaArea) {
        Intent intent = new Intent(TelaHome.this, tela_missoes.class); // Certifique-se que o nome da classe está correto
        intent.putExtra("nome_da_area_extra", nomeDaArea);
        startActivity(intent);
    }

    private void abrirChatbotComMissoes(String nomeDaArea) {
        if (missoesController == null) {
            Log.e(TAG, "MissoesController não inicializado. Não é possível abrir o chatbot com contexto.");
            Toast.makeText(this, "Erro: Serviço de missões não disponível.", Toast.LENGTH_SHORT).show();
            // Opcionalmente, abrir o chatbot sem contexto de missões:
            // Intent intent = new Intent(telaHome.this, ChatbotActivity.class);
            // startActivity(intent);
            return;
        }

        // Carregar as descrições das missões para a área selecionada
        ArrayList<String> descricoes = new ArrayList<>();
        try {
            // Garante que o controller esteja aberto se não foi aberto no onCreate ou foi fechado.
            // missoesController.open(); // O open já é chamado no onResume, e no onCreate. Cuidado com múltiplas chamadas sem close.
            // É melhor garantir que esteja aberto no onResume.

            List<Missao> missoesDaArea = missoesController.getMissoesPorArea(nomeDaArea);
            if (missoesDaArea != null && !missoesDaArea.isEmpty()) {
                for (Missao missao : missoesDaArea) {
                    descricoes.add(missao.getDescricao());
                }
                Log.d(TAG, "Descrições de missões carregadas para o chatbot: " + descricoes.size());
            } else {
                Log.d(TAG, "Nenhuma missão encontrada para a área '" + nomeDaArea + "' para passar ao chatbot.");
                Toast.makeText(this, "Nenhuma missão específica para '" + nomeDaArea + "' no momento.", Toast.LENGTH_SHORT).show();
                // Mesmo sem missões específicas, podemos abrir o chat
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar missões para o chatbot: ", e);
            Toast.makeText(this, "Erro ao carregar dados das missões.", Toast.LENGTH_SHORT).show();
            // Mesmo com erro, podemos abrir o chat
        }

        Intent intent = new Intent(TelaHome.this, ChatBot.class);
        intent.putStringArrayListExtra("lista_descricoes_missoes", descricoes);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (missoesController != null) {
                missoesController.open(); // Reabre a conexão se necessário
                Log.d(TAG, "onResume: Conexão com MissoesController (re)aberta.");
            }
        } catch (android.database.SQLException e) {
            Log.e(TAG, "onResume: Erro ao (re)abrir MissoesController!", e);
            Toast.makeText(this, "Erro ao reconectar com o banco para missões.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (missoesController != null) {
            missoesController.close();
            Log.d(TAG, "onDestroy: Conexão com MissoesController fechada.");
        }
        Log.d(TAG, "onDestroy: TelaHome destruída.");
    }
}