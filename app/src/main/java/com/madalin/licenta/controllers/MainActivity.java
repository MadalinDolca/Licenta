package com.madalin.licenta.controllers;

import static com.madalin.licenta.EdgeToEdge.*;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.fragments.AcasaFragment;
import com.madalin.licenta.controllers.fragments.AdaugaFragment;
import com.madalin.licenta.controllers.fragments.BibliotecaFragment;
import com.madalin.licenta.controllers.fragments.CautaFragment;
import com.madalin.licenta.services.MuzicaService;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    BottomNavigationView bottomNavigationView;

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

    public static String URL_MELODIE_FRAGMENT = null;
    public static String IMAGINE_MELODIE_FRAGMENT = null;
    public static String NUME_ARTIST_FRAGMENT = null;
    public static String NUME_MELODIE_FRAGMENT = null;

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

        bottomNavigationView = findViewById(R.id.main_bottomNavigationView); // obtinere vedere bara de navigare
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.acasa:
                    afisareFragment(fragmentAcasa, "acasa", 0); // afisare fragment acasa
                    return true; // marcare buton bara navigare ca checked

                case R.id.adauga:
                    afisareFragment(fragmentAdauga, "adauga", 1);
                    return true;

                case R.id.cauta:
                    afisareFragment(fragmentCauta, "cauta", 2);
                    return true;

                case R.id.biblioteca:
                    afisareFragment(fragmentBiblioteca, "biblioteca", 3);
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

        // verifica daca utilizatorul curent Firebase este autentificat
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // daca nu este autentificat, se lanseaza activitatea de autentificare
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, AutentificareActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        obtineDateMelodieStocata(this);
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

    public static void obtineDateMelodieStocata(Activity activitate) {
        // obtine datele melodiei stocate in baza de date locala a sistemului
        SharedPreferences sharedPreferences = activitate.getSharedPreferences(MuzicaService.ULTIMA_MELODIE_REDATA, MODE_PRIVATE);
        URL_MELODIE_FRAGMENT = sharedPreferences.getString(MuzicaService.URL_MELODIE, null); // obtine URL-ul melodiei la cheia "URL_MELODIE"
        IMAGINE_MELODIE_FRAGMENT = sharedPreferences.getString(MuzicaService.IMAGINE_MELODIE, null); // obtine imaginea melodiei la cheia "IMAGINE_MELODIE"
        NUME_MELODIE_FRAGMENT = sharedPreferences.getString(MuzicaService.NUME_MELODIE, null); // obtine numele melodiei la cheia "NUME_MELODIE"
        NUME_ARTIST_FRAGMENT = sharedPreferences.getString(MuzicaService.NUME_ARTIST, null); // obtine numele artistului la cheia "NUME_ARTIST"

        if (URL_MELODIE_FRAGMENT != null) {
            arataMiniplayer = true;
        } else {
            arataMiniplayer = false;

            URL_MELODIE_FRAGMENT = null;
            IMAGINE_MELODIE_FRAGMENT = null;
            NUME_MELODIE_FRAGMENT = null;
            NUME_ARTIST_FRAGMENT = null;
        }
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

    // metoda pentru afisarea dialogului meniu
    private void afisareDialogMeniu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogMeniuTheme);
        View bottomSheetDialogView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_meniu, null, false); // inflate layout din xml
        bottomSheetDialog.setContentView(bottomSheetDialogView); // setare continut bottom sheet

        // adaugare email in bottom sheet dialog
        TextView textViewNume = bottomSheetDialog.findViewById(R.id.bottom_sheet_meniu_textViewNume);
        textViewNume.setText(firebaseAuth.getCurrentUser().getEmail().toString());

        // accesare activitate profil
        bottomSheetDialog.findViewById(R.id.bottom_sheet_meniu_linearLayoutProfil).setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(MainActivity.this, ProfilActivity.class));
        });

        // deconectare de la firebase
        bottomSheetDialog.findViewById(R.id.bottom_sheet_meniu_buttonDeconectare).setOnClickListener(view -> {
            firebaseAuth.signOut();
            bottomSheetDialog.dismiss();
            startActivity(new Intent(MainActivity.this, AutentificareActivity.class));
        });

        bottomSheetDialog.show(); // afisare bottom sheet
    }
}