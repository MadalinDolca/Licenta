package com.madalin.licenta;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.madalin.licenta.models.Licenta;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.models.Solicitare;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class GeneratorLicenta {
    private View viewLicenta;
    private LinearLayout linearLayout;
    private TextView textViewNumarContract;
    private TextView textViewIntroducereArtist;
    private TextView textViewIntroducereBeneficiar;
    private TextView textViewConditii;
    private TextView textViewDataIncepereValabilitate;
    private TextView textViewSemnaturaArtist;
    private TextView textViewSemnaturaBeneficiar;

    private Context context;
    private Bitmap bitmap;
    private Licenta licenta; // memoreaza datele licentei nou create
    private String cheieLicenta;
    private DatabaseReference databaseReference; // referinta spre baza de date
    private StorageReference storageReference; // referinta spre locatia de stocare

    public GeneratorLicenta(Context context) {
        this.context = context;

        databaseReference = FirebaseDatabase.getInstance().getReference("licente"); // obtine referinta spre baza de date "licente"
        storageReference = FirebaseStorage.getInstance().getReference("licente"); // obtine referinta spre locatia de stocare "licente"
    }

    public void generareLicenta(Solicitare solicitare) {
        cheieLicenta = databaseReference.push().getKey(); // creeaza o referinta in baza de date spre noul child si obtine cheia acestuia

        // creeaza noul obiect licenta cu cheile din solicitare
        licenta = new Licenta(
                solicitare.getCheieArtist(),
                solicitare.getCheieSolicitant(),
                solicitare.getCheieMelodie(),
                null
        );

        // seteaza datele licentei nou create
        databaseReference.child(cheieLicenta).setValue(licenta)
                // daca datele licentei s-au adaugat cu succes in baza de date, se creeaza PDF-ul si se incarca in Storage
                .addOnSuccessListener(unused -> {
                    viewLicenta = LayoutInflater.from(context).inflate(R.layout.layout_licenta, null); // obtine layout-ul XML al licentei
                    viewLicenta.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)); // masoara dimensiunile layout-ului
                    initializareVederi();

                    // seteaza datele vederii licentei
                    String introducereArtist = "Subsemnatul " + solicitare.getTemp_numeArtist()
                            + ", îi ofer permisiunea lui " + solicitare.getTemp_numeSolicitant()
                            + " de a-mi utiliza melodia \"" + solicitare.getTemp_numeMelodie()
                            + "\" în scopul, locația și cu motivul stabilit în solicitare doar dacă acesta respectă condițiile impuse în cadrul acestui contract."
                            + " Declar faptul că nu voi sancționa beneficiarul atât timp cât aceste criterii prezentate în contract sunt respectate.";

                    String introducereBeneficiar = "Subsemnatul " + solicitare.getTemp_numeSolicitant()
                            + " declar faptul că voi respecta detaliile precizate în solicitare și condițiile impuse în cadrul acestui contract în utilizarea melodiei."
                            + " Am luat la cunoștință faptul că voi fi sancționat în cazul în care criteriile stabilite în acest contract vor fi încălcate.";

                    String conditii = "- Beneficiarul trebuie să-i ofere credit artistului în cadrul fiecărei utilizări a melodiei acestuia. Creditul constă în precizarea numelui artistului, a melodiei și a link-urilor spre rețelele sociale ale artistului;"
                            + "\n- Beneficiarul are dretul de a folosi melodia doar în scopul \"" + solicitare.getScopulUtilizarii() + "\";"
                            + "\n- Beneficiarul are dretul de a folosi melodia doar în mediul \"" + solicitare.getMediulUtilizarii() + "\";"
                            + "\n- Beneficiarul are dretul de a folosi melodia doar la locația \"" + solicitare.getLoculUtilizarii() + "\";"
                            + "\n- Beneficiarul are dretul de a folosi melodia doar pentru motivul \"" + solicitare.getMotivulUtilizarii() + "\";";

                    textViewIntroducereArtist.setSingleLine(false);
                    textViewIntroducereBeneficiar.setSingleLine(false);
                    textViewConditii.setSingleLine(false);

                    textViewNumarContract.setText(cheieLicenta);
                    textViewIntroducereArtist.setText(introducereArtist);
                    textViewIntroducereBeneficiar.setText(introducereBeneficiar);
                    textViewConditii.setText(conditii);
                    textViewDataIncepereValabilitate.setText(new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime()));
                    textViewSemnaturaArtist.setText(solicitare.getCheieArtist());
                    textViewSemnaturaBeneficiar.setText(solicitare.getCheieSolicitant());

                    // conversie View la Bitmap
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

                    if (isExternalStorageAvailableForRW()) {
                        // scrie PDF-ul in fisier
                        File locatieFisier = new File(context.getExternalFilesDir("licente_stocate"), cheieLicenta + ".pdf"); // locatia de salvare a fisierului in memoria telefonului

                        try {
                            pdfDocument.writeTo(new FileOutputStream(locatieFisier)); // scrie documentul in memorie
                            pdfDocument.close(); // inchide documentul
                            //Toast.makeText(context, "PDF salvat!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Eroare salvare licență: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        storageReference.child(cheieLicenta + ".pdf").putFile(Uri.fromFile(new File(locatieFisier.toURI()))) // incarca asincron continutul URI-ului licentei in noul child creat
                                .addOnSuccessListener(taskSnapshot ->
                                        storageReference.child(cheieLicenta + ".pdf").getDownloadUrl()
                                                .addOnSuccessListener(uri -> { // obtine locatia la care s-a incarcat licenta
                                                    databaseReference.child(cheieLicenta).child("urlLicenta").setValue(uri.toString()); // adauga adresa licentei in baza de date
                                                }))
                                .addOnFailureListener(e -> Toast.makeText(context, "Eroare încărcare licență: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                });

    }

    private boolean isExternalStorageAvailableForRW() {
        String externalStorageState = Environment.getExternalStorageState();

        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }

        return false;
    }

    /**
     * Initializeaza vederile layout-ului licentei.
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