package com.madalin.licenta.controller;

import static com.madalin.licenta.EdgeToEdge.Directie;
import static com.madalin.licenta.EdgeToEdge.Spatiere;
import static com.madalin.licenta.EdgeToEdge.edgeToEdge;

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
import com.google.firebase.auth.FirebaseUser;
import com.madalin.licenta.R;

public class AutentificareActivity extends AppCompatActivity {

    TextInputEditText editTextEmail;
    TextInputEditText editTextParola;
    TextView textViewInregistreazate;
    Button buttonLogin;
    TextView textViewRecuperareCont;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autentificare);
        edgeToEdge(this, findViewById(R.id.autentificare_textViewRecuperareCont), Spatiere.MARGIN, Directie.JOS);

        firebaseAuth = FirebaseAuth.getInstance(); // initializare Firebase Auth

        // obtinere vederi
        editTextEmail = findViewById(R.id.autentificare_editTextEmail);
        editTextParola = findViewById(R.id.autentificare_editTextParola);
        textViewInregistreazate = findViewById(R.id.autentificare_textViewInregistreazate);
        buttonLogin = findViewById(R.id.autentificare_buttonLogin);
        textViewRecuperareCont = findViewById(R.id.autentificare_textViewRecuperareCont);

        // listeners
        buttonLogin.setOnClickListener(view -> loginUtilizator());
        textViewInregistreazate.setOnClickListener(view -> startActivity(new Intent(AutentificareActivity.this, InregistrareActivity.class)));
        textViewRecuperareCont.setOnClickListener(view -> startActivity(new Intent(AutentificareActivity.this, ResetareParolaActivity.class)));
    }

    private void loginUtilizator() {
        // obtinere continut edit text
        String email = editTextEmail.getText().toString().trim();
        String parola = editTextParola.getText().toString();

        // verifica corectitudinea datelor din campuri
        // email
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email-ul nu poate fi gol!");
            editTextEmail.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email-ul nu este valid!");
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
        // daca datele sunt corecte se trece la autentificarea in Firebase
        else {
            firebaseAuth.signInWithEmailAndPassword(email, parola)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                            // verifica daca adresa de email a utilizatorului este confirmata
                            if (firebaseUser.isEmailVerified()) {
                                Toast.makeText(AutentificareActivity.this, "Te-ai autentificat cu succes!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(AutentificareActivity.this, MainActivity.class)); // lansare activitate principala dupa autentificare
                            } else {
                                firebaseUser.sendEmailVerification(); // trimite mail pentru verificare
                                Toast.makeText(AutentificareActivity.this, "Verifică-ți emailul pentru confirmarea contului!", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(AutentificareActivity.this, "Eroare la autentificare: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}