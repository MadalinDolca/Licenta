package com.madalin.licenta.interfaces;

/**
 * Interfata de comunicare dintre {@link com.madalin.licenta.controllers.fragments.MiniPlayerFragment}
 * si {@link com.madalin.licenta.services.MuzicaService}.
 */
public interface MiniPlayerInterface {
    void obtineDateMelodieStocata();

    void afisareDateMelodie();
}
