package com.example.avalia.missoes;

import android.database.SQLException; // Correto
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avalia.usuario.GerenciadorDeSessao;
import com.example.avalia.R;
import com.example.avalia.usuario.UsuarioController;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TelaMissoes extends AppCompatActivity implements MissoesAdapter.OnMissaoInteractionListener {
    private final String TAG = "TelaMissoes";
    private TextView textViewTituloArea;
    private TextView textViewPontuacaoLocal; // Renomeado para clareza (pontos desta tela)
    private RecyclerView recyclerViewMissoes;
    private Button buttonVoltarMissoes;
    private MissoesAdapter missoesAdapter;
    private List<Missao> listaDeMissoesAtual;
    private String nomeAreaAtual;
    // private int pontuacaoTotalUsuario = 0; // A pontuação total do usuário virá do UsuarioController
    private MissoesController missoesController;
    private UsuarioController usuarioController;
    private GerenciadorDeSessao gerenciadorDeSessao;
    private long idUsuarioLogado = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_missoes);

        gerenciadorDeSessao = new GerenciadorDeSessao(getApplicationContext());
        usuarioController = new UsuarioController(this);
        missoesController = new MissoesController(this);

        try {
            if (missoesController != null && !missoesController.isOpen()) missoesController.open();
            if (usuarioController != null && !usuarioController.isOpen()) usuarioController.open();
        } catch (SQLException e) { // Usar SQLException mais genérico
            Log.e(TAG, "Erro ao abrir o banco de dados!", e);
            Toast.makeText(this, "Erro crítico ao conectar com o banco de dados!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Pegar ID do usuário da Intent, como fizemos na TelaHome ao chamá-la
        idUsuarioLogado = getIntent().getLongExtra("id_usuario_logado", -1);
        // Fallback para o gerenciador de sessão se não vier pela Intent (embora devesse vir)
        if (idUsuarioLogado == -1) {
            idUsuarioLogado = gerenciadorDeSessao.getUsuarioId();
        }

        if (idUsuarioLogado == -1) {
            Toast.makeText(this, "Erro: Usuário não identificado. Faça login novamente.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textViewTituloArea = findViewById(R.id.textViewTituloArea);
        textViewPontuacaoLocal = findViewById(R.id.textViewPontuacaoTotal); // ID do XML é textViewPontuacaoTotal
        recyclerViewMissoes = findViewById(R.id.recyclerViewMissoes);
        buttonVoltarMissoes = findViewById(R.id.buttonVoltarMissoes);

        nomeAreaAtual = getIntent().getStringExtra("nome_da_area_extra");
        if (nomeAreaAtual == null || nomeAreaAtual.isEmpty()) {
            nomeAreaAtual = "Área Desconhecida";
            Toast.makeText(this, "Erro: Área do conhecimento não especificada.", Toast.LENGTH_LONG).show();
            // Considerar finalizar a activity se a área for essencial
        }
        textViewTituloArea.setText("Missões de " + nomeAreaAtual);

        carregarMissoesDaArea(); // Novo método para carregar/recarregar missões

        buttonVoltarMissoes.setOnClickListener(v -> finish());
    }

    private void carregarMissoesDaArea() {
        if (missoesController == null || idUsuarioLogado == -1) {
            listaDeMissoesAtual = new ArrayList<>(); // Evita NullPointerException
            Toast.makeText(this, "Não foi possível carregar missões.", Toast.LENGTH_SHORT).show();
        } else {
            // ATUALIZAÇÃO AQUI: Usar o método que considera o usuário
            listaDeMissoesAtual = missoesController.getMissoesPorAreaParaUsuario(nomeAreaAtual, idUsuarioLogado);
        }

        if (listaDeMissoesAtual.isEmpty() && !nomeAreaAtual.equals("Área Desconhecida")) {
            Toast.makeText(this, "Nenhuma missão encontrada para " + nomeAreaAtual, Toast.LENGTH_SHORT).show();
        }

        if (recyclerViewMissoes.getAdapter() == null) {
            missoesAdapter = new MissoesAdapter(this, listaDeMissoesAtual, this);
            recyclerViewMissoes.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewMissoes.setAdapter(missoesAdapter);
        } else {
            missoesAdapter.atualizarLista(listaDeMissoesAtual); // Supondo que MissoesAdapter tem este método
        }
        calcularEAtualizarPontuacaoLocal();
    }

    private void calcularEAtualizarPontuacaoLocal() {
        int pontuacaoLocal = 0;
        if (listaDeMissoesAtual != null) {
            for (Missao missao : listaDeMissoesAtual) {
                // O estado 'concluida' do objeto Missao já foi definido em getMissoesPorAreaParaUsuario
                if (missao.isConcluida()) {
                    pontuacaoLocal += missao.getPontos();
                }
            }
        }
        // Exibe a soma dos pontos das missões CONCLUÍDAS PARA O USUÁRIO NESTA ÁREA ESPECÍFICA
        textViewPontuacaoLocal.setText(String.format(Locale.getDefault(), "Pontos na Área: %d pts", pontuacaoLocal));
    }

    //AKI

    // Interface MissoesAdapter.OnMissaoInteractionListener precisa ter o parâmetro position
    // public interface OnMissaoInteractionListener {
    //    void onMissaoStatusChanged(Missao missao, boolean isChecked, int position);
    // }
    // E no MissoesAdapter, no onBindViewHolder, ao configurar o listener do checkbox:
    // holder.checkBoxConcluida.setOnCheckedChangeListener((buttonView, isChecked) -> {
    //     if (listener != null) {
    //         listener.onMissaoStatusChanged(missaoAtual, isChecked, holder.getAdapterPosition());
    //     }
    // });


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (missoesController != null) missoesController.close();
        if (usuarioController != null) usuarioController.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (missoesController != null && !missoesController.isOpen()) missoesController.open();
            if (usuarioController != null && !usuarioController.isOpen()) usuarioController.open();

            // Recarregar missões e atualizar pontuação ao resumir
            if (idUsuarioLogado != -1) { // Garante que temos um usuário
                carregarMissoesDaArea();
            }

        } catch (SQLException e) {
            Log.e(TAG, "Erro ao reabrir banco de dados no onResume", e);
            Toast.makeText(this, "Erro ao conectar com servidor de missões.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onMissaoStatusChanged(Missao missao, boolean isChecked, int position) {
        if (idUsuarioLogado == -1) {
            Toast.makeText(this, "Não é possível atualizar: usuário não logado.", Toast.LENGTH_SHORT).show();
            // Reverter o checkbox visualmente se necessário
            missoesAdapter.notifyItemChanged(position);
            return;
        }

        boolean sucessoOperacaoDB;
        int pontosDaMissao = missao.getPontos();

        if (isChecked) { // Usuário MARCOU a missão como concluída
            sucessoOperacaoDB = missoesController.marcarMissaoComoConcluidaPeloUsuario(idUsuarioLogado, missao.getIdOriginal());
            if (sucessoOperacaoDB) {
                Log.d(TAG, "Missão " + missao.getIdOriginal() + " marcada como concluída no BD.");
                // Adicionar pontos ao total do usuário
                boolean sucessoPontuacao = usuarioController.atualizarPontuacaoUsuario(idUsuarioLogado, pontosDaMissao);
                if (!sucessoPontuacao) {
                    Log.e(TAG, "Falha ao ADICIONAR pontuação do usuário " + idUsuarioLogado);
                    // Considerar reverter a marcação da missão no BD ou notificar erro crítico
                    missoesController.desmarcarMissaoComoConcluidaPeloUsuario(idUsuarioLogado, missao.getIdOriginal()); // Tenta reverter
                    sucessoOperacaoDB = false; // Indica que a operação completa falhou
                }
            } else {
                Log.e(TAG, "Falha ao MARCAR missão " + missao.getIdOriginal() + " como concluída no BD.");
            }
        } else { // Usuário DESMARCOU a missão
            sucessoOperacaoDB = missoesController.desmarcarMissaoComoConcluidaPeloUsuario(idUsuarioLogado, missao.getIdOriginal());
            if (sucessoOperacaoDB) {
                Log.d(TAG, "Missão " + missao.getIdOriginal() + " desmarcada no BD.");
                // Subtrair pontos do total do usuário
                boolean sucessoPontuacao = usuarioController.atualizarPontuacaoUsuario(idUsuarioLogado, -pontosDaMissao); // Subtrai
                if (!sucessoPontuacao) {
                    Log.e(TAG, "Falha ao SUBTRAIR pontuação do usuário " + idUsuarioLogado);
                    // Considerar reverter a desmarcação da missão no BD ou notificar erro crítico
                    missoesController.marcarMissaoComoConcluidaPeloUsuario(idUsuarioLogado, missao.getIdOriginal()); // Tenta reverter
                    sucessoOperacaoDB = false; // Indica que a operação completa falhou
                }
            } else {
                Log.e(TAG, "Falha ao DESMARCAR missão " + missao.getIdOriginal() + " no BD.");
            }
        }

        if (sucessoOperacaoDB) {
            // Atualiza o estado do objeto na lista local para reflexão imediata na UI
            missao.setConcluida(isChecked); // Assumindo que Missao.setConcluida() existe
            missoesAdapter.notifyItemChanged(position); // Atualiza apenas o item modificado

            calcularEAtualizarPontuacaoLocal(); // Recalcula pontos desta tela

            String feedback = missao.getDescricao() + (isChecked ? " concluída!" : " marcada como pendente.");
            Toast.makeText(this, feedback, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao atualizar status da missão. Tente novamente.", Toast.LENGTH_SHORT).show();
            // Reverter o estado visual do checkbox para o estado anterior à tentativa
            // É importante que o adapter não assuma a mudança se o BD falhou.
            // Recarregar a lista pode ser uma opção mais simples para garantir consistência visual.
            carregarMissoesDaArea(); // Recarrega a lista para refletir o estado real do BD
        }
    }
}
