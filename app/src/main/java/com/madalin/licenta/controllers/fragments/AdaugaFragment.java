package com.madalin.licenta.controllers.fragments;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.AutentificareActivity;
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.global.UtilitareFragment;
import com.madalin.licenta.models.Melodie;

public class AdaugaFragment extends Fragment {

    private ImageView imageViewImagineMelodie;
    private ImageView imageViewFisierAudio;
    private TextView textViewNumeFisierAudio;
    private EditText editTextNumeMelodie;
    private EditText editTextDescriereMelodie;
    private Spinner spinnerGenMelodie;
    private Button buttonIncarcaMelodia;
    private ProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseStorage firebaseStorage;

    private Uri uriImagine;
    private Uri uriMelodie;

    private final int CERERE_ALEGERE_IMAGINE = 1;
    private final int CERERE_ALEGERE_MELODIE = 2;

    // constructor gol folosit atunci cand Android decide sa recreeze fragmentul
    public AdaugaFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initializare instante Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_adauga, container, false); // returneaza vederea de utilizat pentru fragmentul curent
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializareVederi();

        //spinnerGenMelodie = viewFragmentAdauga.findViewById(R.id.adauga_spinnerGenMelodie);
        ArrayAdapter<CharSequence> adapterSpinnerGenMelodie = ArrayAdapter.createFromResource(getContext(), R.array.array_genuri_muzicale, R.layout.layout_spinner_item); // populare spinner si setare aspect optiune
        adapterSpinnerGenMelodie.setDropDownViewResource(R.layout.layout_spinner_dropdown_item); // setare aspect optiuni din dropdown
        spinnerGenMelodie.setAdapter(adapterSpinnerGenMelodie); // aplicare adapter la spinner

        imageViewImagineMelodie.setOnClickListener(v -> alegeImagineaMelodiei());
        imageViewFisierAudio.setOnClickListener(v -> alegeFisierulAudio());
        buttonIncarcaMelodia.setOnClickListener(v -> incarcaMelodia());
    }

    @Override
    public void onResume() {
        super.onResume();

        Button buttonAutentificare;

        // daca utilizatorul nu este conectat
        if (firebaseAuth.getCurrentUser() == null) {
            UtilitareFragment.inlocuireLayoutVedereFragment(AdaugaFragment.this, R.layout.layout_solicitare_autentificare); // inlocuieste layout-ul vederii fragmentului cu cel pentru solicitarea autentificarii

            buttonAutentificare = requireView().findViewById(R.id.solicitare_autentificare_buttonAutentificare); // obtine vederea butonului de autentificare
            buttonAutentificare.setOnClickListener(view -> startActivity(new Intent(getActivity(), AutentificareActivity.class))); // seteaza un listener pentru lansarea AutentificareActivity la apasarea butonului
        }
    }

    /**
     * Apelata atunci cand activitatea de alegere a unei imagini sau de alegere a unei melodii este
     * inchisa. Ofera un requestCode identic cu cel oferit la lansare, un resultCode returnat și
     * datele respective fisierului selectat. Salveaza datele acestora si le afiseaza in vederi.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // imagine
        if (requestCode == CERERE_ALEGERE_IMAGINE
                && resultCode == Activity.RESULT_OK /* daca s-a ales o  imagine */
                && data != null && data.getData() != null /* daca s-au obtinut date mari */) {

            uriImagine = data.getData(); // obtine URI-ul imaginii
            Glide.with(this).load(uriImagine).into(imageViewImagineMelodie); // adauga imaginea in vedere
        }
        // melodie
        else if (requestCode == CERERE_ALEGERE_MELODIE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            uriMelodie = data.getData(); // obtine URI-ul melodiei
            imageViewFisierAudio.setVisibility(View.INVISIBLE); // ascunde resursa
            textViewNumeFisierAudio.setText(getNumeFisier(uriMelodie)); // afiseaza numele melodiei
        }
    }

    /**
     * Returneaza extensia unui fisier in functie de URI-ul acestuia.
     *
     * @param uri URI-ul fisierului
     * @return extensia fara punct a fisierului
     */
    private String getExtensieFisier(Uri uri) {
        ContentResolver contentResolver = requireContext().getContentResolver();
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
        Cursor returnCursor = requireContext().getContentResolver().query(uri, null, null, null, null); // obtine numele si dimensiunea fisierului
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME); // obtine numele fisierului din cursor

        returnCursor.moveToFirst();

        return returnCursor.getString(nameIndex); // numele fisierului
        //Long.toString(returnCursor.getLong(sizeIndex)) // dimensiunea fisierului
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
     * Lanseaza o activitate de tip File Chooser pentru selectarea fisierelor audio.
     */
    private void alegeFisierulAudio() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT); // permite utilizatorului sa selecteze datele si sa le returneze
        intent.setType("audio/*"); // afiseaza doar fisierele audio in File Chooser
        startActivityForResult(intent, CERERE_ALEGERE_MELODIE);
    }

    /**
     * Verifica corectitudinea datelor introduse in campuri si afiseaza dialogul de incarcare.
     * Creeaza un child in Storage in nodul "melodii" format din timpul curent si extensia melodiei.
     * Incarca melodia in noul child creat si obtine URL-ul de descarcare. Creeaza un nou obiect
     * {@link Melodie} si adauga datele aferente melodiei, iar apoi il adauga in baza de date.
     * Creeaza un child in Storage in nodul "imagini" avand ca nume timpul curent si extensia imaginii.
     * Incarca imaginea in noul child si obtine URL-ul de descarcare, iar apoi il adauga ca data in
     * nodul melodiei din baza de date. Actualizeaza dialogul intr-un mesaj de succes.
     */
    private void incarcaMelodia() {
        // obtinere continut din edit text
        String numeMelodie = editTextNumeMelodie.getText().toString().trim();
        String descriereMelodie = editTextDescriereMelodie.getText().toString().trim();
        String genMelodie = spinnerGenMelodie.getSelectedItem().toString();

        // verifica corectitudinea datelor
        // fisierul audio
        if (uriMelodie == null) {
            Toast.makeText(getContext(), "Trebuie să alegi o melodie!", Toast.LENGTH_LONG).show();
        }
        // nume melodie
        else if (TextUtils.isEmpty(numeMelodie)) {
            editTextNumeMelodie.setError("Numele melodiei nu poate fi gol!");
            editTextNumeMelodie.requestFocus();
        } else if (numeMelodie.length() > 100) {
            editTextNumeMelodie.setError("Numele melodiei este prea lung!");
            editTextNumeMelodie.requestFocus();
        }
        // daca datele sunt corecte se trece la incarcarea datelor in Firebase
        else {
            progressDialog = new ProgressDialog(getContext()); // initializare Progress Dialog
            progressDialog.show(); // afiseaza progress dialog-ul
            progressDialog.setContentView(R.layout.layout_progress_dialog); // seteaza continutul progress dialog-ului cu layout_progress_dialog
            TextView textViewProgressDialog = progressDialog.findViewById(R.id.progress_dialog_textViewMesaj);
            textViewProgressDialog.setText("Se încarcă");
            progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // seteaza fundalul progress dialog-ului ca transparent

            StorageReference storageReferenceMelodie = firebaseStorage.getReference("melodii")
                    .child(System.currentTimeMillis() + "." + getExtensieFisier(uriMelodie)); // returneaza locatia noului child melodie din Storage avand ca nume timpul curent in milisecunde

            storageReferenceMelodie.putFile(uriMelodie) // incarca asincron continutul URI-ului melodiei in noul child creat
                    .addOnSuccessListener(taskSnapshotMelodie -> { // daca melodia s-a incarcat cu succes

                        String cheieIncarcare = firebaseDatabase.getReference("melodii").push().getKey(); // creeaza o referinta in baza de date spre noul child si obtine cheia acestuia
                        firebaseDatabase.getReference("melodii").child(cheieIncarcare).setValue(new Melodie());

                        storageReferenceMelodie.getDownloadUrl().addOnSuccessListener(uri -> { // obtine locatia la care s-a incarcat melodia
                            Melodie melodie = new Melodie(
                                    firebaseAuth.getCurrentUser().getUid(), // cheie artist
                                    MainActivity.utilizator.getNume(), // nume artist
                                    numeMelodie, // nume melodie
                                    null,
                                    uri.toString(), // locatia fisierului audio
                                    genMelodie, // gen melodie
                                    descriereMelodie, // descriere melodie
                                    0,
                                    0/*,
                                    ServerValue.TIMESTAMP // timpul curent*/
                            );

                            firebaseDatabase.getReference("melodii").child(cheieIncarcare).setValue(melodie); // adauga datele melodiei in baza de date
                        });

                        // incarca imaginea aferenta melodiei in cazul in care a fost selectata
                        if (uriImagine != null) {
                            StorageReference storageReferenceImagine = firebaseStorage.getReference("imagini")
                                    .child(System.currentTimeMillis() + "." + getExtensieFisier(uriImagine)); // returneaza locatia noului child imagine din Storage avand ca nume timpul curent in milisecunde

                            storageReferenceImagine.putFile(uriImagine) // incarca asincron continutul URI-ului imaginii in noul child creat
                                    .addOnSuccessListener(taskSnapshotImagine ->
                                            storageReferenceImagine.getDownloadUrl().addOnSuccessListener(uri -> { // obtine locatia la care s-a incarcat imaginea
                                                firebaseDatabase.getReference("melodii").child(cheieIncarcare).child("imagineMelodie").setValue(uri.toString()); // adauga imaginea melodiei in baza de date
                                            }))
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Nu s-a putut încărca imaginea: " + e.getMessage(), Toast.LENGTH_LONG).show())
                                    .addOnProgressListener(taskSnapshot -> progressDialog.setProgress((int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()))); // actualizeaza progressDialog-ul
                        }

                        progressDialog.findViewById(R.id.progress_dialog_progressBar).setVisibility(View.GONE); // ascunde spinner-ul
                        progressDialog.findViewById(R.id.progress_dialog_lottie).setVisibility(View.VISIBLE); // afiseaza animatia lottie
                        textViewProgressDialog.setText("Melodia s-a încărcat cu succes!");

                        new Handler().postDelayed(() -> {
                            progressDialog.setProgress(0);
                            progressDialog.dismiss(); // inlatura progress dialog-ul
                        }, 3000); // adauga un delay inainte de a reseta progresul

                        reseteazaElementeFragment(); // reseteaza layout-ul fragmentului
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Nu s-a putut încarca melodia: " + e.getMessage(), Toast.LENGTH_LONG).show())
                    .addOnProgressListener(taskSnapshot -> progressDialog.setProgress((int) (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()))); // actualizeaza progressDialog-ul
        }
    }

    /**
     * Resereaza elementele vizuale ale fragmentului {@link AdaugaFragment}. Seteaza cu null
     * {@link #uriImagine} si {@link #uriMelodie}.
     */
    private void reseteazaElementeFragment() {
        uriImagine = null;
        uriMelodie = null;

        imageViewImagineMelodie.setImageResource(R.drawable.ic_imagine);
        imageViewFisierAudio.setVisibility(View.VISIBLE);
        textViewNumeFisierAudio.setVisibility(View.GONE);

        editTextNumeMelodie.setText("");
        editTextDescriereMelodie.setText("");
    }

    /**
     * Initializeaza toate vederile din cadrul acestui fragment.
     */
    private void initializareVederi() {
        imageViewImagineMelodie = requireView().findViewById(R.id.adauga_imageViewImagineMelodie);
        imageViewFisierAudio = requireView().findViewById(R.id.adauga_imageViewFisierAudio);
        textViewNumeFisierAudio = requireView().findViewById(R.id.adauga_textViewNumeFisierAudio);
        editTextNumeMelodie = requireView().findViewById(R.id.adauga_editTextNumeMelodie);
        editTextDescriereMelodie = requireView().findViewById(R.id.adauga_editTextDescriereMelodie);
        spinnerGenMelodie = requireView().findViewById(R.id.adauga_spinnerGenMelodie);
        buttonIncarcaMelodia = requireView().findViewById(R.id.adauga_buttonIncarcaMelodia);
    }

}