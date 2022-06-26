package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.madalin.licenta.R;
import com.madalin.licenta.global.EdgeToEdge;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Melodie;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class EditareMelodieActivity extends AppCompatActivity {

    private LinearLayout toolbar;
    private ImageView imageViewImagineMelodie;
    private EditText editTextNumeMelodie;
    private EditText editTextDescriereMelodie;
    private Spinner spinnerGenMelodie;
    private Button buttonActualizareMelodie;
    private ProgressDialog progressDialog;

    private Melodie melodieSelectata;
    private Uri uriImagine;
    private final int CERERE_ALEGERE_IMAGINE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editare_melodie);

        initializareVederi();
        edgeToEdge(this, findViewById(R.id.editare_melodie_toolbar), EdgeToEdge.Spatiere.PADDING, EdgeToEdge.Directie.SUS);
        edgeToEdge(this, findViewById(R.id.editare_melodie_linearLayoutContainer), EdgeToEdge.Spatiere.MARGIN, EdgeToEdge.Directie.JOS);

        melodieSelectata = (Melodie) getIntent().getSerializableExtra(NumeExtra.EDITARE_MELODIE); // obtine datele melodiei din extra

        // configurare spinner
        ArrayAdapter<CharSequence> adapterSpinnerGenMelodie = ArrayAdapter.createFromResource(this, R.array.array_genuri_muzicale, R.layout.layout_spinner_item); // populare spinner si setare aspect optiune
        adapterSpinnerGenMelodie.setDropDownViewResource(R.layout.layout_spinner_dropdown_item); // setare aspect optiuni din dropdown
        spinnerGenMelodie.setAdapter(adapterSpinnerGenMelodie); // aplicare adapter la spinner

        // populare campuri
        Glide.with(this).load(melodieSelectata.getImagineMelodie()).placeholder(R.drawable.logo_music).error(R.drawable.ic_eroare).into(imageViewImagineMelodie);
        editTextNumeMelodie.setText(melodieSelectata.getNumeMelodie());
        editTextDescriereMelodie.setText(melodieSelectata.getDescriere());
        spinnerGenMelodie.setSelection(adapterSpinnerGenMelodie.getPosition(melodieSelectata.getGenMelodie()));

        imageViewImagineMelodie.setOnClickListener(v -> alegeImagineaMelodiei());
        buttonActualizareMelodie.setOnClickListener(v -> actualizareMelodie());
    }


    /**
     * Apelata atunci cand activitatea de alegere a unei imagini este inchisa. Ofera un requestCode
     * identic cu cel oferit la lansare, un resultCode returnat și datele respective fisierului
     * selectat. Salveaza datele acestora si le afiseaza in vederi.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CERERE_ALEGERE_IMAGINE
                && resultCode == Activity.RESULT_OK /* daca s-a ales o  imagine */
                && data != null && data.getData() != null /* daca s-au obtinut date mari */) {

            uriImagine = data.getData(); // obtine URI-ul imaginii
            Glide.with(this).load(uriImagine).into(imageViewImagineMelodie); // adauga imaginea in vedere
        }
    }

    /**
     * Lanseaza o activitate de tip File Chooser pentru selectarea fisierelor de tip imagine.
     */
    private void alegeImagineaMelodiei() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // permite utilizatorului sa selecteze datele si sa le returneze
        intent.setType("image/*"); // afiseaza doar imaginile in File Chooser
        startActivityForResult(intent, CERERE_ALEGERE_IMAGINE);
    }

    /**
     * Creeaza o referinta spre nodul melodiei din baza de date.
     * Verifica corectitudinea datelor introduse in campuri si afiseaza dialogul de incarcare.
     * Adauga datele din campuri in valorile nodului melodiei.
     * Daca s-a ales o imagine, creeaza o referinta in Storage "imagini" spre un child avand ca
     * nume timpul curent si extensia imaginii.
     * Incarca imaginea in noul child si obtine URL-ul de descarcare, iar apoi il adauga ca data in
     * nodul melodiei din baza de date. Actualizeaza dialogul intr-un mesaj de succes.
     */
    private void actualizareMelodie() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("melodii").child(melodieSelectata.getCheie()); // referinta spre nodul melodiei din baza de date

        // obtinere continut din edit text
        String numeMelodie = editTextNumeMelodie.getText().toString().trim();
        String descriereMelodie = editTextDescriereMelodie.getText().toString().trim();
        String genMelodie = spinnerGenMelodie.getSelectedItem().toString();

        // verifica corectitudinea datelor
        // nume melodie
        if (TextUtils.isEmpty(numeMelodie)) {
            editTextNumeMelodie.setError("Numele melodiei nu poate fi gol!");
            editTextNumeMelodie.requestFocus();
        } else if (numeMelodie.length() > 100) {
            editTextNumeMelodie.setError("Numele melodiei este prea lung!");
            editTextNumeMelodie.requestFocus();
        }
        // daca datele sunt corecte se trece la incarcarea datelor in Firebase
        else {
            progressDialog = new ProgressDialog(this); // initializare Progress Dialog
            progressDialog.show(); // afiseaza progress dialog-ul
            progressDialog.setContentView(R.layout.layout_progress_dialog); // seteaza continutul progress dialog-ului cu layout_progress_dialog
            TextView textViewProgressDialog = progressDialog.findViewById(R.id.progress_dialog_textViewMesaj);
            textViewProgressDialog.setText("Se actualizează");
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // seteaza fundalul progress dialog-ului ca transparent

            // actualizeaza datele de nodului melodiei din baza de date
            databaseReference.child("numeMelodie").setValue(numeMelodie);
            databaseReference.child("descriere").setValue(descriereMelodie);
            databaseReference.child("genMelodie").setValue(genMelodie);

            // incarca imaginea aferenta melodiei in cazul in care a fost selectata
            if (uriImagine != null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference("imagini")
                        .child(System.currentTimeMillis() + "." + getExtensieFisier(uriImagine)); // returneaza locatia noului child imagine din Storage avand ca nume timpul curent in milisecunde

                storageReference.putFile(uriImagine) // incarca asincron continutul URI-ului imaginii in noul child creat
                        .addOnSuccessListener(taskSnapshotImagine ->
                                storageReference.getDownloadUrl().addOnSuccessListener(uri -> { // obtine locatia la care s-a incarcat imaginea
                                    databaseReference.child("imagineMelodie").setValue(uri.toString()); // adauga imaginea melodiei in baza de date
                                }))
                        .addOnFailureListener(e -> Toast.makeText(this, "Nu s-a putut actualiza imaginea: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }

            progressDialog.findViewById(R.id.progress_dialog_progressBar).setVisibility(View.GONE); // ascunde spinner-ul
            progressDialog.findViewById(R.id.progress_dialog_lottie).setVisibility(View.VISIBLE); // afiseaza animatia lottie
            textViewProgressDialog.setText("Melodia s-a actualizat cu succes!");

            new Handler().postDelayed(() -> {
                progressDialog.setProgress(0);
                progressDialog.dismiss(); // inlatura progress dialog-ul
                finish(); // inchide activitatea
            }, 3000); // adauga un delay inainte de a reseta progresul
        }
    }

    /**
     * Returneaza extensia unui fisier in functie de URI-ul acestuia.
     *
     * @param uri URI-ul fisierului
     * @return extensia fara punct a fisierului
     */
    private String getExtensieFisier(Uri uri) {
        ContentResolver contentResolver = this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * Returneaza numele fisierului in functie de URI-ul acestuia.
     *
     * @param uri URI-ul fisierului
     * @return numele fisierului
     */
    private String getNumeFisier(Uri uri) {
        Cursor returnCursor = this.getContentResolver().query(uri, null, null, null, null); // obtine numele si dimensiunea fisierului
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME); // obtine numele fisierului din cursor

        returnCursor.moveToFirst();

        return returnCursor.getString(nameIndex); // numele fisierului
        //Long.toString(returnCursor.getLong(sizeIndex)) // dimensiunea fisierului
    }

    /**
     * Initializeaza vederile activitatii.
     */
    private void initializareVederi() {
        toolbar = findViewById(R.id.editare_melodie_toolbar);
        imageViewImagineMelodie = findViewById(R.id.editare_melodie_imageViewImagineMelodie);
        editTextNumeMelodie = findViewById(R.id.editare_melodie_editTextNumeMelodie);
        editTextDescriereMelodie = findViewById(R.id.editare_melodie_editTextDescriereMelodie);
        spinnerGenMelodie = findViewById(R.id.editare_melodie_spinnerGenMelodie);
        buttonActualizareMelodie = findViewById(R.id.editare_melodie_buttonActualizeazaMelodia);

        toolbar.findViewById(R.id.toolbar_imageViewButonInapoi).setOnClickListener(v -> super.onBackPressed());
        TextView textView = toolbar.findViewById(R.id.toolbar_textViewTitlu);
        textView.setText("Editează Melodia");
    }
}