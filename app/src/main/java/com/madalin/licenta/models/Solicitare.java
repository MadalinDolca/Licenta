package com.madalin.licenta.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.Map;

public class Solicitare implements Serializable {
    private String cheie;
    private String cheieSolicitant;
    private String cheieArtist;
    private String cheieMelodie;
    private String scopulUtilizarii;
    private String mediulUtilizarii;
    private String loculUtilizarii;
    private String motivulUtilizarii;
    private String stadiu;
    private Long dataCrearii;

    // obiecte folosite pentru afisarea datelor
    private Utilizator solicitant;
    private Melodie melodie;

    // stadiul in care se poate afla o solicitare
    public static final String NEEVALUATA = "neevaluata";
    public static final String ACCEPTATA = "acceptata";
    public static final String RESPINSA = "respinsa";

    public Solicitare() {
    }

    public Solicitare(String cheieSolicitant, String cheieArtist, String cheieMelodie,
                      String scopulUtilizarii, String mediulUtilizarii, String loculUtilizarii,
                      String motivulUtilizarii, String stadiu) {
        this.cheieSolicitant = cheieSolicitant;
        this.cheieArtist = cheieArtist;
        this.cheieMelodie = cheieMelodie;
        this.scopulUtilizarii = scopulUtilizarii;
        this.mediulUtilizarii = mediulUtilizarii;
        this.loculUtilizarii = loculUtilizarii;
        this.motivulUtilizarii = motivulUtilizarii;
        this.stadiu = stadiu;
    }

    public String getCheie() {
        return cheie;
    }

    public void setCheie(String cheie) {
        this.cheie = cheie;
    }

    public String getCheieSolicitant() {
        return cheieSolicitant;
    }

    public void setCheieSolicitant(String cheieSolicitant) {
        this.cheieSolicitant = cheieSolicitant;
    }

    public String getCheieArtist() {
        return cheieArtist;
    }

    public void setCheieArtist(String cheieArtist) {
        this.cheieArtist = cheieArtist;
    }

    public String getCheieMelodie() {
        return cheieMelodie;
    }

    public void setCheieMelodie(String cheieMelodie) {
        this.cheieMelodie = cheieMelodie;
    }

    public String getScopulUtilizarii() {
        return scopulUtilizarii;
    }

    public void setScopulUtilizarii(String scopulUtilizarii) {
        this.scopulUtilizarii = scopulUtilizarii;
    }

    public String getMediulUtilizarii() {
        return mediulUtilizarii;
    }

    public void setMediulUtilizarii(String mediulUtilizarii) {
        this.mediulUtilizarii = mediulUtilizarii;
    }

    public String getLoculUtilizarii() {
        return loculUtilizarii;
    }

    public void setLoculUtilizarii(String loculUtilizarii) {
        this.loculUtilizarii = loculUtilizarii;
    }

    public String getMotivulUtilizarii() {
        return motivulUtilizarii;
    }

    public void setMotivulUtilizarii(String motivulUtilizarii) {
        this.motivulUtilizarii = motivulUtilizarii;
    }

    public String getStadiu() {
        return stadiu;
    }

    public void setStadiu(String stadiu) {
        this.stadiu = stadiu;
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
    public Utilizator getSolicitant() {
        return solicitant;
    }

    public void setSolicitant(Utilizator solicitant) {
        this.solicitant = solicitant;
    }

    public Melodie getMelodie() {
        return melodie;
    }

    public void setMelodie(Melodie melodie) {
        this.melodie = melodie;
    }
}
