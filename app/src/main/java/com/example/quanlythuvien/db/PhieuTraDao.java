package com.example.quanlythuvien.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlythuvien.model.ChiTietMuon;
import com.example.quanlythuvien.model.PhieuTra;

import java.util.ArrayList;
import java.util.List;

public class PhieuTraDao {

    private final DatabaseHelper helper;

    public PhieuTraDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public List<PhieuTra> listAll() {
        List<PhieuTra> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT pt.pt_id, pt.pm_id, pt.ngay_tra, pt.tienphat, " +
                "bd.ten, pm.ngay_muon " +
                "FROM PhieuTra pt " +
                "LEFT JOIN PhieuMuon pm ON pt.pm_id = pm.pm_id " +
                "LEFT JOIN BanDoc bd ON pm.bd_id = bd.bd_id " +
                "ORDER BY pt.pt_id DESC";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(readRow(c));
            }
        }
        return list;
    }

    public PhieuTra findById(int ptId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT pt.pt_id, pt.pm_id, pt.ngay_tra, pt.tienphat, " +
                "bd.ten, pm.ngay_muon " +
                "FROM PhieuTra pt " +
                "LEFT JOIN PhieuMuon pm ON pt.pm_id = pm.pm_id " +
                "LEFT JOIN BanDoc bd ON pm.bd_id = bd.bd_id " +
                "WHERE pt.pt_id = ?";
        PhieuTra pt = null;
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(ptId)})) {
            if (c.moveToFirst()) {
                pt = readRow(c);
            }
        }
        if (pt != null) {
            pt.chiTiet = listChiTiet(ptId);
        }
        return pt;
    }

    public List<ChiTietMuon> listChiTiet(int ptId) {
        List<ChiTietMuon> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT ctt.id, ctt.pt_id, ctt.sach_id, ctt.soluong, s.ten " +
                "FROM ChiTietTra ctt " +
                "LEFT JOIN Sach s ON ctt.sach_id = s.sach_id " +
                "WHERE ctt.pt_id = ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(ptId)})) {
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

    /** Trả pt_id của phiếu trả đầu tiên gắn với pm_id; 0 nếu chưa có. */
    public int findIdByPmId(int pmId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT pt_id FROM PhieuTra WHERE pm_id = ? ORDER BY pt_id LIMIT 1",
                new String[]{String.valueOf(pmId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    /**
     * Insert PhieuTra + ChiTietTra. Trigger trg_tra_sach tự cộng Sach.soluong.
     * @return pt_id mới, hoặc -1 nếu fail.
     */
    public long insertWithDetails(int pmId, String ngayTra, java.util.List<ChiTietMuon> details) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put("pm_id", pmId);
            cv.put("ngay_tra", ngayTra);
            cv.put("tienphat", 0);
            long ptId = db.insert("PhieuTra", null, cv);
            if (ptId <= 0) return -1;

            for (ChiTietMuon ct : details) {
                ContentValues d = new ContentValues();
                d.put("pt_id", ptId);
                d.put("sach_id", ct.sachId);
                d.put("soluong", ct.soluong);
                long rid = db.insert("ChiTietTra", null, d);
                if (rid <= 0) return -1;
            }
            db.setTransactionSuccessful();
            return ptId;
        } finally {
            db.endTransaction();
        }
    }

    public int count() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM PhieuTra", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }

    private PhieuTra readRow(Cursor c) {
        PhieuTra p = new PhieuTra();
        p.ptId = c.getInt(0);
        p.pmId = c.getInt(1);
        p.ngayTra = c.getString(2);
        p.tienphat = c.getDouble(3);
        p.tenBanDoc = c.getString(4);
        p.ngayMuon = c.getString(5);
        return p;
    }
}
