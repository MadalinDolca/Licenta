package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.madalin.licenta.global.EdgeToEdge.*;
import com.madalin.licenta.R;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.models.Solicitare;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

public class TrimitereSolicitareActivity extends AppCompatActivity {

    LinearLayout toolbar;
    ImageView imageViewImagineMelodie;
    TextView textViewNumeMelodie;
    TextView textViewNumeArtist;
    Spinner spinnerScop;
    Spinner spinnerMediu;
    EditText editTextLoculUtilizarii;
    EditText editTextMotivulUtilizarii;
    Button buttonTrimiteSolicitarea;
    ProgressDialog progressDialog;

    Melodie melodieSelectata; // datele melodiei selectate
    Solicitare solicitare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicita_permisiunea);

        initializareVederi(); // initializeaza vederile activitatii
        edgeToEdge(this, toolbar, Spatiere.PADDING, Directie.SUS);
        edgeToEdge(this, buttonTrimiteSolicitarea, Spatiere.MARGIN, Directie.JOS);

        melodieSelectata = (Melodie) getIntent().getSerializableExtra(NumeExtra.SOLICITA_PERMISIUNEA); // obtine datele melodiei din extra

        // populeaza campurile
        Glide.with(this).load(melodieSelectata.getImagineMelodie()).placeholder(R.drawable.logo_music).into(imageViewImagineMelodie);
        textViewNumeMelodie.setText(melodieSelectata.getNumeMelodie());
        textViewNumeArtist.setText(melodieSelectata.getNumeArtist());

        // spinner scop
        ArrayAdapter<CharSequence> adapterSpinnerScop = ArrayAdapter.createFromResource(this, R.array.array_scop_solicitare, R.layout.layout_spinner_item); // populeaza spinner-ul cu datele din resursa array si seteaza aspectul optiunilor
        adapterSpinnerScop.setDropDownViewResource(R.layout.layout_spinner_dropdown_item); // seteaza aspectul optiunilor din dropdown
        spinnerScop.setAdapter(adapterSpinnerScop); // seteaza adapter-ul spinner-ului

        // spinner mediu
        ArrayAdapter<CharSequence> adapterSpinnerMediu = ArrayAdapter.createFromResource(this, R.array.array_mediu_utilizare, R.layout.layout_spinner_item);
        adapterSpinnerMediu.setDropDownViewResource(R.layout.layout_spinner_dropdown_item);
        spinnerMediu.setAdapter(adapterSpinnerMediu);

        // listener optiune selectata spinner mediu
        spinnerMediu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // daca optiunea selectata este "Online" sau "Ambele"
                if (Objects.equals(parent.getItemAtPosition(position).toString(), "Online")
                        || Objects.equals(parent.getItemAtPosition(position).toString(), "Ambele")) {
                    editTextLoculUtilizarii.setVisibility(View.VISIBLE); // se afiseaza campul pentru locul utilizarii
                } else {
                    editTextLoculUtilizarii.setVisibility(View.GONE);  // se ascunde campul pentru locul utilizarii
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // listener buton trimitere solicitare
        buttonTrimiteSolicitarea.setOnClickListener(v -> trimitereSolicitare());
    }

    private void trimitereSolicitare() {
        // obtinere continut din edit text
        String loculUtilizarii = editTextLoculUtilizarii.getText().toString().trim();
        String motivulUtilizarii = editTextMotivulUtilizarii.getText().toString().trim();
        boolean isLocCorect = true;
        boolean isMotivCorect = true;

        // verifica corectitudinea datelor din campuri
        // locul utilizarii
        if (!spinnerMediu.getSelectedItem().toString().equals("Fizic")) {
            if (TextUtils.isEmpty(loculUtilizarii)) {
                editTextLoculUtilizarii.setError("Locul utilizarii nu poate fi gol!");
                editTextLoculUtilizarii.requestFocus();
                isLocCorect = false;
            }
        }

        // motivul utilizarii
        if (TextUtils.isEmpty(motivulUtilizarii)) {
            editTextMotivulUtilizarii.setError("Motivul utilizarii nu poate fi gol!");
            editTextMotivulUtilizarii.requestFocus();
            isMotivCorect = false;
        }

        // daca datele au fost introduse corect, se trece la trimiterea solicitarii
        if (isLocCorect && isMotivCorect) {
            progressDialog = new ProgressDialog(TrimitereSolicitareActivity.this); // initializare Progress Dialog
            progressDialog.show(); // afiseaza progress dialog-ul
            progressDialog.setContentView(R.layout.layout_progress_dialog); // seteaza continutul progress dialog-ului cu layout_progress_dialog
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // seteaza fundalul progress dialog-ului ca transparent

            // construieste obiectul solicitarii
            // daca NU s-a selectat optiunea "Fizic", se adauga locul utilizarii in obiect
            solicitare = new Solicitare(
                    FirebaseAuth.getInstance().getCurrentUser().getUid(), // cheie solicitant
                    melodieSelectata.getCheieArtist(), // cheie artist
                    melodieSelectata.getCheie(), // cheie melodie
                    spinnerScop.getSelectedItem().toString(), // scopul utilizarii
                    spinnerMediu.getSelectedItem().toString(), // mediul utilizarii
                    spinnerMediu.getSelectedItem().toString().equals("Fizic") ? null : loculUtilizarii,
                    motivulUtilizarii,
                    Solicitare.NEEVALUATA // stadiul solicitarii
            );

            // adauga datele solicitarii in baza de date
            FirebaseDatabase.getInstance().getReference("solicitari").push()
                    .setValue(solicitare) // seteaza valorile de adaugat noului nod creat

                    .addOnCompleteListener(task -> {
                        progressDialog.findViewById(R.id.progress_dialog_progressBar).setVisibility(View.GONE); // ascunde spinner-ul
                        progressDialog.findViewById(R.id.progress_dialog_lottie).setVisibility(View.VISIBLE); // afiseaza animatia lottie
                        TextView textViewProgressDialog = progressDialog.findViewById(R.id.progress_dialog_textViewMesaj);
                        textViewProgressDialog.setText("Solicitarea a fost trimisa cu succes!");

                        new Handler().postDelayed(() -> {
                            progressDialog.dismiss();
                            finish();// incheie activitatea
                        }, 3000); // inlatura progress dialog-ul dupa 3 secunde
                    })

                    .addOnFailureListener(e -> Toast.makeText(this, "Eroare: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * Initializeaza vederile activitatii curente.
     */
    private void initializareVederi() {
        toolbar = findViewById(R.id.toolbar);
        imageViewImagineMelodie = findViewById(R.id.solicita_permisiunea_imageViewImagineMelodie);
        textViewNumeMelodie = findViewById(R.id.solicita_permisiunea_textViewNumeMelodie);
        textViewNumeArtist = findViewById(R.id.solicita_permisiunea_textViewNumeArtist);
        spinnerScop = findViewById(R.id.solicita_permisiunea_spinnerScop);
        spinnerMediu = findViewById(R.id.solicita_permisiunea_spinnerMediu);
        editTextLoculUtilizarii = findViewById(R.id.solicita_permisiunea_editTextLoculUtilizarii);
        editTextMotivulUtilizarii = findViewById(R.id.solicita_permisiunea_editTextMotiv);
        buttonTrimiteSolicitarea = findViewById(R.id.solicita_permisiunea_buttonTrimiteSolicitarea);

        toolbar.findViewById(R.id.toolbar_imageViewButonInapoi).setOnClickListener(v -> super.onBackPressed());
        TextView textView = toolbar.findViewById(R.id.toolbar_textViewTitlu);
        textView.setText("Solicită permisiunea");
    }
}