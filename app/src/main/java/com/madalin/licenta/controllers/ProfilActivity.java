package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.Directie;
import static com.madalin.licenta.global.EdgeToEdge.Spatiere;
import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.BannerMelodieProfilAdapter;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.models.Utilizator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ProfilActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayoutContainer;
    private LinearLayout toolbar;
    private RecyclerView recyclerView;
    private TextView textViewNumeUtilizator;
    private TextView textViewGrad;
    private TextView textViewNumarMelodii;
    private TextView textViewNumarRedari;

    private DatabaseReference databaseReferenceUtilizatori;
    private DatabaseReference databaseReferenceMelodii;
    private String cheieUtilizator; // cheia din baza de date a utilizatorului
    private List<Melodie> listaMelodii;
    private int numarRedari;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        initializareVederi(); // initializeaza vederile activitatii
        edgeToEdge(this, toolbar, Spatiere.PADDING, Directie.SUS);
        edgeToEdge(this, recyclerView, Spatiere.MARGIN, Directie.JOS);

        listaMelodii = new ArrayList<>();
        cheieUtilizator = getIntent().getStringExtra(NumeExtra.CHEIE_UTILIZATOR); // obtine cheia utilizatorului oferita in extra
        databaseReferenceUtilizatori = FirebaseDatabase.getInstance().getReference("utilizatori");
        databaseReferenceMelodii = FirebaseDatabase.getInstance().getReference("melodii");

        getSetDateUtilizator(); // obtine si afiseaza datele utilizatorului curent
        getSetMelodiiUtilizator(); // obtine si afiseaza melodiile utilizatorului curent impreuna cu numarul de melodii si de redari

        // reincarca datele la glisare
        /*swipeRefreshLayoutContainer.setOnRefreshListener(() -> {
            getSetDateUtilizator();
            getSetMelodiiUtilizator();
        });*/
    }

    /**
     * Obtine din baza de date datele utilizatorului curent si le afiseaza.
     */
    private void getSetDateUtilizator() {
        databaseReferenceUtilizatori.child(cheieUtilizator).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // obtinere date din DB printr-un data snapshot si marshalling date in clasa Utilizator
                Utilizator profilUtilizator = snapshot.getValue(Utilizator.class);

                if (profilUtilizator != null) {
                    textViewNumeUtilizator.setText(profilUtilizator.getNume());

                    if (Objects.equals(profilUtilizator.getGrad(), Utilizator.GRAD_ADMIN)) {
                        textViewGrad.setText(profilUtilizator.getGrad());
                    } else {
                        textViewGrad.setText("utilizator");
                    }
                }

                //swipeRefreshLayoutContainer.setRefreshing(false); // inchide animatia de refresh
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfilActivity.this, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Obtine din baza de date melodiile care apartin utilizatorului selectat si le afiseaza
     * impreuna cu numarul de melodii incarcate si numarul de redari totale.
     * Se parcurg melodiile din baza de date, iar in cazul in care {@link #cheieUtilizator} este
     * egala cu cheia artistului melodiei, atunci se adauga melodia in {@link #listaMelodii} si se
     * incrementeaza {@link #numarRedari}. Seteaza {@link #listaMelodii} pentru adapter-ul
     * {@link #recyclerView}-ului.
     */
    private void getSetMelodiiUtilizator() {
        databaseReferenceMelodii.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaMelodii.clear();
                numarRedari = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) { // parcurge toti descendentii nodului "melodii" din baza de date
                    Melodie melodie = dataSnapshot.getValue(Melodie.class); // adauga valorile descendentului in obiect
                    melodie.setCheie(dataSnapshot.getKey()); // adauga cheia descendentului in obiect

                    if (Objects.equals(melodie.getCheieArtist(), cheieUtilizator)) { // daca cheia utilizatorului din melodie corespunde cu cheia utilizatorului selectat
                        listaMelodii.add(melodie); // adauga datele melodiei in lista
                        numarRedari += melodie.getNumarRedari(); // incrementeaza numarul de redari
                    }
                }

                // afiseaza numarul total de melodii incarcat si numarul total de redari
                textViewNumarMelodii.setText(listaMelodii.size() + " melodii încărcate");
                textViewNumarRedari.setText(numarRedari + " redări totale");

                Collections.reverse(listaMelodii); // reordoneaza lista cu melodii

                // creeaza si seteaza adapter-ul pentru banner-ele cu melodii
                BannerMelodieProfilAdapter bannerMelodieProfilAdapter = new BannerMelodieProfilAdapter(ProfilActivity.this, listaMelodii); // creeaza adapter banner melodii
                recyclerView.setLayoutManager(new LinearLayoutManager(ProfilActivity.this));
                recyclerView.setAdapter(bannerMelodieProfilAdapter); // setare adapter pe recyclerView pentru a furniza child views la cerere

                //swipeRefreshLayoutContainer.setRefreshing(false); // inchide animatia de refresh
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfilActivity.this, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Initializeaza toate vederile din {@link ProfilActivity}.
     */
    private void initializareVederi() {
        //swipeRefreshLayoutContainer = findViewById(R.id.profil_swipeRefreshLayoutContainer);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.profil_recyclerViewMelodii);
        textViewNumeUtilizator = findViewById(R.id.profil_textViewNumeUtilizator);
        textViewGrad = findViewById(R.id.profil_textViewGrad);
        textViewNumarMelodii = findViewById(R.id.profil_textViewNumarMelodii);
        textViewNumarRedari = findViewById(R.id.profil_textViewNumarRedari);

        toolbar.findViewById(R.id.toolbar_imageViewButonInapoi).setOnClickListener(v -> super.onBackPressed());
        TextView textView = toolbar.findViewById(R.id.toolbar_textViewTitlu);
        textView.setText("Profil");
    }
}