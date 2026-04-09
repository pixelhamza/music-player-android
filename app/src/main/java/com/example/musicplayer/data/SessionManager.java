package com.example.musicplayer.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "musebox_session";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_REMEMBERED_EMAIL = "remembered_email";
    private static final String KEY_HAS_USER = "has_user";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void registerUser(String name, String email, String password, boolean rememberEmail) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_HAS_USER, true);
        editor.putBoolean(KEY_LOGGED_IN, true);
        if (rememberEmail) {
            editor.putString(KEY_REMEMBERED_EMAIL, email);
        }
        editor.apply();
    }

    public boolean login(String email, String password, boolean rememberEmail) {
        String savedEmail = preferences.getString(KEY_EMAIL, "");
        String savedPassword = preferences.getString(KEY_PASSWORD, "");
        boolean matches = email.equals(savedEmail) && password.equals(savedPassword);
        if (matches) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(KEY_LOGGED_IN, true);
            if (rememberEmail) {
                editor.putString(KEY_REMEMBERED_EMAIL, email);
            }
            editor.apply();
        }
        return matches;
    }

    public boolean hasRegisteredUser() {
        return preferences.getBoolean(KEY_HAS_USER, false);
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGGED_IN, false);
    }

    public String getDisplayName() {
        return preferences.getString(KEY_NAME, "Listener");
    }

    public String getRememberedEmail() {
        return preferences.getString(KEY_REMEMBERED_EMAIL, "");
    }

    public void logout() {
        preferences.edit().putBoolean(KEY_LOGGED_IN, false).apply();
    }
}
