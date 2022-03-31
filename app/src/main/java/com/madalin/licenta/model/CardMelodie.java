package com.madalin.licenta.model;

// modelul unui card care contine datele despre o melodie
public class CardMelodie {
    public String imagineMelodie;
    public String numeMelodie;
    public String numeArtist;

    public CardMelodie(String imagineMelodie, String numeMelodie, String numeArtist) {
        this.imagineMelodie = imagineMelodie;
        this.numeMelodie = numeMelodie;
        this.numeArtist = numeArtist;
    }
}