package com.example.avalia.missoes;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avalia.R;
import com.example.avalia.bancodedados.MissoesController;

import java.util.ArrayList;
import java.util.List;

public class tela_missoes extends AppCompatActivity implements MissoesAdapter.OnMissaoInteractionListener {
    private TextView textViewTituloArea;
    private TextView textViewPontuacaoTotal;
    private RecyclerView recyclerViewMissoes;
    private Button buttonVoltarMissoes;

    private MissoesAdapter missoesAdapter;
    private List<Missao> listaDeMissoesAtual;
    private String nomeAreaAtual;
    private int pontuacaoTotalUsuario = 0;

    // MUDANÇA: Usar MissaoDAOMissoesController
    private MissoesController MissoesController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_missoes);

        // MUDANÇA: Inicializar e abrir MissaoDAO
        MissoesController = new MissoesController(this);
        try {
            MissoesController.open();
        } catch (android.database.SQLException e) {
            Toast.makeText(this, "Erro ao abrir o banco de dados para missões!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // ... (findViewById igual)
        textViewTituloArea = findViewById(R.id.textViewTituloArea);
        textViewPontuacaoTotal = findViewById(R.id.textViewPontuacaoTotal);
        recyclerViewMissoes = findViewById(R.id.recyclerViewMissoes);
        buttonVoltarMissoes = findViewById(R.id.buttonVoltarMissoes);

        nomeAreaAtual = getIntent().getStringExtra("nome_da_area_extra");
        if (nomeAreaAtual == null || nomeAreaAtual.isEmpty()) {
            nomeAreaAtual = "Área Desconhecida";
            Toast.makeText(this, "Erro: Área do conhecimento não especificada.", Toast.LENGTH_LONG).show();
        }
        textViewTituloArea.setText("Missões de " + nomeAreaAtual);

        // MUDANÇA: Carregar missões usando missaoDAO
        listaDeMissoesAtual = MissoesController.getMissoesPorArea(nomeAreaAtual);
        if (listaDeMissoesAtual.isEmpty() && !nomeAreaAtual.equals("Área Desconhecida")) {
            Toast.makeText(this, "Nenhuma missão encontrada para " + nomeAreaAtual, Toast.LENGTH_LONG).show();
        }

        if (listaDeMissoesAtual == null) {
            listaDeMissoesAtual = new ArrayList<>();
        }
        recyclerViewMissoes.setLayoutManager(new LinearLayoutManager(this));
        missoesAdapter = new MissoesAdapter(this, listaDeMissoesAtual, this);
        recyclerViewMissoes.setAdapter(missoesAdapter);

        calcularEAtualizarPontuacaoTotal();
        buttonVoltarMissoes.setOnClickListener(v -> finish());
    }

    private void calcularEAtualizarPontuacaoTotal() {
        // ... (lógica igual)
        pontuacaoTotalUsuario = 0;
        if (listaDeMissoesAtual != null) {
            for (Missao missao : listaDeMissoesAtual) {
                if (missao.isConcluida()) {
                    pontuacaoTotalUsuario += missao.getPontos();
                }
            }
        }
        textViewPontuacaoTotal.setText("Pontuação Total: " + pontuacaoTotalUsuario + " pts");
    }

    @Override
    public void onMissaoStatusChanged(Missao missao, boolean isChecked) {
        // MUDANÇA: Usar missaoDAO
        int linhasAfetadas = MissoesController.updateStatusMissao(missao.getDbId(), isChecked);

        if (linhasAfetadas > 0) {
            Log.d("TelaMissoesActivity", "Status da missão " + missao.getDescricao() + " salvo no BD.");
            calcularEAtualizarPontuacaoTotal();
            String feedback = missao.getDescricao() + (isChecked ? " concluída!" : " pendente.");
            Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("TelaMissoesActivity", "Erro ao salvar status da missão " + missao.getDescricao() + " no BD.");
            Toast.makeText(this, "Erro ao salvar status da missão.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        // MUDANÇA: Fechar DAO
        if (MissoesController != null) {
            MissoesController.close();
        }
        super.onDestroy();
    }
}