package com.example.avalia; // Certifique-se de que o pacote está correto

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView; // Import necessário para ImageView
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MissoesAdapter extends RecyclerView.Adapter<MissoesAdapter.MissaoViewHolder> {

    private List<Missao> listaMissoes;
    private Context context; // Embora não usado diretamente no código abaixo, é bom ter para futuras necessidades
    private OnMissaoInteractionListener listener;

    // Interface para comunicar cliques/mudanças para a Activity
    public interface OnMissaoInteractionListener {
        void onMissaoStatusChanged(Missao missao, boolean isChecked);
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
        // Infla (cria) a view do item da lista a partir do XML item_missao.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_missao, parent, false);
        return new MissaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MissaoViewHolder holder, int position) {
        // Pega a missão atual da lista
        Missao missaoAtual = listaMissoes.get(position);

        // Define os dados da missão nos componentes visuais do ViewHolder

        // **INÍCIO DA LÓGICA PARA O ÍCONE DA MISSÃO**
        if (missaoAtual.getIconResourceId() != 0) { // Verifica se tem um ID de ícone válido (0 geralmente é um ID inválido)
            holder.imageViewIconeMissao.setImageResource(missaoAtual.getIconResourceId());
        } else {
            // Define um ícone placeholder padrão se nenhum ID específico for fornecido ou se for inválido
            holder.imageViewIconeMissao.setImageResource(R.drawable.ic_task_placeholder);
        }
        // **FIM DA LÓGICA PARA O ÍCONE DA MISSÃO**

        holder.textViewDescricao.setText(missaoAtual.getDescricao());
        holder.textViewPontos.setText("+" + missaoAtual.getPontos() + " PTS"); // Adicionando o "+" para ficar como na imagem

        // Remove o listener antigo para evitar chamadas múltiplas e define o estado correto do CheckBox
        holder.checkBoxConcluida.setOnCheckedChangeListener(null);
        holder.checkBoxConcluida.setChecked(missaoAtual.isConcluida());

        // Aplica/remove o efeito de "riscado" na descrição se a missão estiver concluída
        if (missaoAtual.isConcluida()) {
            holder.textViewDescricao.setPaintFlags(holder.textViewDescricao.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.textViewDescricao.setAlpha(0.5f); // Opcional: Deixar o texto um pouco transparente
        } else {
            holder.textViewDescricao.setPaintFlags(holder.textViewDescricao.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.textViewDescricao.setAlpha(1.0f); // Opcional: Restaurar opacidade total
        }

        // Define o listener para o CheckBox
        holder.checkBoxConcluida.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                // Atualiza o status da missão no objeto Missao
                missaoAtual.setConcluida(isChecked);
                // Notifica a Activity sobre a mudança
                listener.onMissaoStatusChanged(missaoAtual, isChecked);

                // Atualiza o visual (riscado e opacidade) imediatamente
                if (isChecked) {
                    holder.textViewDescricao.setPaintFlags(holder.textViewDescricao.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.textViewDescricao.setAlpha(0.5f);
                } else {
                    holder.textViewDescricao.setPaintFlags(holder.textViewDescricao.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.textViewDescricao.setAlpha(1.0f);
                }
                // notifyItemChanged(position); // Alternativa para redesenhar o item, pode ser mais pesado.
                // A atualização direta dos PaintFlags costuma ser suficiente e mais leve.
            }
        });
    }

    @Override
    public int getItemCount() {
        // Retorna o número total de missões na lista
        return listaMissoes != null ? listaMissoes.size() : 0;
    }

    // ViewHolder: Mantém as referências para os componentes visuais de cada item da lista
    public static class MissaoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewIconeMissao; // Referência para o ImageView do ícone
        TextView textViewDescricao;
        TextView textViewPontos;
        CheckBox checkBoxConcluida;

        public MissaoViewHolder(@NonNull View itemView) {
            super(itemView);
            // Referencia os componentes do layout item_missao.xml
            imageViewIconeMissao = itemView.findViewById(R.id.imageViewIconeMissao);
            textViewDescricao = itemView.findViewById(R.id.textViewDescricaoMissao);
            textViewPontos = itemView.findViewById(R.id.textViewPontosMissao);
            checkBoxConcluida = itemView.findViewById(R.id.checkboxMissaoConcluida);
        }
    }

    // Método para atualizar a lista de missões, se necessário (ex: após carregar do banco)
    public void atualizarLista(List<Missao> novasMissoes) {
        this.listaMissoes.clear();
        if (novasMissoes != null) {
            this.listaMissoes.addAll(novasMissoes);
        }
        notifyDataSetChanged(); // Notifica o RecyclerView que os dados mudaram para ele redesenhar
    }

    // Método para adicionar uma única missão (pode ser útil)
    public void adicionarMissao(Missao missao) {
        if (this.listaMissoes != null && missao != null) {
            this.listaMissoes.add(missao);
            notifyItemInserted(this.listaMissoes.size() - 1);
        }
    }

    // Método para remover uma missão (pode ser útil)
    public void removerMissao(int position) {
        if (this.listaMissoes != null && position >= 0 && position < this.listaMissoes.size()) {
            this.listaMissoes.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, this.listaMissoes.size()); // Para atualizar posições
        }
    }
}