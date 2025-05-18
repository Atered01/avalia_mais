package com.example.avalia;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TelaEsqueceuSenha extends AppCompatActivity implements View.OnClickListener {

    TextView txtVoltarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tela_esqueceu_senha);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.telaEsqueceuSenha), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtVoltarLogin = findViewById(R.id.txtVoltarLogin);
        txtVoltarLogin.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.txtVoltarLogin) {
            Intent voltarLogin = new Intent(this, MainActivity.class);
            startActivity(voltarLogin);
        }
    }
}