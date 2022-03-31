package com.madalin.licenta.controller;

import static com.madalin.licenta.EdgeToEdge.Directie;
import static com.madalin.licenta.EdgeToEdge.Spatiere;
import static com.madalin.licenta.EdgeToEdge.edgeToEdge;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.madalin.licenta.R;
import com.madalin.licenta.model.Utilizator;

public class InregistrareActivity extends AppCompatActivity {
    TextInputEditText editTextNume;
    TextInputEditText editTextEmail;
    TextInputEditText editTextParola;
    TextView textViewAutentificate;
    Button buttonInregistreazate;
    ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inregistrare);
        edgeToEdge(this, findViewById(R.id.inregistrare_linearLayoutAutentificate), Spatiere.MARGIN, Directie.JOS);

        firebaseAuth = FirebaseAuth.getInstance(); // initializare Firebase Auth

        // obtinere vederi
        editTextNume = findViewById(R.id.inregistrare_editTextNume);
        editTextEmail = findViewById(R.id.inregistrare_editTextEmail);
        editTextParola = findViewById(R.id.inregistrare_editTextParola);
        textViewAutentificate = findViewById(R.id.inregistrare_textViewAutentificate);
        buttonInregistreazate = findViewById(R.id.inregistrare_buttonInregistreazate);

        // listeners
        buttonInregistreazate.setOnClickListener(view -> creazaUtilizator());
        textViewAutentificate.setOnClickListener(view -> startActivity(new Intent(InregistrareActivity.this, AutentificareActivity.class)));
    }

    private void creazaUtilizator() {
        // obtinere continut edit text
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
            progressDialog.show(); // afisare dialog
            progressDialog.setContentView(R.layout.layout_progress_dialog); // setare Content View
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // fundal transparent

            firebaseAuth.createUserWithEmailAndPassword(email, parola)
                    .addOnCompleteListener(taskCreareUtilizator -> {
                        // daca s-a inregistrat cu succes
                        if (taskCreareUtilizator.isSuccessful()) {
                            Utilizator utilizator = new Utilizator(nume, email);

                            FirebaseDatabase.getInstance().getReference("Utilizatori")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid()) // inregistrarii cu ID-ului respectiv
                                    .setValue(utilizator) // i se atribuie datele din obiect
                                    .addOnCompleteListener(taskAdaugareInBazaDeDate -> {
                                        if (taskAdaugareInBazaDeDate.isSuccessful()) {
                                            Toast.makeText(InregistrareActivity.this, "Te-ai inregistrat cu succes!", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss(); // inchidere Progress Dialog

                                            // lansare activitate login dupa inregistrare
                                            startActivity(new Intent(InregistrareActivity.this, AutentificareActivity.class));
                                        } else {
                                            Toast.makeText(InregistrareActivity.this, "Eroare la inregistrare date: " + taskCreareUtilizator.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        }
                                    });
                        } else {
                            Toast.makeText(InregistrareActivity.this, "Eroare la inregistrare: " + taskCreareUtilizator.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    });
        }
    }
}