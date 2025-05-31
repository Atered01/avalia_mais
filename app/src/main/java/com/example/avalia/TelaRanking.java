package com.example.avalia;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avalia.bancodedados.UsuarioController;
import com.example.avalia.usuario.Usuario;

import java.util.ArrayList;
import java.util.List;

public class TelaRanking extends AppCompatActivity {

    private static final String TAG = "TelaRankingActivity";
    private RecyclerView recyclerViewRanking;
    private RankingAdapter rankingAdapter;
    private List<Usuario> listaRanking; // Usando a classe Usuario que já tem a pontuação
    private UsuarioController usuarioController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_ranking); // Seu layout activity_tela_ranking.xml

        recyclerViewRanking = findViewById(R.id.recyclerViewRanking);
        recyclerViewRanking.setLayoutManager(new LinearLayoutManager(this));

        listaRanking = new ArrayList<>();
        rankingAdapter = new RankingAdapter(this, listaRanking); // Passa o contexto se o adapter precisar
        recyclerViewRanking.setAdapter(rankingAdapter);

        usuarioController = new UsuarioController(this);
        try {
            usuarioController.open();
            carregarRanking();
        } catch (android.database.SQLException e) {
            Log.e(TAG, "Erro ao abrir banco para carregar ranking", e);
            Toast.makeText(this, "Erro ao carregar ranking.", Toast.LENGTH_SHORT).show();
        }

        // Configurar botão voltar da Toolbar (se você adicionar uma Toolbar no XML)
        // Toolbar toolbar = findViewById(R.id.toolbarRanking);
        // setSupportActionBar(toolbar);
        // if (getSupportActionBar() != null) {
        //     getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //     getSupportActionBar().setTitle("Ranking");
        // }
    }

    private void carregarRanking() {
        List<Usuario> usuariosDoBanco = usuarioController.getUsuariosParaRanking(20); // Pega o Top 20, por exemplo
        if (usuariosDoBanco != null && !usuariosDoBanco.isEmpty()) {
            listaRanking.clear();
            listaRanking.addAll(usuariosDoBanco);
            rankingAdapter.notifyDataSetChanged();
            Log.d(TAG, "Ranking carregado com " + usuariosDoBanco.size() + " usuários.");
        } else {
            Log.d(TAG, "Nenhum usuário encontrado para o ranking.");
            Toast.makeText(this, "Ranking ainda não disponível.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (usuarioController != null) {
                usuarioController.open();
                carregarRanking(); // Recarrega o ranking caso haja atualizações
            }
        } catch (android.database.SQLException e) {
            Log.e(TAG, "Erro ao reabrir banco no onResume para ranking", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioController != null) {
            usuarioController.close();
        }
    }

    // Se usar Toolbar com botão voltar
    // @Override
    // public boolean onSupportNavigateUp() {
    //     onBackPressed();
    //     return true;
    // }
}