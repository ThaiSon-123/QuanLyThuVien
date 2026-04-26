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
