package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.Directie;
import static com.madalin.licenta.global.EdgeToEdge.Spatiere;
import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;

public class AutentificareActivity extends AppCompatActivity {

    TextInputEditText editTextEmail;
    TextInputEditText editTextParola;
    TextView textViewInregistreazate;
    Button buttonLogin;
    TextView textViewRecuperareCont;

    private FirebaseAuth firebaseAuth; // punctul de intrare al SDK-ului Firebase Authentication
    private FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autentificare);
        edgeToEdge(this, findViewById(R.id.autentificare_textViewRecuperareCont), Spatiere.MARGIN, Directie.JOS);

        firebaseAuth = FirebaseAuth.getInstance(); // initializare Firebase Auth
        firebaseDatabase = FirebaseDatabase.getInstance();

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

    /**
     * Obtine si valideaza datele introduse in campuri. Daca datele sunt corecte, se apeleaza
     * {@link FirebaseAuth#signInWithEmailAndPassword(String, String)} pentru autentificare. Daca
     * autentificarea s-a realizat cu succes, se verifica daca adresa de email a contului este
     * verificata.
     */
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
            editTextParola.setError("Parola nu poate fi goală!");
            editTextParola.requestFocus();
        } else if (parola.length() < 6) {
            editTextParola.setError("Parola este prea scurtă!");
            editTextParola.requestFocus();
        }
        // daca datele sunt corecte se trece la autentificarea in Firebase
        else {
            firebaseAuth.signInWithEmailAndPassword(email, parola)
                    .addOnCompleteListener(task -> {
                        // daca autentificarea s-a realizat cu succes
                        if (task.isSuccessful()) {
                            // verifica daca adresa de email a utilizatorului este confirmata
                            if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                startActivity(new Intent(AutentificareActivity.this, MainActivity.class)); // lansare activitate principala dupa autentificare
                            } else {
                                firebaseAuth.getCurrentUser().sendEmailVerification(); // trimite mail pentru verificare
                                Toast.makeText(AutentificareActivity.this, "Verifică-ți emailul pentru confirmarea contului!", Toast.LENGTH_LONG).show();
                            }
                        }
                        // daca autentificarea a esuat
                        else {
                            Toast.makeText(AutentificareActivity.this, "Eroare la autentificare: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}