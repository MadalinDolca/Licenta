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
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.controllers.PlayerActivity;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.models.Utilizator;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

// Adapter pentru legarea setului de date al banner-ului melodiei la vederile acestuia care sunt afisate intr-un RecyclerView
public class BannerMelodieAdapter extends RecyclerView.Adapter<BannerMelodieAdapter.BannerMelodieViewHolder> {
    private Context context;
    private List<Melodie> listaMelodii;

    public BannerMelodieAdapter(Context context, List<Melodie> listaMelodii) {
        this.context = context;
        this.listaMelodii = listaMelodii;
    }

    // returneaza un nou ViewHolder cu un View umflat cu layout-ul cardului din XML atunci cand RecyclerView-ul are nevoie
    @NonNull
    @Override
    public BannerMelodieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewBannerMelodie = LayoutInflater.from(context).inflate(R.layout.layout_banner_melodie, parent, false);

        return new BannerMelodieViewHolder(viewBannerMelodie);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerMelodieViewHolder holder, int position) {
        if (listaMelodii.get(position).getImagineMelodie() == null) { // daca melodia nu are link spre imagine
            holder.imageViewImagineMelodie.setImageResource(R.drawable.ic_nota_muzicala); // se adauga o resursa inlocuitoare
        } else { // daca melodia are link spre imagine, aceasta se obtine si se adauga in card-ul melodiei
            Glide.with(context).load(listaMelodii.get(position).getImagineMelodie())
                    .apply(RequestOptions.centerCropTransform())
                    .placeholder(R.drawable.logo_music)
                    .error(R.drawable.ic_eroare) // in caz ca nu s-a putut incarca imaginea, se adauga o resursa inlocuitoare
                    .into(holder.imageViewImagineMelodie);
        }

        // daca utilizatorul curent este proprietarul melodiei sau admin, se afiseaza butonul de edit
        if (Objects.equals(MainActivity.utilizator.getCheie(), listaMelodii.get(position).getCheieArtist())
                || Objects.equals(MainActivity.utilizator.getGrad(), Utilizator.GRAD_ADMIN)) {
            holder.imageViewButonEdit.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewButonEdit.setVisibility(View.GONE);
        }

        // seteaza datele de tip text
        holder.textViewNumeMelodie.setText(listaMelodii.get(position).getNumeMelodie());
        holder.textViewNumeMelodie.setSelected(true); // pentru marquee

        holder.textViewNumarRedari.setText(listaMelodii.get(position).getNumarRedari() + " redări");
        holder.textViewNumarRedari.setSelected(true); // pentru marquee

        holder.textViewGenMuzical.setText(listaMelodii.get(position).getGenMelodie());
        holder.textViewGenMuzical.setSelected(true); // pentru marquee

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

    // ViewHolder-ul descrie vederea unui item si metadatele despre locul acesteia intr-un RecyclerView
    public static class BannerMelodieViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayoutContainer;
        ImageView imageViewButonEdit;
        ImageView imageViewImagineMelodie;
        TextView textViewNumeMelodie;
        TextView textViewNumarRedari;
        TextView textViewGenMuzical;

        public BannerMelodieViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayoutContainer = itemView.findViewById(R.id.banner_melodie_relativeLayoutContainer);
            imageViewButonEdit = itemView.findViewById(R.id.banner_melodie_imageViewButonEdit);
            imageViewImagineMelodie = itemView.findViewById(R.id.banner_melodie_imageViewImagine);
            textViewNumeMelodie = itemView.findViewById(R.id.banner_melodie_textViewNumeMelodie);
            textViewNumarRedari = itemView.findViewById(R.id.banner_melodie_textViewInformatie1);
            textViewGenMuzical = itemView.findViewById(R.id.banner_melodie_textViewInformatie2);
        }
    }
}
