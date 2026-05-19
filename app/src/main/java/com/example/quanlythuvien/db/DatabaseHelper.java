package com.example.quanlythuvien.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "quanlythuvien.db";
    public static final int DB_VERSION = 9;

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
        // ── Users ──────────────────────────────────────────────────────────────
        db.execSQL("INSERT INTO Users (username, password, role) VALUES " +
                "('admin', '123', 'admin'), " +
                "('nv1',   '123', 'nhanvien'), " +
                "('nv2',   '123', 'nhanvien')");

        // ── Nhân viên ──────────────────────────────────────────────────────────
        db.execSQL("INSERT INTO NhanVien (ten, sdt, email, diachi, chucvu, ngay_vao_lam, user_id) VALUES " +
                "('Võ Thái Sơn',       '0123457678', 'thaisonpro328@gmail.com',   'Hà Nội',  'Quản lý',   '2025-01-15', 1), " +
                "('Đinh Sỹ Vinh',      '0123456678', 'thaisonpro328@gmail.com',   'Đà Nẵng', 'Nhân viên', '2025-06-01', 2), " +
                "('Nguyễn Trọng Hiếu', '0356602342', 'tronghieu160618@gmail.com', 'TP.HCM',  'Nhân viên', '2026-01-20', 3)");

        // ── Bạn đọc ────────────────────────────────────────────────────────────
        db.execSQL("INSERT INTO BanDoc (ten, sdt, diachi) VALUES " +
                "('Lê Văn Long',       '0911111111', 'TP.HCM'),     " +
                "('Phạm Tấn Lộc',      '0922222222', 'Bình Dương'), " +
                "('Nguyễn Trọng Hiếu', '0933333333', 'Hà Nội'),    " +
                "('Trần Thị Mai',      '0944444444', 'Đà Nẵng'),   " +
                "('Nguyễn Văn Khoa',   '0955555555', 'Cần Thơ')");

        // ── Thể loại ───────────────────────────────────────────────────────────
        db.execSQL("INSERT INTO TheLoai (ten) VALUES " +
                "('Văn Học'),      " +
                "('Khoa Học'),     " +
                "('Công Nghệ'),    " +
                "('Lịch Sử'),      " +
                "('Kinh Tế'),      " +
                "('Kỹ Năng Sống')");

        // ── Sách (soluong = số lượng TRƯỚC KHI trigger giảm) ──────────────────
        db.execSQL("INSERT INTO Sach (ten, tacgia, nxb, namxb, soluong, tl_id) VALUES " +
                // --- Văn Học ---
                "('Ngữ Văn 12 - Tập 1',         'Bộ GD&ĐT',        'NXB Giáo Dục',    2024, 10, 1), " +
                "('Ngữ Văn 12 - Tập 2',         'Bộ GD&ĐT',        'NXB Giáo Dục',    2024, 10, 1), " +
                "('Dế Mèn Phiêu Lưu Ký',        'Tô Hoài',         'NXB Kim Đồng',    2022,  5, 1), " +
                "('Nhà Giả Kim',                 'Paulo Coelho',    'NXB Hội Nhà Văn', 2023,  6, 1), " +
                "('Tắt Đèn',                     'Ngô Tất Tố',      'NXB Văn Học',     2022,  5, 1), " +
                // --- Khoa Học ---
                "('Hóa Học Đại Cương',           'Nguyễn Đình Huề', 'NXB ĐH Quốc Gia', 2023, 4, 2), " +
                "('Vật Lý Đại Cương - Tập 1',   'Lương Duyên Bình','NXB Giáo Dục',    2023,  4, 2), " +
                // --- Công Nghệ ---
                "('Lập Trình Java Cơ Bản',       'Trần Văn Nam',    'NXB KHKT',        2024,  6, 3), " +
                "('Lập Trình Android',           'Nguyễn Anh Tuấn', 'NXB KHKT',        2025,  5, 3), " +
                "('Cơ Sở Dữ Liệu',              'Hồ Thuần',        'NXB Giáo Dục',    2023,  4, 3), " +
                // --- Lịch Sử ---
                "('Đại Việt Sử Ký Toàn Thư',    'Ngô Sĩ Liên',    'NXB KHXH',        2022,  3, 4), " +
                "('Lịch Sử Việt Nam - Tập 1',   'Ngô Văn Hòa',    'NXB KHXH',        2023,  4, 4), " +
                // --- Kinh Tế ---
                "('Nghĩ Giàu Làm Giàu',          'Napoleon Hill',   'NXB Lao Động',    2024,  4, 5), " +
                "('Kinh Tế Học Vi Mô',           'N. Gregory',      'NXB Kinh Tế',     2023,  5, 5), " +
                // --- Kỹ Năng Sống ---
                "('Đắc Nhân Tâm',                'Dale Carnegie',   'NXB Tổng Hợp',    2023,  5, 6), " +
                "('7 Thói Quen Hiệu Quả',        'Stephen Covey',   'NXB Tổng Hợp',    2024,  4, 6)");

        // ── Phiếu mượn ─────────────────────────────────────────────────────────
        // PM1 – Lê Văn Long – đã trả
        // PM2 – Phạm Tấn Lộc – đã trả
        // PM3 – Nguyễn Trọng Hiếu – đã trả
        // PM4 – Trần Thị Mai – đang mượn
        // PM5 – Lê Văn Long – đang mượn
        // PM6 – Nguyễn Văn Khoa – đang mượn
        db.execSQL("INSERT INTO PhieuMuon (bd_id, nv_id, ngay_muon, ngay_tra, trangthai) VALUES " +
                "(1, 2, '2026-01-10', '2026-01-24', 'datra'),    " +
                "(2, 2, '2026-02-05', '2026-02-19', 'datra'),    " +
                "(3, 1, '2026-03-10', '2026-03-24', 'datra'),    " +
                "(4, 3, '2026-04-01', '2026-04-20', 'dangmuon'), " +
                "(1, 1, '2026-04-15', '2026-05-05', 'dangmuon'), " +
                "(5, 2, '2026-04-28', '2026-05-12', 'dangmuon')");

        // ── Chi tiết mượn (trigger tự giảm soluong Sach) ──────────────────────
        db.execSQL("INSERT INTO ChiTietMuon (pm_id, sach_id, soluong) VALUES " +
                "(1,  1, 1), (1,  4, 1), " +   // PM1 mượn: Ngữ Văn 12-T1, Nhà Giả Kim
                "(2,  3, 1), (2, 15, 1), " +   // PM2 mượn: Dế Mèn, Đắc Nhân Tâm
                "(3,  8, 1), (3,  9, 1), " +   // PM3 mượn: Java, Android
                "(4,  2, 1), (4, 13, 1), " +   // PM4 mượn: Ngữ Văn 12-T2, Nghĩ Giàu
                "(5,  6, 1), (5, 14, 1), " +   // PM5 mượn: Hóa Đại Cương, Kinh Tế Vi Mô
                "(6, 11, 1), (6, 16, 1)");     // PM6 mượn: Đại Việt Sử Ký, 7 Thói Quen

        // ── Phiếu trả (cho PM1, PM2, PM3) ─────────────────────────────────────
        db.execSQL("INSERT INTO PhieuTra (pm_id, ngay_tra, tienphat) VALUES " +
                "(1, '2026-01-22', 0), " +
                "(2, '2026-02-17', 0), " +
                "(3, '2026-03-23', 0)");

        // ── Chi tiết trả (trigger tự tăng soluong Sach) ───────────────────────
        db.execSQL("INSERT INTO ChiTietTra (pt_id, sach_id, soluong) VALUES " +
                "(1,  1, 1), (1,  4, 1), " +   // PT1 trả: Ngữ Văn 12-T1, Nhà Giả Kim
                "(2,  3, 1), (2, 15, 1), " +   // PT2 trả: Dế Mèn, Đắc Nhân Tâm
                "(3,  8, 1), (3,  9, 1)");     // PT3 trả: Java, Android
    }
}
