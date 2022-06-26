package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.barteksc.pdfviewer.PDFView;
import com.madalin.licenta.R;
import com.madalin.licenta.global.EdgeToEdge.Directie;
import com.madalin.licenta.global.EdgeToEdge.Spatiere;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Licenta;
import com.madalin.licenta.models.Melodie;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class VizualizareLicentaActivity extends AppCompatActivity {

    private LinearLayout toolbar;
    private RelativeLayout relativeLayoutDateMelodie;
    private ImageView imageViewImagineMelodie;
    private TextView textViewNumeMelodie;
    private TextView textViewNumeArtist;
    private TextView textViewDataLicentierii;
    private TextView textViewNumeBeneficiar;
    private PDFView pdfView;
    private LinearLayout linearLayoutButoane;
    private Button buttonDescarcaMelodia;
    private Button buttonDescarcaLicenta;

    private Licenta licentaSelectata; // memoreaza datele licentei selectate pentru afisare

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vizualizare_licenta);

        initializareVederi();
        edgeToEdge(this, toolbar, Spatiere.PADDING, Directie.SUS);
        edgeToEdge(this, linearLayoutButoane, Spatiere.PADDING, Directie.JOS);

        licentaSelectata = (Licenta) getIntent().getSerializableExtra(NumeExtra.VIZUALIZARE_LICENTA); // obtine datele licentei din extra

        // seteaza datele in vederi
        Glide.with(this)
                .load(licentaSelectata.getMelodie().getImagineMelodie())
                .apply(RequestOptions.centerCropTransform())
                .placeholder(R.drawable.logo_music)
                .error(R.drawable.ic_eroare)
                .into(imageViewImagineMelodie);
        textViewNumeMelodie.setText(licentaSelectata.getMelodie().getNumeMelodie());
        textViewNumeArtist.setText(licentaSelectata.getMelodie().getNumeArtist());
        textViewDataLicentierii.setText("Data licențierii: " + new SimpleDateFormat("dd.MM.yyyy").format(new Date(licentaSelectata.getDataCreariiLong())));
        textViewNumeBeneficiar.setText(licentaSelectata.getBeneficiar().getNume());

        new ObtinePDFdinURL().execute(licentaSelectata.getUrlLicenta());

        // listener lansare PlayerActivity la apasare
        relativeLayoutDateMelodie.setOnClickListener(v -> {
            if (licentaSelectata.getMelodie() != null) {
                List<Melodie> listaMelodie = new ArrayList<>();
                listaMelodie.add(licentaSelectata.getMelodie()); // adauga melodia curenta intr-o lista

                Intent intent = new Intent(VizualizareLicentaActivity.this, PlayerActivity.class);
                intent.putExtra(NumeExtra.LISTA_MELODII, (Serializable) listaMelodie); // adauga lista cu melodia curenta in extra
                intent.putExtra(NumeExtra.POZITIE_MELODIE, 0); // adauga pozitia melodiei selectate
                startActivity(intent); // lanseaza PlayerActivity
            }
        });

        // listener lansare ProfilActivity la apasarea numelui beneficiarului
        textViewNumeBeneficiar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfilActivity.class);
            intent.putExtra(NumeExtra.CHEIE_UTILIZATOR, licentaSelectata.getCheieBeneficiar());
            startActivity(intent);
        });

        // listener descarcare melodiei la apasarea butonului
        buttonDescarcaMelodia.setOnClickListener(v -> descarcaFisier(licentaSelectata, new Melodie()));

        // listener descarcare licenta la apasarea butonului
        buttonDescarcaLicenta.setOnClickListener(v -> descarcaFisier(licentaSelectata, new Licenta()));
    }

    /**
     * Obtine fisierul PDF din adresa URL furnizata in lista de parametrii si il adauga in
     * {@link #pdfView}.
     */
    class ObtinePDFdinURL extends AsyncTask<String, Void, InputStream> {
        @Override
        protected InputStream doInBackground(String... strings) {
            InputStream inputStream = null; // pentru memorarea PDF-ului

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection(); // creeaza conexiunea

                // in caz de succes se obtine input stream-ul din url si se stocheaza in variabila
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            return inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream) {
            pdfView.fromStream(inputStream).load(); // adauga PDF-ul in vedere
        }
    }

    /**
     * Identifica tipul obiectului transmis si obtine datele fisierului pe baza acestuia. Descarca
     * fisierul pe baza datelor si il adauga intr-un director.
     *
     * @param licenta obiectul din care se vor extrage datele
     * @param obiect  obiectul folosit pentru identificarea tipului fisierului
     * @param <Tip>   tipul obiectului
     */
    private <Tip> void descarcaFisier(Licenta licenta, Tip obiect) {
        String url = null;
        String prefix = "Fisier";
        String numeDirector;
        String numeFisier = "Nume fisier";

        // verifica tipul de obiect transmis ca parametru
        if (obiect instanceof Licenta) {
            //licenta = (Licenta) obiect;
            url = licenta.getUrlLicenta();
            numeFisier = URLUtil.guessFileName(url, null, null);
            prefix = "Licenta ";
        } else if (obiect.getClass() == Melodie.class) {
            String numeFisierTemp;
            String extensie;

            //melodie = (Melodie) obiect;
            url = licenta.getMelodie().getUrlMelodie(); // obtine adresa URL a melodiei
            numeFisierTemp = URLUtil.guessFileName(url, null, null); // obtine numele melodiei din adresa URL
            extensie = numeFisierTemp.substring(numeFisierTemp.lastIndexOf(".")); // obtine extensia fisierului din numele melodiei
            numeFisier = licenta.getMelodie().getNumeMelodie()
                    + " - "
                    + licenta.getMelodie().getNumeArtist()
                    + extensie;
            prefix = "";
        }

        // seteaza numele datelor
        numeDirector = "Pachet "
                + new SimpleDateFormat("dd-MM-yyyy").format(new Date(licenta.getDataCreariiLong()))
                + " - " + licenta.getMelodie().getNumeMelodie();

        // cererea de descarcare
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url)); // cerere pentru descarcarea URL-ului
        request.setTitle(prefix + numeFisier); // titlul descarcarii din notificare
        request.setDescription("Se descarcă fișierul ..."); // descrierea descarcarii din notificare
        request.addRequestHeader("cookie", CookieManager.getInstance().getCookie(url)); // adauga in header-ul cererii adresa fisierului drep cookie
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // afiseaza notificarea de descarcare
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, File.separator + "Licente" + File.separator + numeDirector + File.separator + prefix + numeFisier); // destinatia de descarcare a fisierului
        //request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

        // descarcarea fisierului
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE); // obtine serviciul de descarcare
        downloadManager.enqueue(request); // descarca fisierul din cererea DownloadManager-ului

        Toast.makeText(this, "Descărcarea a început!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Initializeaza vederile acestei activitati.
     */
    private void initializareVederi() {
        toolbar = findViewById(R.id.vizualizare_licenta_toolbar);
        relativeLayoutDateMelodie = findViewById(R.id.vizualizare_licenta_relativeLayoutDateMelodie);
        imageViewImagineMelodie = findViewById(R.id.vizualizare_licenta_imageViewImagineMelodie);
        textViewNumeMelodie = findViewById(R.id.vizualizare_licenta_textViewNumeMelodie);
        textViewNumeArtist = findViewById(R.id.vizualizare_licenta_textViewNumeArtist);
        textViewDataLicentierii = findViewById(R.id.vizualizare_licenta_textViewDataLicentierii);
        textViewNumeBeneficiar = findViewById(R.id.vizualizare_licenta_textViewNumeBeneficiar);
        pdfView = findViewById(R.id.vizualizare_licenta_pdfView);
        buttonDescarcaMelodia = findViewById(R.id.vizualizare_licenta_buttonDescarcaMelodia);
        buttonDescarcaLicenta = findViewById(R.id.vizualizare_licenta_buttonDescarcaLicenta);
        linearLayoutButoane = findViewById(R.id.vizualizare_licenta_linearLayoutButoane);

        toolbar.findViewById(R.id.toolbar_imageViewButonInapoi).setOnClickListener(v -> super.onBackPressed());
        TextView textView = toolbar.findViewById(R.id.toolbar_textViewTitlu);
        textView.setText("Vizualizează Licența");
    }
}