package com.madalin.licenta.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.EvaluareSolicitareActivity;
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Solicitare;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

// Adapter pentru legarea setului de date al banner-ului solicitarii la vederile acestuia care sunt afisate intr-un RecyclerView
public class BannerSolicitareAdapter extends RecyclerView.Adapter<BannerSolicitareAdapter.BannerSolicitareViewHolder> {
    private Context context;
    private List<Solicitare> listaSolicitari;

    public BannerSolicitareAdapter(Context context, List<Solicitare> listaSolicitari) {
        this.context = context;
        this.listaSolicitari = listaSolicitari;
    }

    // returneaza un nou ViewHolder cu un View umflat cu layout-ul cardului din XML atunci cand RecyclerView-ul are nevoie
    @NonNull
    @Override
    public BannerSolicitareViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewBannerSolicitare = LayoutInflater.from(context).inflate(R.layout.layout_banner_solicitare, parent, false);

        return new BannerSolicitareViewHolder(viewBannerSolicitare); // returneaza un nou ViewHolder
    }

    // leaga datele de vederea item-ului de la pozitia specificata atunci cand este apelata de RecyclerView
    @Override
    public void onBindViewHolder(@NonNull BannerSolicitareViewHolder holder, int position) {
        if (listaSolicitari.get(position).getTemp_imagineMelodie() == null) { // daca melodia nu are link spre imagine
            holder.imageViewImagineMelodie.setImageResource(R.drawable.ic_nota_muzicala); // se adauga o resursa inlocuitoare
        } else { // daca melodia are link spre imagine, aceasta se obtine si se adauga in card-ul melodiei
            Glide.with(context).load(listaSolicitari.get(position).getTemp_imagineMelodie())
                    .apply(RequestOptions.centerCropTransform())
                    .placeholder(R.drawable.logo_music)
                    .error(R.drawable.ic_eroare) // in caz ca nu s-a putut incarca imaginea, se adauga o resursa inlocuitoare
                    .into(holder.imageViewImagineMelodie);
        }

        // seteaza datele de tip text
        holder.textViewNumeMelodie.setText(listaSolicitari.get(position).getCheie()); /////////
        holder.textViewNumeMelodie.setFocusable(true); // pentru marquee

        // daca artistul este cel care vede solicitarea
        if (Objects.equals(listaSolicitari.get(position).getCheieArtist(), MainActivity.utilizator.getCheie())) {
            holder.textViewNumePersoana.setText("Solicitant: " + listaSolicitari.get(position).getTemp_numeSolicitant());
        }
        // daca solicitantul este cel care vede solicitarea
        else {
            holder.textViewNumePersoana.setText("Artist: " + listaSolicitari.get(position).getTemp_numeArtist());
        }

        holder.textViewDataSolicitarii.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date(listaSolicitari.get(position).getDataCreariiLong())));

        // lansare activitate Player la apasarea cardului
        holder.relativeLayoutContainer.setOnClickListener(v -> {
            Intent intent = new Intent(context, EvaluareSolicitareActivity.class);
            intent.putExtra(NumeExtra.EVALUARE_SOLICITARE, (Serializable) listaSolicitari.get(position)); // extra cu datele solicitarii selectate
            context.startActivity(intent);
        });
    }

    // numarul total de randuri
    @Override
    public int getItemCount() {
        return listaSolicitari.size();
    }

    // ViewHolder-ul descrie vederea unui item si metadatele despre locul acesteia intr-un RecyclerView
    public static class BannerSolicitareViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayoutContainer;
        ImageView imageViewImagineMelodie;
        TextView textViewNumeMelodie;
        TextView textViewNumePersoana;
        TextView textViewDataSolicitarii;

        public BannerSolicitareViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayoutContainer = itemView.findViewById(R.id.banner_solicitare_relativeLayoutContainer);
            imageViewImagineMelodie = itemView.findViewById(R.id.banner_solicitare_imageViewImagine);
            textViewNumeMelodie = itemView.findViewById(R.id.banner_solicitare_textViewNumeMelodie);
            textViewNumePersoana = itemView.findViewById(R.id.banner_solicitare_textViewNumePersoana);
            textViewDataSolicitarii = itemView.findViewById(R.id.banner_solicitare_textViewDataSolicitarii);
        }
    }
}
