package com.madalin.licenta.controller.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.madalin.licenta.R;

public class AdaugaFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1"; // parametru de initializare a fragmentului
    private String mParam1;

    // constructor gol folosit atunci cand Android decide sa recreeze fragmentul
    public AdaugaFragment() {
    }

    // metoda pentru crearea unei noi instante a fragmentului folosind parametrii
    public static AdaugaFragment newInstance(String param1) {
        AdaugaFragment fragment = new AdaugaFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_adauga, container, false);
    }
}