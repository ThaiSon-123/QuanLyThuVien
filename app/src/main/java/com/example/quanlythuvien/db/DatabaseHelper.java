package com.example.quanlythuvien.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "quanlythuvien.db";
    public static final int DB_VERSION = 13;

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
        db.execSQL("DROP TABLE IF EXISTS CauHinh");
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
                "ngay_tra_goc DATE, " +
                "lan_gia_han INTEGER DEFAULT 0, " +
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

        db.execSQL("CREATE TABLE CauHinh (" +
                "key TEXT PRIMARY KEY, " +
                "value TEXT NOT NULL)");
        db.execSQL("INSERT INTO CauHinh (key, value) VALUES " +
                "('fine_per_day', '500'), " +
                "('default_borrow_days', '7'), " +
                "('max_books_per_slip', '5'), " +
                "('max_gia_han', '2'), " +
                "('gia_han_days', '7'), " +
                "('low_stock_threshold', '2'), " +
                "('near_due_days', '3')");
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
                "('Nguyễn Văn Khoa',   '0955555555', 'Cần Thơ'),   " +
                "('Hoàng Minh Anh',    '0966666666', 'Hải Phòng'), " +
                "('Vũ Thị Lan',        '0977777777', 'Nghệ An'),   " +
                "('Đặng Quốc Bảo',     '0988888888', 'Huế'),       " +
                "('Bùi Gia Hân',       '0999999999', 'Đồng Nai'),  " +
                "('Phan Nhật Nam',     '0901234567', 'Khánh Hòa')");

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

        // ── Phiếu mượn tháng 5/2026 ───────────────────────────────────────────
        // Hạn trả mặc định 7 ngày; PM12 đã gia hạn 1 lần thêm 7 ngày.
        db.execSQL("INSERT INTO PhieuMuon " +
                "(bd_id, nv_id, ngay_muon, ngay_tra, ngay_tra_goc, lan_gia_han, trangthai, songaytre, tienphat) VALUES " +
                "(1,  2, '2026-05-01', '2026-05-08', '2026-05-08', 0, 'datra',    0,    0), " +
                "(2,  2, '2026-05-02', '2026-05-09', '2026-05-09', 0, 'datra',    1, 1000), " +
                "(3,  1, '2026-05-03', '2026-05-10', '2026-05-10', 0, 'datra',    0,    0), " +
                "(4,  3, '2026-05-04', '2026-05-11', '2026-05-11', 0, 'datra',    3, 3000), " +
                "(5,  2, '2026-05-05', '2026-05-12', '2026-05-12', 0, 'datra',    0,    0), " +
                "(6,  3, '2026-05-06', '2026-05-13', '2026-05-13', 0, 'datra',    2, 2000), " +
                "(7,  1, '2026-05-08', '2026-05-15', '2026-05-15', 0, 'datra',    1, 1000), " +
                "(8,  2, '2026-05-10', '2026-05-17', '2026-05-17', 0, 'datra',    0,    0), " +
                "(9,  3, '2026-05-12', '2026-05-19', '2026-05-19', 0, 'dangmuon', 0,    0), " +
                "(10, 1, '2026-05-14', '2026-05-21', '2026-05-21', 0, 'dangmuon', 0,    0), " +
                "(1,  2, '2026-05-15', '2026-05-22', '2026-05-22', 0, 'dangmuon', 0,    0), " +
                "(6,  2, '2026-05-11', '2026-05-25', '2026-05-18', 1, 'dangmuon', 0,    0)");

        // ── Chi tiết mượn (trigger tự giảm soluong Sach) ──────────────────────
        db.execSQL("INSERT INTO ChiTietMuon (pm_id, sach_id, soluong) VALUES " +
                "(1,   1, 1), (1,   4, 1), " +
                "(2,   3, 1), (2,  15, 1), " +
                "(3,   8, 1), (3,   9, 1), " +
                "(4,   2, 1), (4,  13, 1), " +
                "(5,   6, 1), (5,  14, 1), " +
                "(6,  11, 1), (6,  16, 1), " +
                "(7,   5, 1), (7,  10, 1), " +
                "(8,   7, 1), (8,  12, 1), " +
                "(9,   1, 1), (9,   8, 1), (9,  15, 1), " +
                "(10,  4, 1), (10,  9, 1), " +
                "(11,  2, 1), (11,  6, 1), (11, 13, 1), " +
                "(12,  3, 1), (12, 11, 1), (12, 16, 1)");

        // ── Phiếu trả tháng 5/2026 (cho PM1..PM8) ─────────────────────────────
        db.execSQL("INSERT INTO PhieuTra (pm_id, ngay_tra, tienphat) VALUES " +
                "(1, '2026-05-07',    0), " +
                "(2, '2026-05-10', 1000), " +
                "(3, '2026-05-10',    0), " +
                "(4, '2026-05-14', 3000), " +
                "(5, '2026-05-12',    0), " +
                "(6, '2026-05-15', 2000), " +
                "(7, '2026-05-16', 1000), " +
                "(8, '2026-05-17',    0)");

        // ── Chi tiết trả (trigger tự tăng soluong Sach) ───────────────────────
        db.execSQL("INSERT INTO ChiTietTra (pt_id, sach_id, soluong) VALUES " +
                "(1,  1, 1), (1,   4, 1), " +
                "(2,  3, 1), (2,  15, 1), " +
                "(3,  8, 1), (3,   9, 1), " +
                "(4,  2, 1), (4,  13, 1), " +
                "(5,  6, 1), (5,  14, 1), " +
                "(6, 11, 1), (6,  16, 1), " +
                "(7,  5, 1), (7,  10, 1), " +
                "(8,  7, 1), (8,  12, 1)");
    }
}
