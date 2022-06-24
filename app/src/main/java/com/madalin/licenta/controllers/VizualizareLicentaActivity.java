package com.madalin.licenta.controllers;

import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.barteksc.pdfviewer.PDFView;
import com.madalin.licenta.R;
import com.madalin.licenta.global.EdgeToEdge;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.models.Licenta;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class VizualizareLicentaActivity extends AppCompatActivity {

    LinearLayout toolbar;
    RelativeLayout relativeLayoutDateMelodie;
    ImageView imageViewImagineMelodie;
    TextView textViewNumeMelodie;
    TextView textViewNumeArtist;
    TextView textViewDataLicentierii;
    TextView textViewNumeBeneficiar;
    PDFView pdfView;
    LinearLayout linearLayoutButoane;
    Button buttonDescarcaMelodia;
    Button buttonDescarcaLicenta;

    Licenta licenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vizualizare_licenta);

        initializareVederi();
        edgeToEdge(this, toolbar, EdgeToEdge.Spatiere.PADDING, EdgeToEdge.Directie.SUS);
        edgeToEdge(this, linearLayoutButoane, EdgeToEdge.Spatiere.MARGIN, EdgeToEdge.Directie.JOS);

        licenta = (Licenta) getIntent().getSerializableExtra(NumeExtra.VIZUALIZARE_LICENTA); // obtine datele licentei din extra

        // seteaza datele in vederi
        Glide.with(this)
                .load(licenta.getTemp_imagineMelodie())
                .apply(RequestOptions.centerCropTransform())
                .placeholder(R.drawable.logo_music)
                .error(R.drawable.ic_eroare)
                .into(imageViewImagineMelodie);
        textViewNumeMelodie.setText(licenta.getTemp_numeMelodie());
        textViewNumeArtist.setText(licenta.getTemp_numeArtist());
        textViewDataLicentierii.setText("Data licențierii: " + new SimpleDateFormat("dd.MM.yyyy").format(new Date(licenta.getDataCreariiLong())));
        textViewNumeBeneficiar.setText(licenta.getTemp_numeBeneficiar());

        new ObtinePDFdinURL().execute(licenta.getUrlLicenta());

        // listener lansare PlayerActivity la apasare
        relativeLayoutDateMelodie.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerActivity.class);
        });

        // listener lansare ProfilActivity la apasarea numelui beneficiarului
        textViewNumeBeneficiar.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfilActivity.class);
            intent.putExtra(NumeExtra.CHEIE_UTILIZATOR, licenta.getCheieBeneficiar());
            startActivity(intent);
        });

        // listener descarcare melodie la apasarea butonului
        buttonDescarcaMelodia.setOnClickListener(v -> {
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(licenta.getCheieMelodie());
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            long reference = manager.enqueue(request);
        });

        // listener descarcare licenta la apasarea butonului
        buttonDescarcaLicenta.setOnClickListener(v -> {
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(licenta.getUrlLicenta());
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            long reference = manager.enqueue(request);
        });
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