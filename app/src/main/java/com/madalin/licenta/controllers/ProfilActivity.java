package com.madalin.licenta.controllers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.models.Utilizator;

public class ProfilActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private String userID;

    private TextView textViewSalut;
    private TextView textViewEmail;
    private TextView textViewNume;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil);

        button = findViewById(R.id.profil_buttonMeniu);

        // deconectare de la firebase
        /*button.setOnClickListener(v -> afisareDialog());*/

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Utilizatori"); // referinta catre ...
        userID = firebaseUser.getUid(); // obtinere ID utilizator

        textViewSalut = findViewById(R.id.profil_textViewSalut);
        textViewEmail = findViewById(R.id.profil_textViewEmail);
        textViewNume = findViewById(R.id.bottom_sheet_meniu_textViewNume);

        // obtinere date utilizator din baza de date in functie de ID-ul acestuia
        databaseReference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // obtinere date din DB printr-un data snapshot si marshalling date in clasa Utilizator
                Utilizator profilUtilizator = snapshot.getValue(Utilizator.class);

                if (profilUtilizator != null) {
                    textViewSalut.setText("Salut, " + profilUtilizator.nume + "!");
                    textViewNume.setText(profilUtilizator.nume);
                    textViewEmail.setText(profilUtilizator.email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfilActivity.this, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

/*
    // metoda pentru afisarea dialogului meniu
    private void afisareDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ProfilActivity.this, R.style.BottomSheetDialogTheme);
        //bottomSheetDialog.setCanceledOnTouchOutside(false);

        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.layout_bottom_sheet,
                        (LinearLayout) findViewById(R.id.bottom_sheet_container));

        // accesare activitate profil
        bottomSheetView.findViewById(R.id.profil).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            //startActivity(new Intent(MainActivity.this, ProfilActivity.class));
        });

        // deconectare de la firebase
        bottomSheetView.findViewById(R.id.buttonDeconectare).setOnClickListener(view -> {
            //firebaseAuth.signOut();
            bottomSheetDialog.dismiss();
            //startActivity(new Intent(MainActivity.this, AutentificareActivity.class));
        });

        bottomSheetDialog.setContentView(bottomSheetView); // setare continut bottom sheet
        bottomSheetDialog.show(); // afisare bottom sheet
    }*/
}