package com.madalin.licenta;

import android.content.ContentResolver;
import android.graphics.Color;
import android.os.Build;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

public class EdgeToEdge {
    public enum Inset {BARS, GESTURES}

    public enum Spatiere {MARGIN, PADDING}

    public enum Directie {STANGA, SUS, DREAPTA, JOS}

    // metoda pentru extinderea activitatii pe intregul ecran si transparentizarea barilor de sistem
    public static void fullscreen(AppCompatActivity activitate) {
        WindowCompat.setDecorFitsSystemWindows(activitate.getWindow(), false); // activitate full screen
        activitate.getWindow().setStatusBarColor(Color.TRANSPARENT); // bara de status transparenta
        activitate.getWindow().setNavigationBarColor(Color.TRANSPARENT); // bara de navigare transparenta
    }

    // metoda pentru detectarea navigarii prin gesturi
    public static boolean isGestureNavigationEnabled(ContentResolver contentResolver) {
        return Settings.Secure.getInt(contentResolver, "navigation_mode", 0) == 2;
    }

    // metoda pentru aplicarea spatiilor unei vederi pentru inlaturarea suprapunerilor cu barile de sistem folosind Insets
    public static void edgeToEdge(AppCompatActivity activitate, View vedere, Spatiere spatiere, Directie directie) {
        // se aplica edge to edge daca versiunea este >= Q (API 29) si daca navigarea prin gesturi este activata
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isGestureNavigationEnabled(activitate.getContentResolver())) {
                fullscreen(activitate);

                vedere.setOnApplyWindowInsetsListener((v, insets) -> {
                    // aplicare insets ca margini pentru vedere
                    if (spatiere == Spatiere.MARGIN) {
                        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

                        switch (directie) {
                            case STANGA:
                                mlp.leftMargin = insets.getSystemWindowInsetLeft();
                                break;

                            case SUS:
                                mlp.topMargin = insets.getSystemWindowInsetTop();
                                break;

                            case DREAPTA:
                                mlp.rightMargin = insets.getSystemWindowInsetRight();
                                break;

                            case JOS:
                                mlp.bottomMargin = insets.getSystemWindowInsetBottom();
                                break;

                            default:
                                break;
                        }

                        v.setLayoutParams(mlp); // setare parametrii
                    }

                    // aplicare insets ca padding pentru vedere
                    else if (spatiere == Spatiere.PADDING) {
                        switch (directie) {
                            case STANGA:
                                v.setPadding(insets.getSystemWindowInsetLeft(), v.getPaddingTop(), v.getPaddingRight(), v.getPaddingBottom());
                                break;

                            case SUS:
                                v.setPadding(v.getPaddingLeft(), insets.getSystemWindowInsetTop(), v.getPaddingRight(), v.getPaddingBottom());
                                break;

                            case DREAPTA:
                                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), insets.getSystemWindowInsetRight(), v.getPaddingBottom());
                                break;

                            case JOS:
                                v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.getSystemWindowInsetBottom());
                                break;

                            default:
                                break;
                        }
                    }

                    return insets; // returnare inseturi
                });
            }
        }
    }

/*  // Versiunea 2
    public static void edgeToEdge(AppCompatActivity activitate, View vedere, Inset inset, Spatiere spatiere, Directie directie) {
        // se aplica edge to edge daca versiunea este >= Q (API 29)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           fullscreen(activitate);

            // manipulare suprapuneri folosind insets
            ViewCompat.setOnApplyWindowInsetsListener(vedere, (v, windowInsets) -> {
                Insets insets = null;

                // stabilire tip inset
                if (inset == Inset.BARS) {
                    insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()); // system bars insets
                } else if (inset == Inset.GESTURES) {
                    insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures()); // system gesture insets
                }

                // aplicare insets ca margini pentru vedere
                if (spatiere == Spatiere.MARGIN) {
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();

                    switch (directie) {
                        case STANGA:
                            mlp.leftMargin = insets.left;
                            break;

                        case SUS:
                            mlp.topMargin = insets.top;
                            break;

                        case DREAPTA:
                            mlp.rightMargin = insets.right;
                            break;

                        case JOS:
                            mlp.bottomMargin = insets.bottom;
                            break;

                        default:
                            break;
                    }

                    v.setLayoutParams(mlp); // setare parametrii
                }
                // aplicare insets ca padding pentru vedere
                else if (spatiere == Spatiere.PADDING) {
                    switch (directie) {
                        case STANGA:
                            vedere.setPadding(insets.left, 0, 0, 0);

                        case SUS:
                            vedere.setPadding(0, insets.top, 0, 0);

                        case DREAPTA:
                            vedere.setPadding(0, 0, insets.right, 0);

                        case JOS:
                            vedere.setPadding(0, 0, 0, insets.bottom);

                        default:
                            break;
                    }
                }

                return WindowInsetsCompat.CONSUMED; // returnare CONSUMED astfel incat window insets sa nu treaca la vederile descendente
            });
        }
    }
*/

/*  // Versiunea 1
    public static void edgeToEdge(AppCompatActivity activitate, View vedere) {
        // se aplica edge to edge daca versiunea este >= Q (API 29)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            WindowCompat.setDecorFitsSystemWindows(activitate.getWindow(), false); // full screen
            activitate.getWindow().setStatusBarColor(Color.TRANSPARENT); // bara de status transparenta
            activitate.getWindow().setNavigationBarColor(Color.TRANSPARENT); // bara de navigare transparenta

            // manipulare suprapuneri folosind insets
            ViewCompat.setOnApplyWindowInsetsListener(vedere, (v, windowInsets) -> {
                //Insets insetsGestures = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures()); // System gesture insets
                //vedere.setPadding(0, 0, 0, insetsGestures.bottom); // aplicare insets ca padding pentru vedere

                Insets insetsBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()); // System bars insets
                // aplicare insets ca margini pentru vedere
                ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                // mlp.leftMargin = insetsBars.left;
                mlp.bottomMargin = insetsBars.bottom;
                // mlp.rightMargin = insetsBars.right;
                v.setLayoutParams(mlp);

                return WindowInsetsCompat.CONSUMED; // returnare CONSUMED astfel incat window insets sa nu treaca la vederile descendente
            });

            // modul immersive
            // WindowInsetsControllerCompat windowInsetsController = ViewCompat.getWindowInsetsController(activitate.getWindow().getDecorView());
            // if (windowInsetsController != null) {
            // windowInsetsController.hide(WindowInsetsCompat.Type.systemBars()); // ascunde system bars
            // windowInsetsController.show(WindowInsetsCompat.Type.systemBars()); // afiseaza system bars
            // }
        }
    }
*/
}