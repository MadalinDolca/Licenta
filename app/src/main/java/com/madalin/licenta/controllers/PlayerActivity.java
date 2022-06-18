package com.madalin.licenta.controllers;

import static com.madalin.licenta.controllers.MainActivity.repeatBoolean;
import static com.madalin.licenta.controllers.MainActivity.shuffleBoolean;
import static com.madalin.licenta.global.EdgeToEdge.Directie;
import static com.madalin.licenta.global.EdgeToEdge.Spatiere;
import static com.madalin.licenta.global.EdgeToEdge.edgeToEdge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.CardMelodieAdapter;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.interfaces.PlayerInterface;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.services.MuzicaService;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity
        implements
        PlayerInterface,
        ServiceConnection {

    private LinearLayout linearLayoutContainer;
    private TextView textViewInCursDeRedare;
    private TextView textViewNumeMelodie;
    private TextView textViewNumeArtist;
    private TextView textViewDurataRedata;
    private TextView textViewDurataMelodie;
    private SeekBar seekBar;
    private ImageView imageViewImagineMelodie;
    private ImageView imageViewButonInapoi;
    private ImageView imageViewButonSkipPrevious;
    private ImageView imageViewButonSkipNext;
    private ImageView imageViewButonShuffle;
    private ImageView imageViewButonRepeat;
    private FloatingActionButton floatingActionButtonPlayPause;
    private Button buttonSolicitaPermisiunea;
    private Button buttonAfiseazaDateMelodie;

    private MuzicaService muzicaService; // instanta serviciu muzical
    private List<Melodie> listaMelodiiPlayer; // lista melodiilor
    private int pozitieMelodie = -1; // pozitia implicita a melodiei curente
    private Thread playThread, previousThread, nextThread;
    private Palette.Swatch swatchCuloareDominanta; // memoreaza culoarea dominanta a imaginii melodiei

    static boolean isActivitatePlayerActiva = false; // specifica daca activitatea este activa sau nu pentru utilizarea sigura a serviciului muzical & notificarii

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        edgeToEdge(this, findViewById(R.id.player_linearLayoutContainer), Spatiere.PADDING, Directie.SUS);
        edgeToEdge(this, findViewById(R.id.player_buttonAfiseazaDateMelodie), Spatiere.MARGIN, Directie.JOS);

        initializareVederi();
        textViewNumeMelodie.setText("Se încarcă...");
        textViewNumeArtist.setText("Se încarcă...");

        // listener buton inapoi
        imageViewButonInapoi.setOnClickListener(v -> {
            super.onBackPressed();
        });

        //new PregatirePlayerAsyncTask().execute(); // pregatire mediaPlayer si afisare date melodie
        lansareMuzicaService();

        // listener pentru schimbarea starii seekBar-ului
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (muzicaService != null && fromUser) {
                    muzicaService.seekTo(progress * 1000); // cauta in melodie la pozitia de timp specificata de seekBar
                    textViewDurataRedata.setText(formatareMilisecunde(muzicaService.getPozitieCurenta())); // actualizeaza TextView-ul duratei redate
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // thread pentru actualizarea seekBar-ului si a duratei redate
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (muzicaService != null) { // daca exista serviciu
                    if (muzicaService.isPlaying()) { // daca MediaPlayer-ul serviciului ruleaza
                        seekBar.setProgress(muzicaService.getPozitieCurenta() / 1000);
                        textViewDurataRedata.setText(formatareMilisecunde(muzicaService.getPozitieCurenta()));
                    }
                }
                new Handler().postDelayed(this, 1000); // intarzieri de o secunda intre actualizari
            }
        });

        // listener buton shuffle
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

        // listener buton repeat
        imageViewButonRepeat.setOnClickListener(v -> {
            if (repeatBoolean) {
                repeatBoolean = false;
                imageViewButonRepeat.setImageResource(R.drawable.ic_repeat_oprit);
            } else {
                repeatBoolean = true;
                imageViewButonRepeat.setImageResource(R.drawable.ic_repeat_pornit);
            }
        });

        // listener afisare dialog cu datele melodiei
        buttonAfiseazaDateMelodie.setOnClickListener(v -> afisareDialogDetaliiMelodie());
    }

    /**
     * La crearea activitatii sau la repornirea activitatii dupa apelarea metodei {@link #onStop()}
     * seteaza {@link #isActivitatePlayerActiva} ca <code>true</code> pentru a marca faptul ca activitatea
     * este vizibila utilizatorului.
     */
    @Override
    protected void onStart() {
        super.onStart();
        isActivitatePlayerActiva = true; // marcheaza activitatea ca activa
    }

    /**
     * Cand activitatea este vizibila utilizatorului, iar acesta poate interactiona cu ea sau se
     * intoarce la aceasta, {@link PlayerActivity} se leaga la serviciul {@link MuzicaService} si
     * apeleaza metodele {@link #butonPlayPauseThread()}, {@link #butonPreviousThread()} si
     * {@link #butonNextThread()}.
     */
    @Override
    protected void onResume() {
        Log.e("", "PlayerActivity#onResume()");

        // leaga PlayerActivity la serviciul muzical
        Intent intent = new Intent(this, MuzicaService.class);
        bindService(intent, this, BIND_AUTO_CREATE);

        // lanseaza Thread-urile pentru butoane
        butonPlayPauseThread();
        butonPreviousThread();
        butonNextThread();

        super.onResume();
    }

    /**
     * Cand activitatea nu este vizibila utilizatorului, {@link PlayerActivity} se dezleaga de la
     * serviciul muzical {@link MuzicaService}.
     */
    @Override
    protected void onPause() {
        Log.e("", "PlayerActivity#onPause()");
        super.onPause();

        unbindService(this); // unbind de la serviciul muzical
    }

    /**
     * Cand activitatea nu mai este vizibila utilizatorului, {@link #isActivitatePlayerActiva} se seteaza
     * ca <code>false</code>.
     */
    @Override
    protected void onStop() {
        super.onStop();
        isActivitatePlayerActiva = false; // marcheaza activitatea ca inactiva
    }

    /**
     * La conectarea la serviciul {@link MuzicaService} se obtine o instanta a acestuia si se
     * initializeaza {@link #muzicaService} pentru a face posibila manipularea serviciului muzical.
     * Permite instantei {@link MuzicaService#playerInterface} din MuzicaService sa
     * foloseasca implementarea din aceasta clasa. Seteaza valoarea maxima a {@link #seekBar}-ului
     * si apeleaza {@link #afisareDateMelodie()} pentru afisarea datelor melodiei curente.
     * Apeleaza {@link MuzicaService#onTerminareMelodie()} pentru reactionarea la terminarea melodiei.
     * Apeleaza {@link MuzicaService#afisareNotificare(int)} pentru afisarea notificarii.
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MuzicaService.MuzicaServiceBinder playerMuzicaServiceBinder = (MuzicaService.MuzicaServiceBinder) service;
        muzicaService = playerMuzicaServiceBinder.getServiciu(); // obtine instanta serviciul MuzicaService

        muzicaService.setCallbackPlayerInterface(this); // permite instantei PlayerInterface din MuzicaService sa foloseasca implementarea din PlayerActivity

        Log.e("", "PlayerActivity#onServiceConnected(ComponentName, IBinder) " + muzicaService);

        seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // mutat de la pregatireMediaPlayer()
        afisareDateMelodie(); // mutat de la pregatireMediaPlayer()

        muzicaService.onTerminareMelodie(); // mutat de la onCreate()
        muzicaService.afisareNotificare(R.drawable.ic_pause); // afisare notificare
    }

    /**
     * La deconectarea de la {@link MuzicaService} se dezinstantiaza {@link #muzicaService}, facand
     * imposibila manipularea serviciului muzical.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.e("", "PlayerActivity#onServiceDisconnected()");
        muzicaService = null;
    }

    // thread-uri butoane
    private void butonPlayPauseThread() {
        playThread = new Thread(() -> floatingActionButtonPlayPause.setOnClickListener(v -> butonPlayPauseClicked()));
        playThread.start();
    }

    private void butonPreviousThread() {
        previousThread = new Thread(() -> imageViewButonSkipPrevious.setOnClickListener(v -> butonPreviousClicked()));
        previousThread.start();
    }

    private void butonNextThread() {
        nextThread = new Thread(() -> imageViewButonSkipNext.setOnClickListener(v -> butonNextClicked()));
        nextThread.start();
    }

    /**
     * Porneste si opreste {@link #muzicaService}-ul si schimba imaginea butonului
     * {@link #floatingActionButtonPlayPause}. Actualizeaza valoarea maxima a seekBar-ului.
     * Apeleaza {@link MuzicaService#afisareNotificare(int)} pentru afisarea notificarii.
     */
    public void butonPlayPauseClicked() {
        // pauza player daca melodia este in curs de redare
        if (muzicaService.isPlaying()) {
            muzicaService.afisareNotificare(R.drawable.ic_play); // afisare notificare
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_play);
            muzicaService.pauza();
            seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // setare valoare maxima seekBar in functie de durata melodiei din mediaPlayer
        }
        // pornire player daca melodia NU este in curs de redare
        else {
            muzicaService.afisareNotificare(R.drawable.ic_pause); // afisare notificare
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
            muzicaService.start();
            seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // actualizare progres seekBar
        }
    }

    /**
     * Trece la melodia precedenta in functie de starea {@link #muzicaService}-ului. Afiseaza datele
     * noii melodii prin {@link #afisareDateMelodie()}. Actualizeaza valoarea maxima a seekBar-ului.
     * Apeleaza listener-ul {@link MuzicaService#onTerminareMelodie()}.
     * Apeleaza {@link MuzicaService#afisareNotificare(int)} pentru afisarea notificarii.
     */
    public void butonPreviousClicked() {
        // porneste player cu noua melodie daca melodia precedenta este in curs de redare
        if (muzicaService.isPlaying()) {
            muzicaService.stop(); // opreste player-ul
            muzicaService.eliberare(); // elibereaza resursele

            // daca modul "shuffle" este pornit si modul "repeat" este oprit
            if (shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = getRandom(listaMelodiiPlayer.size() - 1); // trece la o pozitie aleatoare
            }
            // daca modul "shuffle" este oprit si modul "repeat" este oprit
            else if (!shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = ((pozitieMelodie - 1) < 0 ? listaMelodiiPlayer.size() - 1 : pozitieMelodie - 1); // obtine pozitia melodiei anterioare din lista
            }

            muzicaService.creeazaMediaPlayer(pozitieMelodie); // creeaza player pentru melodia de la pozitia data
            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // actualizare progres seekBar
            muzicaService.onTerminareMelodie(); // apelare listener pentru schimbarea melodiei la finalizare
            muzicaService.afisareNotificare(R.drawable.ic_pause); // afisare notificare
            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_pause); // schimba imaginea butonului PlayPause in "pause"
            muzicaService.start(); // porneste player-ul
        }
        // daca MediaPlayer-ul nu este in curs de redare
        else {
            muzicaService.stop(); // opreste player-ul
            muzicaService.eliberare(); // elibereaza resursele

            // daca modul "shuffle" este pornit si modul "repeat" este oprit
            if (shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = getRandom(listaMelodiiPlayer.size() - 1); // trece la o pozitie aleatoare
            }
            // daca modul "shuffle" este oprit si modul "repeat" este oprit
            else if (!shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = ((pozitieMelodie - 1) < 0 ? listaMelodiiPlayer.size() - 1 : pozitieMelodie - 1); // obtine pozitia melodiei anterioare din lista
            }

            muzicaService.creeazaMediaPlayer(pozitieMelodie);
            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // actualizare progres seekBar
            muzicaService.onTerminareMelodie(); // apelare listener pentru schimbarea melodiei la finalizare
            muzicaService.afisareNotificare(R.drawable.ic_play); // afisare notificare
            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_play); // schimba imaginea butonului PlayPause in "play"
        }
    }

    /**
     * Trece la melodia urmatoare in functie de starea {@link #muzicaService}-ului. Afiseaza datele
     * noii melodii prin {@link #afisareDateMelodie()}. Actualizeaza valoarea maxima a seekBar-ului.
     * Apeleaza listener-ul {@link MuzicaService#onTerminareMelodie()}.
     * Apeleaza {@link MuzicaService#afisareNotificare(int)} pentru afisarea notificarii.
     */
    public void butonNextClicked() {
        // porneste player cu noua melodie daca melodia precedenta este in curs de redare
        if (muzicaService.isPlaying()) {
            muzicaService.stop(); // opreste player-ul
            muzicaService.eliberare(); // elibereaza resursele

            // daca modul "shuffle" este pornit si modul "repeat" este oprit
            if (shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = getRandom(listaMelodiiPlayer.size() - 1); // trece la o pozitie aleatoare
            }
            // daca modul "shuffle" este oprit si modul "repeat" este oprit
            else if (!shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = ((pozitieMelodie + 1) % listaMelodiiPlayer.size()); // obtine pozitia urmatoarei melodii din lista
            }

            // ALTFEL pozitia melodiei ramane neschimbata
            muzicaService.creeazaMediaPlayer(pozitieMelodie);
            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // actualizare progres seekBar
            muzicaService.onTerminareMelodie(); // apelare listener pentru schimbarea melodiei la finalizare
            muzicaService.afisareNotificare(R.drawable.ic_pause); // afisare notificare
            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_pause); // schimba imaginea butonului PlayPause in "pause"
            muzicaService.start(); // porneste player-ul
        }
        // daca MediaPlayer-ul nu este in curs de redare
        else {
            muzicaService.stop(); // opreste player-ul
            muzicaService.eliberare(); // elibereaza resursele

            // daca modul "shuffle" este pornit si modul "repeat" este oprit
            if (shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = getRandom(listaMelodiiPlayer.size() - 1);
            }
            // daca modul "shuffle" este oprit si modul "repeat" este oprit
            else if (!shuffleBoolean && !repeatBoolean) {
                pozitieMelodie = ((pozitieMelodie + 1) % listaMelodiiPlayer.size()); // obtine pozitia urmatoarei melodii din lista
            }

            muzicaService.creeazaMediaPlayer(pozitieMelodie);
            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // actualizare progres seekBar
            muzicaService.onTerminareMelodie(); // apelare listener pentru schimbarea melodiei la finalizare
            muzicaService.afisareNotificare(R.drawable.ic_play); // afisare notificare
            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_play); // schimba imaginea butonului PlayPause in "play"
        }
    }

    /*@SuppressLint("StaticFieldLeak")
    public class PregatirePlayerAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //pregatireMediaPlayer(); // pregatire mediaPlayer

            pozitieMelodie = getIntent().getIntExtra(NumeExtra.POZITIE_MELODIE, -1); // obtine pozitia melodiei selectate din intent AcasaFragment
            listaMelodiiPlayer = (List<Melodie>) getIntent().getSerializableExtra(NumeExtra.LISTA_MELODII);

            // daca lista cu melodii nu este goala, se va obtine URI-ul melodiei din aceasta
            if (listaMelodiiPlayer != null) {
                floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
            }

            // pregatire si lansare serviciu muzical
            Intent intent = new Intent(PlayerActivity.this, MuzicaService.class);
            intent.putExtra(NumeExtra.LISTA_MELODII, (Serializable) listaMelodiiPlayer);
            intent.putExtra(NumeExtra.POZITIE_MELODIE_SERVICE, pozitieMelodie); // extra cu pozitia melodiei curente
            startService(intent); // lansare serviciu

            seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // seteaza limita maxima a seekBar-ului dupa ce s-a conectat la serviciu

            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);

            afisareDateMelodie();
            seekBar.setMax(muzicaService.getDurataMelodie() / 1000);//////////////

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
                    if (muzicaService != null && fromUser) {
                        muzicaService.seekTo(progress * 1000); // cauta in melodie la pozitia de timp specificata de seekBar
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
//            mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
//                @Override
//                public void onBufferingUpdate(MediaPlayer mp, int percent) {
//                    seekBar.setSecondaryProgress(percent (mediaPlayer.getDuration() / 1000));
//                }
//            });

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
        }
    }*/

    /**
     * Obtine din {@link Intent} lista cu melodii si pozitia melodiei de redat.
     * Lanseaza serviciul {@link MuzicaService} prin intermediul unui {@link Intent} furnizand
     * drept extra lista cu melodii si pozitia melodiei de redat obtinute anterior.
     */
    private void lansareMuzicaService() {
        listaMelodiiPlayer = new ArrayList<>();
        listaMelodiiPlayer = (List<Melodie>) getIntent().getSerializableExtra(NumeExtra.LISTA_MELODII); // obtine lista cu melodii din intent
        pozitieMelodie = getIntent().getIntExtra(NumeExtra.POZITIE_MELODIE, -1); // obtine pozitia melodiei selectate din intent

        // daca lista cu melodii nu este goala, seteaza resursa Pause
        if (listaMelodiiPlayer != null) {
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
        }

        // pregatire si lansare serviciu muzical
        Intent intent = new Intent(this, MuzicaService.class);
        intent.putExtra(NumeExtra.LISTA_MELODII, (Serializable) listaMelodiiPlayer); // extra cu lista melodiilor
        intent.putExtra(NumeExtra.POZITIE_MELODIE_SERVICE, pozitieMelodie); // extra cu pozitia melodiei curente
        startService(intent); // lanseaza serviciul muzical
    }

    /**
     * Afiseaza datele unei melodii in {@link PlayerActivity}. Daca {@link #isActivitatePlayerActiva} este
     * <code>true</code>, inseamna ca activitatea este vizibila utilizatorului si se genereaza o
     * paleta de culori in functie de culorile imaginii melodiei pe care o aplica in interfata.
     * Aplica o animatie de tranzitie pe imaginea melodiei prin
     * {@link #animatieImagine(Context, ImageView, Bitmap)}. Seteaza culorile elementelor cu
     * {@link #setareCuloriElemente(Palette.Swatch)}. Daca {@link #isActivitatePlayerActiva} este
     * <code>false</code>, inseamna ca aplicatia este inchisa si doar serviciul ruleaza.
     */
    public void afisareDateMelodie() {
        // setare date de tip text
        textViewNumeMelodie.setText(listaMelodiiPlayer.get(pozitieMelodie).getNumeMelodie());
        textViewNumeMelodie.setFocusable(true); // pentru marquee

        textViewNumeArtist.setText(listaMelodiiPlayer.get(pozitieMelodie).getNumeArtist());
        textViewNumeArtist.setFocusable(true); // pentru marquee

        textViewDurataMelodie.setText(formatareMilisecunde(muzicaService.getDurataMelodie()));

        // verifica daca activitatea este activa (afisata utilizatorului) sau inchisa
        if (isActivitatePlayerActiva) {
            // setare imagine melodie si paleta de culori
            if (listaMelodiiPlayer.get(pozitieMelodie).getImagineMelodie() == null) { // daca melodia nu are link spre imagine
                imageViewImagineMelodie.setImageResource(R.drawable.ic_nota_muzicala); // se adauga o resursa inlocuitoare
            }
            // daca melodia are link spre imagine, aceasta se obtine ca bitmap, se adauga in card si se aplica paleta de culori
            else {
                Glide.with(this).asBitmap().load(listaMelodiiPlayer.get(pozitieMelodie).getImagineMelodie()) // obtine imaginea ca bitmap
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resursaBitmapImagineMelodie, @Nullable Transition<? super Bitmap> transition) {
                                animatieImagine(PlayerActivity.this, imageViewImagineMelodie, resursaBitmapImagineMelodie);
                                //imageViewImagineMelodie.setImageBitmap(resursaBitmapImagineMelodie); // setare bitmap obtinut ca imagine pentru melodie

                                // generare paleta de culori
                                Palette.from(resursaBitmapImagineMelodie).generate(palette -> {
                                    swatchCuloareDominanta = palette.getDominantSwatch(); // obtine culoarea dominanta a imaginii melodiei

                                    if (swatchCuloareDominanta != null) {
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
    }

    /**
     * Afiseaza datele unei melodii in {@link BottomSheetDialog}. Initializeaza vederile acestuia si
     * seteaza culorile elementelor. Obtine din {@link FirebaseDatabase} datele melodiei curente
     * din lista in functie de cheia acesteia.
     */
    private void afisareDialogDetaliiMelodie() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogMeniuTheme);
        View bottomSheetDialogView = getLayoutInflater().inflate(R.layout.layout_bottom_sheet_date_melodie, null, false); // inflate layout bottom sheet dialog din XML
        bottomSheetDialog.setContentView(bottomSheetDialogView); // setare continut bottom sheet

        // initializare vederi bottom sheet dialog
        LinearLayout linearLayoutContainer = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_linearLayoutContainer);
        TextView textViewNumarRedari = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_textViewNumarRedari);
        TextView textViewGenMuzical = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_textViewGenMuzical);
        TextView textViewDataIncarcarii = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_textViewDataIncarcarii);
        TextView textViewTitluDescriereMelodie = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_textViewTitluDescriereMelodie);
        TextView textViewDescriereMelodie = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_textViewDescriereMelodie);
        ImageView imageViewPill = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_imageViewPill);
        ImageView imageViewNumarRedari = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_imageViewNumarRedari);
        ImageView imageViewGenMuzical = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_imageViewGenMuzical);
        ImageView imageViewDataIncarcarii = bottomSheetDialog.findViewById(R.id.bottom_sheet_date_melodie_imageViewDataIncarcarii);

        if (swatchCuloareDominanta != null) {
            ColorStateList colorStateListElement = ColorStateList.valueOf(swatchCuloareDominanta.getBodyTextColor()); // culoare elemente
            ColorStateList colorStateListFundal = ColorStateList.valueOf(swatchCuloareDominanta.getRgb()); // culoare fundal

            // schimbare culori elemente
            linearLayoutContainer.setBackgroundTintList(colorStateListFundal);
            textViewNumarRedari.setTextColor(colorStateListElement);
            textViewGenMuzical.setTextColor(colorStateListElement);
            textViewDataIncarcarii.setTextColor(colorStateListElement);
            textViewTitluDescriereMelodie.setTextColor(colorStateListElement);
            textViewDescriereMelodie.setTextColor(colorStateListElement);
            imageViewPill.setImageTintList(colorStateListElement);
            imageViewNumarRedari.setImageTintList(colorStateListElement);
            imageViewGenMuzical.setImageTintList(colorStateListElement);
            imageViewDataIncarcarii.setImageTintList(colorStateListElement);
        }

        FirebaseDatabase.getInstance().getReference("melodii/" + listaMelodiiPlayer.get(pozitieMelodie).getCheie())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Melodie melodieCurenta = snapshot.getValue(Melodie.class);

                            // adaugare date in elemente
                            textViewNumarRedari.setText(melodieCurenta.getNumarRedari() + " redări");
                            textViewGenMuzical.setText(melodieCurenta.getGenMelodie());
                            textViewDataIncarcarii.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date(melodieCurenta.getDataCreariiLong())));
                            textViewDescriereMelodie.setText(melodieCurenta.getDescriere());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(PlayerActivity.this, "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        bottomSheetDialog.show(); // afiseaza bottom sheet dialog meniu
    }

    /**
     * Genereaza un numar intreg aleator pe baza dimensiunii listei melodiilor specificata.
     *
     * @param dimensiuneLista dimensiunea listei melodiilor
     * @return pozitia aleatoare
     */
    private int getRandom(int dimensiuneLista) {
        Random random = new Random();
        return random.nextInt(dimensiuneLista + 1);
    }

    /**
     * Seteaza culorile elementelor din player folosind culoarea dominanta a imaginii melodiei.
     *
     * @param swatchCuloareDominanta culoarea dominanta a imaginii melodiei
     */
    private void setareCuloriElemente(Palette.Swatch swatchCuloareDominanta) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{swatchCuloareDominanta.getRgb(), 0xFF000000}); // culoare fundal cu gradient
        ColorStateList colorStateListElement = ColorStateList.valueOf(swatchCuloareDominanta.getBodyTextColor()); // culoare elemente
        ColorStateList colorStateListDominant = ColorStateList.valueOf(swatchCuloareDominanta.getRgb()); // culoare dominanta

        linearLayoutContainer.setBackground(gradientDrawable); // setare culoare fundal

        imageViewButonInapoi.setImageTintList(colorStateListElement);
        textViewInCursDeRedare.setTextColor(colorStateListElement);

        seekBar.setThumbTintList(colorStateListElement);
        seekBar.setProgressTintList(colorStateListElement);
        //seekBar.setSecondaryProgressTintList(swatchColorState);
        seekBar.setProgressBackgroundTintList(colorStateListElement);

        textViewNumeMelodie.setTextColor(colorStateListElement);
        textViewNumeArtist.setTextColor(colorStateListElement);
        textViewDurataRedata.setTextColor(colorStateListElement);
        textViewDurataMelodie.setTextColor(colorStateListElement);

        floatingActionButtonPlayPause.setBackgroundTintList(colorStateListElement);
        imageViewButonShuffle.setImageTintList(colorStateListElement);
        imageViewButonSkipPrevious.setImageTintList(colorStateListElement);
        imageViewButonSkipNext.setImageTintList(colorStateListElement);
        imageViewButonRepeat.setImageTintList(colorStateListElement);

        buttonSolicitaPermisiunea.setBackgroundTintList(colorStateListElement);
        buttonAfiseazaDateMelodie.setBackgroundTintList(colorStateListDominant);
    }

    /**
     * Formateaza milisecundele pozitiei curente din {@link #seekBar}.
     *
     * @param pozitieCurentaMilisecunde pozitia din {@link #seekBar}
     * @return timpul intr-un format HH:MM:SS
     */
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

    /**
     * Aplica o animatie de tranzitie asupra imaginilor melodiilor.
     *
     * @param context   contextul curent
     * @param imageView vederea imaginii melodiei
     * @param bitmap    imaginea melodiei
     */
    public static void animatieImagine(Context context, ImageView imageView, Bitmap bitmap) {
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

    /**
     * Initializeaza toate vederile din cadrul acestei activitati.
     */
    private void initializareVederi() {
        linearLayoutContainer = findViewById(R.id.player_linearLayoutContainer);

        textViewInCursDeRedare = findViewById(R.id.player_textViewInCursDeRedare);

        textViewNumeMelodie = findViewById(R.id.player_textViewNumeMelodie);
        textViewNumeArtist = findViewById(R.id.player_textViewNumeArtist);
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
        buttonAfiseazaDateMelodie = findViewById(R.id.player_buttonAfiseazaDateMelodie);
    }
}