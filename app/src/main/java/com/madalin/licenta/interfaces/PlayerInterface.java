package com.madalin.licenta.interfaces;

/**
 * Interfata de comunicare dintre {@link com.madalin.licenta.controllers.PlayerActivity} si
 * {@link com.madalin.licenta.services.MuzicaService}.
 */
public interface PlayerInterface {
    void butonPlayPauseClicked();

    void butonPreviousClicked();

    void butonNextClicked();
}
