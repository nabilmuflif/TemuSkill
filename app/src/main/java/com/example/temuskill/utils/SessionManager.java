package com.example.temuskill.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "TemuSkillSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String userId, String name, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply(); // Wajib apply agar tersimpan
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId() {
        try {
            // Coba ambil sebagai String (Format Baru Firebase)
            return pref.getString(KEY_USER_ID, null);
        } catch (ClassCastException e) {
            // JIKA ERROR (Karena data lama di HP masih Integer SQLite)
            // Otomatis reset sesi biar tidak force close
            logout();
            return null;
        }
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "");
    }

    public boolean isPenyediaJasa() {
        return "penyedia_jasa".equals(getUserRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}