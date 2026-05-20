package com.example.quanlythuvien.util;

import android.content.Context;

import com.example.quanlythuvien.LoginActivity;

/** Tiện ích check role của user hiện tại trong SharedPreferences. */
public class RoleHelper {

    public static final String ROLE_ADMIN     = "admin";
    public static final String ROLE_NHAN_VIEN = "nhanvien";

    private RoleHelper() {}

    public static String currentRole(Context ctx) {
        return ctx.getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE)
                .getString(LoginActivity.KEY_ROLE, "");
    }

    public static boolean isAdmin(Context ctx) {
        return ROLE_ADMIN.equals(currentRole(ctx));
    }

    public static boolean isNhanVien(Context ctx) {
        return ROLE_NHAN_VIEN.equals(currentRole(ctx));
    }

    /** Hiển thị tên role tiếng Việt. */
    public static String displayLabel(Context ctx) {
        return isAdmin(ctx) ? "Quản trị viên" : "Nhân viên";
    }
}
