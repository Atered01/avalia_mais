package com.example.avalia.usuario;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // Import para Log
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.avalia.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

public class TelaCadastro extends AppCompatActivity implements View.OnClickListener {

    // Declaração dos componentes da UI
    private EditText txtCadNome, txtCadNascimento, txtCadCpf, txtCadEmail, txtCadSenha, txtCadRepetirSenha;
    private Button btCadastrar;
    private TextView txtLogin;

    private UsuarioController usuarioController; // Controller para operações de usuário
    private Calendar calendarioNascimento;
    private static final String TAG = "TelaCadastro"; // Tag para logs

    // Padrão simples para validar CPF (aceita com ou sem pontos/traço, mas precisa ter 11 dígitos numéricos)
    private static final Pattern CPF_PATTERN = Pattern.compile("^\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_cadastro); // Seu layout XML

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaCadastro), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.d(TAG, "onCreate: Iniciando TelaCadastro.");

        // Inicializa o UsuarioController
        usuarioController = new UsuarioController(this);
        try {
            usuarioController.open(); // Abre a conexão com o banco de dados
            Log.d(TAG, "onCreate: Conexão com o banco de dados aberta via UsuarioController.");
        } catch (android.database.SQLException e) {
            Log.e(TAG, "onCreate: Erro ao abrir o banco de dados!", e);
            Toast.makeText(this, "Erro crítico ao conectar com o banco de dados.", Toast.LENGTH_LONG).show();
            // Considerar fechar a activity se o banco não puder ser aberto
            finish();
            return;
        }

        calendarioNascimento = Calendar.getInstance();

        // Inicializa os componentes da UI
        txtCadNome = findViewById(R.id.txtCadNome);
        txtCadNascimento = findViewById(R.id.txtCadNascimento);
        txtCadCpf = findViewById(R.id.txtCadCpf);
        txtCadEmail = findViewById(R.id.txtCadEmail);
        txtCadSenha = findViewById(R.id.txtCadSenha);
        txtCadRepetirSenha = findViewById(R.id.txtCadRepetirSenha);
        btCadastrar = findViewById(R.id.btCadastrar);
        txtLogin = findViewById(R.id.txtLogin);

        // Configura os listeners de clique
        btCadastrar.setOnClickListener(this);
        txtLogin.setOnClickListener(this);

