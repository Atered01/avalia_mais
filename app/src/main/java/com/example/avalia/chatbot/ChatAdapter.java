package com.example.avalia.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avalia.R;
import java.util.List;
import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MensagemViewHolder> {

    private final List<MensagemChat> listaMensagens;
    private final Markwon markwon;

    public ChatAdapter(List<MensagemChat> listaMensagens, Markwon markwon) {
        this.listaMensagens = listaMensagens;
        this.markwon = markwon;
    }

    @Override
    public int getItemViewType(int position) {
        // A lógica para inflar layouts diferentes continua a mesma
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
        return new MensagemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MensagemViewHolder holder, int position) {
        MensagemChat mensagem = listaMensagens.get(position);

        // A lógica para aplicar o Markwon continua a mesma
        if (mensagem.getTipoMensagem() == MensagemChat.TIPO_BOT) {
            markwon.setMarkdown(holder.textViewMensagem, mensagem.getTexto());
        } else {
            holder.textViewMensagem.setText(mensagem.getTexto());
        }
    }

    @Override
    public int getItemCount() {
        return listaMensagens != null ? listaMensagens.size() : 0;
    }

    // >>> VIEWHOLDER SIMPLIFICADO <<<
    static class MensagemViewHolder extends RecyclerView.ViewHolder {
        TextView textViewMensagem;

        public MensagemViewHolder(@NonNull View itemView) {
            super(itemView);
            // Agora só precisamos procurar por um único ID, que existe em ambos os layouts
            textViewMensagem = itemView.findViewById(R.id.textViewMensagem);
        }
    }

    public void adicionarMensagem(MensagemChat mensagem) {
        if (listaMensagens != null) {
            listaMensagens.add(mensagem);
            notifyItemInserted(listaMensagens.size() - 1);
        }
    }
}