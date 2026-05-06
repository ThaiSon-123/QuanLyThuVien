# 01 — Cơ sở dữ liệu (Database)

> **File chính:** `app/src/main/java/com/example/quanlythuvien/db/DatabaseHelper.java`

---

## Giải thích đơn giản (nói với thầy)

App dùng **SQLite** — một loại cơ sở dữ liệu được cài sẵn trên mọi điện thoại Android, không cần kết nối internet, lưu ngay trong bộ nhớ điện thoại. Lớp `DatabaseHelper` là "cầu nối" giữa app và file database đó — nó tạo bảng, tạo dữ liệu mẫu, và nâng cấp khi cần thiết.

---

## Sơ đồ tổng quan

```
App khởi động
    │
    ├─ DatabaseHelper.getInstance(context)
    │       ├─ Lần đầu → new DatabaseHelper() → onCreate()
    │       │       ├─ createTables()   ← tạo 9 bảng
    │       │       ├─ createTriggers() ← tạo 2 trigger
    │       │       └─ seedData()       ← insert dữ liệu mẫu
    │       └─ Lần sau → trả về instance cũ (Singleton)
    │
    └─ Mọi DAO dùng chung instance này
```

---

## 1. Hằng số và Singleton

```java
public static final String DB_NAME    = "quanlythuvien.db";
public static final int    DB_VERSION = 7;
private static DatabaseHelper instance;
```

### Giải thích từng dòng

| Dòng | Ý nghĩa |
|------|---------|
| `DB_NAME` | Tên file lưu trong `/data/data/com.example.quanlythuvien/databases/` |
| `DB_VERSION = 7` | Mỗi khi đổi cấu trúc bảng, tăng số này lên → Android tự gọi `onUpgrade()` |
| `instance` | Biến lưu một instance duy nhất — đây là **Singleton Pattern** |

### Singleton Pattern — tại sao dùng?

```java
public static synchronized DatabaseHelper getInstance(Context context) {
    if (instance == null) {
        instance = new DatabaseHelper(context.getApplicationContext());
    }
    return instance;
}
```

**Giải thích đơn giản:** Giống như trong trường chỉ có 1 phòng hiệu trưởng — ai muốn gặp đều phải đến đúng phòng đó, không ai tự xây phòng riêng. Ở đây, mọi nơi trong app muốn dùng database đều gọi `getInstance()` và nhận về **đúng 1 kết nối** duy nhất.

**`synchronized`:** Đảm bảo nếu 2 thread cùng gọi `getInstance()` cùng lúc, chỉ 1 thread được vào tạo instance — tránh tạo ra 2 instance.

**`getApplicationContext()`:** Dùng Application Context (sống suốt app) thay vì Activity Context (chết khi xoay màn hình) để tránh memory leak.

---

## 2. onConfigure — Bật Foreign Key

```java
@Override
public void onConfigure(SQLiteDatabase db) {
    super.onConfigure(db);
    db.setForeignKeyConstraintsEnabled(true);
}
```

**Vấn đề:** SQLite mặc định **không** kiểm tra khóa ngoại. Ví dụ bảng `Sach` có `tl_id` tham chiếu `TheLoai`, nhưng nếu không bật, app vẫn cho insert `tl_id = 999` dù thể loại 999 không tồn tại — dữ liệu sẽ bị lỗi.

**Giải pháp:** `setForeignKeyConstraintsEnabled(true)` → SQLite kiểm tra trước khi insert/update.

**Tại sao đặt ở `onConfigure` không phải `onCreate`?**
- `onConfigure` chạy **mỗi lần** mở DB (kể cả lần 2, 3...).
- `onCreate` chỉ chạy **lần đầu** tạo DB.
- Nếu đặt ở `onCreate` → lần 2 mở app, FK không được bật → dữ liệu có thể sai.

---

## 3. onCreate — Chỉ chạy 1 lần duy nhất

```java
@Override
public void onCreate(SQLiteDatabase db) {
    createTables(db);
    createTriggers(db);
    seedData(db);
}
```

Chạy khi: **cài app lần đầu** hoặc **xóa data app** trong Settings.

Thứ tự **bắt buộc**: Tạo bảng → Tạo trigger (trigger tham chiếu bảng) → Insert data (data cần bảng).

---

## 4. onUpgrade — Nâng cấp khi tăng DB_VERSION

```java
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
```

**Khi nào chạy:** Khi `DB_VERSION` trong code > version trong file DB (tức là app được cập nhật).

**Tại sao DROP theo thứ tự con → cha?**
Vì khóa ngoại (Foreign Key). Ví dụ `ChiTietTra` có FK tham chiếu `PhieuTra` — phải DROP `ChiTietTra` trước, sau đó mới DROP được `PhieuTra`.

**`IF EXISTS`:** Tránh lỗi nếu bảng chưa tồn tại (vd DB cũ chưa có bảng mới).

---

