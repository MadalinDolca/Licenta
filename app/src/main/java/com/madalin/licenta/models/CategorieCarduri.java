package com.madalin.licenta.models;

import java.util.List;

/**
 * Modelul unei categorii care contine titlul categoriei si lista de carduri cu muzica specifica
 * categoriei.
 */
public class CategorieCarduri {
    private String titluCategorie;
    private List<Melodie> listaMelodii;

    public CategorieCarduri(String titluCategorie, List<Melodie> listaMelodii) {
        this.titluCategorie = titluCategorie;
        this.listaMelodii = listaMelodii;
    }

    public String getTitluCategorie() {
        return titluCategorie;
    }

    public void setTitluCategorie(String titluCategorie) {
        this.titluCategorie = titluCategorie;
    }

    public List<Melodie> getListaMelodii() {
        return listaMelodii;
    }

    public void setListaMelodii(List<Melodie> listaMelodii) {
        this.listaMelodii = listaMelodii;
    }
}
