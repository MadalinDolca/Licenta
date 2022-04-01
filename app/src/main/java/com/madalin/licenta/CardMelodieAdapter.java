package com.madalin.licenta;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.madalin.licenta.model.CardMelodie;

import java.util.List;

// Adapter pentru legarea setului de date al cardului melodiei la vederile acesteia care sunt afisate intr-un RecyclerView
public class CardMelodieAdapter extends RecyclerView.Adapter<CardMelodieAdapter.CardMelodieViewHolder> {

    Context context;
    List<CardMelodie> listaCardMelodie;

    // carui context trebuie sa i se ofere datele
    public CardMelodieAdapter(Context context, List<CardMelodie> listaCardMelodie) {
        this.context = context;
        this.listaCardMelodie = listaCardMelodie;
    }

    // returneaza un nou ViewHolder cu un View umflat cu layout-ul cardului din XML atunci cand RecyclerView-ul are nevoie
    @NonNull
    @Override
    public CardMelodieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCardMelodie = LayoutInflater.from(context).inflate(R.layout.layout_card_melodie, parent/*null*/, false);

        return new CardMelodieViewHolder(viewCardMelodie);
    }

    // leaga datele de vederea item-ului de la pozitia specificata atunci cand este apelata de RecyclerView
    @Override
    public void onBindViewHolder(@NonNull CardMelodieViewHolder holder, int position) {
        //Log.d("ggggggggggg", listaCardMelodie.get(position).numeMelodie);
        //holder.imageViewImagineMelodie.setImageResource(listaCardMelodie.get(position).imagineMelodie);
        Glide.with(context).load(listaCardMelodie.get(position).imagineMelodie).apply(RequestOptions.centerCropTransform()).placeholder(R.drawable.logo_music).into(holder.imageViewImagineMelodie);
        holder.textViewNumeMelodie.setText(listaCardMelodie.get(position).numeMelodie);
        holder.textViewNumeArtist.setText(listaCardMelodie.get(position).numeArtist);

        holder.imageViewImagineMelodie.setOnClickListener(v -> Toast.makeText(context, holder.textViewNumeArtist.getText(), Toast.LENGTH_SHORT).show());

        //holder.imageViewImagineChild.setOnClickListener();
    }

    // numarul total de randuri
    @Override
    public int getItemCount() {
        return listaCardMelodie.size();
    }

    // ViewHolder-ul descrie vederea unui item si metadatele despre locul acesteia intr-un RecyclerView
    public class CardMelodieViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewImagineMelodie;
        TextView textViewNumeMelodie;
        TextView textViewNumeArtist;

        public CardMelodieViewHolder(@NonNull View itemView) {
            super(itemView);

            imageViewImagineMelodie = itemView.findViewById(R.id.card_melodie_imageViewImagine);
            textViewNumeMelodie = itemView.findViewById(R.id.card_melodie_textViewNume);
            textViewNumeArtist = itemView.findViewById(R.id.card_melodie_textViewArtist);
        }
    }
}
