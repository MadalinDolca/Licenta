package com.madalin.licenta.model;

import java.util.List;

// modelul unei categorii care contine titlul categoriei si lista de carduri muzicale specifice categoriei
public class CategorieCarduri {
    public String titluCategorie;
    public List<CardMelodie> listaCardMelodie;

    public CategorieCarduri(String titluCategorie, List<CardMelodie> listaCardMelodie) {
        this.titluCategorie = titluCategorie;
        this.listaCardMelodie = listaCardMelodie;
    }
}
