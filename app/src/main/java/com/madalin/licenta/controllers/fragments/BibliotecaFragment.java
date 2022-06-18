package com.madalin.licenta.controllers.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.madalin.licenta.R;
import com.madalin.licenta.controllers.AutentificareActivity;
import com.madalin.licenta.global.UtilitareFragment;

public class BibliotecaFragment extends Fragment {

    FirebaseAuth firebaseAuth;

    public BibliotecaFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance(); // obtinere instanta Firebase Auth
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_biblioteca, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        Button buttonAutentificare;

        // daca utilizatorul nu este conectat
        if (firebaseAuth.getCurrentUser() == null) {
            UtilitareFragment.inlocuireLayoutVedereFragment(BibliotecaFragment.this, R.layout.layout_solicitare_autentificare); // inlocuieste layout-ul vederii fragmentului cu cel pentru solicitarea autentificarii

            buttonAutentificare = requireView().findViewById(R.id.solicitare_autentificare_buttonAutentificare); // obtine vederea butonului de autentificare
            buttonAutentificare.setOnClickListener(view -> startActivity(new Intent(getActivity(), AutentificareActivity.class))); // seteaza un listener pentru lansarea AutentificareActivity la apasarea butonului
        }
    }
}