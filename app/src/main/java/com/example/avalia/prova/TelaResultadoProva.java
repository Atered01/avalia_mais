package com.example.avalia.prova; // Ajuste o pacote conforme sua estrutura

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.avalia.R; // Sua classe R
import com.example.avalia.principal.TelaHome; // Para o botão de voltar

import java.util.Locale;

public class TelaResultadoProva extends AppCompatActivity {

    private TextView textViewTituloResultado, textViewNomeProvaResultado, textViewDesempenho, textViewPorcentagem;
    private ImageView imageViewResultadoIcon;
    private Button buttonVoltarInicioResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_resultado_prova);

        // Inicializar Views
        textViewNomeProvaResultado = findViewById(R.id.textViewNomeProvaResultado);
        textViewDesempenho = findViewById(R.id.textViewDesempenho);
        textViewPorcentagem = findViewById(R.id.textViewPorcentagem);
        imageViewResultadoIcon = findViewById(R.id.imageViewResultadoIcon); // Opcional
        buttonVoltarInicioResultado = findViewById(R.id.buttonVoltarInicioResultado);

        // Receber dados da Intent
        Intent intent = getIntent();
        int totalQuestoes = intent.getIntExtra("TOTAL_QUESTOES", 0);
        int acertos = intent.getIntExtra("ACERTOS", 0);
        // int erros = intent.getIntExtra("ERROS", 0); // Você pode usar isso se quiser exibir
        String nomeProva = intent.getStringExtra("NOME_PROVA");

        // Popular as Views
        if (nomeProva != null) {
            textViewNomeProvaResultado.setText(nomeProva);
        } else {
            textViewNomeProvaResultado.setText("Prova Finalizada");
        }

        textViewDesempenho.setText(String.format(Locale.getDefault(),
                "Você acertou %d de %d questões!", acertos, totalQuestoes));

        if (totalQuestoes > 0) {
            double porcentagem = ((double) acertos / totalQuestoes) * 100;
            textViewPorcentagem.setText(String.format(Locale.getDefault(), "(%.0f%% de acerto)", porcentagem));

            // Mudar o ícone baseado na porcentagem (exemplo simples)
            if (porcentagem >= 70) {
                imageViewResultadoIcon.setImageResource(R.drawable.ic_star_points); // Supondo que você tenha este drawable
            } else if (porcentagem >= 50) {
                imageViewResultadoIcon.setImageResource(R.drawable.ic_task_placeholder); // Use outro ícone
            } else {
                // imageViewResultadoIcon.setImageResource(R.drawable.ic_feedback_bad); // Ícone para desempenho baixo
                imageViewResultadoIcon.setVisibility(View.GONE); // Ou simplesmente esconde
            }
        } else {
            textViewPorcentagem.setText("(N/A)");
            imageViewResultadoIcon.setVisibility(View.GONE);
        }

        // Configurar botão
        buttonVoltarInicioResultado.setOnClickListener(v -> {
            Intent intentHome = new Intent(TelaResultadoProva.this, TelaHome.class);
            // Limpar a pilha de activities para que o usuário não volte para a prova ou seleção de prova ao pressionar "voltar"
            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            finish(); // Finaliza a TelaResultadoProvaActivity
        });
    }

    // Impedir que o usuário volte para a TelaProva usando o botão "Voltar" do dispositivo
    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Ou comente esta linha para desabilitar completamente o botão voltar
        // Ou redirecione para a TelaHome
        Intent intentHome = new Intent(TelaResultadoProva.this, TelaHome.class);
        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentHome);
        finish();
    }
}