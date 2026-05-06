# 08 — Quản lý Nhân viên

> **Files chính:** `NhanVienActivity.java`, `NhanVienDetailActivity.java`, `NhanVienAddActivity.java`, `db/NhanVienDao.java`

---

## Giải thích đơn giản

Chức năng Nhân viên **chỉ dành cho Admin**. Nhân viên bình thường bấm vào shortcut "Nhân viên" → hiện Toast "Bạn không có quyền" → không mở được. Admin có thể:
- Xem danh sách nhân viên.
- Thêm nhân viên mới (đồng thời tạo tài khoản đăng nhập).
- Xem chi tiết, sửa thông tin, vô hiệu hóa tài khoản.

---

## Điểm đặc biệt: Thêm nhân viên = Tạo 2 bản ghi cùng lúc

Khi thêm nhân viên mới, cần insert vào **2 bảng** trong **1 transaction**:
```
INSERT INTO Users (username, password, role) → lấy user_id mới
INSERT INTO NhanVien (ten, sdt, ..., user_id) → dùng user_id vừa lấy
```

Nếu bước 2 thất bại → bước 1 phải rollback (không để Users thừa bản ghi mồ côi).

---

## db/NhanVienDao.java

### listAll() — JOIN với Users

```java
public List<NhanVien> listAll() {
    String sql =
        "SELECT nv.nv_id, nv.ten, nv.sdt, nv.email, nv.diachi, " +
        "       nv.chucvu, nv.ngay_vao_lam, nv.trangthai, " +
        "       u.username, u.role, u.status AS user_status " +
        "FROM NhanVien nv " +
        "LEFT JOIN Users u ON u.user_id = nv.user_id " +
        "ORDER BY nv.ten";
    // ...
}
```

JOIN với Users để lấy thêm username và role — cần để hiển thị trên màn chi tiết.

---

### insert() — Transaction 2 bảng

```java
public long insert(NhanVien nv, String username, String password, String role) {
    SQLiteDatabase db = helper.getWritableDatabase();
    db.beginTransaction();
    try {
        // Bước 1: Tạo tài khoản Users
        ContentValues cvUser = new ContentValues();
        cvUser.put("username", username);
        cvUser.put("password", password);
        cvUser.put("role",     role);
        long userId = db.insert("Users", null, cvUser);

        if (userId < 0) return -1; // insert Users thất bại

        // Bước 2: Tạo hồ sơ NhanVien
        ContentValues cvNv = new ContentValues();
        cvNv.put("ten",          nv.ten);
        cvNv.put("sdt",          nv.sdt);
        cvNv.put("email",        nv.email);
        cvNv.put("diachi",       nv.diachi);
        cvNv.put("chucvu",       nv.chucvu);
        cvNv.put("ngay_vao_lam", nv.ngayVaoLam);
        cvNv.put("user_id",      (int) userId);  // dùng userId từ bước 1
        long nvId = db.insert("NhanVien", null, cvNv);

        db.setTransactionSuccessful(); // đánh dấu thành công
        return nvId;
    } finally {
        db.endTransaction(); // commit nếu successful, rollback nếu không
    }
}
```

**Transaction hoạt động thế nào?**
- `beginTransaction()` → bắt đầu, mọi thay đổi chưa ghi thật.
- `setTransactionSuccessful()` → đánh dấu "ok để commit".
- `endTransaction()` → nếu có `setTransactionSuccessful()` → **commit** (ghi thật); nếu không → **rollback** (hủy hết).
- `finally` đảm bảo `endTransaction()` **luôn luôn** được gọi, kể cả khi có exception.

**Tại sao cần Transaction?**

Tình huống không có transaction: Insert Users thành công (userId = 5) → Insert NhanVien thất bại (vd tên trùng) → Users có bản ghi id=5 nhưng không có NhanVien → dữ liệu bị "mồ côi". Lần sau tạo nhân viên → username bị chiếm dù không có NhanVien nào dùng.

Transaction → cả 2 cùng thành công hoặc cùng thất bại (tính **Atomicity**).

---

### disableUser() — Vô hiệu hóa tài khoản

```java
public int disableUser(int nvId) {
    SQLiteDatabase db = helper.getWritableDatabase();
    // Lấy user_id từ nv_id
    String sql = "SELECT user_id FROM NhanVien WHERE nv_id = ?";
    try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(nvId)})) {
        if (c.moveToFirst()) {
            int userId = c.getInt(0);
            ContentValues cv = new ContentValues();
            cv.put("status", 0);  // 0 = disabled
            return db.update("Users", cv, "user_id = ?",
                    new String[]{String.valueOf(userId)});
        }
    }
    return 0;
}
```

**Tại sao vô hiệu hóa thay vì xóa?**
- Nhân viên nghỉ việc nhưng vẫn có lịch sử phiếu mượn cần lưu.
- Xóa NhanVien → xóa luôn reference trong PhieuMuon (hoặc FK lỗi) → mất dữ liệu.
- `status = 0` → tài khoản không đăng nhập được (điều kiện `status = 1` trong `UserDao.login()`), nhưng data vẫn còn.

---

## NhanVienAddActivity.java

### Form thêm nhân viên

