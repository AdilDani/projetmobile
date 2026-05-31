package com.fleettracking.app.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Thin wrapper around SharedPreferences for the demo session state. */
public class Prefs {
    private static final String FILE = "fleet_prefs";
    public static final String KEY_LOGGED_IN = "logged_in";
    public static final String KEY_ROLE = "role";          // admin / chauffeur
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USER_ID = "user_id";    // backend id of logged-in user
    public static final String KEY_NAME = "name";          // display name of logged-in user
    public static final String KEY_NOTIFICATIONS = "notifications_enabled";

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_CHAUFFEUR = "chauffeur";

    private final SharedPreferences sp;

    public Prefs(Context ctx) {
        sp = ctx.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public void saveSession(String role, String email) {
        saveSession(role, email, "", "");
    }

    public void saveSession(String role, String email, String userId, String name) {
        sp.edit()
                .putBoolean(KEY_LOGGED_IN, true)
                .putString(KEY_ROLE, role)
                .putString(KEY_EMAIL, email)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_NAME, name)
                .apply();
    }

    public boolean isLoggedIn() { return sp.getBoolean(KEY_LOGGED_IN, false); }
    public String getRole() { return sp.getString(KEY_ROLE, ROLE_ADMIN); }
    public String getEmail() { return sp.getString(KEY_EMAIL, ""); }
    public String getUserId() { return sp.getString(KEY_USER_ID, ""); }
    public String getName() { return sp.getString(KEY_NAME, ""); }

    public boolean isNotificationsEnabled() { return sp.getBoolean(KEY_NOTIFICATIONS, true); }
    public void setNotificationsEnabled(boolean v) { sp.edit().putBoolean(KEY_NOTIFICATIONS, v).apply(); }

    public void logout() { sp.edit().clear().apply(); }
}
