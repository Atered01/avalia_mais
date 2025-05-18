package com.example.avalia;

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

public class telaCadastro extends AppCompatActivity implements View.OnClickListener {
    Button btCadastrar;
    TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_cadastro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaCadastro), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btCadastrar = findViewById(R.id.btCadastrar);
        txtLogin = findViewById(R.id.txtLogin);
        btCadastrar.setOnClickListener(this);
        txtLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btCadastrar) {

        }
        if (v.getId() == R.id.txtLogin) {
            Intent voltarLogin = new Intent(this, MainActivity.class);
            startActivity(voltarLogin);
        }
    }
}