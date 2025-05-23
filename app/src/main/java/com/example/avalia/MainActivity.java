package com.example.avalia;

import android.content.Intent;
// import android.content.SharedPreferences; // Descomente se/quando for implementar "Lembrar-me"
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Import para Log
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avalia.bancodedados.UsuarioController;

public class MainActivity extends AppCompatActivity {

    private EditText editTextEmailLogin, editTextSenhaLogin;
    private Button buttonLogin;
    private TextView textViewLinkCadastro; // Para o link "Cadastre-se"
    private UsuarioController usuarioController; // Controller para operações de usuário

    private static final String TAG = "MainActivity"; // Tag para logs

    // Constantes para SharedPreferences (se for usar para "Lembrar-me")
    // public static final String PREFS_NAME = "AppLoginPrefs";
    // public static final String PREF_USER_LOGGED_IN_ID = "userLoggedInId";
    // public static final String PREF_USER_LOGGED_IN_EMAIL = "userLoggedInEmail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Define o layout da tela de login

        Log.d(TAG, "onCreate: Iniciando MainActivity.");

        // Inicializa o UsuarioController
        usuarioController = new UsuarioController(this);
        try {
            usuarioController.open(); // Abre a conexão com o banco de dados
            Log.d(TAG, "onCreate: Conexão com o banco de dados aberta via UsuarioController.");
        } catch (android.database.SQLException e) {
            Log.e(TAG, "onCreate: Erro ao abrir o banco de dados!", e);
            Toast.makeText(this, "Erro crítico ao conectar com o banco de dados. O app pode não funcionar corretamente.", Toast.LENGTH_LONG).show();
            // Considerar fechar o app ou desabilitar funcionalidades se o banco não puder ser aberto.
            // Por ora, apenas mostramos um Toast.
            // finish(); // Descomente se quiser fechar o app em caso de erro no banco
            // return;
        }

        // Referencia os componentes da UI a partir do layout XML
        // Certifique-se que os IDs no seu R.layout.activity_main correspondem!
        editTextEmailLogin = findViewById(R.id.txtEmail); // Assumindo ID 'email' para o campo de email
        editTextSenhaLogin = findViewById(R.id.txtSenha); // Assumindo ID 'senha' para o campo de senha
        buttonLogin = findViewById(R.id.btEntrar);       // Assumindo ID 'entrar' para o botão de login
        textViewLinkCadastro = findViewById(R.id.txtCadastro); // Assumindo ID 'cadastrar' para o texto/link de cadastro

        // Configura o listener de clique para o botão de Login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Botão de Login clicado.");
                tentarLogin();
            }
        });

        // Configura o listener de clique para o link "Cadastre-se"
        textViewLinkCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Link de Cadastro clicado.");
                Intent intent = new Intent(MainActivity.this, telaCadastro.class);
                startActivity(intent);
            }
        });

        // // Lógica para "Lembrar-me" (se implementada com SharedPreferences)
        // SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // long userId = prefs.getLong(PREF_USER_LOGGED_IN_ID, -1);
        // if (userId != -1) {
        //     Log.d(TAG, "Usuário já logado (ID: " + userId + "). Indo para telaHome.");
        //     Intent intent = new Intent(MainActivity.this, telaHome.class);
        //     startActivity(intent);
        //     finish(); // Fecha a MainActivity para não voltar para ela
        // }
    }

    private void tentarLogin() {
        String email = editTextEmailLogin.getText().toString().trim();
        String senha = editTextSenhaLogin.getText().toString(); // Senha não deve ter trim() no final

        Log.d(TAG, "tentarLogin: Email digitado: " + email);
        // Não logar a senha!

        // Validações básicas dos campos
        if (TextUtils.isEmpty(email)) {
            editTextEmailLogin.setError("Email é obrigatório.");
            editTextEmailLogin.requestFocus();
            Log.w(TAG, "tentarLogin: Email vazio.");
            return;
        }
        // Opcional: Validação de formato de email aqui também, embora o controller possa fazer
        // if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        //     editTextEmailLogin.setError("Formato de email inválido.");
        //     editTextEmailLogin.requestFocus();
        //     return;
        // }

        if (TextUtils.isEmpty(senha)) {
            editTextSenhaLogin.setError("Senha é obrigatória.");
            editTextSenhaLogin.requestFocus();
            Log.w(TAG, "tentarLogin: Senha vazia.");
            return;
        }

        // Verifica as credenciais usando o UsuarioController
        Usuario usuarioLogado = usuarioController.verificarLogin(email, senha);

        if (usuarioLogado != null) {
            Log.i(TAG, "tentarLogin: Login bem-sucedido para o usuário: " + usuarioLogado.getEmail());
            Toast.makeText(this, "Login bem-sucedido! Bem-vindo, " + usuarioLogado.getNomeCompleto(), Toast.LENGTH_SHORT).show();

            // // Opcional: Salvar o estado de login em SharedPreferences
            // SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
            // editor.putLong(PREF_USER_LOGGED_IN_ID, usuarioLogado.getId());
            // editor.putString(PREF_USER_LOGGED_IN_EMAIL, usuarioLogado.getEmail());
            // editor.apply();
            // Log.d(TAG, "tentarLogin: Dados do usuário salvos em SharedPreferences.");

            // Navega para a tela principal do aplicativo (telaHome)
            Intent intent = new Intent(MainActivity.this, telaHome.class);
            // Você pode passar dados do usuário para a telaHome se necessário, por exemplo:
            // intent.putExtra("USER_ID", usuarioLogado.getId());
            // intent.putExtra("USER_EMAIL", usuarioLogado.getEmail());
            // intent.putExtra("USER_NOME", usuarioLogado.getNomeCompleto());
            startActivity(intent);
            finish(); // Fecha a MainActivity para que o usuário não volte para ela ao pressionar "Voltar"
        } else {
            Log.w(TAG, "tentarLogin: Email ou senha inválidos para: " + email);
            Toast.makeText(this, "Email ou senha inválidos.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // É uma boa prática reabrir a conexão se ela puder ter sido fechada,
        // especialmente se você fechar no onPause().
        try {
            if (usuarioController != null) { // Verifica se foi inicializado
                usuarioController.open();
                Log.d(TAG, "onResume: Conexão com o banco de dados reaberta.");
            }
        } catch (android.database.SQLException e) {
            Log.e(TAG, "onResume: Erro ao reabrir o banco de dados!", e);
            Toast.makeText(this, "Erro ao reconectar com o banco de dados.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Algumas pessoas fecham o banco no onPause, mas isso pode ser problemático
        // se a activity ainda estiver parcialmente visível ou se outra activity precisar
        // do banco logo em seguida. Fechar no onDestroy é geralmente mais seguro.
        // if (usuarioController != null) {
        //     usuarioController.close();
        //     Log.d(TAG, "onPause: Conexão com o banco de dados fechada.");
        // }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fecha a conexão com o banco de dados quando a Activity é destruída
        if (usuarioController != null) {
            usuarioController.close();
            Log.d(TAG, "onDestroy: Conexão com o banco de dados fechada via UsuarioController.");
        }
        Log.d(TAG, "onDestroy: MainActivity destruída.");
    }
}