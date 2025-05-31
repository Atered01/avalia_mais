package com.example.avalia;

// ... todos os outros imports permanecem os mesmos ...

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avalia.bancodedados.MissoesController;
import com.example.avalia.bancodedados.UsuarioController;
import com.example.avalia.missoes.Missao;
import com.example.avalia.missoes.TelaMissoes;
import com.example.avalia.usuario.TelaLogin;
import com.example.avalia.usuario.Usuario;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TelaHome extends AppCompatActivity {

    private static final String TAG = "TelaHome";

    private TextView textWelcome;
    private ImageView userPhoto;
    private TextView textLicoesInfo;
    private Button buttonHome, buttonLicoes, buttonProvas, buttonIa, buttonRanking;

    private MissoesController missoesController;
    private UsuarioController usuarioController;
    private GerenciadorDeSessao gerenciadorDeSessao;

    private Usuario usuarioLogado;
    private long idUsuarioLogado;

    private ActivityResultLauncher<Intent> galleryLauncher;

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

        gerenciadorDeSessao = new GerenciadorDeSessao(getApplicationContext());
        long idDoUsuarioLogado = gerenciadorDeSessao.getUsuarioId();
        usuarioController = new UsuarioController(this);
        missoesController = new MissoesController(this);

        try {
            if (usuarioController != null && !usuarioController.isOpen()) usuarioController.open();
            if (missoesController != null && !missoesController.isOpen()) missoesController.open();
            Log.d(TAG, "onCreate: Conexões com controllers abertas.");
        } catch (SQLiteException e) {
            Log.e(TAG, "onCreate: Erro ao abrir banco de dados!", e);
            Toast.makeText(this, "Erro crítico ao conectar com o banco de dados.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (!gerenciadorDeSessao.isLoggedIn()) {
            Log.w(TAG, "Usuário não logado. Redirecionando para TelaLogin.");
            Intent intent = new Intent(TelaHome.this, TelaLogin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        idUsuarioLogado = gerenciadorDeSessao.getUsuarioId();
        Log.d(TAG, "ID do Usuário Logado: " + idUsuarioLogado);

        inicializarUI();
        configurarListeners();

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            try {
                                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                userPhoto.setImageBitmap(bitmap);
                                gerenciadorDeSessao.salvarUriFotoPerfil(imageUri.toString());
                                Log.d(TAG, "Foto de perfil atualizada e URI salvo: " + imageUri.toString());
                            } catch (FileNotFoundException e) {
                                Log.e(TAG, "Arquivo da imagem não encontrado", e);
                                Toast.makeText(TelaHome.this, "Não foi possível carregar a imagem.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void inicializarUI() {
        textWelcome = findViewById(R.id.text_welcome);
        userPhoto = findViewById(R.id.user_photo);
        textLicoesInfo = findViewById(R.id.text_licoes);

        buttonHome = findViewById(R.id.buttonHome);
        buttonLicoes = findViewById(R.id.buttonLicoes);
        buttonProvas = findViewById(R.id.buttonProvas);
        buttonIa = findViewById(R.id.buttonIa);
        buttonRanking = findViewById(R.id.buttonRanking);
    }

    private void configurarListeners() {
        buttonLicoes.setOnClickListener(v -> mostrarDialogoSelecaoArea(false));
        buttonIa.setOnClickListener(v -> mostrarDialogoSelecaoArea(true));
        buttonRanking.setOnClickListener(v -> {
            Intent intent = new Intent(TelaHome.this, TelaRanking.class);
            startActivity(intent);
        });

        userPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });
    }

    private void carregarEAtualizarDadosDoUsuario() {
        if (idUsuarioLogado == -1) {
            Log.e(TAG, "ID do usuário logado é -1. Não é possível carregar dados.");
            gerenciadorDeSessao.logoutUser();
            Intent i = new Intent(TelaHome.this, TelaLogin.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        // Garante que o controller esteja aberto para buscar o usuário
        if (usuarioController != null && !usuarioController.isOpen()) {
            try {
                usuarioController.open();
            } catch (SQLiteException e) {
                Log.e(TAG, "Falha ao abrir UsuarioController em carregarEAtualizarDadosDoUsuario", e);
                Toast.makeText(this, "Erro ao carregar dados do usuário.", Toast.LENGTH_SHORT).show();
                return; // Não podemos prosseguir
            }
        }
        usuarioLogado = usuarioController.getUsuarioPorId(idUsuarioLogado);

        if (usuarioLogado != null) {
            Log.d(TAG, "Dados do usuário carregados: " + usuarioLogado.getNomeCompleto());
            textWelcome.setText("Bem vindo, " + usuarioLogado.getNomeCompleto() + "!");

            String uriFotoSalva = gerenciadorDeSessao.getUriFotoPerfil();
            if (uriFotoSalva != null && !uriFotoSalva.isEmpty()) {
                try {
                    Uri imageUri = Uri.parse(uriFotoSalva);
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    userPhoto.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Arquivo da foto de perfil salva não encontrado", e);
                    userPhoto.setImageResource(R.drawable.ic_launcher_foreground);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao carregar foto de perfil: ", e);
                    userPhoto.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                userPhoto.setImageResource(R.drawable.ic_launcher_foreground);
            }

            // Garante que o controller esteja aberto para buscar missões
            if (missoesController != null && !missoesController.isOpen()) {
                try {
                    missoesController.open();
                } catch (SQLiteException e) {
                    Log.e(TAG, "Falha ao abrir MissoesController em carregarEAtualizarDadosDoUsuario", e);
                    Toast.makeText(this, "Erro ao carregar dados de missões.", Toast.LENGTH_SHORT).show();
                    textLicoesInfo.setText("Informações de missões indisponíveis");
                    return; // Não podemos prosseguir com a parte de missões
                }
            }
            int pontuacaoTotal = usuarioLogado.getPontuacaoTotal();
            int missoesPendentes = missoesController.getNumeroMissoesPendentesParaUsuario(idUsuarioLogado);

            String infoUsuario = String.format(Locale.getDefault(), "%s. %d Pontos",
                    usuarioLogado.getNomeCompleto(), pontuacaoTotal);
            String infoMissoes = String.format(Locale.getDefault(), "%d Missões Pendentes",
                    missoesPendentes);

            textLicoesInfo.setText(infoUsuario + "\n" + infoMissoes);
            Log.d(TAG, "Painel do usuário atualizado: " + infoUsuario + " | " + infoMissoes);

        } else {
            Log.e(TAG, "Não foi possível carregar os dados do usuário logado com ID: " + idUsuarioLogado);
            textWelcome.setText("Bem-vindo!");
            textLicoesInfo.setText("Informações do usuário indisponíveis");
            gerenciadorDeSessao.logoutUser();
            Intent i = new Intent(TelaHome.this, TelaLogin.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        }
    }

    private void mostrarDialogoSelecaoArea(final boolean abrirChatbotAposSelecao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(TelaHome.this);
        String tituloDialogo = abrirChatbotAposSelecao ? "Escolha uma Área para o Chatbot" : "Escolha uma Área para as Missões";
        builder.setTitle(tituloDialogo);

        builder.setItems(areasEnem, (dialog, which) -> {
            String areaSelecionada = areasEnem[which];
            if (abrirChatbotAposSelecao) {
                Log.d(TAG, "Área selecionada para Chatbot: " + areaSelecionada);
                abrirChatbotComMissoes(areaSelecionada);
            } else {
                Log.d(TAG, "Área selecionada para Missões: " + areaSelecionada);
                abrirTelaMissoes(areaSelecionada);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void abrirTelaMissoes(String nomeDaArea) {
        Intent intent = new Intent(TelaHome.this, TelaMissoes.class);
        intent.putExtra("nome_da_area_extra", nomeDaArea);
        intent.putExtra("id_usuario_logado", idUsuarioLogado); // Passando o ID do usuário
        startActivity(intent);
    }

    private void abrirChatbotComMissoes(String nomeDaArea) {
        if (missoesController == null || !missoesController.isOpen()) {
            Log.e(TAG, "MissoesController não inicializado ou fechado para chatbot.");
            Toast.makeText(this, "Erro: Serviço de missões não disponível para o chatbot.", Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<String> descricoes = new ArrayList<>();
        try {
            // ATUALIZAÇÃO AQUI: Usar o método que considera o usuário
            // Para o chatbot, podemos passar -1 como idUsuarioLogado se quisermos todas as missões da área,
            // ou o idUsuarioLogado se quisermos apenas as que ele ainda não concluiu (ou todas, dependendo da lógica do chatbot)
            // Por ora, vamos pegar todas da área, independentemente do status do usuário, para popular o chatbot.
            // Se você quiser apenas as pendentes para o chatbot, ajuste a lógica aqui.
            List<Missao> missoesDaArea = missoesController.getMissoesPorAreaParaUsuario(nomeDaArea, -1); // Passando -1 para pegar todas da área
            // ou idUsuarioLogado para pegar personalizadas

            if (missoesDaArea != null && !missoesDaArea.isEmpty()) {
                for (Missao missao : missoesDaArea) {
                    descricoes.add(missao.getDescricao());
                }
            } else {
                Log.d(TAG, "Nenhuma missão encontrada para a área " + nomeDaArea + " para o chatbot.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar missões para o chatbot: ", e);
        }

        if (descricoes.isEmpty()) {
            Toast.makeText(this, "Nenhuma descrição de missão disponível para esta área no momento.", Toast.LENGTH_SHORT).show();
            // Não abrir o chatbot se não houver missões para exibir, ou o chatbot pode ter uma mensagem padrão.
            // return; // Opcional: não abrir se vazio
        }

        Intent intent = new Intent(TelaHome.this, ChatBot.class); // Supondo que sua classe ChatBot se chame ChatBot.class
        intent.putStringArrayListExtra("lista_descricoes_missoes", descricoes);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: TelaHome resumida.");
        try {
            if (usuarioController != null && !usuarioController.isOpen()) usuarioController.open();
            if (missoesController != null && !missoesController.isOpen()) missoesController.open();
            Log.d(TAG, "onResume: Conexões com controllers (re)abertas se necessário.");

            if (gerenciadorDeSessao.isLoggedIn()) {
                idUsuarioLogado = gerenciadorDeSessao.getUsuarioId(); // Garante que temos o ID mais recente
                if (idUsuarioLogado != -1) {
                    carregarEAtualizarDadosDoUsuario();
                } else {
                    // Se o ID ainda for -1 após pegar da sessão, é um problema.
                    Log.e(TAG, "onResume: ID do usuário é -1 mesmo após pegar da sessão. Deslogando.");
                    gerenciadorDeSessao.logoutUser();
                    Intent i = new Intent(TelaHome.this, TelaLogin.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            } else {
                Log.w(TAG, "onResume: Usuário não está logado. Redirecionando para TelaLogin.");
                Intent intent = new Intent(TelaHome.this, TelaLogin.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        } catch (SQLException e) {
            Log.e(TAG, "onResume: Erro ao (re)abrir banco de dados!", e);
            Toast.makeText(this, "Erro ao reconectar com o banco de dados.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (missoesController != null) missoesController.close();
        if (usuarioController != null) usuarioController.close();
        Log.d(TAG, "onDestroy: Conexões com controllers fechadas. TelaHome destruída.");
    }
}