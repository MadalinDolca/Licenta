package com.madalin.licenta.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.EvaluareSolicitareActivity;
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.models.Solicitare;
import com.madalin.licenta.models.Utilizator;

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

        getSetDateMelodie(holder, listaSolicitari.get(position)); // obtine datele melodiei si a utilizatorului din solicitare si le seteaza

        holder.textViewDataSolicitarii.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date(listaSolicitari.get(position).getDataCreariiLong())));

        // seteaza culoarea fundalului banner-ului in functie de stadiul solicitarii
        switch (listaSolicitari.get(position).getStadiu()) {
            case Solicitare.ACCEPTATA:
                holder.relativeLayoutContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFC8FFBF")));
                seteazaCuloriNightMode(holder);
                break;

            case Solicitare.RESPINSA:
                holder.relativeLayoutContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFFFBFBF")));
                seteazaCuloriNightMode(holder);
                break;

            default:
                break;
        }

        // lanseaza EvaluareSolicitareActivity la apasarea banner-ului
        holder.relativeLayoutContainer.setOnClickListener(v -> {
            Intent intent = new Intent(context, EvaluareSolicitareActivity.class);
            intent.putExtra(NumeExtra.EVALUARE_SOLICITARE, listaSolicitari.get(position)); // extra cu datele solicitarii selectate
            context.startActivity(intent);
        });
    }

    // numarul total de randuri
    @Override
    public int getItemCount() {
        return listaSolicitari.size();
    }

    /**
     * {@link androidx.recyclerview.widget.RecyclerView.ViewHolder}-ul descrie vederea unui item
     * si metadatele despre locul acesteia intr-un {@link RecyclerView}.
     */
    public static class BannerSolicitareViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayoutContainer;
        ImageView imageViewImagineMelodie;
        ImageView imageViewPersoana;
        ImageView imageViewCalendar;
        TextView textViewNumeMelodie;
        TextView textViewNumePersoana;
        TextView textViewDataSolicitarii;

        public BannerSolicitareViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayoutContainer = itemView.findViewById(R.id.banner_solicitare_relativeLayoutContainer);
            imageViewImagineMelodie = itemView.findViewById(R.id.banner_solicitare_imageViewImagine);
            imageViewPersoana = itemView.findViewById(R.id.banner_solicitare_imageViewPersoana);
            imageViewCalendar = itemView.findViewById(R.id.banner_solicitare_imageViewCalendar);
            textViewNumeMelodie = itemView.findViewById(R.id.banner_solicitare_textViewNumeMelodie);
            textViewNumePersoana = itemView.findViewById(R.id.banner_solicitare_textViewNumePersoana);
            textViewDataSolicitarii = itemView.findViewById(R.id.banner_solicitare_textViewDataSolicitarii);
        }
    }

    /**
     * Obtine datele melodiei din solicitare si le adauga in {@link #listaSolicitari} si vederi.
     * Apeleaza {@link #getSetDateSolicitant(BannerSolicitareViewHolder, Solicitare)} pentru
     * obtinerea si adaugarea datelor solicitantului din solicitare in {@link #listaSolicitari} si vederi.
     *
     * @param holder     detinatorul vederii curente
     * @param solicitare solicitarea folosita pentru obtinerea datelor melodiei
     */
    private void getSetDateMelodie(BannerSolicitareViewHolder holder, Solicitare solicitare) {
        FirebaseDatabase.getInstance().getReference("melodii")
                .child(solicitare.getCheieMelodie()) // cheia melodiei din solicitare
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Melodie melodie = snapshot.getValue(Melodie.class);
                        melodie.setCheie(snapshot.getKey());

                        // adauga datele melodiei in obiectul solicitarii
                        solicitare.setTemp_numeMelodie(melodie.getNumeMelodie());
                        solicitare.setTemp_numeArtist(melodie.getNumeArtist());
                        solicitare.setTemp_imagineMelodie(melodie.getImagineMelodie());

                        // seteaza datele obtinute din baza de date
                        Glide.with(context)
                                .load(solicitare.getTemp_imagineMelodie())
                                .apply(RequestOptions.centerCropTransform())
                                .placeholder(R.drawable.logo_music)
                                .error(R.drawable.ic_eroare)
                                .into(holder.imageViewImagineMelodie);

                        holder.textViewNumeMelodie.setText(solicitare.getTemp_numeMelodie());
                        holder.textViewNumeMelodie.setFocusable(true); // pentru marquee

                        getSetDateSolicitant(holder, solicitare); // obtine datele solicitantului din solicitare si le seteaza
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Obtine datele solicitantului din solicitare si le adauga in {@link #listaSolicitari} si vederi.
     *
     * @param holder     detinatorul vederii curente
     * @param solicitare solicitarea folosita pentru obtinerea datelor solicitantului
     */
    private void getSetDateSolicitant(BannerSolicitareViewHolder holder, Solicitare solicitare) {
        FirebaseDatabase.getInstance().getReference("utilizatori")
                .child(solicitare.getCheieSolicitant()) // cheia solicitantului din solicitare
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Utilizator utilizator = snapshot.getValue(Utilizator.class);
                        utilizator.setCheie(snapshot.getKey());

                        // adauga datele solicitantului in obiectul solicitarii
                        solicitare.setTemp_numeSolicitant(utilizator.getNume());

                        // daca artistul este cel care vede solicitarea
                        if (Objects.equals(solicitare.getCheieArtist(), MainActivity.utilizator.getCheie())) {
                            holder.textViewNumePersoana.setText("Solicitant: " + solicitare.getTemp_numeSolicitant());
                        }
                        // daca solicitantul este cel care vede solicitarea
                        else {
                            holder.textViewNumePersoana.setText("Către artistul: " + solicitare.getTemp_numeArtist());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Schimba culorile elementelor banner-ului daca modul intunecat este activat.
     *
     * @param holder detinatorul vederilor
     */
    private void seteazaCuloriNightMode(BannerSolicitareViewHolder holder) {
        if (Configuration.UI_MODE_NIGHT_YES == (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)) {
            ColorStateList colorStateList = ContextCompat.getColorStateList(context, R.color.fundal);

            holder.imageViewPersoana.setImageTintList(colorStateList);
            holder.imageViewCalendar.setImageTintList(colorStateList);
            holder.textViewNumeMelodie.setTextColor(colorStateList);
            holder.textViewNumePersoana.setTextColor(colorStateList);
            holder.textViewDataSolicitarii.setTextColor(colorStateList);
        }
    }
}
