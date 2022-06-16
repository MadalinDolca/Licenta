package com.madalin.licenta.global;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class UtilitareFragment {

    /**
     * Inlocuieste layout-ul vederii unui {@link Fragment} cu unul nou.
     *
     * @param fragment    {@link Fragment}-ul pentru care se doreste inlocuirea layout-ului vederii
     * @param idLayoutNou ID-ul noului layout de utilizat
     */
    public static void inlocuireLayoutVedereFragment(Fragment fragment, int idLayoutNou) {
        LayoutInflater inflater = (LayoutInflater) fragment.requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View viewFragment = inflater.inflate(idLayoutNou, null); // umple vederea cu noul layout

        ViewGroup rootView = (ViewGroup) fragment.getView(); // obtine vederea radacina care sta la baza fragmentului

        assert rootView != null;
        rootView.removeAllViews(); // inlatura toate vederile copil din ViewGroup

        rootView.addView(viewFragment); // adauga vederea modificata
    }
}
