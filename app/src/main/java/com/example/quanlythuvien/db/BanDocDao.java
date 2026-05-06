package com.example.quanlythuvien.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlythuvien.model.BanDoc;

import java.util.ArrayList;
import java.util.List;

public class BanDocDao {

    private final DatabaseHelper helper;

    public BanDocDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public List<BanDoc> listAll() {
        List<BanDoc> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT bd_id, ten, sdt, diachi FROM BanDoc ORDER BY ten", null)) {
            while (c.moveToNext()) {
                list.add(readRow(c));
            }
        }
        return list;
    }

    public List<BanDoc> listWithStats() {
        List<BanDoc> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT bd.bd_id, bd.ten, bd.sdt, bd.diachi, " +
                " COALESCE((SELECT SUM(ctm.soluong) FROM ChiTietMuon ctm " +
                "  JOIN PhieuMuon pm ON pm.pm_id = ctm.pm_id " +
                "  WHERE pm.bd_id = bd.bd_id " +
                "  AND NOT EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)" +
                " ), 0) AS sach_giu " +
                "FROM BanDoc bd ORDER BY bd.ten";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                BanDoc b = new BanDoc();
                b.bdId = c.getInt(0);
                b.ten = c.getString(1);
                b.sdt = c.getString(2);
                b.diachi = c.getString(3);
                b.sachGiu = c.getInt(4);
                list.add(b);
            }
        }
        return list;
    }

    public BanDoc findById(int bdId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT bd_id, ten, sdt, diachi FROM BanDoc WHERE bd_id = ?",
                new String[]{String.valueOf(bdId)})) {
            if (c.moveToFirst()) return readRow(c);
        }
        return null;
    }

    public int countTotal() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM BanDoc", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /** Số bạn đọc đang giữ ít nhất 1 phiếu mượn chưa trả. */
    public int countDangMuon() {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT COUNT(DISTINCT pm.bd_id) FROM PhieuMuon pm " +
                "WHERE NOT EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)";
        try (Cursor c = db.rawQuery(sql, null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /** Số sách đang giữ của 1 bạn đọc (sum chi tiết các PM chưa trả). */
    public int countSachGiu(int bdId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT COALESCE(SUM(ctm.soluong),0) FROM ChiTietMuon ctm " +
                "JOIN PhieuMuon pm ON pm.pm_id = ctm.pm_id " +
                "WHERE pm.bd_id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(bdId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /** Tổng số phiếu mượn của 1 bạn đọc. */
    public int countLanMuon(int bdId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM PhieuMuon WHERE bd_id = ?",
                new String[]{String.valueOf(bdId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    public long insert(BanDoc b) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ten", b.ten);
        cv.put("sdt", b.sdt);
        cv.put("diachi", b.diachi);
        return db.insert("BanDoc", null, cv);
    }

    private BanDoc readRow(Cursor c) {
        BanDoc b = new BanDoc();
        b.bdId = c.getInt(0);
        b.ten = c.getString(1);
        b.sdt = c.getString(2);
        b.diachi = c.getString(3);
        return b;
    }
}