        // Configurar DatePickerDialog para o campo de data de nascimento
        final DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendarioNascimento.set(Calendar.YEAR, year);
                calendarioNascimento.set(Calendar.MONTH, monthOfYear);
                calendarioNascimento.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                atualizarCampoDataNascimento();
            }
        };

        txtCadNascimento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(TelaCadastro.this, dateSetListener,
                        calendarioNascimento.get(Calendar.YEAR),
                        calendarioNascimento.get(Calendar.MONTH),
                        calendarioNascimento.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        // Para impedir a digitação direta e forçar o uso do DatePicker
        txtCadNascimento.setFocusable(false);
        txtCadNascimento.setClickable(true);
    }

    private void atualizarCampoDataNascimento() {
        String formatoData = "dd/MM/yyyy"; // Formato de exibição
        SimpleDateFormat sdf = new SimpleDateFormat(formatoData, Locale.getDefault());
        txtCadNascimento.setText(sdf.format(calendarioNascimento.getTime()));
        Log.d(TAG, "Data de nascimento selecionada: " + txtCadNascimento.getText().toString());
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btCadastrar) {
            Log.d(TAG, "Botão Cadastrar clicado.");
            realizarCadastro();
        } else if (viewId == R.id.txtLogin) {
            Log.d(TAG, "Link Login clicado.");
            Intent voltarLogin = new Intent(this, TelaLogin.class);
            startActivity(voltarLogin);
            finish(); // Fecha a tela de cadastro ao voltar para o login
        }
    }

    private void realizarCadastro() {
        String nome = txtCadNome.getText().toString().trim();
        String dataNascimento = txtCadNascimento.getText().toString().trim(); // Formato dd/MM/yyyy
        String cpf = txtCadCpf.getText().toString().trim();
        String email = txtCadEmail.getText().toString().trim();
        String senha = txtCadSenha.getText().toString(); // Não fazer trim na senha
        String repetirSenha = txtCadRepetirSenha.getText().toString();

        // Validações
        if (TextUtils.isEmpty(nome)) {
            txtCadNome.setError("Nome completo é obrigatório.");
            txtCadNome.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(dataNascimento)) {
            txtCadNascimento.setError("Data de nascimento é obrigatória.");
            Toast.makeText(this, "Por favor, selecione sua data de nascimento.", Toast.LENGTH_SHORT).show();
            return;
        }

        String cpfNumerico = cpf.replaceAll("[^0-9]", ""); // Remove pontos, traços, etc.
        if (TextUtils.isEmpty(cpfNumerico)) {
            txtCadCpf.setError("CPF é obrigatório.");
            txtCadCpf.requestFocus();
            return;
        }
        if (cpfNumerico.length() != 11) {
            txtCadCpf.setError("CPF deve conter 11 dígitos.");
            txtCadCpf.requestFocus();
            return;
        }
        // Uma validação de formato mais visual (XXX.XXX.XXX-XX) pode ser feita com InputMasks ou TextWatchers se desejado,
        // mas para o banco salvamos apenas os números.

        if (TextUtils.isEmpty(email)) {
            txtCadEmail.setError("Email é obrigatório.");
            txtCadEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            txtCadEmail.setError("Insira um email válido.");
            txtCadEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(senha)) {
            txtCadSenha.setError("Senha é obrigatória.");
            txtCadSenha.requestFocus();
            return;
        }
        if (senha.length() < 6) {
            txtCadSenha.setError("Senha deve ter no mínimo 6 caracteres.");
            txtCadSenha.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(repetirSenha)) {
            txtCadRepetirSenha.setError("Confirme a senha.");
            txtCadRepetirSenha.requestFocus();
            return;
        }
        if (!senha.equals(repetirSenha)) {
            txtCadRepetirSenha.setError("As senhas não coincidem.");
            txtCadRepetirSenha.requestFocus();
            return;
        }

        Log.d(TAG, "Validações de campos passaram. Verificando email existente...");
        // Verificar se o email já existe no banco
        if (usuarioController.verificarEmailExistente(email)) {
            Toast.makeText(this, "Este email já está cadastrado. Tente outro.", Toast.LENGTH_LONG).show();
            txtCadEmail.requestFocus();
            Log.w(TAG, "Tentativa de cadastro com email já existente: " + email);
            return;
        }

        Log.d(TAG, "Email não existe. Tentando adicionar usuário ao banco...");
        // Se todas as validações passarem, adicionar ao banco de dados
        // Passando o CPF apenas com números
        long idNovoUsuario = usuarioController.adicionarUsuario(nome, email, senha, dataNascimento, cpfNumerico);

        if (idNovoUsuario != -1) {
            Log.i(TAG, "Cadastro realizado com sucesso! ID do usuário: " + idNovoUsuario);
            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show();
            // Navegar para a tela de login ou home
            Intent intent = new Intent(TelaCadastro.this, TelaLogin.class); // Mude para telaHome se quiser ir direto
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity(); // Fecha todas as activities da task atual e a tela de cadastro
        } else {
            Log.e(TAG, "Erro ao realizar o cadastro para o email: " + email);
            Toast.makeText(this, "Erro ao realizar o cadastro. Verifique os logs ou tente novamente.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reabre a conexão com o banco se ela não estiver aberta (importante se fechada no onPause)
        try {
            if (usuarioController != null) {
                usuarioController.open();
                Log.d(TAG, "onResume: Conexão com o banco de dados (re)aberta.");
            }
        } catch (android.database.SQLException e) {
            Log.e(TAG, "onResume: Erro ao (re)abrir o banco de dados!", e);
            Toast.makeText(this, "Erro ao reconectar com o banco de dados.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // É uma opção fechar aqui, mas geralmente onDestroy é suficiente para
        // a instância do controller gerenciada pela activity.
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
        Log.d(TAG, "onDestroy: TelaCadastro destruída.");
    }
}