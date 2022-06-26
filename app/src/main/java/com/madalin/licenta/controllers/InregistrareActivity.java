package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.Directie;
import static com.madalin.licenta.global.EdgeToEdge.Spatiere;
import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.madalin.licenta.R;
import com.madalin.licenta.models.Utilizator;

public class InregistrareActivity extends AppCompatActivity {
    private TextInputEditText editTextNume;
    private TextInputEditText editTextEmail;
    private TextInputEditText editTextParola;
    private TextView textViewAutentificate;
    private Button buttonInregistreazate;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inregistrare);
        edgeToEdge(this, findViewById(R.id.inregistrare_linearLayoutAutentificate), Spatiere.MARGIN, Directie.JOS);

        initializareVederi();

        firebaseAuth = FirebaseAuth.getInstance(); // initializare Firebase Auth
        firebaseDatabase = FirebaseDatabase.getInstance(); // initializare Firebase Database

        // listeners
        buttonInregistreazate.setOnClickListener(view -> creazaUtilizator());
        textViewAutentificate.setOnClickListener(view -> startActivity(new Intent(InregistrareActivity.this, AutentificareActivity.class))); // lansare AutentificareActivity la apasarea butonului
    }

    /**
     * Obtine si valideaza datele introduse in campuri. Daca datele sunt corecte, se creeaza noul
     * utilizator Firebase folosind {@link FirebaseAuth#createUserWithEmailAndPassword(String, String)}.
     * Daca crearea utilizatorului s-a efectuat cu succes, se adauga datele acestuia in baza de date.
     */
    private void creazaUtilizator() {
        // obtinere continut din edit text
        String nume = editTextNume.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String parola = editTextParola.getText().toString().trim();

        // verifica corectitudinea datelor din campuri
        // nume
        if (TextUtils.isEmpty(nume)) {
            editTextNume.setError("Numele nu poate fi gol!");
            editTextNume.requestFocus();
        } else if (nume.length() > 20) {
            editTextNume.setError("Numele este prea lung!");
            editTextNume.requestFocus();
        }
        // email
        else if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email-ul nu poate fi gol!");
            editTextEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email-ul este invalid!");
            editTextEmail.requestFocus();
        }
        // parola
        else if (TextUtils.isEmpty(parola)) {
            editTextParola.setError("Parola nu poate fi goala!");
            editTextParola.requestFocus();
        } else if (parola.length() < 6) {
            editTextParola.setError("Parola este prea scurta!");
            editTextParola.requestFocus();
        }
        // daca datele sunt corecte se trece la inregistrarea in Firebase
        else {
            progressDialog = new ProgressDialog(InregistrareActivity.this); // initializare Progress Dialog
            progressDialog.show(); // afiseaza progress dialog-ul
            progressDialog.setContentView(R.layout.layout_progress_dialog); // seteaza continutul progress dialog-ului cu layout_progress_dialog
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // seteaza fundalul progress dialog-ului ca transparent

            firebaseAuth.createUserWithEmailAndPassword(email, parola) // creeaza noul utilizator folosind adresa de email si parola
                    .addOnCompleteListener(taskCreareUtilizator -> { // listener pentru efectuarea actiunilor la finalizarea crearii utilizatorului
                        // daca utilizatorul s-a creat cu succes
                        if (taskCreareUtilizator.isSuccessful()) {
                            Utilizator utilizator = new Utilizator(nume, email, Utilizator.GRAD_NORMAL); // memoreaza datele utilizatorului curent intr-un obiect Utilizator

                            firebaseDatabase.getReference("utilizatori") // obtine referinta spre "utilizatori"
                                    .child(firebaseAuth.getCurrentUser().getUid()) // adauga child lui "utilizatori" avand ca nume UID-ul utilizatorului curent
                                    .setValue(utilizator) // adauga in noul child datele din obiect
                                    .addOnCompleteListener(taskAdaugareInBazaDeDate -> { // listener pentru efectuarea actiunilor la finalizarea adaugarii datelor utilizatorului in baza de date
                                        // daca datele s-au adaugat cu succes in baza de date
                                        if (taskAdaugareInBazaDeDate.isSuccessful()) {
                                            progressDialog.findViewById(R.id.progress_dialog_progressBar).setVisibility(View.GONE); // ascunde spinner-ul
                                            progressDialog.findViewById(R.id.progress_dialog_lottie).setVisibility(View.VISIBLE); // afiseaza animatia lottie
                                            TextView textViewProgressDialog = progressDialog.findViewById(R.id.progress_dialog_textViewMesaj);
                                            textViewProgressDialog.setText("Te-ai înregistrat cu succes!");

                                            new Handler().postDelayed(() -> {
                                                progressDialog.dismiss();
                                                startActivity(new Intent(InregistrareActivity.this, AutentificareActivity.class)); // lanseaza AutentificareActivity dupa crearea utilizatorului si adaugarea datelor acestuia in baza de date
                                            }, 3000); // inlatura progress dialog-ul si lanseaza AutentificareActivity dupa 3 secunde
                                        }
                                        // daca datele nu s-au putut adauga
                                        else {
                                            Toast.makeText(InregistrareActivity.this, "Eroare la inregistrare date: " + taskCreareUtilizator.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss(); // inlatura progress dialog-ul
                                        }
                                    });
                        }
                        // daca utilizatorul nu s-a putut crea
                        else {
                            Toast.makeText(InregistrareActivity.this, "Eroare la inregistrare: " + taskCreareUtilizator.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss(); // inlatura progress dialog-ul
                        }
                    });
        }
    }

    /**
     * Initializeaza toate vederile din cadrul acestei activitati.
     */
    private void initializareVederi() {
        editTextNume = findViewById(R.id.inregistrare_editTextNume);
        editTextEmail = findViewById(R.id.inregistrare_editTextEmail);
        editTextParola = findViewById(R.id.inregistrare_editTextParola);
        textViewAutentificate = findViewById(R.id.inregistrare_textViewAutentificate);
        buttonInregistreazate = findViewById(R.id.inregistrare_buttonInregistreazate);
    }
}