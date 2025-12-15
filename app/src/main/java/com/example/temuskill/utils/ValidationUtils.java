package com.example.temuskill.utils;

import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.length() >= 10 && phone.matches("\\d+");
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }
}