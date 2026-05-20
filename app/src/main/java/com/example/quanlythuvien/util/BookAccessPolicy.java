package com.example.quanlythuvien.util;

public class BookAccessPolicy {

    private BookAccessPolicy() {}

    public static boolean canOpenBookEditScreen(String role) {
        return RoleHelper.ROLE_ADMIN.equals(role);
    }

    public static String deniedMessage() {
        return "Bạn không có quyền chỉnh sửa sách";
    }
}
