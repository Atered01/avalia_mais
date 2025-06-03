package com.example.avalia.prova; // Seu pacote

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avalia.R;

import java.util.List;

public class ProvaSelecaoAdapter extends RecyclerView.Adapter<ProvaSelecaoAdapter.ProvaViewHolder> {

    private List<Prova> listaProvas;
    private OnProvaClickListener onProvaClickListener;

    public interface OnProvaClickListener {
        void onProvaClick(Prova prova);
    }

    public ProvaSelecaoAdapter(List<Prova> listaProvas, OnProvaClickListener listener) {
        this.listaProvas = listaProvas;
        this.onProvaClickListener = listener;
    }

    @NonNull
    @Override
    public ProvaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_prova_selecao, parent, false);
        // Não passamos mais o listener para o construtor do ViewHolder diretamente aqui,
        // pois o 'bind' cuidará da lógica de clique com o objeto Prova correto.
        return new ProvaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProvaViewHolder holder, int position) {
        Prova provaAtual = listaProvas.get(position);
        // Chama o método bind para configurar dados e o listener de clique
        holder.bind(provaAtual, onProvaClickListener);
    }

    @Override
    public int getItemCount() {
        return listaProvas == null ? 0 : listaProvas.size();
    }

    public void setProvas(List<Prova> novasProvas) {
        this.listaProvas = novasProvas;
        notifyDataSetChanged();
    }

    public static class ProvaViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewNomeProva;
        public TextView textViewDetalhesProva;

        // Construtor simplificado: apenas encontra as views
        public ProvaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNomeProva = itemView.findViewById(R.id.textViewNomeProvaItem);
            textViewDetalhesProva = itemView.findViewById(R.id.textViewDetalhesProvaItem);
            // O OnClickListener é configurado no método bind agora
        }

        // Método para vincular os dados e o listener de clique
        public void bind(final Prova prova, final OnProvaClickListener listener) {
            textViewNomeProva.setText(prova.getNome());
            textViewDetalhesProva.setText("10 questões"); // Placeholder

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    // Agora 'prova' é o objeto correto para este item específico
                    listener.onProvaClick(prova);
                }
            });
        }
    }
}