```java
private void onConfirm() {
    String ten      = edtTen.getText().toString().trim();
    String username = edtUsername.getText().toString().trim();
    String password = edtPassword.getText().toString().trim();
    String chucvu   = edtChucvu.getText().toString().trim();

    // Validate
    if (TextUtils.isEmpty(ten)) {
        edtTen.setError("Nhập tên nhân viên"); return;
    }
    if (TextUtils.isEmpty(username)) {
        edtUsername.setError("Nhập tên đăng nhập"); return;
    }
    if (TextUtils.isEmpty(password) || password.length() < 3) {
        edtPassword.setError("Mật khẩu ít nhất 3 ký tự"); return;
    }

    // Check username trùng
    if (userDao.existsUsername(username)) {
        edtUsername.setError("Tên đăng nhập đã tồn tại"); return;
    }

    String role = rbAdmin.isChecked() ? "admin" : "nhanvien";

    NhanVien nv = new NhanVien();
    nv.ten    = ten;
    nv.chucvu = chucvu;
    // ...

    long id = nhanVienDao.insert(nv, username, password, role);
    if (id > 0) {
        Toast.makeText(this, "Đã thêm nhân viên", Toast.LENGTH_SHORT).show();
        finish();
    }
}
```

**`existsUsername(username)`:** Query `SELECT 1 FROM Users WHERE username = ?` — kiểm tra trùng trước khi insert. Tránh trường hợp `username UNIQUE` throw exception.

**RadioButton chọn role:**
```java
String role = rbAdmin.isChecked() ? "admin" : "nhanvien";
```

---

## NhanVienDetailActivity.java

### Sửa thông tin

```java
private void onUpdate() {
    if (!isAdmin()) { toast("Bạn không có quyền"); return; }

    nv.ten    = edtTen.getText().toString().trim();
    nv.sdt    = edtSdt.getText().toString().trim();
    nv.chucvu = edtChucvu.getText().toString().trim();

    int rows = nhanVienDao.update(nv);
    if (rows > 0) {
        toast("Đã cập nhật");
        loadData(); // refresh UI
    }
}
```

### Vô hiệu hóa tài khoản

```java
private void onDisable() {
    new AlertDialog.Builder(this)
            .setTitle("Vô hiệu hóa tài khoản")
            .setMessage("Tài khoản đăng nhập sẽ bị khóa. Tiếp tục?")
            .setPositiveButton("Xác nhận", (d, w) -> {
                nhanVienDao.disableUser(nvId);
                toast("Đã vô hiệu hóa tài khoản");
                loadData();
            })
            .setNegativeButton("Hủy", null)
            .show();
}
```

---

## model/NhanVien.java

```java
public class NhanVien {
    public int    nvId;
    public String ten;
    public String sdt;
    public String email;
    public String diachi;
    public String chucvu;
    public String ngayVaoLam;
    public String trangthai;    // "lamviec" hoặc "nghiviec"
    public int    userId;       // FK → Users
    public String username;     // join field
    public String role;         // join field
    public int    userStatus;   // join field (Users.status)
}
```

---

## Câu hỏi thầy hay hỏi

**Q: Transaction là gì? Tại sao phải dùng khi thêm nhân viên?**
> Transaction là một nhóm thao tác được xem như **một đơn vị nguyên tử** — hoặc tất cả thành công, hoặc tất cả thất bại (không có trạng thái trung gian). Khi thêm nhân viên cần insert 2 bảng: Users + NhanVien. Nếu không dùng transaction: Users insert thành công nhưng NhanVien lỗi → Users có bản ghi "mồ côi", username bị chiếm dù không dùng. Transaction đảm bảo cả 2 cùng commit hoặc cùng rollback.

**Q: ACID trong database là gì?**
> ACID là 4 tính chất của transaction:
> - **A**tomicity: toàn bộ hoặc không — không có trạng thái giữa chừng.
> - **C**onsistency: DB luôn ở trạng thái hợp lệ trước và sau transaction.
> - **I**solation: các transaction chạy song song không ảnh hưởng nhau.
> - **D**urability: sau khi commit, dữ liệu được lưu vĩnh viễn dù app crash.

**Q: Tại sao không xóa nhân viên nghỉ việc mà phải vô hiệu hóa?**
> Vì tính toàn vẹn dữ liệu — nhân viên đã lập nhiều phiếu mượn (có `nv_id` trong PhieuMuon). Nếu xóa NhanVien, các phiếu mượn sẽ mất reference (FK violation hoặc data orphan). Vô hiệu hóa (`Users.status = 0`) → tài khoản không đăng nhập được, nhưng lịch sử phiếu mượn vẫn nguyên vẹn.

**Q: Chức năng Nhân viên ai mới xem được?**
> Chỉ Admin. Kiểm tra trong `MainActivity.openStaff()`: `if (!"admin".equals(currentRole)) { toast("Bạn không có quyền"); return; }`. Nhân viên thường bấm shortcut → hiện Toast, không mở Activity. Đây là phân quyền ở tầng UI — phân quyền thêm có thể đặt ở DAO hoặc xác minh lại trong Activity.

**Q: Tại sao tách bảng Users và NhanVien thay vì dùng chung 1 bảng?**
> Tách để rõ trách nhiệm: `Users` chứa thông tin **xác thực** (username/password/role — nhạy cảm), `NhanVien` chứa thông tin **hồ sơ** (tên/sdt/địa chỉ/chức vụ — nghiệp vụ). Cấu trúc này linh hoạt hơn: nếu sau này muốn cho phép bạn đọc đăng nhập, chỉ cần thêm bảng mới JOIN với Users mà không phải sửa NhanVien.
