package com.madalin.licenta.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.madalin.licenta.ApplicationClass;
import com.madalin.licenta.global.NumeExtra;
import com.madalin.licenta.services.MuzicaService;

/**
 * Receptioneaza actiunile din cadrul notificarii {@link com.madalin.licenta.controllers.PlayerActivity}-ului si aplica comenzile trimitand
 * intent-uri spre {@link MuzicaService}.
 */
public class NotificarePlayerReceiver extends BroadcastReceiver {

    // comenzile oferite de notificare pentru MuzicaService prin Intent
    public static final String PLAY_PAUSE = "playPause";
    public static final String NEXT = "next";
    public static final String PREVIOUS = "previous";

    /**
     * Receptioneaza {@link Intent} broadcast-urile emise ca actiuni din notificarea
     * {@link MuzicaService#afisareNotificare(int)}. Identifica
     * numele actiunilor din {@link Intent}-uri comparandu-le cu cele din {@link ApplicationClass}
     * si lanseaza servicii spre {@link MuzicaService} printr-un {@link Intent} catre
     * acesta avand ca date numele actiunii.
     *
     * @param context contextul in care ruleaza receiver-ul
     * @param intent  {@link Intent}-ul primit
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String numeActiune = intent.getAction(); // obtine numele actiunii
        Intent notificareMuzicaServiceIntent = new Intent(context, MuzicaService.class); // intent spre MuzicaService

        // identifica numele actiunilor din Intent
        if (numeActiune != null) {
            switch (numeActiune) {
                case ApplicationClass.ACTION_PLAY:
                    notificareMuzicaServiceIntent.putExtra(NumeExtra.ACTIUNE_NOTIFICARE, PLAY_PAUSE);
                    context.startService(notificareMuzicaServiceIntent); // lanseaza serviciul spre MuzicaService cu datele "playPause"
                    break;

                case ApplicationClass.ACTION_NEXT:
                    notificareMuzicaServiceIntent.putExtra(NumeExtra.ACTIUNE_NOTIFICARE, NEXT);
                    context.startService(notificareMuzicaServiceIntent); // lanseaza serviciul spre MuzicaService cu datele "previous"
                    break;

                case ApplicationClass.ACTION_PREVIOUS:
                    notificareMuzicaServiceIntent.putExtra(NumeExtra.ACTIUNE_NOTIFICARE, PREVIOUS);
                    context.startService(notificareMuzicaServiceIntent); // lanseaza serviciul spre MuzicaService cu datele "previous"
                    break;
            }
        }
    }
}
