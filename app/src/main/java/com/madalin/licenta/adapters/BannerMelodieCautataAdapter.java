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
import com.madalin.licenta.controllers.PlayerActivity;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Melodie;

import java.io.Serializable;
import java.util.List;

// Adapter pentru legarea setului de date al banner-ului solicitarii la vederile acestuia care sunt afisate intr-un RecyclerView
public class BannerMelodieCautataAdapter extends RecyclerView.Adapter<BannerMelodieCautataAdapter.BannerMelodieCautataViewHolder> {
    private Context context;
    private List<Melodie> listaMelodii;

    public BannerMelodieCautataAdapter(Context context, List<Melodie> listaMelodii) {
        this.context = context;
        this.listaMelodii = listaMelodii;
    }

    // returneaza un nou ViewHolder cu un View umflat cu layout-ul cardului din XML atunci cand RecyclerView-ul are nevoie
    @NonNull
    @Override
    public BannerMelodieCautataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewBannerMelodieCautata = LayoutInflater.from(context).inflate(R.layout.layout_banner_melodie, parent, false);
        return new BannerMelodieCautataViewHolder(viewBannerMelodieCautata); // returneaza un nou ViewHolder
    }

    // leaga datele de vederea item-ului de la pozitia specificata atunci cand este apelata de RecyclerView
    @Override
    public void onBindViewHolder(@NonNull BannerMelodieCautataViewHolder holder, int position) {
        if (listaMelodii.get(position).getImagineMelodie() == null) { // daca melodia nu are link spre imagine
            holder.imageViewImagineMelodie.setImageResource(R.drawable.ic_nota_muzicala); // se adauga o resursa inlocuitoare
        } else { // daca melodia are link spre imagine, aceasta se obtine si se adauga in card-ul melodiei
            Glide.with(context).load(listaMelodii.get(position).getImagineMelodie())
                    .apply(RequestOptions.centerCropTransform())
                    .placeholder(R.drawable.logo_music)
                    .error(R.drawable.ic_eroare) // in caz ca nu s-a putut incarca imaginea, se adauga o resursa inlocuitoare
                    .into(holder.imageViewImagineMelodie);
        }

        holder.textViewNumeMelodie.setText(listaMelodii.get(position).getNumeMelodie());
        holder.textViewNumeNumeArtist.setText(listaMelodii.get(position).getNumeArtist());
        holder.textViewGenMelodie.setText(listaMelodii.get(position).getGenMelodie());
        holder.imageViewButonEdit.setVisibility(View.GONE);

        // lansare activitate Player la apasarea cardului
        holder.relativeLayoutContainer.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra(NumeExtra.LISTA_MELODII, (Serializable) listaMelodii); // extra cu lista cu melodii
            intent.putExtra(NumeExtra.POZITIE_MELODIE, position); // extra cu pozitia melodiei selectate
            context.startActivity(intent);
        });
    }

    // numarul total de randuri
    @Override
    public int getItemCount() {
        return listaMelodii.size();
    }

    /**
     * {@link androidx.recyclerview.widget.RecyclerView.ViewHolder}-ul descrie vederea unui item
     * si metadatele despre locul acesteia intr-un {@link RecyclerView}.
     */
    public static class BannerMelodieCautataViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayoutContainer;
        ImageView imageViewImagineMelodie;
        ImageView imageViewButonEdit;
        TextView textViewNumeMelodie;
        TextView textViewNumeNumeArtist;
        TextView textViewGenMelodie;

        public BannerMelodieCautataViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayoutContainer = itemView.findViewById(R.id.banner_melodie_relativeLayoutContainer);
            imageViewImagineMelodie = itemView.findViewById(R.id.banner_melodie_imageViewImagine);
            imageViewButonEdit = itemView.findViewById(R.id.banner_melodie_imageViewButonEdit);
            textViewNumeMelodie = itemView.findViewById(R.id.banner_melodie_textViewNumeMelodie);
            textViewNumeNumeArtist = itemView.findViewById(R.id.banner_melodie_textViewInformatie1);
            textViewGenMelodie = itemView.findViewById(R.id.banner_melodie_textViewInformatie2);
        }
    }
}
