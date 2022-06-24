package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.*;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.fragments.AcasaFragment;
import com.madalin.licenta.controllers.fragments.AdaugaFragment;
import com.madalin.licenta.controllers.fragments.BibliotecaFragment;
import com.madalin.licenta.controllers.fragments.CautaFragment;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Utilizator;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private FirebaseAuth firebaseAuth; // punctul de intrare al SDK-ului Firebase Authentication
    private FirebaseDatabase firebaseDatabase;
    public static Utilizator utilizator; // memoreaza datele utilizatorului curent

    // instantele fragmentelor din activitate
    final Fragment fragmentAcasa = new AcasaFragment();
    final Fragment fragmentAdauga = new AdaugaFragment();
    final Fragment fragmentCauta = new CautaFragment();
    final Fragment fragmentBiblioteca = new BibliotecaFragment();

    Fragment fragmentActiv; // fragmentul activ din activitate

    boolean doubleBackToExitPressedOnce = false; // tine evidenta apasarii optiunii "back"
    static boolean shuffleBoolean = false; // tine evidenta starii butonului "shuffle" din PlayerActivity
    static boolean repeatBoolean = false; // tine evidenta starii butonului "repeat" din PlayerActivity
    public static boolean arataMiniplayer = false; // tine evidenta starii de afisare a miniplayer-ului

    public static final int COD_SOLICITARE_SCRIERE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edgeToEdge(this, findViewById(R.id.main_frameLayoutFragment), Spatiere.PADDING, Directie.SUS);

        /*
        // Reload current fragment
Fragment frg = null;
frg = getSupportFragmentManager().findFragmentByTag("Your_Fragment_TAG");
final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
ft.detach(frg);
ft.attach(frg);
ft.commit();
        */

        verificarePermisiuni();

        firebaseAuth = FirebaseAuth.getInstance(); // initializare Firebase Auth
        firebaseDatabase = FirebaseDatabase.getInstance();

        getDateUtilizatorCurent();

        bottomNavigationView = findViewById(R.id.main_bottomNavigationView); // obtinere vedere bara de navigare
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.acasa:
                    afisareFragment(fragmentAcasa, "acasa", 0); // afisare fragment acasa
                    afisareMiniPlayerFragment(true);
                    return true; // marcare buton bara navigare ca checked

                case R.id.adauga:
                    afisareFragment(fragmentAdauga, "adauga", 1); // afiseaza fragmentul AdaugaFragment
                    afisareMiniPlayerFragment(false); // ascunde miniplayer-ul
                    return true;

                case R.id.cauta:
                    afisareFragment(fragmentCauta, "cauta", 2);
                    afisareMiniPlayerFragment(true);
                    return true;

                case R.id.biblioteca:
                    afisareFragment(fragmentBiblioteca, "biblioteca", 3);
                    afisareMiniPlayerFragment(true);
                    return true;

                case R.id.meniu:
                    afisareDialogMeniu();
                    return false;
            }
            return false;
        });

        afisareFragment(fragmentAcasa, "acasa", 0); // deschidere fragment acasa la pornirea aplicatiei
    }

    @Override
    protected void onStart() {
        super.onStart();

//        // verifica daca utilizatorul curent Firebase este autentificat
//        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
//
//        // daca nu este autentificat, se lanseaza activitatea de autentificare
//        if (currentUser == null) {
//            startActivity(new Intent(MainActivity.this, AutentificareActivity.class));
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //obtineDateMelodieStocata(this);
    }

    @Override
    public void onBackPressed() {
        if (fragmentActiv == fragmentAcasa) { // daca fragmentul activ este fragmentul acasa
            if (doubleBackToExitPressedOnce) { // daca s-a apasat BACK de doua ori
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true; // marcare BACK ca fiind apasat o data
            Toast.makeText(this, "Apasa din nou BACK ca sa iesi", Toast.LENGTH_SHORT).show();
        } else { // daca fragmentul activ NU este fragmentul acasa
            afisareFragment(fragmentAcasa, "acasa", 0); // se afiseaza fragmentul acasa
        }
    }

    /**
     * Apelata atunci cand o activitate lansata (din fragmente) este inchisa. Ofera un requestCode
     * identic cu cel oferit la lansare, un resultCode returnat și orice date suplimentare din aceasta.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    // metoda pentru verificarea si solicitarea permisiunilor
    private void verificarePermisiuni() {
        // daca nu s-a oferit permisiunea de scriere, se va solicita permisiunea de scriere
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, COD_SOLICITARE_SCRIERE);
        }
    }

    // metoda pentru gestionarea raspunsului utilizatorului la dialogul de solicitare a permisiunilor
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case COD_SOLICITARE_SCRIERE:
                // daca s-a oferit permisiunea de scriere
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permisiune acordată!", Toast.LENGTH_SHORT).show();
                    // ...
                } else { // altfel resolicita permisiunea
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, COD_SOLICITARE_SCRIERE);
                }
                return;

            default:
                break;
        }
    }

    // metoda pentru inlocuirea FrameLayout-ului cu fragmentul specificat
    private void afisareFragment(Fragment fragmentSelectat, String tag, int pozitie) {
        FragmentManager fragmentManager = getSupportFragmentManager(); // pentru interactiunile cu fragmentele asociate acestei activitati

        if (fragmentSelectat.isAdded()) { // daca fragmentul selectat este deja adaugat in activitate
            fragmentManager.beginTransaction()
                    .hide(fragmentActiv) // ascunde fragmentul activ
                    .show(fragmentSelectat) // si il afiseaza pe cel nou
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        } else { // daca fragmentul nu este adaugat in activitate
            if (fragmentActiv != null) { // daca exista fragment activ
                fragmentManager.beginTransaction()
                        .add(R.id.main_frameLayoutFragment, fragmentSelectat, tag) // adaugare fragment selectat in activitate
                        .hide(fragmentActiv) // ascundere fragment anterior
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            } else { // daca NU exista fragment activ
                fragmentManager.beginTransaction()
                        .add(R.id.main_frameLayoutFragment, fragmentSelectat, tag) // adaugare fragment selectat in activitate
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }
        }

        bottomNavigationView.getMenu().getItem(pozitie).setChecked(true); // setare buton din bara de navigare ca bifat
        fragmentActiv = fragmentSelectat; // setare fragment activ
    }

    private void afisareMiniPlayerFragment(boolean arata) {
        View viewMiniPlayer = findViewById(R.id.main_frameLayoutMiniPlayer);

        if (arata) {
            viewMiniPlayer.setVisibility(View.VISIBLE);
        } else {
            viewMiniPlayer.setVisibility(View.GONE);

//            viewMiniPlayer.animate()
//                    .translationY(viewMiniPlayer.getHeight())
//                    .alpha(0.0f)
//                    .setDuration(300)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            viewMiniPlayer.setVisibility(View.INVISIBLE);
//                        }
//                    });
        }

//        FragmentManager fragmentManager = getSupportFragmentManager();
//
//        if (arata) {
//            fragmentManager.beginTransaction()
//                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                    .show(fragmentMiniPlayer)
//                    .commit();
//            // fragmentMiniPlayer.getView().setVisibility(View.);
//        } else {
//            fragmentManager.beginTransaction()
//                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                    .hide(fragmentMiniPlayer)
//                    .commit();
//            fragmentMiniPlayer.getView().setVisibility(View.GONE);
//        }
    }

    // metoda pentru afisarea dialogului meniu
    private void afisareDialogMeniu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogMeniuTheme);
        View bottomSheetDialogView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_meniu, null, false); // inflate layout bottom sheet dialog meniu din XML
        bottomSheetDialog.setContentView(bottomSheetDialogView); // setare continut bottom sheet

        // initializare vederi bottom sheet dialog
        LinearLayout linearLayoutProfil = bottomSheetDialog.findViewById(R.id.bottom_sheet_meniu_linearLayoutProfil);
        LinearLayout linearLayoutSolicitari = bottomSheetDialog.findViewById(R.id.bottom_sheet_meniu_linearLayoutSolicitari);
        LinearLayout linearLayoutLicente = bottomSheetDialog.findViewById(R.id.bottom_sheet_meniu_linearLayoutLicente);
        TextView textViewNume = bottomSheetDialog.findViewById(R.id.bottom_sheet_meniu_textViewNume);
        Button buttonDeconectare = bottomSheetDialog.findViewById(R.id.bottom_sheet_meniu_buttonDeconectare);

        // daca utilizatorul este conectat
        if (firebaseAuth.getCurrentUser() != null) {
            textViewNume.setText(utilizator.getNume()); // adauga numele utilizatorului in bottom sheet dialog

            // accesare activitate profil
            linearLayoutProfil.setOnClickListener(view -> {
                bottomSheetDialog.dismiss(); // inchide dialogul
                Intent intentProfil = new Intent(MainActivity.this, ProfilActivity.class);
                intentProfil.putExtra(NumeExtra.CHEIE_UTILIZATOR, utilizator.getCheie()); // adauga cheia utilizatorului in extra
                startActivity(intentProfil); // lanseaza activitatea ProfilActivity
            });

            // accesare activitate solicitari
            linearLayoutSolicitari.setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                Intent intentSolicitari = new Intent(MainActivity.this, SolicitariActivity.class);
                startActivity(intentSolicitari);
            });

            // accesare activitate licente
            linearLayoutLicente.setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                Intent intentLicente = new Intent(MainActivity.this, LicenteActivity.class);
                startActivity(intentLicente);
            });

            // deconectare de la Firebase
            buttonDeconectare.setOnClickListener(view -> {
                firebaseAuth.signOut(); // deconecteaza utilizatorul si il sterge din cache-ul disk-ului
                bottomSheetDialog.dismiss(); // inchide dialogul
                startActivity(new Intent(MainActivity.this, AutentificareActivity.class)); // lanseaza activitatea AutentificareActivity
            });
        }
        // daca utilizatorul nu este conectat
        else {
            textViewNume.setText("Autentifică-te pentru a vedea mai multe");
            buttonDeconectare.setText("Autentifică-te");

            // layout profil
            linearLayoutProfil.setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(MainActivity.this, AutentificareActivity.class));
            });

            // layout solicitari
            linearLayoutSolicitari.setOnClickListener(view -> {
                afisareDialogAlerta();
            });

            // layout licente
            linearLayoutLicente.setOnClickListener(view -> {
                afisareDialogAlerta();
            });

            // buton deconectare
            buttonDeconectare.setOnClickListener(view -> {
                bottomSheetDialog.dismiss();
                startActivity(new Intent(MainActivity.this, AutentificareActivity.class));
            });
        }

        bottomSheetDialog.show(); // afiseaza bottom sheet dialog meniu
    }

    /**
     * Afiseaza un dialog de alerta pentru autentificare in scenariul in care se doreste utilizarea
     * unei functionalitati care necesita autentificarea.
     */
    private void afisareDialogAlerta() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_alert_dialog); // layout dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); // fundal transparent

        // initializare vederi
        TextView textViewMesaj = dialog.findViewById(R.id.alert_dialog_textViewMesaj);
        Button buttonAutentificare = dialog.findViewById(R.id.alert_dialog_buttonOptiune1);
        Button buttonRenunta = dialog.findViewById(R.id.alert_dialog_buttonOptiune2);

        // setare continut
        textViewMesaj.setText("Pentru a vedea solicitările va trebui să te autentifici!");
        buttonRenunta.setText("Renunță");
        buttonAutentificare.setText("Autentifică-te");

        // listener text renunta
        buttonRenunta.setOnClickListener(v -> {
            dialog.dismiss(); // inlatura dialogul
        });

        // listener buton autentificare
        buttonAutentificare.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(MainActivity.this, AutentificareActivity.class)); // lanseaza AutentificareActivity
        });

        dialog.show(); // afiseaza dialogul
    }

    /**
     * Daca utilizatorul este conectat, obtine datele utilizatorului curent din baza de date de
     * fiecare data cand acestea se modifica si le memoreaza in {@link #utilizator}.
     */
    private void getDateUtilizatorCurent() {
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseDatabase.getReference("utilizatori/" + firebaseAuth.getCurrentUser().getUid()) // locatia datelor utilizatorului curent
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) { // obtine datele utilizatorului curent la fiecare modificare a acestora
                            utilizator = snapshot.getValue(Utilizator.class); // memoreaza valorile
                            utilizator.setCheie(snapshot.getKey()); // memoreaza cheia
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "Eroare preluare date utilizator: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Log.e("", "Utilizatorul nu este autentificat!");
        }
    }
}