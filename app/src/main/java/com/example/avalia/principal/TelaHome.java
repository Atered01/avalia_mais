package com.example.avalia.principal;

// ... (imports existentes) ...
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
// Remova import android.database.SQLException; e android.database.sqlite.SQLiteException; daqui se não forem mais usados diretamente na TelaHome
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

import com.example.avalia.R;
import com.example.avalia.missoes.MissoesController;
import com.example.avalia.prova.ProvaController;
import com.example.avalia.usuario.GerenciadorDeSessao;
import com.example.avalia.usuario.UsuarioController;
import com.example.avalia.chatbot.ChatBot;
import com.example.avalia.missoes.Missao;
import com.example.avalia.missoes.TelaMissoes;
// Importe a futura tela de seleção de provas
// import com.example.avalia.prova.SelecionarProvaActivity; (Quando for criada)
import com.example.avalia.prova.SelecionarProva;
import com.example.avalia.ranking.TelaRanking;
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
    private ProvaController provaController; // Adicionado para consistência na declaração
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

        // Instanciar controllers
        usuarioController = new UsuarioController(this);
        missoesController = new MissoesController(this);
        provaController = new ProvaController(this); // Instanciado aqui

        // Popular dados iniciais das provas (este método já gerencia sua própria conexão)
        provaController.popularDadosIniciaisSeVazio();

        // Bloco de abertura explícita dos controllers foi removido daqui (assumindo refatoração dos controllers)

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
        buttonLicoes.setOnClickListener(v -> mostrarDialogoSelecaoArea(false)); // Para Missões
        buttonIa.setOnClickListener(v -> mostrarDialogoSelecaoArea(true));     // Para Chatbot

        buttonRanking.setOnClickListener(v -> {
            Intent intent = new Intent(TelaHome.this, TelaRanking.class);
            startActivity(intent);
        });

        userPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            galleryLauncher.launch(intent);
        });

        // Adicionar listener para o botão de Provas
        buttonProvas.setOnClickListener(v -> {
            Log.d(TAG, "Botão Provas clicado.");

            // Descomente e ajuste o nome da classe da Activity se necessário
            Intent intent = new Intent(TelaHome.this, SelecionarProva.class);
            startActivity(intent);

            // Você pode remover ou comentar a linha do Toast agora,
            // a menos que queira mantê-la para depuração por um tempo.
            // Toast.makeText(TelaHome.this, "Indo para seleção de provas...", Toast.LENGTH_SHORT).show();
        });
    }

    private void carregarEAtualizarDadosDoUsuario() {
        if (idUsuarioLogado <= 0) { // ID de usuário inválido
            Log.e(TAG, "ID do usuário logado é inválido ("+ idUsuarioLogado +"). Não é possível carregar dados. Deslogando.");
            fazerLogoutEIrParaLogin();
            return;
        }

        // Métodos dos controllers agora gerenciam seu próprio open/close
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
                    userPhoto.setImageResource(R.drawable.ic_launcher_foreground); // Placeholder
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao carregar foto de perfil: ", e);
                    userPhoto.setImageResource(R.drawable.ic_launcher_foreground); // Placeholder
                }
            } else {
                userPhoto.setImageResource(R.drawable.ic_launcher_foreground); // Placeholder
            }

            int pontuacaoTotal = usuarioLogado.getPontuacaoTotal();
            // missoesController.getNumeroMissoesPendentesParaUsuario já gerencia open/close
            int missoesPendentes = missoesController.getNumeroMissoesPendentesParaUsuario(idUsuarioLogado);

            String infoUsuario = String.format(Locale.getDefault(), "%s. %d Pontos",
                    usuarioLogado.getNomeCompleto(), pontuacaoTotal);
            String infoMissoes = String.format(Locale.getDefault(), "%d Missões Pendentes",
                    missoesPendentes);

            textLicoesInfo.setText(infoUsuario + "\n" + infoMissoes);
            Log.d(TAG, "Painel do usuário atualizado: " + infoUsuario + " | " + infoMissoes);

        } else {
            Log.e(TAG, "Não foi possível carregar os dados do usuário logado com ID: " + idUsuarioLogado + ". Deslogando.");
            fazerLogoutEIrParaLogin();
        }
    }

    private void fazerLogoutEIrParaLogin() {
        gerenciadorDeSessao.logoutUser();
        Intent i = new Intent(TelaHome.this, TelaLogin.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
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
        intent.putExtra("id_usuario_logado", idUsuarioLogado);
        startActivity(intent);
    }

    private void abrirChatbotComMissoes(String nomeDaArea) {
        // missoesController.getMissoesPorAreaParaUsuario já gerencia open/close
        ArrayList<String> descricoes = new ArrayList<>();
        List<Missao> missoesDaArea = missoesController.getMissoesPorAreaParaUsuario(nomeDaArea, -1);

        if (missoesDaArea != null && !missoesDaArea.isEmpty()) {
            for (Missao missao : missoesDaArea) {
                descricoes.add(missao.getDescricao());
            }
        } else {
            Log.d(TAG, "Nenhuma missão encontrada para a área " + nomeDaArea + " para o chatbot.");
        }
        // ... (resto da lógica do chatbot)
        Intent intent = new Intent(TelaHome.this, ChatBot.class);
        intent.putStringArrayListExtra("lista_descricoes_missoes", descricoes);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: TelaHome resumida.");
        // O bloco try-catch para abrir controllers foi removido daqui,
        // pois os controllers agora gerenciam suas conexões por método.

        if (gerenciadorDeSessao.isLoggedIn()) {
            idUsuarioLogado = gerenciadorDeSessao.getUsuarioId();
            if (idUsuarioLogado > 0) { // ID válido
                carregarEAtualizarDadosDoUsuario();
            } else {
                Log.e(TAG, "onResume: ID do usuário da sessão é inválido. Deslogando.");
                fazerLogoutEIrParaLogin();
            }
        } else {
            Log.w(TAG, "onResume: Usuário não está logado. Redirecionando para TelaLogin.");
            fazerLogoutEIrParaLogin();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // É uma boa prática fechar os controllers aqui, assumindo que
        // os métodos close() deles fecham apenas a instância 'database' e não o 'dbHelper'.
        if (missoesController != null) {
            // missoesController.close(); // Se o close() do controller só fecha 'database'
        }
        if (usuarioController != null) {
            // usuarioController.close(); // Se o close() do controller só fecha 'database'
        }
        if (provaController != null) {
            // provaController.close(); // Se o close() do controller só fecha 'database'
        }
        // Se os métodos já fecham a 'database' no finally, chamar close() aqui para o controller
        // pode não ser estritamente necessário, mas também não prejudica se o close() for idempotente
        // (ou seja, chamar close() em uma conexão já fechada não causa erro).
        // O principal é NÃO fechar o dbHelper globalmente aqui.
        Log.d(TAG, "onDestroy: TelaHome destruída.");
    }
}