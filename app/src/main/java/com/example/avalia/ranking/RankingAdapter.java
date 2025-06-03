package com.example.avalia.ranking;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.avalia.R;
import com.example.avalia.usuario.Usuario;

import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.RankingViewHolder> {

    private List<Usuario> listaUsuarios;
    private Context context;

    public RankingAdapter(Context context, List<Usuario> listaUsuarios) {
        this.context = context;
        this.listaUsuarios = listaUsuarios;
    }

    @NonNull
    @Override
    public RankingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ranking_usuario, parent, false);
        return new RankingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RankingViewHolder holder, int position) {
        Usuario usuario = listaUsuarios.get(position);
        int posicaoReal = position + 1; // Posição no ranking (1, 2, 3...)

        holder.textViewPosicao.setText(String.format("%d.", posicaoReal));
        holder.textViewNome.setText(usuario.getNomeCompleto());
        holder.textViewPontos.setText(String.format("%d Pontos", usuario.getPontuacaoTotal()));

        // Lógica para mostrar medalhas (opcional)
        if (posicaoReal == 1) {
            holder.imageViewMedalha.setImageResource(R.drawable.ic_medal_gold); // Crie este drawable
            holder.imageViewMedalha.setVisibility(View.VISIBLE);
        } else if (posicaoReal == 2) {
            holder.imageViewMedalha.setImageResource(R.drawable.ic_medal_silver); // Crie este drawable
            holder.imageViewMedalha.setVisibility(View.VISIBLE);
        } else if (posicaoReal == 3) {
            holder.imageViewMedalha.setImageResource(R.drawable.ic_medal_bronze); // Crie este drawable
            holder.imageViewMedalha.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewMedalha.setVisibility(View.GONE); // Ou View.INVISIBLE
        }

        // Carregar avatar (se você tiver URLs de avatar ou drawables)
        // Por agora, deixaremos o placeholder do XML:
        // Glide.with(context).load(usuario.getUrlAvatar()).placeholder(R.drawable.ic_avatar_placeholder).into(holder.imageViewAvatar);
    }

    @Override
    public int getItemCount() {
        return listaUsuarios.size();
    }

    static class RankingViewHolder extends RecyclerView.ViewHolder {
        TextView textViewPosicao;
        ImageView imageViewAvatar;
        TextView textViewNome;
        TextView textViewPontos;
        ImageView imageViewMedalha;

        public RankingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewPosicao = itemView.findViewById(R.id.textViewPosicaoRanking);
            imageViewAvatar = itemView.findViewById(R.id.imageViewAvatarRanking);
            textViewNome = itemView.findViewById(R.id.textViewNomeUsuarioRanking);
            textViewPontos = itemView.findViewById(R.id.textViewPontosUsuarioRanking);
            imageViewMedalha = itemView.findViewById(R.id.imageViewMedalhaRanking);
        }
    }
}