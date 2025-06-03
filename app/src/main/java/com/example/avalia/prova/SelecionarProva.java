package com.example.avalia.prova; // Ou o pacote onde você criou a Activity

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avalia.R; // Importe sua classe R
import com.example.avalia.TelaProva; // Importe a TelaProva (ou o nome que você deu)
import com.example.avalia.adapters.ProvaSelecaoAdapter;
import com.example.avalia.bancodedados.ProvaController;

import java.util.ArrayList;
import java.util.List;

public class SelecionarProva extends AppCompatActivity implements ProvaSelecaoAdapter.OnProvaClickListener {

    private static final String TAG = "SelecionarProvaActivity";

    private RecyclerView recyclerViewProvas;
    private ProvaSelecaoAdapter provaAdapter;
    private ProvaController provaController;
    private List<Prova> listaDeProvas;
    // private ProgressBar progressBar; // Descomente se for usar

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selecionar_prova);

        recyclerViewProvas = findViewById(R.id.recyclerViewProvas);
        // progressBar = findViewById(R.id.progressBarSelecionarProva); // Descomente se for usar

        // Configurar o RecyclerView
        recyclerViewProvas.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewProvas.setHasFixedSize(true); // Otimização se o tamanho dos itens não muda

        listaDeProvas = new ArrayList<>();
        provaAdapter = new ProvaSelecaoAdapter(listaDeProvas, this); // 'this' implementa OnProvaClickListener
        recyclerViewProvas.setAdapter(provaAdapter);

        provaController = new ProvaController(this);

        carregarProvas();
    }

    private void carregarProvas() {
        // if (progressBar != null) progressBar.setVisibility(View.VISIBLE); // Mostrar ProgressBar
        Log.d(TAG, "Carregando provas do banco de dados...");

        // A busca no SQLite local geralmente é rápida, então não precisa de AsyncTask/Thread separada
        // a menos que você tenha um volume MUITO grande de dados ou operações complexas.
        List<Prova> provasDoBanco = provaController.getTodasProvas();

        if (provasDoBanco != null && !provasDoBanco.isEmpty()) {
            Log.d(TAG, provasDoBanco.size() + " provas carregadas.");
            listaDeProvas.clear();
            listaDeProvas.addAll(provasDoBanco);
            provaAdapter.notifyDataSetChanged(); // Notifica o adapter que os dados mudaram
        } else {
            Log.d(TAG, "Nenhuma prova encontrada no banco de dados.");
            Toast.makeText(this, "Nenhuma prova disponível no momento.", Toast.LENGTH_LONG).show();
        }
        // if (progressBar != null) progressBar.setVisibility(View.GONE); // Esconder ProgressBar
    }

    @Override
    public void onProvaClick(Prova prova) {
        // Este método é chamado quando um item da lista é clicado (da interface OnProvaClickListener)
        Log.d(TAG, "Prova selecionada: " + prova.getNome() + " (ID: " + prova.getId() + ")");
        Toast.makeText(this, "Prova selecionada: " + prova.getNome(), Toast.LENGTH_SHORT).show();

        // Iniciar a TelaProvaActivity, passando o ID da prova
        Intent intent = new Intent(SelecionarProva.this, TelaProva.class);
        intent.putExtra("ID_PROVA_SELECIONADA", prova.getId());
        // Você também pode querer passar o nome da prova para exibir na próxima tela
        intent.putExtra("NOME_PROVA_SELECIONADA", prova.getNome());
        startActivity(intent);
    }
}