package com.madalin.licenta.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.controllers.VizualizareLicentaActivity;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Licenta;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.models.Utilizator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

// Adapter pentru legarea setului de date al banner-ului solicitarii la vederile acestuia care sunt afisate intr-un RecyclerView
public class BannerLicentaAdapter extends RecyclerView.Adapter<BannerLicentaAdapter.BannerLicentaViewHolder> {
    private Context context;
    private List<Licenta> listaLicente;

    public BannerLicentaAdapter(Context context, List<Licenta> listaLicente) {
        this.context = context;
        this.listaLicente = listaLicente;
    }

    // returneaza un nou ViewHolder cu un View umflat cu layout-ul cardului din XML atunci cand RecyclerView-ul are nevoie
    @NonNull
    @Override
    public BannerLicentaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewBannerLicenta = LayoutInflater.from(context).inflate(R.layout.layout_banner_licenta, parent, false);

        return new BannerLicentaViewHolder(viewBannerLicenta); // returneaza un nou ViewHolder
    }

    // leaga datele de vederea item-ului de la pozitia specificata atunci cand este apelata de RecyclerView
    @Override
    public void onBindViewHolder(@NonNull BannerLicentaViewHolder holder, int position) {

        getSetDateMelodie(holder, listaLicente.get(position)); // obtine datele melodiei si a utilizatorului din licenta si le seteaza

        holder.textViewDataLicentierii.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date(listaLicente.get(position).getDataCreariiLong())));

        // lanseaza VizualizareLicentaActivity la apasarea banner-ului
        holder.relativeLayoutContainer.setOnClickListener(v -> {
            Intent intent = new Intent(context, VizualizareLicentaActivity.class);
            intent.putExtra(NumeExtra.VIZUALIZARE_LICENTA, listaLicente.get(position)); // extra cu datele licentei selectate
            context.startActivity(intent);
        });
    }

    // numarul total de randuri
    @Override
    public int getItemCount() {
        return listaLicente.size();
    }

    /**
     * {@link androidx.recyclerview.widget.RecyclerView.ViewHolder}-ul descrie vederea unui item
     * si metadatele despre locul acesteia intr-un {@link RecyclerView}.
     */
    public static class BannerLicentaViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayoutContainer;
        ImageView imageViewImagineMelodie;
        TextView textViewNumeMelodie;
        TextView textViewNumePersoana;
        TextView textViewDataLicentierii;

        public BannerLicentaViewHolder(@NonNull View itemView) {
            super(itemView);

            relativeLayoutContainer = itemView.findViewById(R.id.banner_licenta_relativeLayoutContainer);
            imageViewImagineMelodie = itemView.findViewById(R.id.banner_licenta_imageViewImagine);
            textViewNumeMelodie = itemView.findViewById(R.id.banner_licenta_textViewNumeMelodie);
            textViewNumePersoana = itemView.findViewById(R.id.banner_licenta_textViewNumePersoana);
            textViewDataLicentierii = itemView.findViewById(R.id.banner_licenta_textViewDataLicentierii);
        }
    }

    /**
     * Obtine datele melodiei din licenta si le adauga in {@link #listaLicente} si vederi.
     * Apeleaza {@link #getSetDateBeneficiar(BannerLicentaViewHolder, Licenta)} pentru
     * obtinerea si adaugarea datelor beneficiarului din licenta in {@link #listaLicente} si vederi.
     *
     * @param holder  detinatorul vederii curente
     * @param licenta licenta folosita pentru obtinerea datelor melodiei
     */
    private void getSetDateMelodie(BannerLicentaViewHolder holder, Licenta licenta) {
        FirebaseDatabase.getInstance().getReference("melodii")
                .child(licenta.getCheieMelodie()) // cheia melodiei din licenta
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Melodie melodie = snapshot.getValue(Melodie.class);
                        melodie.setCheie(snapshot.getKey());

                        licenta.setMelodie(melodie); // adauga obiectul melodiei in obiectul solicitarii

                        // seteaza datele obtinute din baza de date
                        Glide.with(context)
                                .load(licenta.getMelodie().getImagineMelodie())
                                .apply(RequestOptions.centerCropTransform())
                                .placeholder(R.drawable.logo_music)
                                .error(R.drawable.ic_eroare)
                                .into(holder.imageViewImagineMelodie);

                        holder.textViewNumeMelodie.setText(licenta.getMelodie().getNumeMelodie());
                        holder.textViewNumeMelodie.setSelected(true); // pentru marquee

                        getSetDateBeneficiar(holder, licenta); // obtine datele solicitantului din solicitare si le seteaza
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Obtine datele beneficiarului din licenta si le adauga in {@link #listaLicente} si vederi.
     *
     * @param holder  detinatorul vederii curente
     * @param licenta licenta folosita pentru obtinerea datelor beneficiarului
     */
    private void getSetDateBeneficiar(BannerLicentaViewHolder holder, Licenta licenta) {
        FirebaseDatabase.getInstance().getReference("utilizatori")
                .child(licenta.getCheieBeneficiar()) // cheia beneficiarului din licenta
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Utilizator utilizator = snapshot.getValue(Utilizator.class);
                        utilizator.setCheie(snapshot.getKey());

                        licenta.setBeneficiar((Utilizator) utilizator); // adauga obiectul beneficiarului in obiectul licentei

                        // daca artistul este cel care vede licenta
                        if (Objects.equals(licenta.getCheieArtist(), MainActivity.utilizator.getCheie())) {
                            holder.textViewNumePersoana.setText("Beneficiar: " + licenta.getBeneficiar().getNume());
                            holder.textViewNumePersoana.setSelected(true); // pentru marquee
                        }
                        // daca solicitantul este cel care vede solicitarea
                        else {
                            holder.textViewNumePersoana.setText("Artist: " + licenta.getMelodie().getNumeArtist());
                            holder.textViewNumePersoana.setSelected(true); // pentru marquee
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
