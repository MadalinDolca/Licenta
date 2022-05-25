package com.madalin.licenta.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Melodie {
    @SerializedName("artistName")
    private String numeArtist;

    @SerializedName("trackName")
    private String numeMelodie;

    @SerializedName("image")
    private String imagineMelodie;

    private String url;

    @SerializedName("genres")
    private ArrayList<String> genuri;

    public String getNumeArtist() {
        return numeArtist;
    }

    public String getNumeMelodie() {
        return numeMelodie;
    }

    public String getImagineMelodie() {
        return imagineMelodie;
    }

    public String getUrl() {
        return url;
    }

    public ArrayList<String> getGenuri() {
        return genuri;
    }
}
