package com.flagrun;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by illuminz on 02-11-2017.
 */

public class UtilityClass {
    public static void setPhone(Context context, String deviceId) {
        SharedPreferences share = context.getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        share.edit().putString(Preferences.DEVICE_ID, deviceId).apply();
    }

    public static String getPhone(Context context) {
        SharedPreferences share = context.getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        return share.getString(Preferences.DEVICE_ID, "");
    }

    public static void setTeam(Context context, String team) {
        SharedPreferences share = context.getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        share.edit().putString(Preferences.TEAM, team).apply();
    }

    public static String getTeam(Context context) {
        SharedPreferences share = context.getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        return share.getString(Preferences.TEAM, "");
    }

    public static void setUserOutStatus(Context context, int deviceId) {
        SharedPreferences share = context.getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        share.edit().putInt(Preferences.OUT_STATUS, deviceId).apply();
    }

    public static int getUserOutStatus(Context context) {
        SharedPreferences share = context.getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        return share.getInt(Preferences.OUT_STATUS, 0);
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences share = context.getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        return share.getBoolean(Preferences.IS_LOGGED_IN, false);
    }

    public static void setIsLoggedIn(Context context, boolean deviceId) {
        SharedPreferences share = context.getSharedPreferences(Preferences.SHARED_PREFERENCE_NAME, Activity.MODE_PRIVATE);
        share.edit().putBoolean(Preferences.IS_LOGGED_IN, deviceId).apply();
    }


}
