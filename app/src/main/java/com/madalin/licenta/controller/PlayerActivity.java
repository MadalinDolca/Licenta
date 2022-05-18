package com.madalin.licenta.controller;

import static com.madalin.licenta.EdgeToEdge.Directie;
import static com.madalin.licenta.EdgeToEdge.Spatiere;
import static com.madalin.licenta.EdgeToEdge.edgeToEdge;
import static com.madalin.licenta.controller.fragment.AcasaFragment.listaMelodii;
import static com.madalin.licenta.controller.MainActivity.shuffleBoolean;
import static com.madalin.licenta.controller.MainActivity.repeatBoolean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madalin.licenta.R;
import com.madalin.licenta.model.Melodie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    LinearLayout linearLayoutContainer;

    TextView textViewInCursDeRedare;
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

        new PregatirePlayerAsyncTask().execute(); // pregatire mediaPlayer si afisare date melodie
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

    /**
     * Porneste si opreste mediaPlayer-ul si schimba imaginea butonului PlayPause.
     * Apeleaza {@link PlayerActivity#actualizareSeekBar()} pentru actualizarea seekBar-ului.
     */
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

        actualizareSeekBar(); // actualizare progres seekBar
    }

    /**
     * Trece la melodia precedenta in functie de starea mediaPlayer-ului. Afiseaza datele noii
     * melodii prin {@link PlayerActivity#afisareDateMelodie()}. Actualizeaza seekBar-ul prin
     * {@link PlayerActivity#actualizareSeekBar()}. Apeleaza listener-ul {@link #onCompletion(MediaPlayer)}
     */
    private void previousBtnClicked() {
        // porneste player cu noua melodie daca melodia precedenta este in curs de redare
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop(); // opreste player-ul
            mediaPlayer.release(); // elibereaza resursele

            // daca modul "shuffle" este pornit si modul "repeat" este oprit
            if (shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = getRandom(melodieArrayList.size() - 1); // trece la o pozitie aleatoare
            }
            // daca modul "shuffle" este oprit si modul "repeat" este oprit
            else if (!shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = ((pozitieMelodie - 1) < 0 ? melodieArrayList.size() - 1 : pozitieMelodie - 1); // obtine pozitia melodiei anterioare din lista
            }

            uri = Uri.parse(melodieArrayList.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI

            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            actualizareSeekBar(); // actualizare progres seekBar

            mediaPlayer.setOnCompletionListener(this); // apelare listener pentru schimbarea melodiei la finalizare

            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_pause); // schimba imaginea butonului PlayPause in "pause"
            mediaPlayer.start(); // porneste player-ul
        } else {
            mediaPlayer.stop(); // opreste player-ul
            mediaPlayer.release(); // elibereaza resursele

            // daca modul "shuffle" este pornit si modul "repeat" este oprit
            if (shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = getRandom(melodieArrayList.size() - 1); // trece la o pozitie aleatoare
            }
            // daca modul "shuffle" este oprit si modul "repeat" este oprit
            else if (!shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = ((pozitieMelodie - 1) < 0 ? melodieArrayList.size() - 1 : pozitieMelodie - 1); // obtine pozitia melodiei anterioare din lista
            }

            uri = Uri.parse(melodieArrayList.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI

            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            actualizareSeekBar(); // actualizare progres seekBar

            mediaPlayer.setOnCompletionListener(this); // apelare listener pentru schimbarea melodiei la finalizare

            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_play); // schimba imaginea butonului PlayPause in "play"
        }
    }

    /**
     * Trece la melodia urmatoare in functie de starea mediaPlayer-ului. Afiseaza datele noii
     * melodii prin {@link PlayerActivity#afisareDateMelodie()}. Actualizeaza seekBar-ul prin
     * {@link PlayerActivity#actualizareSeekBar()}. Apeleaza listener-ul {@link #onCompletion(MediaPlayer)}
     */
    private void nextBtnClicked() {
        // porneste player cu noua melodie daca melodia precedenta este in curs de redare
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop(); // opreste player-ul
            mediaPlayer.release(); // elibereaza resursele

            // daca modul "shuffle" este pornit si modul "repeat" este oprit
            if (shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = getRandom(melodieArrayList.size() - 1); // trece la o pozitie aleatoare
            }
            // daca modul "shuffle" este oprit si modul "repeat" este oprit
            else if (!shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = ((pozitieMelodie + 1) % melodieArrayList.size()); // obtine pozitia urmatoarei melodii din lista
            }

            // altfel pozitia melodiei ramane neschimbata
            uri = Uri.parse(melodieArrayList.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI

            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            actualizareSeekBar(); // actualizare progres seekBar

            mediaPlayer.setOnCompletionListener(this); // apelare listener pentru schimbarea melodiei la finalizare

            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_pause); // // schimba imaginea butonului PlayPause in "pause"
            mediaPlayer.start(); // porneste player-ul
        } else {
            mediaPlayer.stop(); // opreste player-ul
            mediaPlayer.release(); // elibereaza resursele

            // daca modul "shuffle" este pornit si modul "repeat" este oprit
            if (shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = getRandom(melodieArrayList.size() - 1);
            }
            // daca modul "shuffle" este oprit si modul "repeat" este oprit
            else if (!shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = ((pozitieMelodie + 1) % melodieArrayList.size()); // obtine pozitia urmatoarei melodii din lista
            }

            uri = Uri.parse(melodieArrayList.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI

            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            actualizareSeekBar(); // actualizare progres seekBar

            mediaPlayer.setOnCompletionListener(this); // apelare listener pentru schimbarea melodiei la finalizare

            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_play); // schimba imaginea butonului PlayPause in "play"
        }
    }

    /**
     * Seteaza valoarea maxima a {@link #seekBar}-ului in functie de lungimea melodiei din
     * {@link #mediaPlayer}. Lanseaza un thread pe UiThread pentru actualizarea continua a
     * progresului {@link #seekBar}-ului si a {@link #textViewDurataRedata} in
     * functie de pozitia curenta a {@link #mediaPlayer}-ului.
     */
    public void actualizareSeekBar() {
        seekBar.setMax(mediaPlayer.getDuration() / 1000); // setare valoare maxima seekBar in functie de durata melodiei din mediaPlayer

        // actualizeaza progresul seekBar-ului in functie de progresul mediaPlayer-ului
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer != null) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
                        textViewDurataRedata.setText(formatareMilisecunde(mediaPlayer.getCurrentPosition())); /////
                    }

                    handlerProgresMelodie.postDelayed(this, 1000);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Listener pentru schimbarea melodiei dupa finalizarea acesteia. Apeleaza
     * {@link PlayerActivity#nextBtnClicked()} pentru a trece la urmatoarea melodie. In cazul in
     * care nu exista un mediaPlayer, creeaza unul.
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked(); // trece la urmatoarea melodie

        // creeaza un nou mediaPlayer in cazul in care nu exista deja unul
        if (mediaPlayer != null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class PregatirePlayerAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            pregatireMediaPlayer(); // pregatire mediaPlayer

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            afisareDateMelodie();
            actualizareSeekBar();//////////////

            // runnable pentru setarea progresului de redare al melodiei in seekBar si textViewDurataRedata
//            PlayerActivity.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        if (mediaPlayer != null) {
//                            seekBar.setProgress(mediaPlayer.getCurrentPosition() / 1000);
//                            textViewDurataRedata.setText(formatareMilisecunde(mediaPlayer.getCurrentPosition()));
//                        }
//
//                        handlerProgresMelodie.postDelayed(this, 1000);
//                    } catch (Exception e) {
//                        // UH OH :(
//                        //mediaPlayer.stop();
//                        //mediaPlayer.release();
//                        //Toast.makeText(PlayerActivity.this, "Eroare: " + e, Toast.LENGTH_LONG).show();
//                    }
//                }
//            });

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

            imageViewButonShuffle.setOnClickListener(v -> {
                // daca modul de amestecare este pornit
                if (shuffleBoolean) {
                    shuffleBoolean = false;
                    imageViewButonShuffle.setImageResource(R.drawable.ic_shuffle_oprit);
                }
                // daca modul de amestecare este oprit
                else {
                    shuffleBoolean = true;
                    imageViewButonShuffle.setImageResource(R.drawable.ic_shuffle_pornit);
                }
            });

            imageViewButonRepeat.setOnClickListener(v -> {
                if (repeatBoolean) {
                    repeatBoolean = false;
                    imageViewButonRepeat.setImageResource(R.drawable.ic_repeat_oprit);
                } else {
                    repeatBoolean = true;
                    imageViewButonRepeat.setImageResource(R.drawable.ic_repeat_pornit);
                }
            });

//            // listener repornire melodie dupa finalizare
//            mediaPlayer.setOnCompletionListener(mp -> {
//                nextBtnClicked();
//
//                if (mediaPlayer != null) {
//                    mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
//                    mediaPlayer.start();
//                }
//
////                seekBar.setProgress(0);
////                floatingActionButtonPlayPause.setImageResource(R.drawable.ic_play);
////                textViewDurataRedata.setText("0:00");
////                textViewDurataMelodie.setText("0:00");
////                mediaPlayer.reset();
////                pregatireMediaPlayer();
//            });
        }
    }

    /**
     * Pregateste mediaPlayer-ul pentru redarea unei melodii din lista de melodii folosind URL-ul acesteia.
     */
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

    /**
     * Afiseaza datele unei melodii in player. Genereaza o paleta de culori in functie de culorile
     * imaginii melodiei pe care o aplica in interfata. Aplica o animatie de tranzitie pe imaginea
     * melodiei prin {@link PlayerActivity#animatieImagine(Context, ImageView, Bitmap)}. Seteaza
     * culorile elementelor cu {@link PlayerActivity#setareCuloriElemente(Palette.Swatch)}.
     */
    void afisareDateMelodie() {
        // setare date de tip text
        textViewNumeMelodie.setText(melodieArrayList.get(pozitieMelodie).getNumeMelodie());
        textViewNumeArtist.setText(melodieArrayList.get(pozitieMelodie).getNumeArtist());
        textViewDurataMelodie.setText(formatareMilisecunde(mediaPlayer.getDuration()));

        // setare imagine melodie si paleta de culori
        if (melodieArrayList.get(pozitieMelodie).getImagineMelodie() == null) { // daca melodia nu are link spre imagine
            imageViewImagineMelodie.setImageResource(R.drawable.ic_nota_muzicala); // se adauga o resursa inlocuitoare
        }
        // daca melodia are link spre imagine, aceasta se obtine ca bitmap, se adauga in card si se aplica paleta de culori
        else {
            Glide.with(this).asBitmap().load(melodieArrayList.get(pozitieMelodie).getImagineMelodie()) // obtine imaginea ca bitmap
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resursaBitmapImagineMelodie, @Nullable Transition<? super Bitmap> transition) {
                            animatieImagine(PlayerActivity.this, imageViewImagineMelodie, resursaBitmapImagineMelodie);
                            //imageViewImagineMelodie.setImageBitmap(resursaBitmapImagineMelodie); // setare bitmap obtinut ca imagine pentru melodie

                            // generare paleta de culori
                            Palette.from(resursaBitmapImagineMelodie).generate(palette -> {
                                Palette.Swatch swatchCuloareDominanta = palette.getDominantSwatch(); // obtine culoarea dominanta a imaginii melodiei

                                if (swatchCuloareDominanta != null) {
                                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{swatchCuloareDominanta.getRgb(), 0xFF000000}); // culoare fundal cu gradient
                                    linearLayoutContainer.setBackground(gradientDrawable); // setare culoare fundal
                                    setareCuloriElemente(swatchCuloareDominanta); // aplica culoarea swatch-ului pe elementele player-ului
                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }

    // metoda pentru setarea culorii elementelor din player folosind culoarea dominanta a imaginii melodiei
    private void setareCuloriElemente(Palette.Swatch swatchCuloareDominanta) {
        int swatchColor = swatchCuloareDominanta.getTitleTextColor(); // culoare elemente
        ColorStateList swatchColorState = ColorStateList.valueOf(swatchColor); // culoare elemente

        imageViewButonInapoi.setImageTintList(ColorStateList.valueOf(swatchColor));
        textViewInCursDeRedare.setTextColor(swatchColor);

        seekBar.setThumbTintList(swatchColorState);
        seekBar.setProgressTintList(swatchColorState);
        //seekBar.setSecondaryProgressTintList(swatchColorState);
        seekBar.setProgressBackgroundTintList(swatchColorState);

        textViewNumeMelodie.setTextColor(swatchColor);
        textViewNumeArtist.setTextColor(swatchColor);
        textViewDurataRedata.setTextColor(swatchColor);
        textViewDurataMelodie.setTextColor(swatchColor);

        floatingActionButtonPlayPause.setBackgroundTintList(swatchColorState);
        imageViewButonShuffle.setImageTintList(swatchColorState);
        imageViewButonSkipPrevious.setImageTintList(swatchColorState);
        imageViewButonSkipNext.setImageTintList(swatchColorState);
        imageViewButonRepeat.setImageTintList(swatchColorState);

        buttonSolicitaPermisiunea.setBackgroundTintList(swatchColorState);
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

    // metoda pentru aplicarea unei animatii la tranzitia dintre imaginile melodiilor
    public void animatieImagine(Context context, ImageView imageView, Bitmap bitmap) {
        Animation animationOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animationIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);

        animationOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);

                animationIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                imageView.startAnimation(animationIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        imageView.startAnimation(animationOut);
    }

    // metoda pentru initializarea vederilor
    private void initializareVederi() {
        linearLayoutContainer = findViewById(R.id.player_linearLayoutContainer);

        textViewInCursDeRedare = findViewById(R.id.player_textViewInCursDeRedare);

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