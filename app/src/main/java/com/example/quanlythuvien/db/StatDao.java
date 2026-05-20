package com.example.quanlythuvien.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class StatDao {

    private final DatabaseHelper helper;

    public StatDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public int countBorrow() {
        return countFromTable("PhieuMuon");
    }

    public int countReturn() {
        return countFromTable("PhieuTra");
    }

    /** Số phiếu mượn chưa trả + đã quá hạn (ngày_tra < today). */
    public int countOverdue() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault()).format(new java.util.Date());
        String sql = "SELECT COUNT(*) FROM PhieuMuon pm " +
                "WHERE NOT EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) " +
                "AND pm.ngay_tra < ?";
        try (Cursor c = db.rawQuery(sql, new String[]{today})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /** Số phiếu mượn chưa trả, sắp đến hạn trong vòng N ngày (today ≤ ngày_tra ≤ today+N). */
    public int countNearDue(int nearDays) {
        SQLiteDatabase db = helper.getReadableDatabase();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd",
                java.util.Locale.getDefault());
        String today = fmt.format(cal.getTime());
        cal.add(java.util.Calendar.DAY_OF_MONTH, nearDays);
        String limit = fmt.format(cal.getTime());
        String sql = "SELECT COUNT(*) FROM PhieuMuon pm " +
                "WHERE NOT EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) " +
                "AND pm.ngay_tra >= ? AND pm.ngay_tra <= ?";
        try (Cursor c = db.rawQuery(sql, new String[]{today, limit})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /** Số đầu sách có tồn kho ≤ threshold. */
    public int countLowStock(int threshold) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM Sach WHERE soluong <= ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(threshold)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    private int countFromTable(String table) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + table, null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        }
    }
}
