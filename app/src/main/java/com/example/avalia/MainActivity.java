package com.example.avalia;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button btEntrar;
    TextView txtEsqueceuSenha,txtCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaLogin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });
        txtEsqueceuSenha = findViewById(R.id.txtEsqueceuSenha);
        txtCadastro = findViewById(R.id.txtCadastro);
        btEntrar = findViewById(R.id.btEntrar);
        btEntrar.setOnClickListener(this);
        txtCadastro.setOnClickListener(this);
        txtEsqueceuSenha.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btEntrar) {
            Intent entrar = new Intent(this, telaHome.class);
            startActivity(entrar);
        }
        if (v.getId() == R.id.txtCadastro) {
            Intent cadastro = new Intent(this, telaCadastro.class);
            startActivity(cadastro);
        }
        if (v.getId() == R.id.txtEsqueceuSenha) {
            Intent esqueceuSenha = new Intent(this, TelaEsqueceuSenha.class);
            startActivity(esqueceuSenha);
        }

    }
}