package com.madalin.licenta.controllers;

import static com.madalin.licenta.EdgeToEdge.Directie;
import static com.madalin.licenta.EdgeToEdge.Spatiere;
import static com.madalin.licenta.EdgeToEdge.edgeToEdge;
import static com.madalin.licenta.controllers.MainActivity.repeatBoolean;
import static com.madalin.licenta.controllers.MainActivity.shuffleBoolean;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
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
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madalin.licenta.NumeExtra;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.CardMelodieAdapter;
import com.madalin.licenta.controllers.fragments.AcasaFragment;
import com.madalin.licenta.interfaces.PlayerInterface;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.services.MuzicaService;

import java.util.ArrayList;
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
    private TextView textViewDescriereMelodie;
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

    MuzicaService muzicaService; // public static MediaPlayer mediaPlayer;
    private int pozitieMelodie = -1; // pozitia implicita a melodiei curente
    public static List<Melodie> listaMelodiiPlayer = new ArrayList<>(); // lista melodiilor
    public static Uri uri; // adresa melodiei
    private Handler handlerProgresMelodie = new Handler(); // handler pentru postarea delay-urilor in UI Thread
    private Thread playThread, previousThread, nextThread;
    //MediaSessionCompat mediaSessionCompat; // sesiune interactiuni cu controalele media din notificare
    static boolean isActivitatePlayerActiva = false; // specifica daca activitatea este activa sau nu pentru utilizarea sigura a serviciului muzical & notificarii

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        edgeToEdge(this, findViewById(R.id.player_linearLayoutContainer), Spatiere.PADDING, Directie.SUS);
        edgeToEdge(this, findViewById(R.id.player_textViewDescriereMelodie), Spatiere.MARGIN, Directie.JOS);

        //mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "madAudio"); // initializare sesiunea media cu token-ul "madAudio"

        initializareVederi();
        textViewNumeMelodie.setText("Se încarcă...");
        textViewNumeArtist.setText("Se încarcă...");
        textViewDescriereMelodie.setText("Se încarcă descrierea...");

        // listener buton inapoi
        imageViewButonInapoi.setOnClickListener(v -> {
            super.onBackPressed();
        });

        //new PregatirePlayerAsyncTask().execute(); // pregatire mediaPlayer si afisare date melodie
        pregatireMediaPlayer();

        //muzicaService.onTerminareMelodie(); // mutat la onServiceConnected()

        // listener pentru schimbarea starii seekBar-ului
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (muzicaService != null && fromUser) {
                    muzicaService.seekTo(progress * 1000); // cauta in melodie la pozitia de timp specificata de seekBar
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
                if (muzicaService != null) {
                    seekBar.setProgress(muzicaService.getPozitieCurenta() / 1000);
                    textViewDurataRedata.setText(formatareMilisecunde(muzicaService.getPozitieCurenta()));
                }

                handlerProgresMelodie.postDelayed(this, 1000); // intarzieri de o secunda intre actualizari
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

    // la reintoarcerea la activitate

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
     * Permite instantei {@link MuzicaService#actiuniRedareInterface} din MuzicaService sa
     * foloseasca implementarea din aceasta clasa. Seteaza valoarea maxima a {@link #seekBar}-ului
     * si apeleaza {@link #afisareDateMelodie()} pentru afisarea datelor melodiei curente.
     * Apeleaza {@link MuzicaService#onTerminareMelodie()} pentru reactionarea la terminarea melodiei.
     * Apeleaza {@link MuzicaService#afisareNotificare(int)} pentru afisarea notificarii.
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MuzicaService.MuzicaServiceBinder playerMuzicaServiceBinder = (MuzicaService.MuzicaServiceBinder) service;
        muzicaService = playerMuzicaServiceBinder.getServiciu(); // obtine instanta serviciul MuzicaService

        muzicaService.setCallbackPlayerInterface(this); // permite instantei ActiuniRedareInterface din MuzicaService sa foloseasca implementarea din PlayerActivity

        Log.e("", "PlayerActivity#onServiceConnected(ComponentName, IBinder) " + muzicaService);

        seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // mutat de la getIntentMethod()
        afisareDateMelodie(); // mutat de la getIntentMethod()

        muzicaService.onTerminareMelodie();
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
     * {@link #floatingActionButtonPlayPause}. Apeleaza {@link #actualizareSeekBar()} pentru
     * actualizarea seekBar-ului. Apeleaza {@link MuzicaService#afisareNotificare(int)} pentru
     * afisarea notificarii.
     */
    public void butonPlayPauseClicked() {
        // pauza player daca melodia este in curs de redare
        if (muzicaService.isPlaying()) {
            muzicaService.afisareNotificare(R.drawable.ic_play); // afisare notificare
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_play);
            muzicaService.pauza();
            actualizareSeekBar(); // actualizare progres seekBar
        }
        // pornire player daca melodia NU este in curs de redare
        else {
            muzicaService.afisareNotificare(R.drawable.ic_pause); // afisare notificare
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
            muzicaService.start();
            actualizareSeekBar();
        }
    }

    /**
     * Trece la melodia precedenta in functie de starea {@link #muzicaService}-ului. Afiseaza datele
     * noii melodii prin {@link #afisareDateMelodie()}. Actualizeaza seekBar-ul prin
     * {@link #actualizareSeekBar()}. Apeleaza listener-ul {@link MuzicaService#onTerminareMelodie()}.
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

            uri = Uri.parse(listaMelodiiPlayer.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI
            muzicaService.creeazaMediaPlayer(pozitieMelodie); // creeaza player pentru melodia de la pozitia data

            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            actualizareSeekBar(); // actualizare progres seekBar

            //mediaPlayer.setOnCompletionListener(this); // apelare listener pentru schimbarea melodiei la finalizare
            muzicaService.onTerminareMelodie(); // apelare listener pentru schimbarea melodiei la finalizare

            muzicaService.afisareNotificare(R.drawable.ic_pause); // afisare notificare

            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_pause); // schimba imaginea butonului PlayPause in "pause"
            muzicaService.start(); // porneste player-ul
        } else {
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

            uri = Uri.parse(listaMelodiiPlayer.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI
            muzicaService.creeazaMediaPlayer(pozitieMelodie);

            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            actualizareSeekBar(); // actualizare progres seekBar

            //mediaPlayer.setOnCompletionListener(this); // apelare listener pentru schimbarea melodiei la finalizare
            muzicaService.onTerminareMelodie();

            muzicaService.afisareNotificare(R.drawable.ic_play); // afisare notificare

            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_play); // schimba imaginea butonului PlayPause in "play"
        }
    }

    /**
     * Trece la melodia urmatoare in functie de starea {@link #muzicaService}-ului. Afiseaza datele
     * noii melodii prin {@link #afisareDateMelodie()}. Actualizeaza seekBar-ul prin
     * {@link #actualizareSeekBar()}. Apeleaza listener-ul {@link MuzicaService#onTerminareMelodie()}.
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

            // altfel pozitia melodiei ramane neschimbata
            uri = Uri.parse(listaMelodiiPlayer.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI
            muzicaService.creeazaMediaPlayer(pozitieMelodie);

            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            actualizareSeekBar(); // actualizare progres seekBar

            //mediaPlayer.setOnCompletionListener(this); // apelare listener pentru schimbarea melodiei la finalizare
            muzicaService.onTerminareMelodie();

            muzicaService.afisareNotificare(R.drawable.ic_pause); // afisare notificare

            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_pause); // schimba imaginea butonului PlayPause in "pause"
            muzicaService.start(); // porneste player-ul
        } else {
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

            uri = Uri.parse(listaMelodiiPlayer.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri); // creare player cu noul URI
            muzicaService.creeazaMediaPlayer(pozitieMelodie);

            afisareDateMelodie(); // actualizare UI cu datele noii melodii
            actualizareSeekBar(); // actualizare progres seekBar

            //mediaPlayer.setOnCompletionListener(this); // apelare listener pentru schimbarea melodiei la finalizare
            muzicaService.onTerminareMelodie();

            muzicaService.afisareNotificare(R.drawable.ic_play); // afisare notificare

            floatingActionButtonPlayPause.setBackgroundResource(R.drawable.ic_play); // schimba imaginea butonului PlayPause in "play"
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class PregatirePlayerAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            //pregatireMediaPlayer(); // pregatire mediaPlayer

            pozitieMelodie = getIntent().getIntExtra(NumeExtra.POZITIE_MELODIE, -1); // obtine pozitia melodiei selectate din intent AcasaFragment
            listaMelodiiPlayer = AcasaFragment.listaMelodii; // obtine lista cu melodii din AcasaFragment

            // daca lista cu melodii nu este goala, se va obtine URI-ul melodiei din aceasta
            if (listaMelodiiPlayer != null) {
                floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
                uri = Uri.parse(listaMelodiiPlayer.get(pozitieMelodie).getUrl());
            }

            // pregatire si lansare serviciu muzical
            Intent intent = new Intent(PlayerActivity.this, MuzicaService.class);
            intent.putExtra(NumeExtra.POZITIE_MELODIE_SERVICE, pozitieMelodie); // extra cu pozitia melodiei curente
            startService(intent); // lansare serviciu

            seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // seteaza limita maxima a seekBar-ului dupa ce s-a conectat la serviciu

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
//                    seekBar.setSecondaryProgress(percent /*(mediaPlayer.getDuration() / 1000)*/);
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
     * Listener pentru schimbarea melodiei dupa finalizarea acesteia. Apeleaza
     * {@link #butonNextClicked()} pentru a trece la urmatoarea melodie. In cazul in
     * care nu exista un {@link #muzicaService}, creeaza unul.
     */
//    @Override
//    public void onCompletion(MediaPlayer mp) {
//        butonNextClicked(); // trece la urmatoarea melodie
//
//        // creeaza un nou mediaPlayer in cazul in care nu exista deja unul
//        if (muzicaService != null) {
//            muzicaService.creeazaMediaPlayer(pozitieMelodie);
//            muzicaService.start();
//            muzicaService.onTerminareMelodie();
//        }
//    }

    /**
     * Pregateste {@link #muzicaService}-ul pentru redarea unei melodii din lista de melodii pe baza
     * pozitiei specificate in {@link Intent}-ul transmis de {@link CardMelodieAdapter}. Copiaza
     * lista de melodii {@link AcasaFragment#listaMelodii}. Apeleaza
     * {@link MuzicaService#afisareNotificare(int)} pentru afisarea notificarii. Lanseaza serviciul
     * {@link MuzicaService} prin intermediul unui {@link Intent} care are ca date pozitia melodiei
     * transmisa de {@link CardMelodieAdapter}.
     */
    private void pregatireMediaPlayer() {
        pozitieMelodie = getIntent().getIntExtra(NumeExtra.POZITIE_MELODIE, -1); // obtinere pozitie melodie selectata din intent
        listaMelodiiPlayer = AcasaFragment.listaMelodii; // obtine lista cu melodii din AcasaFragment

        // daca lista cu melodii nu este goala, se va obtine URI-ul melodiei din aceasta
        if (listaMelodiiPlayer != null) {
            floatingActionButtonPlayPause.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listaMelodiiPlayer.get(pozitieMelodie).getUrl());
        }

/*
        // daca mediaPlayer-ul ruleaza
        if (muzicaService != null) {
            muzicaService.stop(); // opreste redarea
            muzicaService.eliberare(); // elibereaza resursele curente
            muzicaService.creeazaMediaPlayer(pozitieMelodie);
            muzicaService.start(); // porneste redarea cu noua melodie /////// ???
        }
        // daca mediaPlayer-ul NU ruleaza
        else {
            muzicaService.creeazaMediaPlayer(pozitieMelodie);
            muzicaService.start();
        }
*/

        //muzicaService.afisareNotificare(R.drawable.ic_pause); // afisare notificare

        // pregatire si lansare serviciu muzical
        Intent intent = new Intent(this, MuzicaService.class);
        intent.putExtra(NumeExtra.POZITIE_MELODIE_SERVICE, pozitieMelodie); // extra cu pozitia melodiei curente
        startService(intent); // lansare serviciu

        //seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // mutat la onServiceConnected()
        //afisareDateMelodie(); // mutat la onServiceConnected()
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
                                    Palette.Swatch swatchCuloareDominanta = palette.getDominantSwatch(); // obtine culoarea dominanta a imaginii melodiei

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
     * Seteaza valoarea maxima a {@link #seekBar}-ului in functie de lungimea melodiei din
     * {@link #muzicaService}. Lanseaza un thread pe UiThread pentru actualizarea continua a
     * progresului {@link #seekBar}-ului si a {@link #textViewDurataRedata} in functie de pozitia
     * curenta a {@link #muzicaService}-ului.
     */
    public void actualizareSeekBar() {
        seekBar.setMax(muzicaService.getDurataMelodie() / 1000); // setare valoare maxima seekBar in functie de durata melodiei din mediaPlayer

        /*
        // actualizeaza progresul seekBar-ului in functie de progresul mediaPlayer-ului
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (muzicaService != null) {
                    seekBar.setProgress(muzicaService.getDurataMelodie() / 1000);
                    //textViewDurataRedata.setText(formatareMilisecunde(muzicaService.getPozitieCurenta()));
                }

                handlerProgresMelodie.postDelayed(this, 1000); // intarzieri de o secunda intre actualizari

            }
        });
        */
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
        ColorStateList colorStateList = ColorStateList.valueOf(swatchCuloareDominanta.getBodyTextColor()); // culoare elemente

        /*
        int culoareAlba = Color.parseColor("#FFFFFF");
        ColorStateList culoareAlbaState = ColorStateList.valueOf(culoareAlba);

        imageViewButonInapoi.setColorFilter(culoareAlba);
        textViewInCursDeRedare.setTextColor(culoareAlba);

        seekBar.setThumbTintList(culoareAlbaState);
        seekBar.setProgressTintList(culoareAlbaState);
        //seekBar.setSecondaryProgressTintList(swatchColorState);
        seekBar.setProgressBackgroundTintList(swatchColorState);

        textViewNumeMelodie.setTextColor(culoareAlba);
        textViewNumeArtist.setTextColor(culoareAlba);
        textViewDurataRedata.setTextColor(culoareAlba);
        textViewDurataMelodie.setTextColor(culoareAlba);

        floatingActionButtonPlayPause.setBackgroundTintList(swatchColorState);
        imageViewButonShuffle.setImageTintList(culoareAlbaState);
        imageViewButonSkipPrevious.setImageTintList(culoareAlbaState);
        imageViewButonSkipNext.setImageTintList(culoareAlbaState);
        imageViewButonRepeat.setImageTintList(culoareAlbaState);

        buttonSolicitaPermisiunea.setBackgroundTintList(swatchColorState);
        */

        linearLayoutContainer.setBackground(gradientDrawable); // setare culoare fundal

        imageViewButonInapoi.setImageTintList(colorStateList);
        textViewInCursDeRedare.setTextColor(colorStateList);

        seekBar.setThumbTintList(colorStateList);
        seekBar.setProgressTintList(colorStateList);
        //seekBar.setSecondaryProgressTintList(swatchColorState);
        seekBar.setProgressBackgroundTintList(colorStateList);

        textViewNumeMelodie.setTextColor(colorStateList);
        textViewNumeArtist.setTextColor(colorStateList);
        textViewDurataRedata.setTextColor(colorStateList);
        textViewDurataMelodie.setTextColor(colorStateList);

        floatingActionButtonPlayPause.setBackgroundTintList(colorStateList);
        imageViewButonShuffle.setImageTintList(colorStateList);
        imageViewButonSkipPrevious.setImageTintList(colorStateList);
        imageViewButonSkipNext.setImageTintList(colorStateList);
        imageViewButonRepeat.setImageTintList(colorStateList);

        buttonSolicitaPermisiunea.setBackgroundTintList(colorStateList);
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

//    /**
//     * Creeaza {@link Intent}-uri spre {@link NotificarePlayerReceiver} cu actiunile definite in
//     * {@link ApplicationClass}. Obtine {@link PendingIntent}-uri pentru efectuarea broadcast-urilor.
//     * Creeaza notificarea player-ului avand ca actiuni {@link PendingIntent}-urile definite anterior
//     * pentru invocare spre {@link NotificarePlayerReceiver}. Obtine si seteaza imaginea melodiei.
//     *
//     * @param imaginePlayPause resursa care se va afisa in notificare pentru actiunea de Play/Pause
//     */
//    void afisareNotificare(int imaginePlayPause) {
//        // continut
//        Intent intent = new Intent(this, PlayerActivity.class); // la click pe notificare se deschide PlayerActivity
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);
//
//        // intent din notificare spre NotificareReceiver cu actiunea ApplicationClass.ACTION_PLAY
//        Intent pauseIntent = new Intent(this, NotificarePlayerReceiver.class).setAction(ApplicationClass.ACTION_PLAY); // la click pe notificare se deschide NotificareReceiver
//        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT); // invocat de actiunea "Pause" din notificare
//
//        // intent din notificare spre NotificareReceiver cu actiunea ApplicationClass.ACTION_PREVIOUS
//        Intent previousIntent = new Intent(this, NotificarePlayerReceiver.class).setAction(ApplicationClass.ACTION_PREVIOUS); // la click pe notificare se deschide NotificareReceiver
//        PendingIntent previousPending = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT); // invocat de actiunea "Previous" din notificare
//
//        // intent din notificare spre NotificareReceiver cu actiunea ApplicationClass.ACTION_NEXT
//        Intent nextIntent = new Intent(this, NotificarePlayerReceiver.class).setAction(ApplicationClass.ACTION_NEXT); // la click pe notificare se deschide NotificareReceiver
//        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT); // invocat de actiunea "Next" din notificare
//
//        /*Bitmap thumbnail = null;
//
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(PlayerActivity.this, ApplicationClass.CHANNEL_ID_2)
//                        .setSmallIcon(imaginePlayPause)
//                        .setLargeIcon(thumbnail)
//
//                        .setContentTitle(listaMelodiiPlayer.get(pozitieMelodie).getNumeMelodie())
//                        .setContentText(listaMelodiiPlayer.get(pozitieMelodie).getNumeArtist())
//
//                        .addAction(R.drawable.ic_skip_previous, "Previous", previousPending)
//                        .addAction(imaginePlayPause, "Pause", pausePending)
//                        .addAction(R.drawable.ic_skip_next, "Next", nextPending)
//
//                        //.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()))
//                        .setPriority(NotificationCompat.PRIORITY_HIGH)
//                        .setOnlyAlertOnce(true);
//
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//
//        Glide.with(this)
//                .asBitmap()
//                .load(listaMelodiiPlayer.get(pozitieMelodie).getImagineMelodie()) // obtine imaginea ca bitmap
//                .into(new CustomTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resursaBitmapImagineMelodie, @Nullable Transition<? super Bitmap> transition) {
//                        notificationBuilder.setLargeIcon(resursaBitmapImagineMelodie);
//                        notificationBuilder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken()));
//
//                        Notification notification = notificationBuilder.build();
//                        notificationManager.notify(0, notification);
//                    }
//
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//
//                    }
//                });*/
//
//        // obtine imaginea melodiei si creeaza notificarea playerului
//        Glide.with(this).asBitmap().load(listaMelodiiPlayer.get(pozitieMelodie).getImagineMelodie()) // obtine imaginea ca bitmap
//                .into(new CustomTarget<Bitmap>() {
//                    @Override
//                    public void onResourceReady(@NonNull Bitmap resursaBitmapImagineMelodie, @Nullable Transition<? super Bitmap> transition) {
//                        Bitmap thumbnail; // imaginea melodiei de afisat in notificare
//
//                        // daca melodia are link spre imagine, se foloseste resursa Bitmap obtinuta
//                        if (listaMelodiiPlayer.get(pozitieMelodie).getImagineMelodie() != null) {
//                            thumbnail = resursaBitmapImagineMelodie; // setare bitmap obtinut ca imagine pentru melodie
//                        }
//                        // daca melodia nu are link spre imagine, se foloseste o resursa inlocuitoare
//                        else {
//                            thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.logo_music);
//                        }
//
//                        // crearea notificarii playerului cu datele melodiei in curs de rulare si a controalelor media
//                        Notification notificarePlayer = new NotificationCompat.Builder(PlayerActivity.this, ApplicationClass.CHANNEL_ID_2)
//                                .setSmallIcon(imaginePlayPause) // imaginea "Play/Pause" din bara de stare
//                                .setLargeIcon(thumbnail) // imaginea melodiei
//
//                                .setContentTitle(listaMelodiiPlayer.get(pozitieMelodie).getNumeMelodie()) // numele melodiei
//                                .setContentText(listaMelodiiPlayer.get(pozitieMelodie).getNumeArtist()) // numele artistului
//
//                                .addAction(R.drawable.ic_skip_previous, "Previous", previousPending) // buton "Previous", invoca PendingIntent spre NotificareReceiver
//                                .addAction(imaginePlayPause, "Pause", pausePending) // buton "Play/Pause", invoca PendingIntent spre NotificareReceiver
//                                .addAction(R.drawable.ic_skip_next, "Next", nextPending) // buton "Next", invoca PendingIntent spre NotificareReceiver
//
//                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat.getSessionToken())) // aplica stilul notificarii si ataseaza sesiunea de interactiuni cu controalele media din notificare
//                                .setPriority(NotificationCompat.PRIORITY_HIGH) // prioritatea inalta a notificarii
//                                .setOnlyAlertOnce(true)
//                                .build(); // combina optiunile setate si returneaza noul obiect Notification
//
//                        // notificare utilizator in legatura cu evenimentele din fundal
//                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); // obtine handle la serviciul de sistem "NOTIFICATION_SERVICE" pentru informarea utilizatorului despre evenimentele de fundal
//                        notificationManager.notify(0, notificarePlayer); // publica notificarea cu melodia in curs de rulare si inlocuieste notificarile cu acelasi ID
//                    }
//
//                    @Override
//                    public void onLoadCleared(@Nullable Drawable placeholder) {
//                    }
//                });
//    }
}