package com.madalin.licenta.controller;

import static com.madalin.licenta.EdgeToEdge.*;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.madalin.licenta.R;

public class PlayerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        edgeToEdge(this, findViewById(R.id.player_linearLayoutContainer), Spatiere.PADDING, Directie.SUS);
        edgeToEdge(this, findViewById(R.id.player_textViewDescriereMelodie), Spatiere.MARGIN, Directie.JOS);
    }
}