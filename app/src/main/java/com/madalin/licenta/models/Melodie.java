package com.madalin.licenta.models;

import java.util.ArrayList;
import java.util.Map;

public class Melodie {
    //@SerializedName("artistName")
    public String uidArtist;
    public String numeArtist;
    public String numeMelodie;
    public String imagineMelodie;
    public String urlMelodie;
    public String genMelodie;
    public String descriere;
    public int numarRedari;
    public int numarAprecieri;
    public Map<String, String> dataCrearii;

    public Melodie() {

    }

    public Melodie(String uidArtist, String numeArtist, String numeMelodie, String imagineMelodie, String urlMelodie, String genMelodie, String descriere, int numarRedari, int numarAprecieri, Map<String, String> dataCrearii) {
        this.uidArtist = uidArtist;
        this.numeArtist = numeArtist;
        this.numeMelodie = numeMelodie;
        this.imagineMelodie = imagineMelodie;
        this.urlMelodie = urlMelodie;
        this.genMelodie = genMelodie;
        this.descriere = descriere;
        this.numarRedari = numarRedari;
        this.numarAprecieri = numarAprecieri;
        this.dataCrearii = dataCrearii;
    }

    public String getNumeArtist() {
        return numeArtist;
    }

    public String getNumeMelodie() {
        return numeMelodie;
    }

    public String getImagineMelodie() {
        return imagineMelodie;
    }

    public String getUrlMelodie() {
        return urlMelodie;
    }

    public String getGenMelodie() {
        return genMelodie;
    }
}
