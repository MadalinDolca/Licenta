package com.madalin.licenta.controllers.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.services.MuzicaService;

public class MiniPlayerFragment extends Fragment implements ServiceConnection {

    ImageView imageViewImagineMelodie;
    ImageView imageViewButonNext;
    FloatingActionButton floatingActionButtonButonPlayPause;
    TextView textViewNumeMelodie;
    TextView textViewNumeArtist;
    View viewMiniPlayer;

    MuzicaService muzicaService; // instanta serviciu muzical

    public MiniPlayerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewMiniPlayer = inflater.inflate(R.layout.fragment_mini_player, container, false); // obtine vederea fragment_mini_player

        // initializare vederi fragment
        imageViewImagineMelodie = viewMiniPlayer.findViewById(R.id.mini_player_imageViewImagineMelodie);
        textViewNumeMelodie = viewMiniPlayer.findViewById(R.id.mini_player_textViewNumeMelodie);
        textViewNumeArtist = viewMiniPlayer.findViewById(R.id.mini_player_textViewNumeArtist);
        floatingActionButtonButonPlayPause = viewMiniPlayer.findViewById(R.id.mini_player_floatingActionButtonPlayPause);
        imageViewButonNext = viewMiniPlayer.findViewById(R.id.mini_player_imageViewButonNext);

        // listener apasare buton play/pause
        floatingActionButtonButonPlayPause.setOnClickListener(v -> {
            if (muzicaService != null) {
                muzicaService.butonPlayPauseClicked();

                // schimba imaginea butonului play/pause in functie de starea serviciului
                if (muzicaService.isPlaying()) {
                    floatingActionButtonButonPlayPause.setImageResource(R.drawable.ic_pause);
                } else {
                    floatingActionButtonButonPlayPause.setImageResource(R.drawable.ic_play);
                }
            }
        });

        // listener apasare buton next
        imageViewButonNext.setOnClickListener(v -> {
            if (muzicaService != null) {
                muzicaService.butonNextClicked();

                // daca framentul este asociat unei activitati
                if (getActivity() != null) {
                    // stocheaza datele melodiei in baza de date locala a sistemului folosind campurile din MuzicaService
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(MuzicaService.ULTIMA_MELODIE_REDATA, Context.MODE_PRIVATE).edit(); // obtine si pastraza continutul fisierului cu preferinte "ULTIMA_MELODIE_REDATA" si creeaza un editor
                    editor.putString(MuzicaService.URL_MELODIE, muzicaService.listaMelodiiService.get(muzicaService.pozitieMelodie).getUrl()); // seteaza adresa melodiei la cheia "URL_MELODIE"
                    editor.putString(MuzicaService.IMAGINE_MELODIE, muzicaService.listaMelodiiService.get(muzicaService.pozitieMelodie).getImagineMelodie()); // seteaza imaginea melodiei la cheia "IMAGINE_MELODIE"
                    editor.putString(MuzicaService.NUME_MELODIE, muzicaService.listaMelodiiService.get(muzicaService.pozitieMelodie).getNumeMelodie()); // seteaza numele melodiei la cheia "NUME_MELODIE"
                    editor.putString(MuzicaService.NUME_ARTIST, muzicaService.listaMelodiiService.get(muzicaService.pozitieMelodie).getNumeArtist()); // seteaza numele artistului la cheia "NUME_ARTIST"
                    editor.apply(); // comite noile preferinte inapoi editorului

                    // obtine datele melodiei stocate in baza de date locala a sistemului
                    MainActivity.obtineDateMelodieStocata(getActivity());

                    afisareDateMelodie(); // afiseaza datele melodiei
                }
            }
        });

        return viewMiniPlayer;
    }

    /**
     * Cand fragmentul este vizibil utilizatorului, iar acesta poate interactiona cu
     * {@link MiniPlayerFragment} sau se intoarce la acest fragment, vederile se vor popula cu
     * datele salvate in baza de date locala preluate din {@link MainActivity} folosind
     * {@link #afisareDateMelodie()}. Realizeaza conectarea fragmentului {@link MiniPlayerFragment}
     * la serviciul {@link MuzicaService}.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (MainActivity.arataMiniplayer) { // verifica starea de afisarea miniplayer-ului
            if (MainActivity.URL_MELODIE_FRAGMENT != null) { // verifica daca exista locatiea spre melodie

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
            getContext().unbindService(this); // unbind de la serviciul muzical
        }
    }

    /**
     * La conectarea la serviciul {@link MuzicaService} se obtine o instanta a acestuia si se
     * initializeaza {@link #muzicaService} pentru a face posibila manipularea serviciului muzical.
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MuzicaService.MuzicaServiceBinder miniPlayerMuzicaServiceBinder = (MuzicaService.MuzicaServiceBinder) service;
        muzicaService = miniPlayerMuzicaServiceBinder.getServiciu(); // obtine instanta serviciul MuzicaService si initializeaza "muzicaService"
    }

    /**
     * La deconectarea de la {@link MuzicaService} se dezinstantiaza {@link #muzicaService}, facand
     * imposibila manipularea serviciului muzical.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        muzicaService = null;
    }

    void afisareDateMelodie() {
        if (MainActivity.IMAGINE_MELODIE_FRAGMENT != null) {
            Glide.with(getContext())
                    .load(MainActivity.IMAGINE_MELODIE_FRAGMENT/*muzicaService.listaMelodiiService.get(muzicaService.pozitieMelodie).getImagineMelodie()*/)
                    .placeholder(R.drawable.logo_music)
                    .into(imageViewImagineMelodie);
        } else {
            Glide.with(getContext())
                    .load(R.drawable.ic_nota_muzicala)
                    .placeholder(R.drawable.logo_music)
                    .error(R.drawable.ic_eroare)
                    .into(imageViewImagineMelodie);
        }

        textViewNumeMelodie.setText(MainActivity.NUME_MELODIE_FRAGMENT);
        textViewNumeArtist.setText(MainActivity.NUME_ARTIST_FRAGMENT);
    }
}