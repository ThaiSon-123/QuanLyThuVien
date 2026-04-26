package com.example.quanlythuvien.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "quanlythuvien.db";
    public static final int DB_VERSION = 6;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        createTriggers(db);
        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TRIGGER IF EXISTS trg_muon_sach");
        db.execSQL("DROP TRIGGER IF EXISTS trg_tra_sach");
        db.execSQL("DROP TABLE IF EXISTS ChiTietTra");
        db.execSQL("DROP TABLE IF EXISTS PhieuTra");
        db.execSQL("DROP TABLE IF EXISTS ChiTietMuon");
        db.execSQL("DROP TABLE IF EXISTS PhieuMuon");
        db.execSQL("DROP TABLE IF EXISTS Sach");
        db.execSQL("DROP TABLE IF EXISTS TheLoai");
        db.execSQL("DROP TABLE IF EXISTS BanDoc");
        db.execSQL("DROP TABLE IF EXISTS NhanVien");
        db.execSQL("DROP TABLE IF EXISTS Users");
        onCreate(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Users (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "role TEXT CHECK(role IN ('admin','nhanvien')) NOT NULL, " +
                "status INTEGER DEFAULT 1)");

        db.execSQL("CREATE TABLE NhanVien (" +
                "nv_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ten TEXT NOT NULL, " +
                "sdt TEXT, " +
                "email TEXT, " +
                "diachi TEXT, " +
                "chucvu TEXT, " +
                "ngay_vao_lam TEXT, " +
                "trangthai TEXT DEFAULT 'lamviec', " +
                "user_id INTEGER, " +
                "FOREIGN KEY (user_id) REFERENCES Users(user_id))");

        db.execSQL("CREATE TABLE BanDoc (" +
                "bd_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ten TEXT NOT NULL, " +
                "sdt TEXT, " +
                "diachi TEXT)");

        db.execSQL("CREATE TABLE TheLoai (" +
                "tl_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ten TEXT NOT NULL, " +
                "icon_uri TEXT)");

        db.execSQL("CREATE TABLE Sach (" +
                "sach_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ten TEXT NOT NULL, " +
                "tacgia TEXT, " +
                "nxb TEXT, " +
                "namxb INTEGER, " +
                "soluong INTEGER DEFAULT 0, " +
                "trangthai TEXT DEFAULT 'con', " +
                "tl_id INTEGER, " +
                "cover_uri TEXT, " +
                "FOREIGN KEY (tl_id) REFERENCES TheLoai(tl_id))");

        db.execSQL("CREATE TABLE PhieuMuon (" +
                "pm_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "bd_id INTEGER, " +
                "nv_id INTEGER, " +
                "ngay_muon DATE, " +
                "ngay_tra DATE, " +
                "trangthai TEXT DEFAULT 'dangmuon', " +
                "songaytre INTEGER DEFAULT 0, " +
                "tienphat REAL DEFAULT 0, " +
                "FOREIGN KEY (bd_id) REFERENCES BanDoc(bd_id), " +
                "FOREIGN KEY (nv_id) REFERENCES NhanVien(nv_id))");

        db.execSQL("CREATE TABLE ChiTietMuon (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pm_id INTEGER, " +
                "sach_id INTEGER, " +
                "soluong INTEGER, " +
                "FOREIGN KEY (pm_id) REFERENCES PhieuMuon(pm_id), " +
                "FOREIGN KEY (sach_id) REFERENCES Sach(sach_id))");

        db.execSQL("CREATE TABLE PhieuTra (" +
                "pt_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pm_id INTEGER, " +
                "ngay_tra DATE, " +
                "tienphat REAL DEFAULT 0, " +
                "FOREIGN KEY (pm_id) REFERENCES PhieuMuon(pm_id))");

        db.execSQL("CREATE TABLE ChiTietTra (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pt_id INTEGER, " +
                "sach_id INTEGER, " +
                "soluong INTEGER, " +
                "FOREIGN KEY (pt_id) REFERENCES PhieuTra(pt_id), " +
                "FOREIGN KEY (sach_id) REFERENCES Sach(sach_id))");
    }

    private void createTriggers(SQLiteDatabase db) {
        db.execSQL("CREATE TRIGGER trg_muon_sach " +
                "AFTER INSERT ON ChiTietMuon " +
                "BEGIN " +
                "  UPDATE Sach " +
                "    SET soluong = soluong - NEW.soluong, " +
                "        trangthai = CASE WHEN (soluong - NEW.soluong) <= 0 THEN 'het' ELSE trangthai END " +
                "  WHERE sach_id = NEW.sach_id; " +
                "END;");

        db.execSQL("CREATE TRIGGER trg_tra_sach " +
                "AFTER INSERT ON ChiTietTra " +
                "BEGIN " +
                "  UPDATE Sach " +
                "    SET soluong = soluong + NEW.soluong, " +
                "        trangthai = 'con' " +
                "  WHERE sach_id = NEW.sach_id; " +
                "END;");
    }

    private void seedData(SQLiteDatabase db) {
        db.execSQL("INSERT INTO Users (username, password, role) VALUES " +
                "('admin', '123', 'admin'), " +
                "('nv1', '123', 'nhanvien'), " +
                "('nv2', '123', 'nhanvien')");

        db.execSQL("INSERT INTO NhanVien (ten, sdt, email, diachi, chucvu, ngay_vao_lam, user_id) VALUES " +
                "('Võ Thái Sơn', '0123457678', 'son@thuvien.com', 'HÀ NỘI', 'Quản lý', '2025-01-15', 1), " +
                "('Đinh Sỹ Vinh', '0123456678', 'vinh@thuvien.com', 'ĐÀ NẴNG', 'Nhân viên', '2025-06-01', 2), " +
                "('Nguyễn Trọng Hiếu', '0356602342', 'Hieu@thuvien.com', 'TP.HCM', 'Nhân viên', '2026-01-20', 3)");

        db.execSQL("INSERT INTO BanDoc (ten, sdt, diachi) VALUES " +
                "('Lê Văn Long', '0911111111', 'TPHCM'), " +
                "('Phạm Tấn Lộc', '0922222222', 'Bình Dương'), " +
                "('Nguyễn Trọng Hiếu', '0933333333', 'Hà Nội')");

        db.execSQL("INSERT INTO TheLoai (ten) VALUES " +
                "('Văn Học'), " +
                "('Hóa Học'), " +
                "('Công Nghệ'), " +
                "('Lịch Sử')");

        db.execSQL("INSERT INTO Sach (ten, tacgia, soluong, tl_id) VALUES " +
                "('Sách ngữ văn lớp 6 tập 1', 'Bộ GD&ĐT', 10, 1), " +
                "('Sách ngữ văn lớp 6 tập 2', 'Bộ GD&ĐT', 10, 1), " +
                "('Dế Mèn phiêu lưu ký', 'Tô Hoài', 3, 1), " +
                "('Hóa học đại cương', 'Nguyễn Văn A', 4, 2), " +
                "('Java cơ bản', 'ABC', 5, 3), " +
                "('Đại Việt sử ký', 'Lê Văn Hưu', 2, 4)");

        // Seed phiếu mượn / trả mẫu
        db.execSQL("INSERT INTO PhieuMuon (bd_id, nv_id, ngay_muon, ngay_tra, trangthai) VALUES " +
                "(3, 1, '2021-03-12', '2021-03-19', 'dangmuon'), " +
                "(3, 1, '2021-03-12', '2021-03-19', 'dangmuon'), " +
                "(3, 1, '2021-03-12', '2021-03-19', 'dangmuon')");

        db.execSQL("INSERT INTO ChiTietMuon (pm_id, sach_id, soluong) VALUES " +
                "(1, 1, 1), (1, 2, 1), " +
                "(2, 3, 1), " +
                "(3, 5, 2)");

        db.execSQL("INSERT INTO PhieuTra (pm_id, ngay_tra, tienphat) VALUES " +
                "(1, '2021-03-20', 0), " +
                "(2, '2021-03-20', 0)");

        db.execSQL("INSERT INTO ChiTietTra (pt_id, sach_id, soluong) VALUES " +
                "(1, 1, 1), (1, 2, 1), " +
                "(2, 3, 1)");
    }
}
