package com.madalin.licenta.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.Map;

public class Utilizator implements Serializable {
    private String cheie;
    private String nume;
    private String email;
    private String grad;
    private Long dataCrearii;

    public static final String GRAD_ADMIN = "admin";
    public static final String GRAD_NORMAL = "normal";

    public Utilizator() {
    }

    public Utilizator(String nume, String email, String grad) {
        this.nume = nume;
        this.email = email;
        this.grad = grad;
    }

    public String getCheie() {
        return cheie;
    }

    public void setCheie(String cheie) {
        this.cheie = cheie;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGrad() {
        return grad;
    }

    public void setGrad(String grad) {
        this.grad = grad;
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
