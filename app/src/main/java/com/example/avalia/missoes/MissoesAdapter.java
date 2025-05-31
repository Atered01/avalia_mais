package com.example.avalia.missoes; // Certifique-se de que o pacote está correto

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avalia.R;

import java.util.List;

public class MissoesAdapter extends RecyclerView.Adapter<MissoesAdapter.MissaoViewHolder> {

    private List<Missao> listaMissoes;
    private Context context;
    private OnMissaoInteractionListener listener;

    // Interface para comunicar cliques/mudanças para a Activity
    // ATUALIZAÇÃO: Adicionado o parâmetro 'position'
    public interface OnMissaoInteractionListener {
        void onMissaoStatusChanged(Missao missao, boolean isChecked, int position);
    }

    // Construtor
    public MissoesAdapter(Context context, List<Missao> listaMissoes, OnMissaoInteractionListener listener) {
        this.context = context;
        this.listaMissoes = listaMissoes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MissaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_missao, parent, false);
        return new MissaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissaoViewHolder holder, int position) {
        Missao missaoAtual = listaMissoes.get(position);

        // Definir ícone da missão
        if (missaoAtual.getIconResourceId() != 0) {
            holder.imageViewIconeMissao.setImageResource(missaoAtual.getIconResourceId());
        } else {
            holder.imageViewIconeMissao.setImageResource(R.drawable.ic_task_placeholder);
        }

        holder.textViewDescricao.setText(missaoAtual.getDescricao());
        holder.textViewPontos.setText("+" + missaoAtual.getPontos() + " PTS");

        // Configurar o CheckBox e o estilo do texto baseado no estado 'concluida' do objeto Missao
        // O estado 'concluida' do objeto Missao é a fonte da verdade aqui.
        // Ele será atualizado pela TelaMissoes após a confirmação do BD.
        holder.checkBoxConcluida.setOnCheckedChangeListener(null); // Limpar listener antigo
        holder.checkBoxConcluida.setChecked(missaoAtual.isConcluida());

        if (missaoAtual.isConcluida()) {
            holder.textViewDescricao.setPaintFlags(holder.textViewDescricao.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textViewDescricao.setAlpha(0.5f);
        } else {
            holder.textViewDescricao.setPaintFlags(holder.textViewDescricao.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textViewDescricao.setAlpha(1.0f);
        }

        // Configurar o listener para o CheckBox
        holder.checkBoxConcluida.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Verifica se foi uma interação do usuário para evitar chamadas recursivas ou indesejadas
            if (buttonView.isPressed()) {
                if (listener != null) {
                    // Notifica a Activity/Fragment sobre a mudança, passando a posição
                    // A Activity será responsável por atualizar o estado do objeto Missao
                    // e o banco de dados.
                    listener.onMissaoStatusChanged(missaoAtual, isChecked, holder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMissoes != null ? listaMissoes.size() : 0;
    }

    public static class MissaoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewIconeMissao;
        TextView textViewDescricao;
        TextView textViewPontos;
        CheckBox checkBoxConcluida;

        public MissaoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewIconeMissao = itemView.findViewById(R.id.imageViewIconeMissao);
            textViewDescricao = itemView.findViewById(R.id.textViewDescricaoMissao);
            textViewPontos = itemView.findViewById(R.id.textViewPontosMissao);
            checkBoxConcluida = itemView.findViewById(R.id.checkboxMissaoConcluida);
        }
    }

    public void atualizarLista(List<Missao> novasMissoes) {
        this.listaMissoes.clear();
        if (novasMissoes != null) {
            this.listaMissoes.addAll(novasMissoes);
        }
        notifyDataSetChanged(); // Notifica o RecyclerView que os dados mudaram
    }

    // Método para atualizar um item específico, pode ser útil se você não quiser recarregar toda a lista
    // mas a TelaMissoes já está usando notifyItemChanged(position) que internamente fará o rebind.
    // Se você atualizar o objeto na lista da TelaMissoes e chamar notifyItemChanged(position),
    // o onBindViewHolder será chamado para essa posição e redesenhará com os dados atualizados.
}