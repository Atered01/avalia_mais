package com.example.avalia;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog; // Import necessário para o diálogo
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class telaHome extends AppCompatActivity {

    // Declarações dos componentes da UI (como você já tem)
    private TextView textWelcome;
    private ImageView userPhoto;
    private TextView textMotivacional;
    // private CardView cardViewLicoes; // Se precisar referenciar o CardView diretamente
    private TextView textLicoes;
    private Button buttonHome, buttonLicoes, buttonProvas, buttonTopicos, buttonRanking;

    // Nomes das áreas do ENEM (correspondem às chaves no 'bancoDeMissoesPorArea' da TelaMissoesActivity)
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

        // Inicializar os componentes da UI (como você já tem)
        textWelcome = findViewById(R.id.text_welcome);
        userPhoto = findViewById(R.id.user_photo);
        textMotivacional = findViewById(R.id.text_motivacional);
        textLicoes = findViewById(R.id.text_licoes); // ID do TextView dentro do CardView

        buttonHome = findViewById(R.id.buttonHome);
        buttonLicoes = findViewById(R.id.buttonLicoes);
        buttonProvas = findViewById(R.id.buttonProvas);
        buttonTopicos = findViewById(R.id.buttonTopicos);
        buttonRanking = findViewById(R.id.buttonRanking);

        // Configurar o listener para o botão "Lições"
        buttonLicoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarDialogoSelecaoArea(); // Chama o método para exibir o diálogo
            }
        });

        // Você pode adicionar listeners para os outros botões aqui, se necessário
        // buttonHome.setOnClickListener(...);
        // buttonProvas.setOnClickListener(...);
        // etc.
    }

    /**
     * Exibe um diálogo para o usuário selecionar a área do ENEM para as missões.
     */
    private void mostrarDialogoSelecaoArea() {
        AlertDialog.Builder builder = new AlertDialog.Builder(telaHome.this);
        builder.setTitle("Escolha uma Área para as Missões");

        // Define os itens da lista no diálogo (as áreas do ENEM)
        // e o que acontece quando um item é clicado
        builder.setItems(areasEnem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 'which' é o índice do item selecionado na array 'areasEnem'
                String areaSelecionada = areasEnem[which];
                abrirTelaMissoes(areaSelecionada); // Abre a tela de missões com a área escolhida
            }
        });

        // (Opcional) Adicionar um botão "Cancelar" ao diálogo
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Fecha o diálogo
            }
        });

        // Cria e exibe o diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Abre a TelaMissoesActivity passando a área do conhecimento selecionada.
     * @param nomeDaArea A área do ENEM escolhida pelo usuário.
     */
    private void abrirTelaMissoes(String nomeDaArea) {
        Intent intent = new Intent(telaHome.this, tela_missoes.class);
        // A chave "nome_da_area_extra" DEVE ser a mesma usada na TelaMissoesActivity
        // para recuperar este valor (getIntent().getStringExtra("nome_da_area_extra"))
        intent.putExtra("nome_da_area_extra", nomeDaArea);
        startActivity(intent);
    }
}