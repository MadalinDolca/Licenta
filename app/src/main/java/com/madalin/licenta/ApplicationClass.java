package com.madalin.licenta;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

// notificarea melodiei in curs de redare

/**
 * Extinde clasa {@link Application} pentru a crea canalele de notificare atunci cand aplicatia
 * este lansata, inainte de a publica unele notificari.
 */
public class ApplicationClass extends Application {
    public static final String CHANNEL_PLAYER_NOTIFICARE = "channelMuzicaNotificare";
    public static final String CHANNEL_ID_2 = "channel2";

    public static final String ACTION_PREVIOUS = "ApplicationClass.actionPrevious";
    public static final String ACTION_NEXT = "ApplicationClass.actionNext";
    public static final String ACTION_PLAY = "ApplicationClass.actionPlay";

    @Override
    public void onCreate() {
        super.onCreate();
        creeazaNotificationChannel();
    }

    private void creeazaNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel playerNotificareChannel = new NotificationChannel(CHANNEL_PLAYER_NOTIFICARE, "Player Notificare Channel", NotificationManager.IMPORTANCE_HIGH);
            playerNotificareChannel.setDescription("Canal pentru afisarea controlului muzical ca notificare.");

            NotificationChannel channel2 = new NotificationChannel(CHANNEL_ID_2, "Channel(2)", NotificationManager.IMPORTANCE_HIGH);
            channel2.setDescription("Channel 2 Desc...");

            // inregistreaza canalele notificarilor in sistem
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(playerNotificareChannel);
            notificationManager.createNotificationChannel(channel2);
        }
    }
}
