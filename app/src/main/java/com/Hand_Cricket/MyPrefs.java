package com.Hand_Cricket;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;


public class MyPrefs {

    private static final String SOUND_PREF = "sound_pref";
    private static final String VIBRATION_PREF = "vibration_pref";

    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences("SharedPrefsKey", MODE_PRIVATE);
    }

    public static boolean getSoundPref(Context context) {

        return getPrefs(context).getBoolean(SOUND_PREF, true);
    }

    public static boolean getVibrationPref(Context context) {

        return getPrefs(context).getBoolean(VIBRATION_PREF, true);
    }

    public static void setSoundPref(Context context, boolean value) {

        getPrefs(context).edit().putBoolean(SOUND_PREF, value).apply();
    }

    public static void setVibrationPref(Context context, boolean value) {

        getPrefs(context).edit().putBoolean(VIBRATION_PREF, value).apply();
    }
}