## 5. Sơ đồ 9 bảng và quan hệ

```
Users ─────────── NhanVien
                      │
BanDoc ──────── PhieuMuon ──── ChiTietMuon ──── Sach ──── TheLoai
                    │
                 PhieuTra ──── ChiTietTra ──────┘
```

### Bảng Users
```sql
CREATE TABLE Users (
    user_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    role     TEXT CHECK(role IN ('admin','nhanvien')) NOT NULL,
    status   INTEGER DEFAULT 1
);
```
- `username UNIQUE` → không cho 2 người cùng tên đăng nhập.
- `CHECK(role IN (...))` → chỉ được nhập 'admin' hoặc 'nhanvien', SQLite tự báo lỗi nếu sai.
- `status = 1` là active, `0` là đã vô hiệu hóa (không cần xóa hẳn).
- Password lưu plain text vì đây là đồ án học — thực tế phải hash bằng bcrypt.

### Bảng NhanVien
```sql
CREATE TABLE NhanVien (
    nv_id INTEGER PRIMARY KEY AUTOINCREMENT,
    ten TEXT NOT NULL, sdt TEXT, email TEXT, diachi TEXT,
    chucvu TEXT, ngay_vao_lam TEXT, trangthai TEXT DEFAULT 'lamviec',
    user_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);
```
- Tách `Users` và `NhanVien` vì: Users giữ thông tin đăng nhập (bảo mật), NhanVien giữ thông tin hồ sơ (nghiệp vụ). Một nhân viên nghỉ việc → chỉ cần `Users.status = 0`, dữ liệu hồ sơ vẫn còn.
- `ngay_vao_lam TEXT` dạng `"yyyy-MM-dd"` vì SQLite không có kiểu DATE thật.

### Bảng Sach
```sql
CREATE TABLE Sach (
    sach_id INTEGER PRIMARY KEY AUTOINCREMENT,
    ten TEXT NOT NULL, tacgia TEXT, nxb TEXT, namxb INTEGER,
    soluong INTEGER DEFAULT 0,
    trangthai TEXT DEFAULT 'con',
    tl_id INTEGER, cover_uri TEXT,
    FOREIGN KEY (tl_id) REFERENCES TheLoai(tl_id)
);
```
- `soluong`: số cuốn còn trong kho — **trigger tự cập nhật** khi mượn/trả.
- `trangthai`: `'con'` hoặc `'het'` — cũng trigger tự cập nhật, dùng để lọc nhanh.
- `cover_uri`: URI ảnh bìa người dùng chọn từ gallery.

### Bảng PhieuMuon
```sql
CREATE TABLE PhieuMuon (
    pm_id INTEGER PRIMARY KEY AUTOINCREMENT,
    bd_id INTEGER,   -- bạn đọc mượn
    nv_id INTEGER,   -- nhân viên lập phiếu
    ngay_muon DATE, ngay_tra DATE,  -- ngay_tra = hạn trả dự kiến
    trangthai TEXT DEFAULT 'dangmuon',
    songaytre INTEGER DEFAULT 0, tienphat REAL DEFAULT 0,
    FOREIGN KEY (bd_id) REFERENCES BanDoc(bd_id),
    FOREIGN KEY (nv_id) REFERENCES NhanVien(nv_id)
);
```
- `ngay_tra` ở đây là **hạn trả dự kiến**, không phải ngày thực tế trả.
- Ngày trả thực tế ở bảng `PhieuTra.ngay_tra`.

---

## 6. Trigger — Tự động cập nhật kho sách

### Trigger 1: Khi mượn sách

```sql
CREATE TRIGGER trg_muon_sach
AFTER INSERT ON ChiTietMuon
BEGIN
  UPDATE Sach
    SET soluong   = soluong - NEW.soluong,
        trangthai = CASE WHEN (soluong - NEW.soluong) <= 0
                         THEN 'het' ELSE trangthai END
  WHERE sach_id = NEW.sach_id;
END;
```

**Ví dụ cụ thể:**
1. Sách "Java cơ bản" đang có `soluong = 5`.
2. App INSERT vào `ChiTietMuon`: `(pm_id=1, sach_id=3, soluong=2)` — mượn 2 cuốn.
3. **Trigger tự chạy ngay lập tức**: `UPDATE Sach SET soluong = 5 - 2 = 3`.
4. `3 > 0` nên `trangthai` vẫn là `'con'`.
5. Nếu mượn hết 5 cuốn → `soluong = 0 → trangthai = 'het'`.

**Tại sao dùng trigger thay vì code Java?**
- Trigger chạy **bên trong database**, đảm bảo **100% không bỏ sót** — dù code Java có bug, quên update, trigger vẫn chạy.
- Nếu code Java thì phải nhớ gọi update mỗi chỗ, dễ quên → kho sai.

### Trigger 2: Khi trả sách

