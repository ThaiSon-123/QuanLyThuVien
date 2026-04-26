package com.example.quanlythuvien.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserDao {

    private final DatabaseHelper helper;

    public UserDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public static class UserInfo {
        public int userId;
        public String username;
        public String role;
        public int status;
    }

    /** Lấy tên nhân viên gắn với username (qua bảng NhanVien); null nếu không có. */
    public String findStaffName(String username) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT nv.ten FROM NhanVien nv " +
                "JOIN Users u ON u.user_id = nv.user_id " +
                "WHERE u.username = ? LIMIT 1";
        try (Cursor c = db.rawQuery(sql, new String[]{username})) {
            if (c.moveToFirst()) return c.getString(0);
        }
        return null;
    }

    public UserInfo login(String username, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT user_id, username, role, status FROM Users WHERE username = ? AND password = ? AND status = 1",
                new String[]{username, password})) {
            if (c.moveToFirst()) {
                UserInfo u = new UserInfo();
                u.userId = c.getInt(0);
                u.username = c.getString(1);
                u.role = c.getString(2);
                u.status = c.getInt(3);
                return u;
            }
            return null;
        }
    }
}
