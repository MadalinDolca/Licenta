package com.madalin.licenta.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.Map;

public class Licenta implements Serializable {
    private String cheie;
    private String cheieArtist;
    private String cheieSolicitant;
    private String cheieMelodie;
    private String urlLicenta;
    private Long dataCrearii;

    public Licenta() {
    }

    public Licenta(String cheieArtist, String cheieSolicitant, String cheieMelodie, String urlLicenta) {
        this.cheieArtist = cheieArtist;
        this.cheieSolicitant = cheieSolicitant;
        this.cheieMelodie = cheieMelodie;
        this.urlLicenta = urlLicenta;
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

    public String getCheieSolicitant() {
        return cheieSolicitant;
    }

    public void setCheieSolicitant(String cheieSolicitant) {
        this.cheieSolicitant = cheieSolicitant;
    }

    public String getCheieMelodie() {
        return cheieMelodie;
    }

    public void setCheieMelodie(String cheieMelodie) {
        this.cheieMelodie = cheieMelodie;
    }

    public String getUrlLicenta() {
        return urlLicenta;
    }

    public void setUrlLicenta(String urlLicenta) {
        this.urlLicenta = urlLicenta;
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
