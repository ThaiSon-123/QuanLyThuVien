package com.example.quanlythuvien.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.quanlythuvien.model.TheLoai;

import java.util.ArrayList;
import java.util.List;

public class TheLoaiDao {

    private final DatabaseHelper helper;

    public TheLoaiDao(Context context) {
        this.helper = DatabaseHelper.getInstance(context);
    }

    public List<TheLoai> listAll() {
        List<TheLoai> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT tl_id, ten, icon_uri FROM TheLoai ORDER BY ten", null)) {
            while (c.moveToNext()) {
                list.add(new TheLoai(c.getInt(0), c.getString(1), c.getString(2)));
            }
        }
        return list;
    }


    public List<TheLoai> listWithCount() {
        List<TheLoai> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT tl.tl_id, tl.ten, tl.icon_uri, "
                + "(SELECT COUNT(*) FROM Sach s WHERE s.tl_id = tl.tl_id) AS cnt "
                + "FROM TheLoai tl ORDER BY tl.ten";
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                TheLoai t = new TheLoai(c.getInt(0), c.getString(1), c.getString(2));
                t.bookCount = c.getInt(3);
                list.add(t);
            }
        }
        return list;
    }

    public TheLoai findById(int tlId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT tl.tl_id, tl.ten, tl.icon_uri, "
                + "(SELECT COUNT(*) FROM Sach s WHERE s.tl_id = tl.tl_id) AS cnt "
                + "FROM TheLoai tl WHERE tl.tl_id = ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tlId)})) {
            if (c.moveToFirst()) {
                TheLoai t = new TheLoai(c.getInt(0), c.getString(1), c.getString(2));
                t.bookCount = c.getInt(3);
                return t;
            }
        }
        return null;
    }

    public long insert(TheLoai t) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ten", t.ten);
        if (t.iconUri != null) cv.put("icon_uri", t.iconUri);
        return db.insert("TheLoai", null, cv);
    }

    public int update(TheLoai t) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("ten", t.ten);
        if (t.iconUri != null) {
            cv.put("icon_uri", t.iconUri);
        } else {
            cv.putNull("icon_uri");
        }
        return db.update("TheLoai", cv, "tl_id = ?",
                new String[]{String.valueOf(t.tlId)});
    }

    public int delete(int tlId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        // Gỡ FK trước: set tl_id = NULL cho tất cả sách thuộc thể loại này
        // (tránh lỗi FOREIGN KEY constraint vì FK constraints đang bật)
        ContentValues cv = new ContentValues();
        cv.putNull("tl_id");
        db.update("Sach", cv, "tl_id = ?", new String[]{String.valueOf(tlId)});
        // Xóa thể loại
        return db.delete("TheLoai", "tl_id = ?", new String[]{String.valueOf(tlId)});
    }

    public int countBooks(int tlId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM Sach WHERE tl_id = ?",
                new String[]{String.valueOf(tlId)})) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return 0;
    }
}
