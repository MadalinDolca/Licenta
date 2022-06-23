package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.BannerSolicitareAdapter;
import com.madalin.licenta.global.EdgeToEdge;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.models.Solicitare;
import com.madalin.licenta.models.Utilizator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class EvaluareSolicitareActivity extends AppCompatActivity {

    LinearLayout toolbar;
    RelativeLayout relativeLayoutDateMelodie;
    ImageView imageViewImagineMelodie;
    TextView textViewNumeMelodie;
    TextView textViewNumeArtist;
    TextView textViewDataSolicitarii;
    TextView textViewNumeSolicitant;
    TextView textViewScopulUtilizarii;
    TextView textViewMediulUtilizarii;
    TextView textViewTitluLoculUtilizarii;
    TextView textViewLoculUtilizarii;
    TextView textViewMotivulUtilizarii;
    LinearLayout linearLayoutButoane;
    Button buttonAccepta;
    Button buttonRespinge;

    Solicitare solicitareSelectata; // memoreaza datele solicitarii
    Melodie melodieSolicitata; // memoreaza datele melodiei din solicitare
    Utilizator utilizatorSolicitant; // memoreaza datele solicitantului

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluare_solicitare);

        solicitareSelectata = (Solicitare) getIntent().getSerializableExtra(NumeExtra.EVALUARE_SOLICITARE); // obtine datele solicitarii din extra
        initializareVederi(); // initializeaza vederile activitatii
        edgeToEdge(this, toolbar, EdgeToEdge.Spatiere.PADDING, EdgeToEdge.Directie.SUS);
        edgeToEdge(this, linearLayoutButoane, EdgeToEdge.Spatiere.MARGIN, EdgeToEdge.Directie.JOS);

        // ascunde butoanele de evaluare daca utilizatorul curent nu este artistul melodiei sau daca stadiul melodiei difera de "acceptata"
        if (!Objects.equals(solicitareSelectata.getCheieArtist(), MainActivity.utilizator.getCheie())
                || !Objects.equals(solicitareSelectata.getStadiu(), Solicitare.NEEVALUATA)) {
            linearLayoutButoane.setVisibility(View.GONE);
        }

        // ascunde locul utilizarii daca mediul utilizarii este "Fizic"
        if (Objects.equals(solicitareSelectata.getMediulUtilizarii(), "Fizic")) {
            textViewTitluLoculUtilizarii.setVisibility(View.GONE);
            textViewLoculUtilizarii.setVisibility(View.GONE);
        }

        // seteaza datele campurilor
        textViewDataSolicitarii.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date(solicitareSelectata.getDataCreariiLong())));
        textViewNumeSolicitant.setText(solicitareSelectata.getTemp_numeSolicitant());
        textViewScopulUtilizarii.setText(solicitareSelectata.getScopulUtilizarii());
        textViewMediulUtilizarii.setText(solicitareSelectata.getMediulUtilizarii());
        textViewLoculUtilizarii.setText(solicitareSelectata.getLoculUtilizarii());
        textViewMotivulUtilizarii.setText(solicitareSelectata.getMotivulUtilizarii());

        // obtine datele melodiei din baza de date
        FirebaseDatabase.getInstance().getReference("melodii")
                .child(solicitareSelectata.getCheieMelodie())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        melodieSolicitata = snapshot.getValue(Melodie.class);
                        melodieSolicitata.setCheie(snapshot.getKey());

                        // adauga datele melodiei in obiectul solicitarii
                        solicitareSelectata.setTemp_numeMelodie(melodieSolicitata.getNumeMelodie());
                        solicitareSelectata.setTemp_numeArtist(melodieSolicitata.getNumeArtist());
                        solicitareSelectata.setTemp_imagineMelodie(melodieSolicitata.getImagineMelodie());

                        // seteaza datele obtinute din baza de date
                        Glide.with(EvaluareSolicitareActivity.this)
                                .load(solicitareSelectata.getTemp_imagineMelodie())
                                .apply(RequestOptions.centerCropTransform())
                                .placeholder(R.drawable.logo_music)
                                .error(R.drawable.ic_eroare)
                                .into(imageViewImagineMelodie);
                        textViewNumeMelodie.setText(solicitareSelectata.getTemp_numeMelodie());
                        textViewNumeArtist.setText(solicitareSelectata.getTemp_numeArtist());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EvaluareSolicitareActivity.this, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // obtine datele solicitantului din baza de date
        FirebaseDatabase.getInstance().getReference("utilizatori")
                .child(solicitareSelectata.getCheieSolicitant())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        utilizatorSolicitant = snapshot.getValue(Utilizator.class);
                        solicitareSelectata.setTemp_numeSolicitant(utilizatorSolicitant.getNume());
                        textViewNumeSolicitant.setText(solicitareSelectata.getTemp_numeSolicitant()); // seteaza numele solicitantului
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(EvaluareSolicitareActivity.this, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        // la apasarea datelor melodiei se lanseaza PlayerActivity cu melodia din solicitare
        relativeLayoutDateMelodie.setOnClickListener(v -> {
            if (melodieSolicitata != null) {
                List<Melodie> listaMelodie = new ArrayList<>();
                listaMelodie.add(melodieSolicitata); // adauga melodia curenta intr-o lista

                Intent intent = new Intent(EvaluareSolicitareActivity.this, PlayerActivity.class);
                intent.putExtra(NumeExtra.LISTA_MELODII, (Serializable) listaMelodie); // adauga lista cu melodia curenta in extra
                intent.putExtra(NumeExtra.POZITIE_MELODIE, 0); // adauga pozitia melodiei selectate
                startActivity(intent); // lanseaza PlayerActivity
            }
        });

        // la apasarea butonului "Accepta" se actualizeaza stadiul solicitarii in "acceptata"
        buttonAccepta.setOnClickListener(v ->
                FirebaseDatabase.getInstance().getReference("solicitari")
                        .child(solicitareSelectata.getCheie())
                        .child("stadiu").setValue(Solicitare.ACCEPTATA).addOnSuccessListener(unused -> {
                            linearLayoutButoane.setVisibility(View.GONE); // ascunde butoanele
                            EvaluareSolicitareActivity.this.finish(); // incheie activitatea
                        })

        );

        // la apasarea butonului "Respinge" se actualizeaza stadiul solicitarii in "respinsa"
        buttonRespinge.setOnClickListener(v ->
                FirebaseDatabase.getInstance().getReference("solicitari")
                        .child(solicitareSelectata.getCheie())
                        .child("stadiu").setValue(Solicitare.RESPINSA).addOnSuccessListener(unused -> {
                            linearLayoutButoane.setVisibility(View.GONE); // ascunde butoanele
                            EvaluareSolicitareActivity.this.finish(); // incheie activitatea
                        })
        );
    }

    /**
     * Initializeaza toate vederile din cadrul acestei activitati.
     */
    private void initializareVederi() {
        toolbar = findViewById(R.id.evaluare_solicitare_toolbar);
        relativeLayoutDateMelodie = findViewById(R.id.evaluare_solicitare_relativeLayoutDateMelodie);
        imageViewImagineMelodie = findViewById(R.id.evaluare_solicitare_imageViewImagineMelodie);
        textViewNumeMelodie = findViewById(R.id.evaluare_solicitare_textViewNumeMelodie);
        textViewNumeArtist = findViewById(R.id.evaluare_solicitare_textViewNumeArtist);
        textViewDataSolicitarii = findViewById(R.id.evaluare_solicitare_textViewDataSolicitarii);
        textViewNumeSolicitant = findViewById(R.id.evaluare_solicitare_textViewNumeSolicitant);
        textViewScopulUtilizarii = findViewById(R.id.evaluare_solicitare_textViewScopulUtilizarii);
        textViewMediulUtilizarii = findViewById(R.id.evaluare_solicitare_textViewMediulUtilizarii);
        textViewTitluLoculUtilizarii = findViewById(R.id.evaluare_solicitare_textViewTitluLoculUtilizarii);
        textViewLoculUtilizarii = findViewById(R.id.evaluare_solicitare_textViewLoculUtilizarii);
        textViewMotivulUtilizarii = findViewById(R.id.evaluare_solicitare_textViewMotivulUtilizarii);
        linearLayoutButoane = findViewById(R.id.evaluare_solicitare_linearLayoutButoane);
        buttonAccepta = findViewById(R.id.evaluare_solicitare_buttonAccepta);
        buttonRespinge = findViewById(R.id.evaluare_solicitare_buttonRespinge);

        toolbar.findViewById(R.id.toolbar_imageViewButonInapoi).setOnClickListener(v -> super.onBackPressed());
        TextView textView = toolbar.findViewById(R.id.toolbar_textViewTitlu);

        // seteaza titlul toolbar-ului in functie de stadiul solicitarii
        if (Objects.equals(solicitareSelectata.getStadiu(), Solicitare.NEEVALUATA)) {
            textView.setText("Evaluează Solicitarea");
        } else if (Objects.equals(solicitareSelectata.getStadiu(), Solicitare.ACCEPTATA)) {
            textView.setText("Solicitare Acceptată");
        } else if (Objects.equals(solicitareSelectata.getStadiu(), Solicitare.RESPINSA)) {
            textView.setText("Solicitare Respinsă");
        }
    }
}