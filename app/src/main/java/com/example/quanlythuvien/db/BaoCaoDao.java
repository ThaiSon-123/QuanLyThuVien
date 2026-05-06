package com.example.quanlythuvien.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


public class BaoCaoDao {

    public static class TheLoaiStat {
        public int tlId;
        public String ten;
        public int luotMuon;
    }

    private final DatabaseHelper helper;

    public BaoCaoDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public int totalLuotMuonInMonth(String yyyyMM) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM PhieuMuon WHERE strftime('%Y-%m', ngay_muon) = ?",
                new String[]{yyyyMM})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }


    public List<TheLoaiStat> luotMuonByTheLoaiInMonth(String yyyyMM) {
        List<TheLoaiStat> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT tl.tl_id, tl.ten, COUNT(DISTINCT pm.pm_id) AS luot " +
                "FROM TheLoai tl " +
                "LEFT JOIN Sach s ON s.tl_id = tl.tl_id " +
                "LEFT JOIN ChiTietMuon ctm ON ctm.sach_id = s.sach_id " +
                "LEFT JOIN PhieuMuon pm ON pm.pm_id = ctm.pm_id " +
                "  AND strftime('%Y-%m', pm.ngay_muon) = ? " +
                "GROUP BY tl.tl_id, tl.ten " +
                "ORDER BY luot DESC, tl.ten";
        try (Cursor c = db.rawQuery(sql, new String[]{yyyyMM})) {
            while (c.moveToNext()) {
                TheLoaiStat s = new TheLoaiStat();
                s.tlId = c.getInt(0);
                s.ten = c.getString(1);
                s.luotMuon = c.getInt(2);
                list.add(s);
            }
        }
        return list;
    }

    public List<TheLoaiStat> topSachInMonth(String yyyyMM) {
        List<TheLoaiStat> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT s.sach_id, s.ten, COUNT(DISTINCT pm.pm_id) AS luot " +
                "FROM Sach s " +
                "JOIN ChiTietMuon ctm ON ctm.sach_id = s.sach_id " +
                "JOIN PhieuMuon pm ON pm.pm_id = ctm.pm_id " +
                "WHERE strftime('%Y-%m', pm.ngay_muon) = ? " +
                "GROUP BY s.sach_id, s.ten " +
                "ORDER BY luot DESC, s.ten " +
                "LIMIT 20";
        try (Cursor c = db.rawQuery(sql, new String[]{yyyyMM})) {
            while (c.moveToNext()) {
                TheLoaiStat s = new TheLoaiStat();
                s.tlId = c.getInt(0);
                s.ten = c.getString(1);
                s.luotMuon = c.getInt(2);
                list.add(s);
            }
        }
        return list;
    }

    public List<TheLoaiStat> topBanDocInMonth(String yyyyMM) {
        List<TheLoaiStat> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT bd.bd_id, bd.ten, COUNT(pm.pm_id) AS luot " +
                "FROM BanDoc bd " +
                "JOIN PhieuMuon pm ON pm.bd_id = bd.bd_id " +
                "WHERE strftime('%Y-%m', pm.ngay_muon) = ? " +
                "GROUP BY bd.bd_id, bd.ten " +
                "ORDER BY luot DESC, bd.ten " +
                "LIMIT 20";
        try (Cursor c = db.rawQuery(sql, new String[]{yyyyMM})) {
            while (c.moveToNext()) {
                TheLoaiStat s = new TheLoaiStat();
                s.tlId = c.getInt(0);
                s.ten = c.getString(1);
                s.luotMuon = c.getInt(2);
                list.add(s);
            }
        }
        return list;
    }

    public int countDaTraInMonth(String yyyyMM) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM PhieuMuon pm " +
                "WHERE strftime('%Y-%m', pm.ngay_muon) = ? " +
                "AND EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)";
        try (Cursor c = db.rawQuery(sql, new String[]{yyyyMM})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    public int countChuaTraInMonth(String yyyyMM) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT COUNT(*) FROM PhieuMuon pm " +
                "WHERE strftime('%Y-%m', pm.ngay_muon) = ? " +
                "AND NOT EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)";
        try (Cursor c = db.rawQuery(sql, new String[]{yyyyMM})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }
}