```sql
CREATE TRIGGER trg_tra_sach
AFTER INSERT ON ChiTietTra
BEGIN
  UPDATE Sach
    SET soluong   = soluong + NEW.soluong,
        trangthai = 'con'
  WHERE sach_id = NEW.sach_id;
END;
```

**Đơn giản hơn:** Cộng kho lại, set `trangthai = 'con'` (có ít nhất 1 cuốn được trả → chắc chắn còn).

---

## 7. Dữ liệu mẫu (seedData)

Sau khi `onCreate()`, app tự insert sẵn:

| Loại | Số lượng | Nội dung |
|------|----------|---------|
| Users | 3 | admin/123, nv1/123, nv2/123 |
| NhanVien | 3 | Võ Thái Sơn (admin), Đinh Sỹ Vinh, Nguyễn Trọng Hiếu |
| BanDoc | 5 | 5 bạn đọc mẫu |
| TheLoai | 6 | Văn học, Hóa học, Công nghệ... |
| Sach | 16 | Rải đều các thể loại, năm xuất bản 2020–2024 |
| PhieuMuon | 6 | 3 đang mượn, 3 đã trả — tất cả năm 2026 |
| PhieuTra | 3 | Tương ứng 3 phiếu mượn đã trả |

**Tại sao cần seed data?**
Để app vừa cài đã có dữ liệu thử, không phải nhập tay. Thầy bật app lên là thấy ngay list sách, phiếu mượn — dễ demo.

---

## Câu hỏi thầy hay hỏi

**Q: SQLite là gì? Tại sao dùng SQLite không dùng MySQL?**
> SQLite là hệ quản trị cơ sở dữ liệu nhúng (embedded) — file .db lưu thẳng trong điện thoại, không cần server riêng. MySQL cần server kết nối qua mạng — không phù hợp app offline. Android hỗ trợ SQLite sẵn, không cần thư viện thêm.

**Q: Singleton Pattern là gì? Tại sao dùng ở đây?**
> Singleton đảm bảo chỉ có **1 instance** của `DatabaseHelper` trong suốt vòng đời app. Tại sao cần? Vì nếu tạo nhiều instance, mỗi cái giữ 1 kết nối DB — có thể gây xung đột khi 2 nơi cùng đọc/ghi. Dùng singleton → toàn app chỉ có 1 kết nối, an toàn và tiết kiệm tài nguyên.

**Q: Trigger là gì? App có bao nhiêu trigger?**
> Trigger là đoạn lệnh SQL tự động chạy khi có sự kiện (INSERT/UPDATE/DELETE) xảy ra trên một bảng. App có 2 trigger: `trg_muon_sach` (AFTER INSERT ON ChiTietMuon — trừ kho) và `trg_tra_sach` (AFTER INSERT ON ChiTietTra — cộng kho lại).

**Q: Foreign Key là gì? Tại sao phải bật thủ công?**
> Foreign Key (khóa ngoại) là ràng buộc đảm bảo dữ liệu tham chiếu luôn hợp lệ — ví dụ `sach_id` trong `ChiTietMuon` phải tồn tại trong bảng `Sach`. SQLite mặc định **tắt** FK để tương thích với các phiên bản cũ, nên phải bật thủ công bằng `setForeignKeyConstraintsEnabled(true)` trong `onConfigure()`.

**Q: Tại sao lưu ngày dạng TEXT thay vì DATE?**
> SQLite không có kiểu DATE/DATETIME thật — chỉ có TEXT, INTEGER, REAL, NUMERIC. Chuẩn để lưu ngày là TEXT dạng `"yyyy-MM-dd"` (ISO 8601). Dạng này so sánh chuỗi tự động đúng thứ tự thời gian: `"2026-01-01" < "2026-12-31"` — dùng được trực tiếp trong WHERE và ORDER BY.

**Q: onUpgrade xóa hết dữ liệu — nguy hiểm không?**
> Nguy hiểm cho ứng dụng thật vì user mất dữ liệu. Nhưng với đồ án học, khi schema thay đổi thì drop hết và tạo lại là cách đơn giản nhất. Production app thì phải viết migration từng bước: `ALTER TABLE` thêm cột, `INSERT INTO NewTable SELECT FROM OldTable`... để giữ dữ liệu user.

**Q: ContentValues là gì?**
> `ContentValues` là một Map đặc biệt của Android để truyền dữ liệu vào hàm `db.insert()` và `db.update()`. Thay vì ghép chuỗi SQL `"INSERT INTO Sach VALUES ('" + ten + "')"` (dễ lỗi, SQL injection), ta dùng `cv.put("ten", ten)` — Android lo việc escape an toàn.

**Q: Tại sao password lưu plain text?**
> Đây là đồ án học, mục tiêu là học cách kết nối DB và xác thực. Thực tế production phải hash password bằng thuật toán như BCrypt — lưu hash vào DB, khi login thì so sánh hash thay vì so sánh chuỗi trực tiếp.
