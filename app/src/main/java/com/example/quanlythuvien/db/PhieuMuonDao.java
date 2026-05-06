package com.example.quanlythuvien.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlythuvien.model.ChiTietMuon;
import com.example.quanlythuvien.model.PhieuMuon;

import java.util.ArrayList;
import java.util.List;

public class PhieuMuonDao {

    private final DatabaseHelper helper;

    public PhieuMuonDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public List<PhieuMuon> listAll() {
        List<PhieuMuon> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT pm.pm_id, pm.bd_id, pm.nv_id, pm.ngay_muon, pm.ngay_tra, " +
                "CASE WHEN EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) " +
                "     THEN 'datra' ELSE 'chuatra' END AS trangthai, " +
                "pm.songaytre, pm.tienphat, bd.ten " +
                "FROM PhieuMuon pm " +
                "LEFT JOIN BanDoc bd ON pm.bd_id = bd.bd_id " +
                "ORDER BY pm.pm_id DESC";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(readRow(c));
            }
        }
        return list;
    }

    public PhieuMuon findById(int pmId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT pm.pm_id, pm.bd_id, pm.nv_id, pm.ngay_muon, pm.ngay_tra, " +
                "CASE WHEN EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) " +
                "     THEN 'datra' ELSE 'chuatra' END AS trangthai, " +
                "pm.songaytre, pm.tienphat, bd.ten " +
                "FROM PhieuMuon pm " +
                "LEFT JOIN BanDoc bd ON pm.bd_id = bd.bd_id " +
                "WHERE pm.pm_id = ?";
        PhieuMuon pm = null;
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(pmId)})) {
            if (c.moveToFirst()) {
                pm = readRow(c);
            }
        }
        if (pm != null) {
            pm.chiTiet = listChiTiet(pmId);
        }
        return pm;
    }

    public List<ChiTietMuon> listChiTiet(int pmId) {
        List<ChiTietMuon> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT ctm.id, ctm.pm_id, ctm.sach_id, ctm.soluong, s.ten " +
                "FROM ChiTietMuon ctm " +
                "LEFT JOIN Sach s ON ctm.sach_id = s.sach_id " +
                "WHERE ctm.pm_id = ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(pmId)})) {
            while (c.moveToNext()) {
                ChiTietMuon ct = new ChiTietMuon();
                ct.id = c.getInt(0);
                ct.pmId = c.getInt(1);
                ct.sachId = c.getInt(2);
                ct.soluong = c.getInt(3);
                ct.tenSach = c.getString(4);
                list.add(ct);
            }
        }
        return list;
    }

    public long insertWithDetails(PhieuMuon pm, java.util.List<ChiTietMuon> details) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("bd_id", pm.bdId);
            if (pm.nvId > 0) cv.put("nv_id", pm.nvId);
            cv.put("ngay_muon", pm.ngayMuon);
            cv.put("ngay_tra", pm.ngayTra);
            cv.put("trangthai", "dangmuon");
            long pmId = db.insert("PhieuMuon", null, cv);
            if (pmId <= 0) return -1;

            for (ChiTietMuon ct : details) {
                ContentValues d = new ContentValues();
                d.put("pm_id", pmId);
                d.put("sach_id", ct.sachId);
                d.put("soluong", ct.soluong);
                long rid = db.insert("ChiTietMuon", null, d);
                if (rid <= 0) return -1;
            }
            db.setTransactionSuccessful();
            return pmId;
        } finally {
            db.endTransaction();
        }
    }

    public int previewNextId() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT IFNULL(MAX(pm_id),0)+1 FROM PhieuMuon", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 1;
    }

    public java.util.List<PhieuMuon> listByBanDoc(int bdId) {
        java.util.List<PhieuMuon> list = new java.util.ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT pm.pm_id, pm.bd_id, pm.nv_id, pm.ngay_muon, pm.ngay_tra, " +
                "CASE WHEN EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) " +
                "     THEN 'datra' ELSE 'chuatra' END AS trangthai, " +
                "pm.songaytre, pm.tienphat, bd.ten, " +
                "COALESCE((SELECT SUM(ctm.soluong) FROM ChiTietMuon ctm WHERE ctm.pm_id = pm.pm_id),0) AS sl " +
                "FROM PhieuMuon pm " +
                "LEFT JOIN BanDoc bd ON pm.bd_id = bd.bd_id " +
                "WHERE pm.bd_id = ? " +
                "ORDER BY pm.pm_id DESC";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(bdId)})) {
            while (c.moveToNext()) {
                PhieuMuon p = readRow(c);
                p.soQuyen = c.getInt(9);
                list.add(p);
            }
        }
        return list;
    }

    public java.util.List<PhieuMuon> listChuaTra() {
        java.util.List<PhieuMuon> list = new java.util.ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT pm.pm_id, pm.bd_id, pm.nv_id, pm.ngay_muon, pm.ngay_tra, " +
                "'chuatra' AS trangthai, pm.songaytre, pm.tienphat, bd.ten " +
                "FROM PhieuMuon pm " +
                "LEFT JOIN BanDoc bd ON pm.bd_id = bd.bd_id " +
                "WHERE NOT EXISTS (SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) " +
                "ORDER BY pm.pm_id DESC";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(readRow(c));
            }
        }
        return list;
    }

    public int count() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM PhieuMuon", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    private PhieuMuon readRow(Cursor c) {
        PhieuMuon p = new PhieuMuon();
        p.pmId = c.getInt(0);
        p.bdId = c.getInt(1);
        p.nvId = c.getInt(2);
        p.ngayMuon = c.getString(3);
        p.ngayTra = c.getString(4);
        p.trangthai = c.getString(5);
        p.songaytre = c.getInt(6);
        p.tienphat = c.getDouble(7);
        p.tenBanDoc = c.getString(8);
        return p;
    }
}
