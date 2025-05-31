package com.example.avalia.usuario;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avalia.GerenciadorDeSessao;
import com.example.avalia.R;
import com.example.avalia.TelaHome;
import com.example.avalia.bancodedados.UsuarioController;

public class TelaLogin extends AppCompatActivity {

    private EditText editTextEmailLogin, editTextSenhaLogin;
    private Button buttonLogin;
    private TextView textViewLinkCadastro;
    private UsuarioController usuarioController;
    private GerenciadorDeSessao gerenciadorDeSessao;

    private static final String TAG = "TelaLogin"; // Tag para logs (MainActivity -> TelaLogin)

    // REMOVIDAS as constantes de SharedPreferences daqui, GerenciadorDeSessao cuida disso.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_login);

        Log.d(TAG, "onCreate: Iniciando TelaLogin.");

        usuarioController = new UsuarioController(this);
        gerenciadorDeSessao = new GerenciadorDeSessao(getApplicationContext()); // Use getApplicationContext para SharedPreferences globais

        // 1. VERIFICAÇÃO DE SESSÃO ÚNICA E CORRETA
        if (gerenciadorDeSessao.isLoggedIn()) {
            Log.d(TAG, "Usuário já logado (ID: " + gerenciadorDeSessao.getUsuarioId() + "). Indo para TelaHome.");
            Intent intent = new Intent(TelaLogin.this, TelaHome.class);
            startActivity(intent);
            finish(); // Fecha a TelaLogin
            return;   // Importante para não continuar o onCreate
        }

        try {
            if (usuarioController != null && !usuarioController.isOpen()) { // Verifica se não está nulo e se não está aberto
                usuarioController.open();
                Log.d(TAG, "onCreate: Conexão com o banco de dados aberta via UsuarioController.");
            }
        } catch (android.database.SQLException e) {
            Log.e(TAG, "onCreate: Erro ao abrir o banco de dados!", e);
            Toast.makeText(this, "Erro crítico ao conectar com o banco de dados.", Toast.LENGTH_LONG).show();
            // Considere o que fazer aqui. Fechar o app pode ser uma opção se o DB for essencial.
            // finish();
            // return;
        }

        editTextEmailLogin = findViewById(R.id.txtEmail);
        editTextSenhaLogin = findViewById(R.id.txtSenha);
        buttonLogin = findViewById(R.id.btEntrar);
        textViewLinkCadastro = findViewById(R.id.txtCadastro);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botão de Login clicado.");
                tentarLogin();
            }
        });

        textViewLinkCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Link de Cadastro clicado.");
                Intent intent = new Intent(TelaLogin.this, TelaCadastro.class);
                startActivity(intent);
            }
        });

        // REMOVIDA a segunda lógica de verificação de SharedPreferences daqui.
    }

    private void tentarLogin() {
        String email = editTextEmailLogin.getText().toString().trim();
        String senha = editTextSenhaLogin.getText().toString();

        Log.d(TAG, "tentarLogin: Email digitado: " + email);

        if (TextUtils.isEmpty(email)) {
            editTextEmailLogin.setError("Email é obrigatório.");
            editTextEmailLogin.requestFocus();
            Log.w(TAG, "tentarLogin: Email vazio.");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmailLogin.setError("Formato de email inválido.");
            editTextEmailLogin.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(senha)) {
            editTextSenhaLogin.setError("Senha é obrigatória.");
            editTextSenhaLogin.requestFocus();
            Log.w(TAG, "tentarLogin: Senha vazia.");
            return;
        }

        Usuario usuarioLogado = null;
        if (usuarioController != null && usuarioController.isOpen()) { // Garante que o controller está pronto
            usuarioLogado = usuarioController.verificarLogin(email, senha);
        } else {
            Log.e(TAG, "tentarLogin: UsuarioController não está aberto ou é nulo.");
            Toast.makeText(this, "Erro ao tentar login. Tente novamente.", Toast.LENGTH_SHORT).show();
            // Tenta reabrir a conexão como uma medida de recuperação, se necessário.
            try {
                if (usuarioController != null) usuarioController.open();
            } catch (Exception e) {
                Log.e(TAG, "tentarLogin: Falha ao tentar reabrir UsuarioController.", e);
            }
            return;
        }


        if (usuarioLogado != null) {
            Log.i(TAG, "tentarLogin: Login bem-sucedido para o usuário: " + usuarioLogado.getEmail());

            // 2. SALVAMENTO DA SESSÃO USANDO O GERENCIADOR DE SESSÃO
            gerenciadorDeSessao.criarSessaoLogin(usuarioLogado.getId(), usuarioLogado.getEmail(), usuarioLogado.getNomeCompleto());
            Log.d(TAG, "tentarLogin: Sessão do usuário criada via GerenciadorDeSessao.");

            Toast.makeText(this, "Login bem-sucedido! Bem-vindo, " + usuarioLogado.getNomeCompleto(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(TelaLogin.this, TelaHome.class);
            // Os extras abaixo são opcionais se a TelaHome já busca os dados via GerenciadorDeSessao e UsuarioController.
            // Manter pode ser útil para o primeiro carregamento da TelaHome, mas ela deve ser capaz de funcionar sem eles.
            // intent.putExtra("USER_ID", usuarioLogado.getId());
            // intent.putExtra("USER_EMAIL", usuarioLogado.getEmail());
            // intent.putExtra("USER_NOME", usuarioLogado.getNomeCompleto());
            startActivity(intent);
            finish();
        } else {
            Log.w(TAG, "tentarLogin: Email ou senha inválidos para: " + email);
            Toast.makeText(this, "Email ou senha inválidos.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (usuarioController != null && !usuarioController.isOpen()) {
                usuarioController.open();
                Log.d(TAG, "onResume: Conexão com o banco de dados (re)aberta.");
            }
        } catch (android.database.SQLException e) {
            Log.e(TAG, "onResume: Erro ao (re)abrir o banco de dados!", e);
            Toast.makeText(this, "Erro ao reconectar com o banco de dados.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usuarioController != null) {
            usuarioController.close();
            Log.d(TAG, "onDestroy: Conexão com o banco de dados fechada via UsuarioController.");
        }
        Log.d(TAG, "onDestroy: TelaLogin destruída.");
    }
}