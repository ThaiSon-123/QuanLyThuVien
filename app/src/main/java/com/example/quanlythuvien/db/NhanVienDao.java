package com.example.quanlythuvien.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlythuvien.model.NhanVien;

import java.util.ArrayList;
import java.util.List;

public class NhanVienDao {

    private final DatabaseHelper helper;

    public NhanVienDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public List<NhanVien> listAll() {
        List<NhanVien> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT nv_id, ten, sdt, email, diachi, chucvu, ngay_vao_lam, trangthai, user_id " +
                        "FROM NhanVien ORDER BY nv_id DESC", null)) {
            while (c.moveToNext()) list.add(readRow(c));
        }
        return list;
    }

    public NhanVien findById(int nvId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT nv_id, ten, sdt, email, diachi, chucvu, ngay_vao_lam, trangthai, user_id " +
                        "FROM NhanVien WHERE nv_id = ?",
                new String[]{String.valueOf(nvId)})) {
            if (c.moveToFirst()) return readRow(c);
        }
        return null;
    }

    /**
     * Tạo Users + NhanVien trong 1 transaction.
     * @param username thường là email; required nếu password != null.
     * @param password optional; nếu null thì không tạo Users.
     * @return nv_id mới, -1 nếu fail.
     */
    public long insertWithUser(NhanVien nv, String username, String password) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            int userId = 0;
            if (username != null && !username.isEmpty()
                    && password != null && !password.isEmpty()) {
                ContentValues u = new ContentValues();
                u.put("username", username);
                u.put("password", password);
                u.put("role", inferRole(nv.chucvu));
                u.put("status", 1);
                long uid = db.insert("Users", null, u);
                if (uid <= 0) return -1;
                userId = (int) uid;
            }

            ContentValues cv = new ContentValues();
            cv.put("ten", nv.ten);
            cv.put("sdt", nv.sdt);
            cv.put("email", nv.email);
            cv.put("diachi", nv.diachi);
            cv.put("chucvu", nv.chucvu);
            cv.put("ngay_vao_lam", nv.ngayVaoLam);
            cv.put("trangthai", nv.trangthai == null ? "lamviec" : nv.trangthai);
            if (userId > 0) cv.put("user_id", userId);

            long nvId = db.insert("NhanVien", null, cv);
            if (nvId <= 0) return -1;

            db.setTransactionSuccessful();
            return nvId;
        } finally {
            db.endTransaction();
        }
    }

    public int update(NhanVien nv) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ten", nv.ten);
        cv.put("sdt", nv.sdt);
        cv.put("email", nv.email);
        cv.put("diachi", nv.diachi);
        cv.put("chucvu", nv.chucvu);
        cv.put("ngay_vao_lam", nv.ngayVaoLam);
        if (nv.trangthai != null) cv.put("trangthai", nv.trangthai);
        return db.update("NhanVien", cv, "nv_id = ?",
                new String[]{String.valueOf(nv.nvId)});
    }

    public int delete(int nvId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("NhanVien", "nv_id = ?", new String[]{String.valueOf(nvId)});
    }

    public static String inferRole(String chucvu) {
        if (chucvu != null && chucvu.toLowerCase().contains("quản")) return "admin";
        return "nhanvien";
    }

    private NhanVien readRow(Cursor c) {
        NhanVien n = new NhanVien();
        n.nvId = c.getInt(0);
        n.ten = c.getString(1);
        n.sdt = c.getString(2);
        n.email = c.getString(3);
        n.diachi = c.getString(4);
        n.chucvu = c.getString(5);
        n.ngayVaoLam = c.getString(6);
        n.trangthai = c.getString(7);
        n.userId = c.getInt(8);
        return n;
    }
}
