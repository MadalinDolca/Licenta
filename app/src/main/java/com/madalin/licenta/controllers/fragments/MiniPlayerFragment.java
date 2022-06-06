package com.madalin.licenta.controllers.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madalin.licenta.NumeExtra;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.controllers.PlayerActivity;
import com.madalin.licenta.interfaces.MiniPlayerInterface;
import com.madalin.licenta.services.MuzicaService;

public class MiniPlayerFragment extends Fragment
        implements
        MiniPlayerInterface,
        ServiceConnection {

    RelativeLayout relativeLayoutMiniPlayer;
    ImageView imageViewImagineMelodie;
    ImageView imageViewButonNext;
    FloatingActionButton floatingActionButtonButonPlayPause;
    TextView textViewNumeMelodie;
    TextView textViewNumeArtist;
    View viewMiniPlayer;

    MuzicaService muzicaService; // instanta serviciu muzical

    public static String URL_MELODIE_FRAGMENT = null;
    public static String IMAGINE_MELODIE_FRAGMENT = null;
    public static String NUME_ARTIST_FRAGMENT = null;
    public static String NUME_MELODIE_FRAGMENT = null;

    public MiniPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewMiniPlayer = inflater.inflate(R.layout.fragment_mini_player, container, false); // obtine vederea fragment_mini_player

        initializareVederi(); // initializare vederi fragment

        relativeLayoutMiniPlayer.setOnClickListener(v -> {
            if (muzicaService != null) {
                if (muzicaService.mediaPlayer != null) {
                    if (muzicaService.pozitieMelodie != -1) {
                        Intent intent = new Intent(getContext(), PlayerActivity.class);
                        intent.putExtra(NumeExtra.POZITIE_MELODIE, muzicaService.pozitieMelodie); // adaugare pozitie melodie selectata in intent
                        //intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        getActivity().startActivity(intent);
                    }
                } else {
                    Toast.makeText(getContext(), "Selecteaza o melodie!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // listener apasare buton play/pause
        floatingActionButtonButonPlayPause.setOnClickListener(v -> {
            if (muzicaService != null) {
                muzicaService.butonPlayPauseClicked(); // play/pause

                if (muzicaService.mediaPlayer != null) {
                    // schimba imaginea butonului play/pause in functie de starea serviciului
                    if (muzicaService.isPlaying()) {
                        floatingActionButtonButonPlayPause.setImageResource(R.drawable.ic_pause);
                    } else {
                        floatingActionButtonButonPlayPause.setImageResource(R.drawable.ic_play);
                    }
                } else {
                    Toast.makeText(getContext(), "Selecteaza o melodie!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // listener apasare buton next
        imageViewButonNext.setOnClickListener(v -> {
            if (muzicaService != null) {
                muzicaService.butonNextClicked(); // trece la urmatoarea melodie (MuzicaService memoreaza datele melodiei curente atunci cand creaza un MediaPlayer)

                // daca framentul este asociat unei activitati
                if (getActivity() != null) {
                    obtineDateMelodieStocata(); // obtine datele melodiei stocate in baza de date locala a sistemului
                    afisareDateMelodie(); // afiseaza datele melodiei in fragment miniplayer
                }
            }
        });

        return viewMiniPlayer;
    }

    /**
     * Cand fragmentul este vizibil utilizatorului, iar acesta poate interactiona cu
     * {@link MiniPlayerFragment} sau se intoarce la acest fragment, se vor obtine datele melodiei
     * stocate in baza de date locala folosind {@link #obtineDateMelodieStocata()}. Daca
     * {@link MainActivity#arataMiniplayer} este <code>true</code>, se vor popula vederile cu datele
     * obtinute folosind {@link #afisareDateMelodie()}. Realizeaza conectarea fragmentului
     * {@link MiniPlayerFragment} la serviciul {@link MuzicaService}.
     */
    @Override
    public void onResume() {
        super.onResume();

        obtineDateMelodieStocata(); // obtine datele melodiei stocate in baza de date locala a sistemului

        if (MainActivity.arataMiniplayer) { // verifica starea de afisarea miniplayer-ului
            if (URL_MELODIE_FRAGMENT != null) { // verifica daca exista locatiea spre melodie
                afisareDateMelodie(); // afiseaza datele melodiei in miniplayer

                // leaga MiniPlayerFragment la serviciul muzical MuzicaService
                Intent intent = new Intent(getContext(), MuzicaService.class); // creeaza Intent-ul dintre clase

                // daca acest fragment are un Context asociat, se realizeaza conectarea la serviciul muzical
                if (getContext() != null) {
                    getContext().bindService(intent, this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    /**
     * Cand fragmentul nu este vizibil utilizatorului, {@link MiniPlayerFragment} se dezleaga de la
     * serviciul muzical {@link MuzicaService}.
     */
    @Override
    public void onPause() {
        super.onPause();

        // daca acest fragment are un Context asociat, se realizeaza deconectarea la serviciul muzical
        if (getContext() != null) {
            if (muzicaService != null) {
                getContext().unbindService(this); // unbind de la serviciul muzical
                Log.e("", "MiniPlayerFragment#onPause()");
            }
        }
    }

    /**
     * La conectarea la serviciul {@link MuzicaService} se obtine o instanta a acestuia si se
     * initializeaza {@link #muzicaService} pentru a face posibila manipularea serviciului muzical.
     * Permite instantei {@link MuzicaService#miniPlayerInterface} din {@link MuzicaService} sa
     * foloseasca implementarea din aceasta clasa.
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MuzicaService.MuzicaServiceBinder miniPlayerMuzicaServiceBinder = (MuzicaService.MuzicaServiceBinder) service;
        muzicaService = miniPlayerMuzicaServiceBinder.getServiciu(); // obtine instanta serviciul MuzicaService si initializeaza "muzicaService"

        muzicaService.setCallbackMiniPlayerInterface(this); // permite instantei ActiuniRedareInterface din MuzicaService sa foloseasca implementarea din PlayerActivity
    }

    /**
     * La deconectarea de la {@link MuzicaService} se dezinstantiaza {@link #muzicaService}, facand
     * imposibila manipularea serviciului muzical.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        muzicaService = null;
    }

    /**
     * Obtine datele melodiei stocate in baza de date locala a sistemului folosind {@link SharedPreferences}
     * din fisierul cu preferinte {@link MuzicaService#ULTIMA_MELODIE_REDATA}. Adauga datele obtinute
     * in {@link #URL_MELODIE_FRAGMENT}, {@link #IMAGINE_MELODIE_FRAGMENT},
     * {@link #NUME_MELODIE_FRAGMENT} si {@link #NUME_ARTIST_FRAGMENT}.
     */
    public void obtineDateMelodieStocata() {
        // obtine datele melodiei stocate in baza de date locala a sistemului
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MuzicaService.ULTIMA_MELODIE_REDATA, Context.MODE_PRIVATE);
        URL_MELODIE_FRAGMENT = sharedPreferences.getString(MuzicaService.URL_MELODIE, null); // obtine URL-ul melodiei la cheia "URL_MELODIE"
        IMAGINE_MELODIE_FRAGMENT = sharedPreferences.getString(MuzicaService.IMAGINE_MELODIE, null); // obtine imaginea melodiei la cheia "IMAGINE_MELODIE"
        NUME_MELODIE_FRAGMENT = sharedPreferences.getString(MuzicaService.NUME_MELODIE, null); // obtine numele melodiei la cheia "NUME_MELODIE"
        NUME_ARTIST_FRAGMENT = sharedPreferences.getString(MuzicaService.NUME_ARTIST, null); // obtine numele artistului la cheia "NUME_ARTIST"

        if (URL_MELODIE_FRAGMENT != null) {
            MainActivity.arataMiniplayer = true;
        } else {
            MainActivity.arataMiniplayer = false;

            URL_MELODIE_FRAGMENT = null;
            IMAGINE_MELODIE_FRAGMENT = null;
            NUME_MELODIE_FRAGMENT = null;
            NUME_ARTIST_FRAGMENT = null;
        }
    }

    /**
     * Populeaza fragmentul {@link MiniPlayerFragment} cu datele din {@link #IMAGINE_MELODIE_FRAGMENT},
     * {@link #NUME_MELODIE_FRAGMENT} si {@link #NUME_ARTIST_FRAGMENT}.
     */
    public void afisareDateMelodie() {
        // populeaza campurile din miniplayer
        textViewNumeMelodie.setText(NUME_MELODIE_FRAGMENT);
        textViewNumeMelodie.setSelected(true); // pentru marquee

        textViewNumeArtist.setText(NUME_ARTIST_FRAGMENT);
        textViewNumeArtist.setSelected(true); // pentru marquee

        // setare imagine melodie si paleta de culori
        if (IMAGINE_MELODIE_FRAGMENT == null) { // daca melodia nu are link spre imagine
            imageViewImagineMelodie.setImageResource(R.drawable.ic_nota_muzicala); // se adauga o resursa inlocuitoare
        }
        // daca melodia are link spre imagine, aceasta se obtine ca bitmap, se adauga in card si se aplica paleta de culori
        else {
            Glide.with(this).asBitmap().load(IMAGINE_MELODIE_FRAGMENT) // obtine imaginea ca bitmap
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resursaBitmapImagineMelodie, @Nullable Transition<? super Bitmap> transition) {
                            PlayerActivity.animatieImagine(getContext(), imageViewImagineMelodie, resursaBitmapImagineMelodie); // animatie la schimbarea imaginii

                            // generare paleta de culori
                            Palette.from(resursaBitmapImagineMelodie).generate(palette -> {
                                Palette.Swatch swatchCuloareDominanta = palette.getDominantSwatch(); // obtine culoarea dominanta a imaginii melodiei

                                if (swatchCuloareDominanta != null) {
                                    // aplica culoarea swatch-ului pe elementele player-ului
                                    ColorStateList swatchColorState = ColorStateList.valueOf(swatchCuloareDominanta.getBodyTextColor()); // culoare elemente
                                    ColorStateList swatchColorStateDominant = ColorStateList.valueOf(swatchCuloareDominanta.getRgb()); // culoare fundal

                                    relativeLayoutMiniPlayer.setBackgroundTintList(swatchColorStateDominant); // setare culoare fundal
                                    textViewNumeMelodie.setTextColor(swatchColorState);
                                    textViewNumeArtist.setTextColor(swatchColorState);
                                    floatingActionButtonButonPlayPause.setBackgroundTintList(swatchColorState);
                                    imageViewButonNext.setImageTintList(swatchColorState);
                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        }
    }

    /**
     * Initializeaza vederile din {@link MiniPlayerFragment}.
     */
    void initializareVederi() {
        relativeLayoutMiniPlayer = viewMiniPlayer.findViewById(R.id.mini_player_relativeLayout);
        imageViewImagineMelodie = viewMiniPlayer.findViewById(R.id.mini_player_imageViewImagineMelodie);
        textViewNumeMelodie = viewMiniPlayer.findViewById(R.id.mini_player_textViewNumeMelodie);
        textViewNumeArtist = viewMiniPlayer.findViewById(R.id.mini_player_textViewNumeArtist);
        floatingActionButtonButonPlayPause = viewMiniPlayer.findViewById(R.id.mini_player_floatingActionButtonPlayPause);
        imageViewButonNext = viewMiniPlayer.findViewById(R.id.mini_player_imageViewButonNext);
    }
}