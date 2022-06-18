package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.Directie;
import static com.madalin.licenta.global.EdgeToEdge.Spatiere;
import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.BannerMelodieAdapter;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.models.Utilizator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProfilActivity extends AppCompatActivity {

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

        // obtine datele utilizatorului din baza de date in functie de cheia acestuia
        databaseReferenceUtilizatori.child(cheieUtilizator).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // obtinere date din DB printr-un data snapshot si marshalling date in clasa Utilizator
                Utilizator profilUtilizator = snapshot.getValue(Utilizator.class);

                if (profilUtilizator != null) {
                    textViewNumeUtilizator.setText(profilUtilizator.getNume());

                    if (Objects.equals(profilUtilizator.getGrad(), "admin")) {
                        textViewGrad.setText(profilUtilizator.getGrad());
                    } else {
                        textViewGrad.setText("utilizator");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfilActivity.this, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // obtine melodiile care apartin utilizatorului selectat
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

                // creeaza si seteaza adapter-ul pentru banner-ele cu melodii
                BannerMelodieAdapter bannerMelodieAdapter = new BannerMelodieAdapter(ProfilActivity.this, listaMelodii); // creeaza adapter banner melodii
                recyclerView.setLayoutManager(new LinearLayoutManager(ProfilActivity.this));
                recyclerView.setAdapter(bannerMelodieAdapter); // setare adapter pe recyclerView pentru a furniza child views la cerere
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