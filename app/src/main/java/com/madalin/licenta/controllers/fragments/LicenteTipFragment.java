package com.madalin.licenta.controllers.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.madalin.licenta.R;
import com.madalin.licenta.adapters.BannerLicentaAdapter;
import com.madalin.licenta.controllers.MainActivity;
import com.madalin.licenta.models.Licenta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class LicenteTipFragment extends Fragment {

    private RecyclerView recyclerView;

    private static final String KEY_TIP = "key_tip"; // cheia la care se va adauga argumentul (tipul licentei) in Bundle
    private String tipLicenta; // memoreaza tipul licentei la crearea fragmentului
    private List<Licenta> listaLicente; // lista pentru memorarea datelor licentelor din baza de date

    public LicenteTipFragment() {
        // constructor folosit atunci cand Android recreeaza fragmentul
    }

    /**
     * Creeaza si returneaza un nou {@link LicenteTipFragment} dupa adaugarea intr-un
     * {@link Bundle} a argumentelor oferite. {@link Bundle}-ul se seteaza ca argumente pentru noul
     * fragment pentru retinerea si reutilizarea acestora in cazul distrugerii si al crearii
     * fragmentului.
     *
     * @param tip argumentul oferit la crearea fragmentului
     * @return un nou {@link LicenteTipFragment}
     */
    public static LicenteTipFragment newInstance(String tip) {
        LicenteTipFragment fragment = new LicenteTipFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TIP, tip); // adauga argumentul in Bundle la cheia KEY_TIP
        fragment.setArguments(args); // seteaza Bundle-ul ca argumente pentru noul fragment pentru retinere
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // obtine tipul de licenta din lista de argumente
        if (getArguments() != null) {
            tipLicenta = getArguments().getString(KEY_TIP);
        }

        listaLicente = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        switch (tipLicenta) {
            case Licenta.PRIMITA:
                return inflater.inflate(R.layout.fragment_licente_primite, container, false);

            default:
                return inflater.inflate(R.layout.fragment_licente_oferite, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // obtine vederea RecyclerView-ului specific stadiului solicitarii
        switch (tipLicenta) {
            case Licenta.OFERITA:
                recyclerView = view.findViewById(R.id.licente_oferite_recyclerView);
                break;

            default:
                recyclerView = view.findViewById(R.id.licente_primite_recyclerView);
                break;
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // seteaza LayoutManager-ul pe care recyclerView-ul il va folosi

        // obtine licentele din baza de date
        FirebaseDatabase.getInstance().getReference("licente")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        listaLicente.clear();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Licenta licenta = dataSnapshot.getValue(Licenta.class);
                            licenta.setCheie(dataSnapshot.getKey());

                            // daca cheia beneficiarului din licenta este egala cu cheia utilizatorului curent, iar tipul selectat este "PRIMITA"
                            if (Objects.equals(licenta.getCheieBeneficiar(), MainActivity.utilizator.getCheie())
                                    && Objects.equals(tipLicenta, Licenta.PRIMITA)) {
                                listaLicente.add(licenta); // adauga licenta in lista
                            }

                            // daca cheia artistului din licenta este egala cu cheia utilizatorului curent, iar tipul selectat este "OFERITA"
                            if (Objects.equals(licenta.getCheieArtist(), MainActivity.utilizator.getCheie())
                                    && Objects.equals(tipLicenta, Licenta.OFERITA)) {
                                listaLicente.add(licenta); // adauga licenta in lista
                            }
                        }

                        recyclerView.setAdapter(new BannerLicentaAdapter(getContext(), listaLicente)); // seteaza adapterul pentru legarea datelor de vederi
                        afiseazaMesaj(view);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Eroare: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Ascunde {@link com.airbnb.lottie.LottieAnimationView}-ul specific layout-ului stadiului
     * solicitarii dupa afisarea banner-elor.
     */
    private void ascundeLottie(View view) {
        switch (tipLicenta) {
            case Licenta.OFERITA:
                view.findViewById(R.id.licente_oferite_lottieAnimationView).setVisibility(View.GONE);
                break;

            default:
                view.findViewById(R.id.licente_primite_lottieAnimationView).setVisibility(View.GONE);
                break;
        }
    }

    /**
     * Afiseaza un {@link android.widget.TextView} daca nu exista solicitari in stadiul respectiv in
     * {@link #listaLicente}.
     * Apeleaza {@link #ascundeLottie(View)} pentru ascunderea animatiei.
     *
     * @param view vederea fragmentului
     */
    private void afiseazaMesaj(View view) {
        switch (tipLicenta) {
            case Licenta.OFERITA:
                ascundeLottie(view); // ascunde animatia

                // afiseaza un mesaj daca lista licentelor oferite este goala
                if (listaLicente.size() == 0) {
                    view.findViewById(R.id.licente_oferite_textViewMesaj).setVisibility(View.VISIBLE);
                }
                break;

            default:
                ascundeLottie(view); // ascunde animatia

                // afiseaza un mesaj daca lista licentelor primite este goala
                if (listaLicente.size() == 0) {
                    view.findViewById(R.id.licente_primite_textViewMesaj).setVisibility(View.VISIBLE);
                }
                break;
        }
    }
}