package com.madalin.licenta.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class Melodie {
    private String cheie;

    private String cheieArtist;

    @SerializedName("artistName")
    private String numeArtist;

    @SerializedName("trackName")
    private String numeMelodie;

    @SerializedName("image")
    private String imagineMelodie;

    @SerializedName("url")
    private String urlMelodie;
    private String genMelodie;
    private String descriere;
    private int numarRedari;
    private int numarAprecieri;
    //private Map<String, String> dataCrearii;
    private Long dataCrearii;

    public Melodie() {

    }

    public Melodie(String cheieArtist, String numeArtist, String numeMelodie,
                   String imagineMelodie, String urlMelodie, String genMelodie, String descriere,
                   int numarRedari, int numarAprecieri/*, Map<String, String> dataCrearii*/) {
        this.cheieArtist = cheieArtist;
        this.numeArtist = numeArtist;
        this.numeMelodie = numeMelodie;
        this.imagineMelodie = imagineMelodie;
        this.urlMelodie = urlMelodie;
        this.genMelodie = genMelodie;
        this.descriere = descriere;
        this.numarRedari = numarRedari;
        this.numarAprecieri = numarAprecieri;
        //this.dataCrearii = dataCrearii;
    }

    public String getCheie() {
        return cheie;
    }

    public void setCheie(String cheie) {
        this.cheie = cheie;
    }

    public String getCheieArtist() {
        return cheieArtist;
    }

    public void setCheieArtist(String cheieArtist) {
        this.cheieArtist = cheieArtist;
    }

    public String getNumeArtist() {
        return numeArtist;
    }

    public void setNumeArtist(String numeArtist) {
        this.numeArtist = numeArtist;
    }

    public String getNumeMelodie() {
        return numeMelodie;
    }

    public void setNumeMelodie(String numeMelodie) {
        this.numeMelodie = numeMelodie;
    }

    public String getImagineMelodie() {
        return imagineMelodie;
    }

    public void setImagineMelodie(String imagineMelodie) {
        this.imagineMelodie = imagineMelodie;
    }

    public String getUrlMelodie() {
        return urlMelodie;
    }

    public void setUrlMelodie(String urlMelodie) {
        this.urlMelodie = urlMelodie;
    }

    public String getGenMelodie() {
        return genMelodie;
    }

    public void setGenMelodie(String genMelodie) {
        this.genMelodie = genMelodie;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    public int getNumarRedari() {
        return numarRedari;
    }

    public void setNumarRedari(int numarRedari) {
        this.numarRedari = numarRedari;
    }

    public int getNumarAprecieri() {
        return numarAprecieri;
    }

    public void setNumarAprecieri(int numarAprecieri) {
        this.numarAprecieri = numarAprecieri;
    }

    public Map<String, String> getDataCrearii() {
        return ServerValue.TIMESTAMP;
    }

    @Exclude
    public Long getDataCreariiLong() {
        return dataCrearii;
    }

    public void setDataCrearii(Long dataCrearii) {
        this.dataCrearii = dataCrearii;
    }
}
