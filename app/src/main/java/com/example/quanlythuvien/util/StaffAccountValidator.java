package com.example.quanlythuvien.util;

public class StaffAccountValidator {

    private StaffAccountValidator() {}

    public static boolean isValidForCreate(String username, String email, String password) {
        return hasText(username) && hasText(email) && hasText(password);
    }

    public static boolean isValidForUpdate(String username, String email, String password) {
        return hasText(username) && hasText(email);
    }

    public static boolean shouldUpdatePassword(String password) {
        return hasText(password);
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
