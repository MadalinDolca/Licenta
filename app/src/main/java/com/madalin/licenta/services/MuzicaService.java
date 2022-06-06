package com.madalin.licenta.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.madalin.licenta.ApplicationClass;
import com.madalin.licenta.NumeExtra;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.controllers.PlayerActivity;
import com.madalin.licenta.controllers.fragments.MiniPlayerFragment;
import com.madalin.licenta.interfaces.PlayerInterface;
import com.madalin.licenta.interfaces.MiniPlayerInterface;
import com.madalin.licenta.models.Melodie;
import com.madalin.licenta.receivers.NotificarePlayerReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviciu pentru redarea muzicii in fundal. Furnizeaza metode pentru controlarea
 * {@link MuzicaService#mediaPlayer}-ului care se ocupa de aceasta redare.
 */
public class MuzicaService extends Service
        implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener {

    MuzicaServiceBinder muzicaServiceBinder = new MuzicaServiceBinder();

    PlayerInterface playerInterface; // referinta a interfetei de comunicare dintre PlayerActivity si MuzicaService
    MiniPlayerInterface miniPlayerInterface; // referinta a interfetei de comunicare dintre MiniPlayerFragment si MuzicaService

    public MediaPlayer mediaPlayer;
    public List<Melodie> listaMelodiiService = new ArrayList<>();
    Uri uri; // adresa melodiei
    public int pozitieMelodie = -1; // pozitia implicita a melodiei din lista

    MediaSessionCompat mediaSessionCompat; // sesiune interactiuni cu controalele media din notificare

    // variabile finale folosite drept chei pentru stocarea datelor unei melodii in baza de date locala
    public static final String ULTIMA_MELODIE_REDATA = "ULTIMA_MELODIE_REDATA";
    public static final String URL_MELODIE = "URL_MELODIE";
    public static final String IMAGINE_MELODIE = "IMAGINE_MELODIE";
    public static final String NUME_MELODIE = "NUME_MELODIE";
    public static final String NUME_ARTIST = "NUME_ARTIST";

    @Override
    public void onCreate() {
        super.onCreate();

        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "madAudio"); // initializare sesiunea media cu token-ul "madAudio"
    }

    // se apeleaza la legarea serviciului de activitate

    /**
     * Returneaza {@link #muzicaServiceBinder} pe post de canal de comunicare dintre client
     * (activitate / fragment) si acest serviciu {@link MuzicaService}.
     *
     * @return {@link #muzicaServiceBinder} ca instanta a serviciului curent
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("", "MuzicaService#onBind()");
        return muzicaServiceBinder;
    }

    public class MuzicaServiceBinder extends Binder {
        /**
         * Returneaza instanta serviciului curent.
         *
         * @return instanta serviciului curent
         */
        public MuzicaService getServiciu() {
            Log.e("", "MuzicaService#MuzicaServiceBinder#getServiciu()");
            return MuzicaService.this;
        }
    }

    /**
     * Apelata de catre sistem de fiecare data cand clientul porneste explicit serviciul folosind
     * {@link #startService(Intent)}. Obtine pozitia melodiei oferita de {@link PlayerActivity} si
     * numele actiunii media din notificare oferit de {@link NotificarePlayerReceiver} prin
     * intermediul {@link Intent}-urilor. Lanseaza melodia folosind {@link #playMelodie(int)} si
     * pozitia din intent. Apeleaza metodele {@link #butonPlayPauseClicked()},
     * {@link #butonNextClicked()} si {@link #butonPreviousClicked()} in functie de actiunea
     * specificata in intent.
     *
     * @param intent tipul de {@link Intent} furnizat
     * @return START_STICKY - daca procesul acestui serviciu este oprit in timp ce este pornit
     * (dupa revenirea de la onStartCommand), atunci se lasa in starea pornita, dar nu pastraza
     * acest intent livrat. Ulterior, sistemul va incerca sa recreeze serviciul.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int pozitieMelodiePlayer = intent.getIntExtra(NumeExtra.POZITIE_MELODIE_SERVICE, -1); // pozitia melodiei oferita de PlayerActivity prin Intent
        String numeActiuneNotificare = intent.getStringExtra(NumeExtra.NUME_ACTIUNE_NOTIFICARE); // numele actiunii din notificare oferit de NotificarePlayerReceiver prin Intent

        // daca s-a furnizat o pozitie a melodiei, se lanseaza melodia
        if (pozitieMelodiePlayer != -1) {
            playMelodie(pozitieMelodiePlayer);
        }

        // daca s-a furnizat numele actiunii in NotificarePlayerReceiver, se verifica si se apeleaza tipul de actiune
        if (numeActiuneNotificare != null) {
            switch (numeActiuneNotificare) {
                case "playPause":
                    butonPlayPauseClicked();
                    break;

                case "next":
                    butonNextClicked();
                    break;

                case "previous":
                    butonPreviousClicked();
                    break;
            }
        }

        Log.e("", "MuzicaService#onStartCommand(Intent, int, int)");
        return START_STICKY; // return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Permite {@link #playerInterface} sa foloseasca implementarea
     * {@link PlayerInterface} din {@link PlayerActivity} pentru controlul actiunilor din
     * cadrul notificarii playerului {@link PlayerActivity} > {@link NotificarePlayerReceiver} >
     * {@link MuzicaService#onStartCommand(Intent, int, int)}.
     *
     * @param playerInterface clasa care implementeaza interfata {@link PlayerInterface}
     */
    public void setCallbackPlayerInterface(PlayerInterface playerInterface) {
        this.playerInterface = playerInterface;
    }

    /**
     * Permite {@link #miniPlayerInterface} sa foloseasca implementarea {@link MiniPlayerInterface}
     * din {@link MiniPlayerFragment} pentru controlul obtinerii si afisarii datelor in miniplayer-ul
     * din {@link MiniPlayerFragment}.
     *
     * @param miniPlayerInterface clasa care implementeaza interfata {@link MiniPlayerInterface}
     */
    public void setCallbackMiniPlayerInterface(MiniPlayerInterface miniPlayerInterface) {
        this.miniPlayerInterface = miniPlayerInterface;
    }

    /**
     * Copiaza lista cu melodii din {@link PlayerActivity#listaMelodiiPlayer}. Pregateste
     * {@link #mediaPlayer}-ul pentru redarea unei melodii din lista de melodii copiata pe baza unei
     * pozitii specificate.
     *
     * @param pozitieMelodiePlayer pozitia melodiei din lista
     */
    private void playMelodie(int pozitieMelodiePlayer) {
        listaMelodiiService = PlayerActivity.listaMelodiiPlayer; // obtine lista cu melodii din PlayerActivity
        pozitieMelodie = pozitieMelodiePlayer; // obtine pozitia melodiei din PlayerActivity

        // verifica daca mediaPlayer-ul este in curs de rulare
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // opreste mediaPlayer-ul
            mediaPlayer.release(); // elibereaza resursele mediaPlayer-ului

            // verifica daca lista cu melodii NU este goala
            if (listaMelodiiService != null) {
                creeazaMediaPlayer(pozitieMelodie); // creeaza un mediaPlayer pe baza pozitiei melodiei din lista
                mediaPlayer.start(); // lanseaza mediaPlayer-ul
            }
        }
        // daca mediaPlayer-ul este null (nu a inceput inca / nu este in curs de redare)
        else {
            creeazaMediaPlayer(pozitieMelodie); // creeaza un mediaPlayer pe baza pozitiei melodiei din lista
            mediaPlayer.start(); // lanseaza mediaPlayer-ul
        }
    }

    /**
     * Creeaza un {@link MediaPlayer} pentru {@link #mediaPlayer} folosind ca {@link Uri} adresa
     * melodiei de la pozitia data. Stocheaza datele melodiei in baza de date locala a sistemului
     * folosind {@link SharedPreferences} la cheile {@link #URL_MELODIE}, {@link #NUME_MELODIE}
     * si {@link #NUME_ARTIST}.
     *
     * @param pozitieData pozitia melodiei pentru redare din lista
     */
    public void creeazaMediaPlayer(int pozitieData) {
        pozitieMelodie = pozitieData;
        uri = Uri.parse(listaMelodiiService.get(pozitieMelodie).getUrl()); // obtine adresa resursei melodiei

        // stocheaza datele melodiei in baza de date locala a sistemului
        SharedPreferences.Editor editor = getSharedPreferences(ULTIMA_MELODIE_REDATA, MODE_PRIVATE).edit(); // obtine si pastreaza continutul fisierului cu preferinte "ULTIMA_MELODIE_REDATA" si creeaza un editor
        editor.putString(URL_MELODIE, listaMelodiiService.get(pozitieMelodie).getUrl()); // seteaza adresa melodiei la cheia "URL_MELODIE"
        editor.putString(IMAGINE_MELODIE, listaMelodiiService.get(pozitieMelodie).getImagineMelodie()); // seteaza imaginea melodiei la cheia "IMAGINE_MELODIE"
        editor.putString(NUME_MELODIE, listaMelodiiService.get(pozitieMelodie).getNumeMelodie()); // seteaza numele melodiei la cheia "NUME_MELODIE"
        editor.putString(NUME_ARTIST, listaMelodiiService.get(pozitieMelodie).getNumeArtist()); // seteaza numele artistului la cheia "NUME_ARTIST"
        editor.apply(); // comite noile preferinte inapoi editorului

        mediaPlayer = MediaPlayer.create(getBaseContext(), uri); // creaza MediaPlayer cu noul URI
    }

    /**
     * Apeleaza metoda {@link PlayerActivity#butonPlayPauseClicked()} prin intermediul
     * instantei {@link #playerInterface}. Actualizeaza {@link MiniPlayerFragment} folosind
     * {@link #actualizareMiniPlayer()}.
     */
    public void butonPlayPauseClicked() {
        if (playerInterface != null) {
            playerInterface.butonPlayPauseClicked();
            actualizareMiniPlayer();
        }
    }

    /**
     * Apeleaza metoda {@link PlayerActivity#butonPreviousClicked()} prin intermediul
     * instantei {@link #playerInterface}. Actualizeaza {@link MiniPlayerFragment} folosind
     * {@link #actualizareMiniPlayer()}.
     */
    public void butonPreviousClicked() {
        if (playerInterface != null) {
            playerInterface.butonPreviousClicked();
            actualizareMiniPlayer();
        }
    }

    /**
     * Apeleaza metoda {@link PlayerActivity#butonNextClicked()} prin intermediul
     * instantei {@link #playerInterface}. Actualizeaza {@link MiniPlayerFragment} folosind
     * {@link #actualizareMiniPlayer()}.
     */
    public void butonNextClicked() {
        if (playerInterface != null) {
            playerInterface.butonNextClicked();
            actualizareMiniPlayer();
        }
    }

    /**
     * Creeaza {@link Intent}-uri spre {@link NotificarePlayerReceiver} cu actiunile definite in
     * {@link ApplicationClass}. Obtine {@link PendingIntent}-uri pentru efectuarea broadcast-urilor.
     * Creeaza notificarea player-ului avand ca actiuni {@link PendingIntent}-urile definite anterior
     * pentru invocare spre {@link NotificarePlayerReceiver}. Obtine si seteaza imaginea melodiei.
     *
     * @param imaginePlayPause resursa care se va afisa in notificare pentru actiunea de Play/Pause
     */
    public void afisareNotificare(int imaginePlayPause) {
        // intent pentru deschiderea PlayerActivity la apasarea notificatii
        Intent contentIntent = new Intent(this, PlayerActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // la click pe notificare se deschide PlayerActivity
        //contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        contentIntent.putExtra(NumeExtra.POZITIE_MELODIE, pozitieMelodie);
        PendingIntent contentPending = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // intent din notificare spre NotificareReceiver cu actiunea ApplicationClass.ACTION_PLAY
        Intent pauseIntent = new Intent(this, NotificarePlayerReceiver.class).setAction(ApplicationClass.ACTION_PLAY); // la click pe notificare se deschide NotificareReceiver
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT); // invocat de actiunea "Pause" din notificare

        // intent din notificare spre NotificareReceiver cu actiunea ApplicationClass.ACTION_PREVIOUS
        Intent previousIntent = new Intent(this, NotificarePlayerReceiver.class).setAction(ApplicationClass.ACTION_PREVIOUS); // la click pe notificare se deschide NotificareReceiver
        PendingIntent previousPending = PendingIntent.getBroadcast(this, 0, previousIntent, PendingIntent.FLAG_UPDATE_CURRENT); // invocat de actiunea "Previous" din notificare

        // intent din notificare spre NotificareReceiver cu actiunea ApplicationClass.ACTION_NEXT
        Intent nextIntent = new Intent(this, NotificarePlayerReceiver.class).setAction(ApplicationClass.ACTION_NEXT); // la click pe notificare se deschide NotificareReceiver
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT); // invocat de actiunea "Next" din notificare

        // obtine imaginea melodiei si creeaza notificarea playerului
        Glide.with(this).asBitmap().load(listaMelodiiService.get(pozitieMelodie).getImagineMelodie()) // obtine imaginea ca bitmap
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resursaBitmapImagineMelodie, @Nullable Transition<? super Bitmap> transition) {
                        Bitmap thumbnail; // imaginea melodiei de afisat in notificare

                        // daca melodia are link spre imagine, se foloseste resursa Bitmap obtinuta
                        if (listaMelodiiService.get(pozitieMelodie).getImagineMelodie() != null) {
                            thumbnail = resursaBitmapImagineMelodie; // setare bitmap obtinut ca imagine pentru melodie
                        }
                        // daca melodia nu are link spre imagine, se foloseste o resursa inlocuitoare
                        else {
                            thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.logo_music);
                        }

                        // seteaza metadatele sesiunii media inainte de folosirea acesteia in setarea stilului notificarii (> Android 11)
                        mediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
                                .putString(MediaMetadata.METADATA_KEY_TITLE, listaMelodiiService.get(pozitieMelodie).getNumeMelodie())
                                .putString(MediaMetadata.METADATA_KEY_ARTIST, listaMelodiiService.get(pozitieMelodie).getNumeArtist())
                                .build()
                        );

                        // crearea notificarii playerului cu datele melodiei in curs de rulare si a controalelor media folosind canalul CHANNEL_PLAYER_NOTIFICARE
                        Notification notificarePlayer = new NotificationCompat.Builder(MuzicaService.this, ApplicationClass.CHANNEL_PLAYER_NOTIFICARE)
                                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // afiseaza controalele pe ecranul de blocare

                                .setSmallIcon(imaginePlayPause) // imaginea "Play/Pause" din bara de stare

                                .setContentIntent(contentPending) // deschide PlayerActivity la apasarea notificarii folosind "contentPending"

                                .addAction(R.drawable.ic_skip_previous, "Previous", previousPending) // buton "Previous", invoca PendingIntent spre NotificareReceiver
                                .addAction(imaginePlayPause, "Pause", pausePending) // buton "Play/Pause", invoca PendingIntent spre NotificareReceiver
                                .addAction(R.drawable.ic_skip_next, "Next", nextPending) // buton "Next", invoca PendingIntent spre NotificareReceiver

                                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                                        .setShowActionsInCompactView(0, 1, 2) // afiseaza butonul "Previous", "Play/Pause" si "Next" in modul compact
                                        .setMediaSession(mediaSessionCompat.getSessionToken())) // aplica stilul notificarii si ataseaza sesiunea de interactiuni cu controalele media din notificare

                                .setContentTitle(listaMelodiiService.get(pozitieMelodie).getNumeMelodie()) // numele melodiei
                                .setContentText(listaMelodiiService.get(pozitieMelodie).getNumeArtist()) // numele artistului
                                .setLargeIcon(thumbnail) // imaginea melodiei

                                .setPriority(NotificationCompat.PRIORITY_HIGH) // prioritatea inalta a notificarii
                                .setOnlyAlertOnce(true) // afiseaza notificarea ca alerta doar o data
                                .build(); // combina optiunile setate si returneaza noul obiect Notification

                        // notificare utilizator in legatura cu evenimentele din fundal
                        //NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); // obtine handle la serviciul de sistem "NOTIFICATION_SERVICE" pentru informarea utilizatorului despre evenimentele de fundal
                        //notificationManager.notify(0, notificarePlayer); // publica notificarea cu melodia in curs de rulare si inlocuieste notificarile cu acelasi ID

                        startForeground(1, notificarePlayer); // lansare notificare in prim plan
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    /**
     * Obtine si seteaza datele melodiei curente in fragmentul {@link MiniPlayerFragment} prin
     * intermediul {@link #miniPlayerInterface}. Apeleaza cu aceasta referinta a interfetei metodele
     * {@link MiniPlayerFragment#obtineDateMelodieStocata()} si
     * {@link MiniPlayerFragment#afisareDateMelodie()}.
     */
    void actualizareMiniPlayer() {
        if (MainActivity.arataMiniplayer) { // verifica starea de afisarea miniplayer-ului
            if (MiniPlayerFragment.URL_MELODIE_FRAGMENT != null) { // verifica daca exista locatia spre melodie
                if (miniPlayerInterface != null) { // verifica daca s-a setat callback-ul
                    miniPlayerInterface.obtineDateMelodieStocata(); // obtine datele melodiei stocate in baza de date locala a sistemului
                    miniPlayerInterface.afisareDateMelodie(); // afiseaza datele melodiei in miniplayer
                }
            }
        }
    }

    /**
     * Verifica daca {@link MuzicaService#mediaPlayer} este in curs de redare.
     *
     * @return {@link MediaPlayer#isPlaying()}
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /**
     * Porneste {@link MuzicaService#mediaPlayer} folosind {@link MediaPlayer#start()}.
     */
    public void start() {
        mediaPlayer.start();
    }

    /**
     * Opreste {@link MuzicaService#mediaPlayer} folosind {@link MediaPlayer#stop()}.
     */
    public void stop() {
        mediaPlayer.stop();
    }

    /**
     * Intrerupe {@link MuzicaService#mediaPlayer} folosind {@link MediaPlayer#pause()}.
     */
    public void pauza() {
        mediaPlayer.pause();
    }

    /**
     * Elibereaza resursele {@link MuzicaService#mediaPlayer}-ului folosind
     * {@link MediaPlayer#release()}.
     */
    public void eliberare() {
        mediaPlayer.release();
    }

    /**
     * Obtine durata melodiei curente din {@link MuzicaService#mediaPlayer}.
     *
     * @return {@link MediaPlayer#getDuration()}
     */
    public int getDurataMelodie() {
        return mediaPlayer.getDuration();
    }

    /**
     * Cauta in {@link MuzicaService#mediaPlayer} la pozitia de timp specificata.
     *
     * @param pozitie pozitia de timp
     */
    public void seekTo(int pozitie) {
        mediaPlayer.seekTo(pozitie);
    }

    /**
     * Obtine pozitia curenta de redare din {@link MuzicaService#mediaPlayer}.
     *
     * @return {@link MediaPlayer#getCurrentPosition()}
     */
    public int getPozitieCurenta() {
        return mediaPlayer.getCurrentPosition();
    }

    /**
     * Listener pentru schimbarea melodiei dupa finalizarea acesteia. Apeleaza
     * {@link PlayerInterface#butonNextClicked()} pentru a trece la urmatoarea melodie. Daca nu
     * exista un {@link #mediaPlayer}, creeaza unul. Actualizeaza {@link MiniPlayerFragment}
     * folosind {@link #actualizareMiniPlayer()}. Apeleaza {@link #onTerminareMelodie()}.
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        if (playerInterface != null) {
            playerInterface.butonNextClicked(); // trece la urmatoarea melodie

            // creeaza un nou mediaPlayer in cazul in care nu exista deja unul
            if (mediaPlayer != null) {
                creeazaMediaPlayer(pozitieMelodie);
                mediaPlayer.start();

                actualizareMiniPlayer(); // actualizeaza miniplayer-ul

                onTerminareMelodie();
            }
        }
    }

    /**
     * Inregistreaza un callback pentru a fi invocat atunci cand sfarsitul unei surse media a fost
     * atins in timpul redarii folosind {@link MuzicaService#onCompletion(MediaPlayer)}.
     */
    public void onTerminareMelodie() {
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    public void onBufferingUpdate() {

    }
}
