package com.madalin.licenta.controller.fragment;

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

import com.madalin.licenta.CategorieCarduriAdapter;
import com.madalin.licenta.MadalinApi;
import com.madalin.licenta.R;
import com.madalin.licenta.model.CardMelodie;
import com.madalin.licenta.model.CategorieCarduri;
import com.madalin.licenta.model.Melodie;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AcasaFragment extends Fragment {

    RecyclerView recyclerView;
    ArrayList<CategorieCarduri> categorieCarduriArrayList; // pentru memorarea categoriilor

    ArrayList<CardMelodie> melodiiGitArrayList;
    ArrayList<CardMelodie> electronicaArrayList; // pentru memorarea melodiilor din categoria "electronica"
    ArrayList<CardMelodie> favoriteArrayList;

    private static final String ARG_PARAM1 = "param1"; // parametru de initializare a fragmentului
    private String mParam1;

    // constructor gol folosit atunci cand Android decide sa recreeze fragmentul
    public AcasaFragment() {
    }

    // metoda pentru crearea unei noi instante a fragmentului folosind parametrii
    public static AcasaFragment newInstance(String param1) {
        AcasaFragment fragment = new AcasaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);

        return fragment; // returneaza noua instanta a fragmentului
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }

        categorieCarduriArrayList = new ArrayList<>();
        melodiiGitArrayList = new ArrayList<>();
        electronicaArrayList = new ArrayList<>();
        favoriteArrayList = new ArrayList<>();

/*
        electronicaArrayList.clear();
        electronicaArrayList.add(new CardMelodie(R.drawable.background_logo, "My Way", "Max Cameron"));
        electronicaArrayList.add(new CardMelodie(R.drawable.logo_music, "Johnny Wants to Fight", "BADFLOWER"));
        electronicaArrayList.add(new CardMelodie(R.drawable.ic_setari, "NUMB", "Chri$tian Gate$"));
        categorieCarduriArrayList.add(new CategorieCarduri("Yah Yah!", electronicaArrayList));

        favoriteArrayList.clear();
        favoriteArrayList.add(new CardMelodie(R.drawable.background_logo, "Oki doki", "DAZN"));
        favoriteArrayList.add(new CardMelodie(R.drawable.ic_meniu, "Numa Numa", "OOO ZONIII"));
        favoriteArrayList.add(new CardMelodie(R.drawable.logo_music, "AZNAEB", "GTA SA"));
        categorieCarduriArrayList.add(new CategorieCarduri("Favorite", favoriteArrayList));

        electronicaArrayList.clear();
        electronicaArrayList.add(new CardMelodie(R.drawable.background_logo, "My Way", "Max Cameron"));
        electronicaArrayList.add(new CardMelodie(R.drawable.logo_music, "Johnny Wants to Fight", "BADFLOWER"));
        electronicaArrayList.add(new CardMelodie(R.drawable.ic_setari, "NUMB", "Chri$tian Gate$"));
        categorieCarduriArrayList.add(new CategorieCarduri("Yah Yah!", electronicaArrayList));

        favoriteArrayList.clear();
        favoriteArrayList.add(new CardMelodie(R.drawable.background_logo, "Oki doki", "DAZN"));
        favoriteArrayList.add(new CardMelodie(R.drawable.ic_meniu, "Numa Numa", "OOO ZONIII"));
        favoriteArrayList.add(new CardMelodie(R.drawable.logo_music, "AZNAEB", "GTA SA"));
        categorieCarduriArrayList.add(new CategorieCarduri("Favorite", favoriteArrayList));
    */
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewFragmentAcasa = inflater.inflate(R.layout.fragment_acasa, container, false); // obtinere vedere fragment_acasa din MainActivity
/*
        CategorieCarduriAdapter categorieCarduriAdapter;
        categorieCarduriAdapter = new CategorieCarduriAdapter(this.getContext(), categorieCarduriArrayList);

        recyclerView = viewFragmentAcasa.findViewById(R.id.acasa_recyclerViewCategoriiCarduri); // obtinere vedere RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(viewFragmentAcasa.getContext()));
        recyclerView.setAdapter(categorieCarduriAdapter);
        categorieCarduriAdapter.notifyDataSetChanged();
*/
        return viewFragmentAcasa;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

                List<Melodie> listaMelodii = response.body(); // adaugare body raspuns in lista de obiecte Melodie
                melodiiGitArrayList.clear(); // golire lista pentru evitarea duplicarii datelor

                /////////////
                electronicaArrayList.clear();
                favoriteArrayList.clear();
                /////////////

                // iterare melodii si adaugare date in cardul din lista respectiva
                for (Melodie melodie : listaMelodii) {
                    melodiiGitArrayList.add(new CardMelodie(melodie.getImagineMelodie(), melodie.getNumeMelodie(), melodie.getNumeArtist()));

                    /////////
                    electronicaArrayList.add(new CardMelodie(melodie.getImagineMelodie(), melodie.getNumeMelodie(), melodie.getNumeArtist()));
                    favoriteArrayList.add(new CardMelodie(melodie.getImagineMelodie(), melodie.getNumeMelodie(), melodie.getNumeArtist()));
                    /////////
                }

                categorieCarduriArrayList.add(new CategorieCarduri("Mada API", melodiiGitArrayList)); // adaugare lista carduri melodii in lista categoriilor

                //////////
                categorieCarduriArrayList.add(new CategorieCarduri("Mada API", electronicaArrayList)); // adaugare lista carduri melodii in lista categoriilor
                categorieCarduriArrayList.add(new CategorieCarduri("Mada API", favoriteArrayList)); // adaugare lista carduri melodii in lista categoriilor
                //////////

                CategorieCarduriAdapter categorieCarduriAdapter;
                categorieCarduriAdapter = new CategorieCarduriAdapter(/*this.*/getContext(), categorieCarduriArrayList);

                recyclerView = view.findViewById(R.id.acasa_recyclerViewCategoriiCarduri); // obtinere vedere RecyclerView
                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext() /*getActivity()*/));
                recyclerView.setAdapter(categorieCarduriAdapter);
                //categorieCarduriAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<Melodie>> call, Throwable t) {
                Log.e("RETROFAIL", "Eroare: " + t.getMessage());
            }
        });
    }
}