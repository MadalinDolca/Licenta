package com.madalin.licenta.controllers.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.CategorieCarduriAdapter;
import com.madalin.licenta.models.CategorieCarduri;
import com.madalin.licenta.models.Melodie;

import java.util.ArrayList;
import java.util.List;

public class AcasaFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayoutContainer;
    private RecyclerView recyclerView;

    private List<Melodie> listaMelodii; // lista pentru memorarea datelor melodiilor din baza de date
    private List<Melodie> listaMelodiiGitHub; // lista pentru memorarea datelor melodiilor din request API
    private ArrayList<CategorieCarduri> listaCategorii; // pentru memorarea categoriilor

    // constructor gol folosit atunci cand Android decide sa recreeze fragmentul
    public AcasaFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listaMelodii = new ArrayList<>();
        listaCategorii = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acasa, container, false); // obtinere vedere fragment_acasa din MainActivity
        swipeRefreshLayoutContainer = view.findViewById(R.id.acasa_swipeRefreshLayoutContainer);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getMelodiiFirebase(view); // obtine melodiile din firebase

        // reincarca melodiile la glisarea fragmentului
        swipeRefreshLayoutContainer.setOnRefreshListener(() -> {
            getMelodiiFirebase(view);
        });

        //getMelodiiGitHub(view);
    }

    /**
     * Obtine datele melodiilor din baza de date, le memoreaza in array-uri si le adauga in
     * RecyclerView.
     *
     * @param view vederea fragmentului
     */
    private void getMelodiiFirebase(View view) {
        FirebaseDatabase.getInstance().getReference("melodii")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaMelodii.clear();
                        listaCategorii.clear();

                        for (DataSnapshot melodieSnapshot : snapshot.getChildren()) { // parcurge toti descendentii nodului "melodii" din baza de date
                            Melodie melodie = melodieSnapshot.getValue(Melodie.class); // adauga valorile descendentului in obiect
                            melodie.setCheie(melodieSnapshot.getKey()); // memoreaza cheia descendentului

                            listaMelodii.add(melodie); // adauga obiectul in lista
                        }

                        listaCategorii.add(new CategorieCarduri("🎶 Toate Încărcările", listaMelodii)); // adauga lista cu melodii in lista categoriilor

                        CategorieCarduriAdapter categorieCarduriAdapter = new CategorieCarduriAdapter(getContext(), listaCategorii); // creeaza un adapter pentru categoria cardurilor
                        recyclerView = requireActivity().findViewById(R.id.acasa_recyclerViewCategoriiCarduri); // obtinere vedere RecyclerView
                        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                        recyclerView.setAdapter(categorieCarduriAdapter); // setare adapter pe recyclerView pentru a furniza child views la cerere

                        view.findViewById(R.id.acasa_lottieAnimationView).setVisibility(View.GONE); // ascunde animatia Lottie dupa afisarea cardurilor
                        swipeRefreshLayoutContainer.setRefreshing(false); // inchide animatia de refresh
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Eroare: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

//    void getMelodiiGitHub(View view) {
//        listaMelodiiGitHub = new ArrayList<>(); // pentru GitHub
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://raw.githubusercontent.com/MadalinDolca/APIs/main/")
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        MadalinApi madalinApi = retrofit.create(MadalinApi.class);
//        Call<List<Melodie>> call = madalinApi.getMelodii();
//
//        call.enqueue(new Callback<List<Melodie>>() {
//            @Override
//            public void onResponse(Call<List<Melodie>> call, Response<List<Melodie>> response) {
//                if (!response.isSuccessful()) { // daca raspunsul nu se afla in intervalul [200..300)
//                    Toast.makeText(AcasaFragment.this.getContext(), "Cod raspuns: " + response.code(), Toast.LENGTH_LONG).show();
//                    return; // se iese din metoda
//                }
//
//                listaMelodiiGitHub = response.body(); // adaugare body raspuns in lista de obiecte Melodie
//
//                // golire liste pentru evitarea duplicarii datelor
//                melodiiGitArrayList.clear();
//
//                // iterare melodii si adaugare date in cardul din lista respectiva
//                for (Melodie melodie : listaMelodiiGitHub) {
//                    melodiiGitArrayList.add(new CardMelodie(melodie.getImagineMelodie(), melodie.getNumeMelodie(), melodie.getNumeArtist()));
//                }
//
//                listaCategorii.add(new CategorieMelodii("🐙 GitHub API", melodiiGitArrayList)); // adaugare lista carduri melodii in lista categoriilor
//
//                CategorieCarduriAdapter categorieCarduriAdapter = new CategorieCarduriAdapter(/*this.*/getContext(), listaCategorii); // creare adapter categorie carduri
//                recyclerView = view.findViewById(R.id.acasa_recyclerViewCategoriiCarduri); // obtinere vedere RecyclerView
//                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext() /*getActivity()*/));
//                recyclerView.setAdapter(categorieCarduriAdapter); // setare adapter pe recyclerView pentru a furniza child views la cerere
//
//                //categorieCarduriAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(Call<List<Melodie>> call, Throwable t) {
//                Log.e("RETROFAIL", "Eroare: " + t.getMessage());
//            }
//        });
//    }
}