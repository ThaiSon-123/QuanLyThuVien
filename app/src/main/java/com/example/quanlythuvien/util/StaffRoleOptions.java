package com.example.quanlythuvien.util;

public class StaffRoleOptions {

    public static final String MANAGER = "Quản lý";
    public static final String EMPLOYEE = "Nhân viên";
    private static final String[] LABELS = {MANAGER, EMPLOYEE};

    private StaffRoleOptions() {}

    public static String[] labels() {
        return LABELS.clone();
    }

    public static int indexOf(String roleLabel) {
        if (MANAGER.equals(roleLabel)) return 0;
        return 1;
    }
}
