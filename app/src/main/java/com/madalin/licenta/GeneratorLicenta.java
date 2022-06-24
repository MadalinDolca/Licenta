package com.madalin.licenta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.madalin.licenta.models.Solicitare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GeneratorLicenta {
    View viewLicenta;
    LinearLayout linearLayout;
    TextView textViewNumarContract;
    TextView textViewIntroducereArtist;
    TextView textViewIntroducereBeneficiar;
    TextView textViewConditii;
    TextView textViewDataIncepereValabilitate;
    TextView textViewSemnaturaArtist;
    TextView textViewSemnaturaBeneficiar;

    private Context context;
    private Solicitare solicitare;
    private Bitmap bitmap;

    public GeneratorLicenta(Context context) {
        this.context = context;
    }

    public void generareLicenta(Solicitare solicitare) {
        // conversie View la PDF
        viewLicenta = LayoutInflater.from(context).inflate(R.layout.layout_licenta, null); // obtine layout-ul XML al licentei
        viewLicenta.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)); // masoara dimensiunile layout-ului

        initializareVederi();

        linearLayout = viewLicenta.findViewById(R.id.licenta_linearLayout); // obtine vederea LinearLayout din XML
        bitmap = Bitmap.createBitmap(linearLayout.getMeasuredWidth(), linearLayout.getMeasuredHeight(), Bitmap.Config.ARGB_8888); // creeaza un Bitmap cu dimensiunile calculate
        linearLayout.layout(0, 0, linearLayout.getMeasuredWidth(), linearLayout.getMeasuredHeight()); // atribuie vederii si descendentilor ei o dimensiune si pozitie
        Canvas canvas = new Canvas(bitmap); // construieste un nou Canvas cu Bitmapul pentru desenare
        linearLayout.draw(canvas);

        // conversie Bitmap la PDF
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        page.getCanvas().drawBitmap(bitmap, 0, 0, null);
        pdfDocument.finishPage(page);

        // scrie PDF-ul in fisier
        File fisier = new File("/sdcard/fisierPDF.pdf");

        try {
            pdfDocument.writeTo(new FileOutputStream(fisier));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Eroare: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // inchide documentul
        pdfDocument.close();
        Toast.makeText(context, "PDF salvat!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Initializeaza vederile layout-ului.
     */
    private void initializareVederi() {
        textViewNumarContract = viewLicenta.findViewById(R.id.licenta_textViewNumarContract);
        textViewIntroducereArtist = viewLicenta.findViewById(R.id.licenta_textViewIntroducereArtist);
        textViewIntroducereBeneficiar = viewLicenta.findViewById(R.id.licenta_textViewIntroducereBeneficiar);
        textViewConditii = viewLicenta.findViewById(R.id.licenta_textViewConditii);
        textViewDataIncepereValabilitate = viewLicenta.findViewById(R.id.licenta_textViewDataIncepereValabilitate);
        textViewSemnaturaArtist = viewLicenta.findViewById(R.id.licenta_textViewSemnaturaArtist);
        textViewSemnaturaBeneficiar = viewLicenta.findViewById(R.id.licenta_textViewSemnaturaBeneficiar);
    }
}