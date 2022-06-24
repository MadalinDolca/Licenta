package com.madalin.licenta.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.Map;

public class Licenta implements Serializable {
    private String cheie;
    private String cheieArtist;
    private String cheieBeneficiar;
    private String cheieMelodie;
    private String urlLicenta;
    private Long dataCrearii;

    // date folosite pentru afisare
    private String temp_numeBeneficiar;
    private String temp_numeArtist;
    private String temp_numeMelodie;
    private String temp_imagineMelodie;

    // tipul licentei
    public static final String PRIMITA = "primita";
    public static final String OFERITA = "oferita";

    public Licenta() {
    }

    public Licenta(String cheieArtist, String cheieBeneficiar, String cheieMelodie, String urlLicenta) {
        this.cheieArtist = cheieArtist;
        this.cheieBeneficiar = cheieBeneficiar;
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

    public String getCheieBeneficiar() {
        return cheieBeneficiar;
    }

    public void setCheieBeneficiar(String cheieBeneficiar) {
        this.cheieBeneficiar = cheieBeneficiar;
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

    // Getters & Setters date folosite pentru afisare
    public String getTemp_numeBeneficiar() {
        return temp_numeBeneficiar;
    }

    public void setTemp_numeBeneficiar(String temp_numeBeneficiar) {
        this.temp_numeBeneficiar = temp_numeBeneficiar;
    }

    public String getTemp_numeArtist() {
        return temp_numeArtist;
    }

    public void setTemp_numeArtist(String temp_numeArtist) {
        this.temp_numeArtist = temp_numeArtist;
    }

    public String getTemp_numeMelodie() {
        return temp_numeMelodie;
    }

    public void setTemp_numeMelodie(String temp_numeMelodie) {
        this.temp_numeMelodie = temp_numeMelodie;
    }

    public String getTemp_imagineMelodie() {
        return temp_imagineMelodie;
    }

    public void setTemp_imagineMelodie(String temp_imagineMelodie) {
        this.temp_imagineMelodie = temp_imagineMelodie;
    }
}
