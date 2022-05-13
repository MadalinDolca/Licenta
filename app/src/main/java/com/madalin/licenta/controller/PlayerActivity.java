package com.madalin.licenta.controller;

import static com.madalin.licenta.EdgeToEdge.Directie;
import static com.madalin.licenta.EdgeToEdge.Spatiere;
import static com.madalin.licenta.EdgeToEdge.edgeToEdge;
import static com.madalin.licenta.controller.fragment.AcasaFragment.listaMelodii;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madalin.licenta.R;
import com.madalin.licenta.model.Melodie;

import java.util.ArrayList;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    LinearLayout linearLayoutContainer;

    TextView textViewNumeMelodie;
    TextView textViewNumeArtist;
    TextView textViewDescriereMelodie;
    TextView textViewDurataRedata;
    TextView textViewDurataMelodie;

    SeekBar seekBar;

    ImageView imageViewImagineMelodie;
    ImageView imageViewButonInapoi;
    ImageView imageViewButonSkipPrevious;
    ImageView imageViewButonSkipNext;
    ImageView imageViewButonShuffle;
    ImageView imageViewButonRepeat;

    FloatingActionButton floatingActionButtonPlayPause;

    Button buttonSolicitaPermisiunea;

    int pozitieMelodie = -1;
    static List<Melodie> melodieArrayList = new ArrayList<>();
    Uri uri;
    static MediaPlayer mediaPlayer;
    private final Handler handlerProgresMelodie = new Handler();
    private Thread playThread, previousThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        edgeToEdge(this, findViewById(R.id.player_linearLayoutContainer), Spatiere.PADDING, Directie.SUS);
        edgeToEdge(this, findViewById(R.id.player_textViewDescriereMelodie), Spatiere.MARGIN, Directie.JOS);

        initializareVederi();
        textViewNumeMelodie.setText("Se încarcă...");
        textViewNumeArtist.setText("Se încarcă...");
        textViewDescriereMelodie.setText("Se încarcă descrierea...");

        // listener buton inapoi
        imageViewButonInapoi.setOnClickListener(v -> {
            super.onBackPressed();
        });

        new PlayerAsyncTask().execute(); // pregatire mediaPlayer si afisare date melodie
    }

    @Override
    protected void onResume() {
        playThreadButon();
        previousThreadButon();
        nextThreadButon();

        super.onResume();
    }

    private void playThreadButon() {
        playThread = new Thread(() -> floatingActionButtonPlayPause.setOnClickListener(v -> playPauseBtnClicked()));
        playThread.start();
    }

    private void previousThreadButon() {
        previousThread = new Thread(() -> imageViewButonSkipPrevious.setOnClickListener(v -> previousBtnClicked()));
        previousThread.start();
    }

    private void nextThreadButon() {
        nextThread = new Thread(() -> imageViewButonSkipNext.setOnClickListener(v -> nextBtnClicked()));
        nextThread.start();
    }

    private void playPauseBtnClicked() {
        // pauza player daca melodia este in curs de redare
        if (mediaPlayer.isPlaying()) {
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_play);
            mediaPlayer.pause();
        }
        // pornire player daca melodia NU este in curs de redare
        else {
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }

        seekBar.setMax(mediaPlayer.getDuration() / 1000);

        // actualizare progres seekBar
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                }

                handlerProgresMelodie.postDelayed(this, 1000);
            }
        });
    }

    private void previousBtnClicked() {
        // porneste player cu noua melodie daca melodia precedenta este in curs de redare
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop(); // opreste player-ul
            mediaPlayer.release(); // elibereaza resursele

            pozitieMelodie = ((pozitieMelodie - 1) < 0 ? listaMelodii.size() - 1 : pozitieMelodie - 1); // obtine pozitia melodiei anterioare din lista
            uri = Uri.parse(listaMelodii.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI

            afisareDateMelodie(); // actualizare UI cu datele noii melodii

            // actualizare progres seekBar
            actualizareSeekBar();

            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause); // schimba imaginea butonului PlayPause in "pause"
            mediaPlayer.start(); // porneste player-ul
        } else {
            mediaPlayer.stop(); // opreste player-ul
            mediaPlayer.release(); // elibereaza resursele

            pozitieMelodie = ((pozitieMelodie - 1) < 0 ? listaMelodii.size() - 1 : pozitieMelodie - 1); // obtine pozitia melodiei anterioare din lista
            uri = Uri.parse(listaMelodii.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI

            afisareDateMelodie(); // actualizare UI cu datele noii melodii

            // actualizare progres seekBar
            actualizareSeekBar();

            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_play); // schimba imaginea butonului PlayPause in "play"
        }
    }

    private void nextBtnClicked() {
        // porneste player cu noua melodie daca melodia precedenta este in curs de redare
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop(); // opreste player-ul
            mediaPlayer.release(); // elibereaza resursele

            pozitieMelodie = ((pozitieMelodie + 1) % listaMelodii.size()); // obtine pozitia urmatoarei melodii din lista
            uri = Uri.parse(listaMelodii.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI

            afisareDateMelodie(); // actualizare UI cu datele noii melodii

            // actualizare progres seekBar
            actualizareSeekBar();

            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause); // // schimba imaginea butonului PlayPause in "pause"
            mediaPlayer.start(); // porneste player-ul
        } else {
            mediaPlayer.stop(); // opreste player-ul
            mediaPlayer.release(); // elibereaza resursele

            pozitieMelodie = ((pozitieMelodie + 1) % listaMelodii.size()); // obtine pozitia urmatoarei melodii din lista
            uri = Uri.parse(listaMelodii.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI

            afisareDateMelodie(); // actualizare UI cu datele noii melodii

            // actualizare progres seekBar
            actualizareSeekBar();

            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_play); // schimba imaginea butonului PlayPause in "play"
        }
    }

    public void actualizareSeekBar() {
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                }

                handlerProgresMelodie.postDelayed(this, 1000);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public class PlayerAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            pregatireMediaPlayer(); // pregatire mediaPlayer

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            afisareDateMelodie();
            //actualizareSeekBar();//////////////

            // runnable pentru setarea progresului de redare al melodiei in seekBar si textViewDurataRedata
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mediaPlayer != null) {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                            textViewDurataRedata.setText(formatareMilisecunde(mediaPlayer.getCurrentPosition()));
                        }

                        handlerProgresMelodie.postDelayed(this, 1000);
                    } catch (Exception e) {
                        // UH OH :(
                        //mediaPlayer.stop();
                        //mediaPlayer.release();
                        //Toast.makeText(PlayerActivity.this, "Eroare: " + e, Toast.LENGTH_LONG).show();
                    }
                }
            });

            // listener pentru schimbarea starii seekBar-ului
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (mediaPlayer != null && fromUser) {
                        mediaPlayer.seekTo(progress * 1000); // cauta in melodie la pozitia de timp specificata de seekBar
                        //textViewDurataRedata.setText(formatareMilisecunde(mediaPlayer.getCurrentPosition())); /////////
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            // listener play / pause
//            floatingActionButtonPlayPause.setOnClickListener(v -> {
//                // pauza player daca melodia este in curs de redare
//                if (mediaPlayer.isPlaying()) {
//                    //handlerProgresMelodie.removeCallbacks(updaterProgresMelodie); ////////
//                    mediaPlayer.pause();
//                    floatingActionButtonPlayPause.setImageResource(R.drawable.ic_play);
//                }
//                // pornire player daca melodia NU este in curs de redare
//                else {
//                    mediaPlayer.start();
//                    floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
//                    //actualizareSeekBar(); //////////
//                }
//            });

            // listener buffer progres secundar seekBar
            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    seekBar.setSecondaryProgress(percent /*(mediaPlayer.getDuration() / 1000)*/);
                }
            });

            // listener repornire melodie dupa finalizare
            mediaPlayer.setOnCompletionListener(mp -> {
                seekBar.setProgress(0);
                floatingActionButtonPlayPause.setImageResource(R.drawable.ic_play);
                textViewDurataRedata.setText("0:00");
                textViewDurataMelodie.setText("0:00");
                mediaPlayer.reset();
                pregatireMediaPlayer();
            });
        }
    }

    // metoda pentru pregatirea mediaPlayer-ului pentru redarea unei melodii
    private void pregatireMediaPlayer() {
        pozitieMelodie = getIntent().getIntExtra("com.madalin.licenta.position", -1); // obtinere pozitie melodie selectata din intent
        melodieArrayList = listaMelodii; // obtinere lista melodii

        // daca lista cu melodii nu este goala, se va obtine URI-ul melodiei din aceasta
        if (melodieArrayList != null) {
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(melodieArrayList.get(pozitieMelodie).getUrl());
        }

        // daca mediaPlayer-ul ruleaza
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // opreste redarea
            mediaPlayer.release(); // elibereaza resursele curente
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player din URI
            mediaPlayer.start(); // porneste redarea cu noua melodie
        }
        // daca mediaPlayer-ul NU ruleaza
        else {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
        }

        seekBar.setMax(mediaPlayer.getDuration() / 1000);
    }

    ///////////
