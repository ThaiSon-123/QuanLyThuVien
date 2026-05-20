package com.example.quanlythuvien.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * DAO cho bảng CauHinh (cấu hình hệ thống, admin only).
 *
 * Pattern singleton + cache để mọi nơi đọc giá trị nhanh.
 * Gọi {@link #invalidate()} sau khi update để cache được nạp lại.
 */
public class CauHinhDao {

    // Key names — sync with seed in DatabaseHelper
    public static final String KEY_FINE_PER_DAY        = "fine_per_day";
    public static final String KEY_DEFAULT_BORROW_DAYS = "default_borrow_days";
    public static final String KEY_MAX_BOOKS_PER_SLIP  = "max_books_per_slip";
    public static final String KEY_MAX_GIA_HAN         = "max_gia_han";
    public static final String KEY_GIA_HAN_DAYS        = "gia_han_days";
    public static final String KEY_LOW_STOCK_THRESHOLD = "low_stock_threshold";
    public static final String KEY_NEAR_DUE_DAYS       = "near_due_days";

    private static CauHinhDao instance;

    public static synchronized CauHinhDao getInstance(Context ctx) {
        if (instance == null) instance = new CauHinhDao(ctx);
        return instance;
    }

    private final DatabaseHelper helper;
    private Map<String, String> cache;

    private CauHinhDao(Context ctx) {
        this.helper = DatabaseHelper.getInstance(ctx);
    }

    /** Đọc lại cache từ DB. */
    public synchronized void invalidate() {
        cache = null;
    }

    private synchronized void ensureLoaded() {
        if (cache != null) return;
        cache = new HashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT key, value FROM CauHinh", null)) {
            while (c.moveToNext()) {
                cache.put(c.getString(0), c.getString(1));
            }
        }
    }

    public String getString(String key, String defaultValue) {
        ensureLoaded();
        String v = cache.get(key);
        return v == null ? defaultValue : v;
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return Double.parseDouble(getString(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Set value và invalidate cache. */
    public void set(String key, String value) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        // INSERT OR REPLACE
        db.replace("CauHinh", null, cv);
        invalidate();
    }

    public void setInt(String key, int value) { set(key, String.valueOf(value)); }
    public void setDouble(String key, double value) { set(key, String.valueOf(value)); }

    // Convenience methods cho các key đã định nghĩa
    public double finePerDay() { return getDouble(KEY_FINE_PER_DAY, 500); }
    public int defaultBorrowDays() { return getInt(KEY_DEFAULT_BORROW_DAYS, 7); }
    public int maxBooksPerSlip() { return getInt(KEY_MAX_BOOKS_PER_SLIP, 5); }
    public int maxGiaHan() { return getInt(KEY_MAX_GIA_HAN, 2); }
    public int giaHanDays() { return getInt(KEY_GIA_HAN_DAYS, 7); }
    public int lowStockThreshold() { return getInt(KEY_LOW_STOCK_THRESHOLD, 2); }
    public int nearDueDays() { return getInt(KEY_NEAR_DUE_DAYS, 3); }
}
