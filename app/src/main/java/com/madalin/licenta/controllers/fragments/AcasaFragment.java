package com.madalin.licenta.controllers.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.adapters.CategorieCarduriAdapter;
import com.madalin.licenta.interfaces.MadalinApi;
import com.madalin.licenta.R;
import com.madalin.licenta.models.CardMelodie;
import com.madalin.licenta.models.CategorieCarduri;
import com.madalin.licenta.models.Melodie;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AcasaFragment extends Fragment {

    private RecyclerView recyclerView;

    public static List<Melodie> listaMelodii; // lista pentru memorarea datelor melodiilor din request API
    private ArrayList<CardMelodie> cardMelodieArrayList; // pentru memorarea listei cu carduri
    private ArrayList<CategorieCarduri> categorieCarduriArrayList; // pentru memorarea categoriilor

    private DatabaseReference databaseReference;

    List<Melodie> listaMelodiiGithub;

    // constructor gol folosit atunci cand Android decide sa recreeze fragmentul
    public AcasaFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listaMelodii = new ArrayList<>();
        cardMelodieArrayList = new ArrayList<>();
        categorieCarduriArrayList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewFragmentAcasa = inflater.inflate(R.layout.fragment_acasa, container, false); // obtinere vedere fragment_acasa din MainActivity

        return viewFragmentAcasa;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        databaseReference = FirebaseDatabase.getInstance().getReference("melodii");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaMelodii.clear();
                cardMelodieArrayList.clear();
                categorieCarduriArrayList.clear();

                for (DataSnapshot melodieSnapshot : snapshot.getChildren()) { // parcurge toti descendentii nodului "melodii" din baza de date
                    Melodie melodie = melodieSnapshot.getValue(Melodie.class); // adauga valorile descendentului in obiect
                    melodie.setCheie(melodieSnapshot.getKey()); // memoreaza cheia descendentului

                    listaMelodii.add(melodie); // adauga obiectul in lista
                    cardMelodieArrayList.add(new CardMelodie(melodie.getImagineMelodie(), melodie.getNumeMelodie(), melodie.getNumeArtist())); // adauga un nou CardMelodie in lista
                }

                categorieCarduriArrayList.add(new CategorieCarduri("🎶 Toate Încărcările", cardMelodieArrayList)); // adauga lista cu cardurile melodiilor in lista categoriilor

                CategorieCarduriAdapter categorieCarduriAdapter = new CategorieCarduriAdapter(getContext(), categorieCarduriArrayList); // creare adapter categorie carduri
                recyclerView = requireActivity().findViewById(R.id.acasa_recyclerViewCategoriiCarduri); // obtinere vedere RecyclerView
                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                recyclerView.setAdapter(categorieCarduriAdapter); // setare adapter pe recyclerView pentru a furniza child views la cerere

                view.findViewById(R.id.acasa_lottieAnimationView).setVisibility(View.GONE); // ascunde animatie Lottie dupa afisarea cardurilor
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        getGithub(view);
    }

    void getGithub(View view) {
        ArrayList<CardMelodie> melodiiGitArrayList; // pentru memorarea melodiilor din categoria "X" (GitHub)

        melodiiGitArrayList = new ArrayList<>(); // pentru GitHub

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/MadalinDolca/APIs/main/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        MadalinApi madalinApi = retrofit.create(MadalinApi.class);
        Call<List<Melodie>> call = madalinApi.getMelodii();

        call.enqueue(new Callback<List<Melodie>>() {
            @Override
            public void onResponse(Call<List<Melodie>> call, Response<List<Melodie>> response) {
                if (!response.isSuccessful()) { // daca raspunsul nu se afla in intervalul [200..300)
                    Toast.makeText(AcasaFragment.this.getContext(), "Cod raspuns: " + response.code(), Toast.LENGTH_LONG).show();
                    return; // se iese din metoda
                }

                listaMelodiiGithub = response.body(); // adaugare body raspuns in lista de obiecte Melodie

                // golire liste pentru evitarea duplicarii datelor
                melodiiGitArrayList.clear();

                // iterare melodii si adaugare date in cardul din lista respectiva
                for (Melodie melodie : listaMelodiiGithub) {
                    melodiiGitArrayList.add(new CardMelodie(melodie.getImagineMelodie(), melodie.getNumeMelodie(), melodie.getNumeArtist()));
                }

                categorieCarduriArrayList.add(new CategorieCarduri("🐙 Github API", melodiiGitArrayList)); // adaugare lista carduri melodii in lista categoriilor

                CategorieCarduriAdapter categorieCarduriAdapter = new CategorieCarduriAdapter(/*this.*/getContext(), categorieCarduriArrayList); // creare adapter categorie carduri
                recyclerView = view.findViewById(R.id.acasa_recyclerViewCategoriiCarduri); // obtinere vedere RecyclerView
                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext() /*getActivity()*/));
                recyclerView.setAdapter(categorieCarduriAdapter); // setare adapter pe recyclerView pentru a furniza child views la cerere

                //categorieCarduriAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Melodie>> call, Throwable t) {
                Log.e("RETROFAIL", "Eroare: " + t.getMessage());
            }
        });
    }
}