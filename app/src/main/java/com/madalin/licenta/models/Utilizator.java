package com.madalin.licenta.models;

import java.util.Map;

public class Utilizator {
    public String nume;
    public String email;
    public String grad;
    public Map<String, String> dataCrearii;

    public Utilizator() {
    }

    public Utilizator(String nume, String email, Map<String, String> dataCrearii) {
        this.nume = nume;
        this.email = email;
        this.dataCrearii = dataCrearii;
    }
}
