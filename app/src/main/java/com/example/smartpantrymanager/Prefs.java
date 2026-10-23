package com.example.smartpantrymanager;

import android.content.Context;
import android.content.SharedPreferences;

/** Reads and writes the two settings the user can change on the Settings screen. */
public final class Prefs {

    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_EXPIRY_ALERTS = "expiry_alerts";
    private static final String KEY_ALMOST_THERE = "show_almost_there";

    private Prefs() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Show the "expiring soon" banner on the pantry screen. Default: on. */
    public static boolean isExpiryAlertsOn(Context context) {
        return prefs(context).getBoolean(KEY_EXPIRY_ALERTS, true);
    }

    public static void setExpiryAlertsOn(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_EXPIRY_ALERTS, value).apply();
    }

    /** Show the optional, separate "Almost There" section. Default: off. */
    public static boolean isAlmostThereOn(Context context) {
        return prefs(context).getBoolean(KEY_ALMOST_THERE, false);
    }

    public static void setAlmostThereOn(Context context, boolean value) {
        prefs(context).edit().putBoolean(KEY_ALMOST_THERE, value).apply();
    }
}
