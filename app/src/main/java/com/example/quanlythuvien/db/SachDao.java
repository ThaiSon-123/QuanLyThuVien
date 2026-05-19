package com.example.quanlythuvien.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlythuvien.model.Sach;

import java.util.ArrayList;
import java.util.List;

public class SachDao {

    private static final String SELECT_COLS =
            "s.sach_id, s.ten, s.tacgia, s.nxb, s.namxb, s.soluong, s.trangthai, s.tl_id, tl.ten, s.cover_uri";

    private final DatabaseHelper helper;

    public SachDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public List<Sach> listAll() {
        return query(null, null);
    }

    public List<Sach> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listAll();
        }
        String like = "%" + keyword.trim() + "%";
        return query("s.ten LIKE ? OR s.tacgia LIKE ?", new String[]{like, like});
    }

    public List<Sach> listByTheLoai(int tlId) {
        return query("s.tl_id = ?", new String[]{String.valueOf(tlId)});
    }

    public List<Sach> filter(String keyword, String status, int tlId) {
        List<String> conds = new ArrayList<>();
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            conds.add("(s.ten LIKE ? OR s.tacgia LIKE ?)");
            args.add(like);
            args.add(like);
        }
        if ("con".equals(status)) {
            conds.add("s.soluong > 0");
        } else if ("het".equals(status)) {
            conds.add("s.soluong <= 0");
        }
        if (tlId > 0) {
            conds.add("s.tl_id = ?");
            args.add(String.valueOf(tlId));
        }

        String where = null;
        if (!conds.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < conds.size(); i++) {
                if (i > 0) sb.append(" AND ");
                sb.append(conds.get(i));
            }
            where = sb.toString();
        }
        return query(where, args.toArray(new String[0]));
    }

    public Sach findById(int sachId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT " + SELECT_COLS + " FROM Sach s "
                + "LEFT JOIN TheLoai tl ON s.tl_id = tl.tl_id WHERE s.sach_id = ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(sachId)})) {
            if (c.moveToFirst()) {
                return readRow(c);
            }
        }
        return null;
    }

    public long insert(Sach s) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ten", s.ten);
        cv.put("tacgia", s.tacgia);
        cv.put("nxb", s.nxb);
        if (s.namxb > 0) cv.put("namxb", s.namxb);
        cv.put("soluong", s.soluong);
        cv.put("trangthai", s.soluong > 0 ? "con" : "het");
        if (s.tlId > 0) cv.put("tl_id", s.tlId);
        if (s.coverUri != null) cv.put("cover_uri", s.coverUri);
        return db.insert("Sach", null, cv);
    }

    public int update(Sach s) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ten", s.ten);
        cv.put("tacgia", s.tacgia);
        cv.put("nxb", s.nxb);
        cv.put("namxb", s.namxb);
        cv.put("soluong", s.soluong);
        cv.put("trangthai", s.soluong > 0 ? "con" : "het");
        if (s.tlId > 0) {
            cv.put("tl_id", s.tlId);
        } else {
            cv.putNull("tl_id");
        }
        if (s.coverUri != null) {
            cv.put("cover_uri", s.coverUri);
        } else {
            cv.putNull("cover_uri");
        }
        return db.update("Sach", cv, "sach_id = ?",
                new String[]{String.valueOf(s.sachId)});
    }

    public int delete(int sachId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete("Sach", "sach_id = ?", new String[]{String.valueOf(sachId)});
    }

    public int count() {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM Sach", null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
            return 0;
        }
    }

    private List<Sach> query(String where, String[] args) {
        List<Sach> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT " + SELECT_COLS + " FROM Sach s "
                + "LEFT JOIN TheLoai tl ON s.tl_id = tl.tl_id";
        if (where != null) {
            sql += " WHERE " + where;
        }
        sql += " ORDER BY s.sach_id DESC";
        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                list.add(readRow(c));
            }
        }
        return list;
    }

    private Sach readRow(Cursor c) {
        Sach s = new Sach();
        s.sachId = c.getInt(0);
        s.ten = c.getString(1);
        s.tacgia = c.getString(2);
        s.nxb = c.getString(3);
        s.namxb = c.getInt(4);
        s.soluong = c.getInt(5);
        s.trangthai = c.getString(6);
        s.tlId = c.getInt(7);
        s.tenTheLoai = c.getString(8);
        s.coverUri = c.getString(9);
        return s;
    }
}
