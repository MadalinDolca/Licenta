package com.madalin.licenta.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.madalin.licenta.R;
import com.madalin.licenta.models.CategorieCarduri;

import java.util.List;

public class CategorieCarduriAdapter extends RecyclerView.Adapter<CategorieCarduriAdapter.CategorieCarduriViewHolder> {

    Context context;
    List<CategorieCarduri> listaCategorieCarduri;

    public CategorieCarduriAdapter(Context context, List<CategorieCarduri> listaCategorieCarduri) {
        this.context = context;
        this.listaCategorieCarduri = listaCategorieCarduri;
    }

    @NonNull
    @Override
    public CategorieCarduriViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewCategorieCarduri = LayoutInflater.from(context).inflate(R.layout.layout_categorie_carduri, parent/*null*/, false);

        return new CategorieCarduriViewHolder(viewCategorieCarduri);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorieCarduriViewHolder holder, int position) {
        holder.textViewTitlu.setText(listaCategorieCarduri.get(position).getTitluCategorie()); // seteaza numele categoriei

        CardMelodieAdapter cardMelodieAdapter = new CardMelodieAdapter(context, listaCategorieCarduri.get(position).getListaMelodii()); // creeaza un adapter pentru cardurile cu melodii
        holder.recyclerViewCarduri.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.recyclerViewCarduri.setAdapter(cardMelodieAdapter);
        cardMelodieAdapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return listaCategorieCarduri.size();
    }

    public class CategorieCarduriViewHolder extends RecyclerView.ViewHolder {

        TextView textViewTitlu; // titlul categoriei
        RecyclerView recyclerViewCarduri;

        public CategorieCarduriViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewTitlu = itemView.findViewById(R.id.categorie_carduri_textViewTitlu);
            recyclerViewCarduri = itemView.findViewById(R.id.categorie_carduri_recyclerViewCarduri);
        }
    }
}
