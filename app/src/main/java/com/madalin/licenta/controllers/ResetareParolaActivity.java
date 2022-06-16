package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.Directie;
import static com.madalin.licenta.global.EdgeToEdge.Spatiere;
import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.madalin.licenta.R;

public class ResetareParolaActivity extends AppCompatActivity {

    private EditText editTextIntroducereEmail;
    private Button buttonReseteazaParola;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resetare_parola);
        edgeToEdge(this, findViewById(R.id.resetare_parola_buttonReseteazaParola), Spatiere.MARGIN, Directie.JOS);

        firebaseAuth = FirebaseAuth.getInstance();

        // obtinere vederi
        editTextIntroducereEmail = findViewById(R.id.resetare_parola_editTextIntroducereEmail);
        buttonReseteazaParola = findViewById(R.id.resetare_parola_buttonReseteazaParola);

        buttonReseteazaParola.setOnClickListener(v -> {
            String email = editTextIntroducereEmail.getText().toString().trim();
            System.out.println(email);
            if (email.isEmpty()) {
                editTextIntroducereEmail.setError("Email-ul este necesar!");
                editTextIntroducereEmail.requestFocus();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editTextIntroducereEmail.setError("Introdu un email valid!");
                editTextIntroducereEmail.requestFocus();
            } else {
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(taskResetareParola -> {
                    if (taskResetareParola.isSuccessful()) {
                        Toast.makeText(ResetareParolaActivity.this, "Verifică-ți email-ul pentru a-ți reseta parola!", Toast.LENGTH_LONG).show();
                        finish(); // incheiere activitate
                    } else {
                        Toast.makeText(ResetareParolaActivity.this, "Reîncearcă! Ceva nu a mers bine!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
}