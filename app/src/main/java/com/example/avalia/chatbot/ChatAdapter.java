package com.example.avalia.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avalia.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MensagemViewHolder> {

    private List<MensagemChat> listaMensagens;

    public ChatAdapter(List<MensagemChat> listaMensagens) {
        this.listaMensagens = listaMensagens;
    }

    // Este método é crucial para determinar qual layout usar (usuário ou bot)
    @Override
    public int getItemViewType(int position) {
        return listaMensagens.get(position).getTipoMensagem();
    }

    @NonNull
    @Override
    public MensagemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MensagemChat.TIPO_USUARIO) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_usuario, parent, false);
        } else { // TIPO_BOT
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mensagem_bot, parent, false);
        }
        return new MensagemViewHolder(view, viewType); // Passa viewType para o ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull MensagemViewHolder holder, int position) {
        MensagemChat mensagem = listaMensagens.get(position);
        holder.bind(mensagem);
    }

    @Override
    public int getItemCount() {
        return listaMensagens != null ? listaMensagens.size() : 0;
    }

    // ViewHolder para as mensagens
    static class MensagemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMensagem; // Usaremos um ID genérico se os layouts usarem o mesmo ID para o TextView principal

        public MensagemViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            // Encontra o TextView baseado no tipo de view, caso os IDs sejam diferentes
            if (viewType == MensagemChat.TIPO_USUARIO) {
                textViewMensagem = itemView.findViewById(R.id.textViewMensagemUsuario);
            } else { // TIPO_BOT
                textViewMensagem = itemView.findViewById(R.id.textViewMensagemBot);
            }
        }

        public void bind(MensagemChat mensagem) {
            if (textViewMensagem != null) {
                textViewMensagem.setText(mensagem.getTexto());
            }
        }
    }

    // Método para adicionar uma nova mensagem à lista e notificar o adapter
    public void adicionarMensagem(MensagemChat mensagem) {
        if (listaMensagens != null) {
            listaMensagens.add(mensagem);
            notifyItemInserted(listaMensagens.size() - 1);
        }
    }

    // Método para limpar todas as mensagens (útil se quiser reiniciar o chat)
    public void limparMensagens() {
        if (listaMensagens != null) {
            int size = listaMensagens.size();
            listaMensagens.clear();
            notifyItemRangeRemoved(0, size);
        }
    }
}