//    private Runnable updaterProgresMelodie = new Runnable() {
//        @Override
//        public void run() {
//            actualizareSeekBar();
//
//            try {
//                textViewDurataRedata.setText(formatareMilisecunde(mediaPlayer.getCurrentPosition()));
//            } catch (IllegalStateException e) {
//                e.printStackTrace();
//            }
//        }
//    };

    //////////////
//    private void actualizareSeekBar() {
//        try {
//            if (mediaPlayer.isPlaying()) {
//                seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
//                handlerProgresMelodie.postDelayed(updaterProgresMelodie, 1000);
//
//                // textViewDurataRedata.setText(formatareMilisecunde(mediaPlayer.getCurrentPosition()));
//            }
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
//    }

    // metoda pentru afisarea imaginii, numelui, artistului etc. melodiei
    void afisareDateMelodie() {
        textViewNumeMelodie.setText(melodieArrayList.get(pozitieMelodie).getNumeMelodie());
        textViewNumeArtist.setText(melodieArrayList.get(pozitieMelodie).getNumeArtist());
        textViewDurataMelodie.setText(formatareMilisecunde(mediaPlayer.getDuration()));

        if (melodieArrayList.get(pozitieMelodie).getImagineMelodie() == null) { // daca melodia nu are link spre imagine
            imageViewImagineMelodie.setImageResource(R.drawable.ic_nota_muzicala); // se adauga o resursa inlocuitoare
        } else { // daca melodia are link spre imagine, aceasta se obtine si se adauga in card-ul melodiei
            Glide.with(this).load(melodieArrayList.get(pozitieMelodie).getImagineMelodie())
                    .apply(RequestOptions.centerCropTransform())
                    .placeholder(R.drawable.logo_music)
                    .error(R.drawable.ic_eroare) // in caz ca nu s-a putut incarca imaginea, se adauga o resursa inlocuitoare
                    .into(imageViewImagineMelodie);

            //Bitmap bitmap = BitmapFactory.decodeByteArray(imageViewImagineMelodie.getDrawable(), imageViewImagineMelodie.getDrawable().)
            Bitmap bitmap = ((BitmapDrawable) imageViewImagineMelodie.getDrawable()).getBitmap();// BitmapFactory.decodeResource(this, imageViewImagineMelodie.getDrawable());

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();

                    if (swatch != null) {
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{swatch.getRgb(), swatch.getRgb() /*0x00000000*/});

                        linearLayoutContainer.setBackground(gradientDrawable);
                        textViewNumeMelodie.setTextColor(swatch.getTitleTextColor());
                        textViewNumeArtist.setTextColor(swatch.getBodyTextColor());
                    }
                }
            });
        }
    }

    // metoda pentru formatarea milisecundelor pozitiei curente a SeekBar-ului
    private String formatareMilisecunde(int pozitieCurentaMilisecunde) {
        String timerString = "";
        String secundeString;

        int ore = (int) (pozitieCurentaMilisecunde / (1000 * 60 * 60));
        int minute = (int) (pozitieCurentaMilisecunde % (1000 * 60 * 60)) / (1000 * 60);
        int secunde = (int) ((pozitieCurentaMilisecunde % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (ore > 0) {
            timerString = ore + ":";
        }

        if (secunde < 10) {
            secundeString = "0" + secunde;
        } else {
            secundeString = "" + secunde;
        }

        timerString = timerString + minute + ":" + secundeString;

        return timerString;
    }

    // metoda pentru initializarea vederilor
    private void initializareVederi() {
        linearLayoutContainer = findViewById(R.id.player_linearLayoutContainer);

        textViewNumeMelodie = findViewById(R.id.player_textViewNumeMelodie);
        textViewNumeArtist = findViewById(R.id.player_textViewNumeArtist);
        textViewDescriereMelodie = findViewById(R.id.player_textViewDescriereMelodie);
        textViewDurataRedata = findViewById(R.id.player_textViewDurataRedata);
        textViewDurataMelodie = findViewById(R.id.player_textViewDurataMelodie);

        seekBar = findViewById(R.id.player_seekBar);

        imageViewImagineMelodie = findViewById(R.id.player_imageViewImagineMelodie);
        imageViewButonInapoi = findViewById(R.id.player_imageViewButonInapoi);
        imageViewButonSkipPrevious = findViewById(R.id.player_imageViewButonSkipPrevious);
        imageViewButonSkipNext = findViewById(R.id.player_imageViewButonSkipNext);
        imageViewButonShuffle = findViewById(R.id.player_imageViewButonShuffle);
        imageViewButonRepeat = findViewById(R.id.player_imageViewButonRepeat);

        floatingActionButtonPlayPause = findViewById(R.id.player_floatingActionButtonPlayPause);

        buttonSolicitaPermisiunea = findViewById(R.id.player_buttonSolicitaPermisiunea);
    }

    private void metaData(Uri uri) {
        //MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        //mediaMetadataRetriever.setDataSource(uri.toString());
        textViewDurataMelodie.setText(formatareMilisecunde(mediaPlayer.getDuration()));

        if (melodieArrayList.get(pozitieMelodie).getImagineMelodie() == null) { // daca melodia nu are link spre imagine
            imageViewImagineMelodie.setImageResource(R.drawable.ic_nota_muzicala); // se adauga o resursa inlocuitoare
        } else { // daca melodia are link spre imagine, aceasta se obtine si se adauga in card-ul melodiei
            Glide.with(this).load(melodieArrayList.get(pozitieMelodie).getImagineMelodie())
                    .apply(RequestOptions.centerCropTransform())
                    .placeholder(R.drawable.logo_music)
                    .error(R.drawable.ic_eroare) // in caz ca nu s-a putut incarca imaginea, se adauga o resursa inlocuitoare
                    .into(imageViewImagineMelodie);
        }

        /*byte[] art = mediaMetadataRetriever.getEmbeddedPicture();

        if (art != null) {
            Glide.with(this).asBitmap().load(art).into(imageViewImagineMelodie);
        }*/
    }
}