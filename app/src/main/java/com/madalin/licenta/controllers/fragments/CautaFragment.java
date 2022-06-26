package com.madalin.licenta.controllers.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.BannerMelodieCautataAdapter;
import com.madalin.licenta.adapters.CategorieCarduriAdapter;
import com.madalin.licenta.models.Melodie;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CautaFragment extends Fragment {

    private EditText editTextBaraCautare;
    private ImageView imageViewButonCautare;
    private RecyclerView recyclerView;
    private TextView textViewMesaj;
    private LottieAnimationView lottieAnimationView;

    private List<Melodie> listaMelodii;

    public CautaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listaMelodii = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cauta, container, false);
        editTextBaraCautare = view.findViewById(R.id.cauta_editTextBaraCautare);
        imageViewButonCautare = view.findViewById(R.id.cauta_imageViewButonCautare);
        recyclerView = view.findViewById(R.id.cauta_recyclerView);
        textViewMesaj = view.findViewById(R.id.cauta_textViewMesaj);
        lottieAnimationView = view.findViewById(R.id.cauta_lottieAnimationView);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // cauta melodia la apasarea butonului ENTER in EditText
        editTextBaraCautare.setOnKeyListener((v, keyCode, keyevent) -> {
            if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                cautaMelodii(editTextBaraCautare.getText().toString());
                return true;
            }
            return false;
        });

        // cauta melodia la apasarea butonului de cautare
        imageViewButonCautare.setOnClickListener(v -> cautaMelodii(editTextBaraCautare.getText().toString()));
    }

    /**
     * Cauta melodia in baza de date in functie de termenul oferit. Daca datele melodiei corespund
     * termenului, aceasta se adauga in {@link #listaMelodii}. Seteaza un adapter pentru
     * {@link #recyclerView} cu {@link #listaMelodii}.
     *
     * @param termenCautare termenul pentru cautarea melodiei
     */
    private void cautaMelodii(String termenCautare) {
        textViewMesaj.setVisibility(View.GONE); // ascunde mesajul
        lottieAnimationView.setVisibility(View.VISIBLE); // afiseaza animatia de incarcare

        FirebaseDatabase.getInstance().getReference("melodii")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaMelodii.clear();

                        for (DataSnapshot melodieSnapshot : snapshot.getChildren()) { // parcurge toti descendentii nodului "melodii" din baza de date
                            Melodie melodie = melodieSnapshot.getValue(Melodie.class); // adauga valorile descendentului in obiect
                            melodie.setCheie(melodieSnapshot.getKey()); // memoreaza cheia descendentului

                            // verifica daca datele melodiei corespunde termenului de cautare
                            if (melodie.getNumeMelodie().toLowerCase().contains(termenCautare)
                                    || melodie.getNumeArtist().toLowerCase().contains(termenCautare)
                                    || melodie.getGenMelodie().toLowerCase().contains(termenCautare)) {
                                listaMelodii.add(melodie); // adauga obiectul in lista
                            }
                        }

                        // daca lista este goala se afiseaza un mesaj
                        if (listaMelodii.size() == 0) {
                            textViewMesaj.setVisibility(View.VISIBLE);
                        }
                        // altfel se configureaza adapter-ul si recyclerView-ul
                        else {
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(new BannerMelodieCautataAdapter(getContext(), listaMelodii)); // setare adapter pe recyclerView pentru a furniza child views la cerere
                        }

                        lottieAnimationView.setVisibility(View.GONE); // ascunde animatia de incarcare
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Eroare cautare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}