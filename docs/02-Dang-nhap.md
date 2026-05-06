# 02 — Đăng nhập

> **Files chính:** `LoginActivity.java`, `db/UserDao.java`, `res/layout/activity_login.xml`

---

## Giải thích đơn giản

Màn hình đăng nhập làm 3 việc:
1. **Kiểm tra username + password** với bảng `Users` trong SQLite.
2. **Lưu thông tin đăng nhập** vào `SharedPreferences` (bộ nhớ nhỏ dạng key-value trên điện thoại).
3. **Tự động đăng nhập lại** nếu lần trước chưa đăng xuất — mở app là vào thẳng màn chính.

---

## Sơ đồ luồng đăng nhập

```
Mở app
  │
  ▼
LoginActivity.onCreate()
  │
  ├─ Đọc SharedPreferences: có username?
  │       ├─ CÓ → goToMain() (bỏ qua màn login)
  │       └─ KHÔNG → hiển thị form login
  │
  ▼ (user nhập username + password, bấm Đăng nhập)
  │
doLogin()
  ├─ Validate: 2 ô có trống không?
  │       └─ Có trống → Toast "Nhập đủ thông tin" → dừng
  │
  ├─ userDao.login(username, password) → query DB
  │       ├─ Trả null → Toast "Sai tài khoản hoặc mật khẩu" → dừng
  │       └─ Trả UserInfo → tiếp tục
  │
  ├─ Lưu vào SharedPreferences: username + role
  │
  └─ goToMain() → MainActivity (xóa back stack)
```

---

## db/UserDao.java

### Inner class UserInfo

```java
public static class UserInfo {
    public int    userId;
    public String username;
    public String role;    // "admin" hoặc "nhanvien"
    public int    status;  // 1 = active, 0 = disabled
}
```

**Giải thích:** Đây là một class đơn giản để "đóng gói" thông tin user sau khi query DB. Thay vì trả về nhiều biến rời rạc, gói chung vào 1 object cho gọn.

**Tại sao `static class`?** Nếu là inner class thường (non-static), nó giữ reference đến outer class → có thể gây memory leak. `static` → độc lập hoàn toàn.

---

### method login()

```java
public UserInfo login(String username, String password) {
    SQLiteDatabase db = helper.getReadableDatabase();
    try (Cursor c = db.rawQuery(
            "SELECT user_id, username, role, status FROM Users " +
            "WHERE username = ? AND password = ? AND status = 1",
            new String[]{username, password})) {
        if (c.moveToFirst()) {
            UserInfo u = new UserInfo();
            u.userId   = c.getInt(0);
            u.username = c.getString(1);
            u.role     = c.getString(2);
            u.status   = c.getInt(3);
            return u;
        }
        return null;
    }
}
```

**Giải thích từng phần:**

| Phần | Giải thích |
|------|-----------|
| `getReadableDatabase()` | Mở DB ở chế độ đọc (không cần ghi). Tự tạo file DB nếu chưa có. |
| `try (Cursor c = ...)` | Try-with-resources: Cursor tự đóng khi ra khỏi block, tránh rò rỉ kết nối. |
| `WHERE username = ? AND password = ?` | Dấu `?` là placeholder — **tránh SQL Injection** (xem Q&A dưới). |
| `AND status = 1` | Chặn tài khoản đã bị vô hiệu hóa đăng nhập. |
| `new String[]{username, password}` | Giá trị điền vào 2 dấu `?` theo thứ tự. |
| `c.moveToFirst()` | Di chuyển con trỏ đến dòng đầu tiên. Trả `false` nếu không có dòng nào (sai tài khoản). |
| `c.getInt(0)`, `c.getString(1)`... | Đọc giá trị cột theo vị trí (0-indexed, khớp với SELECT). |
| `return null` | Không có row khớp → login thất bại. |

---

### method findStaffName()

```java
public String findStaffName(String username) {
    SQLiteDatabase db = helper.getReadableDatabase();
    String sql = "SELECT nv.ten FROM NhanVien nv " +
                 "JOIN Users u ON u.user_id = nv.user_id " +
                 "WHERE u.username = ? LIMIT 1";
    try (Cursor c = db.rawQuery(sql, new String[]{username})) {
        if (c.moveToFirst()) return c.getString(0);
    }
    return null;
}
```

**Giải thích SQL:**
- `JOIN Users u ON u.user_id = nv.user_id` → ghép bảng NhanVien với Users qua khóa `user_id`.
- Mục đích: từ username đăng nhập (vd: "nv1") lấy được tên đầy đủ (vd: "Đinh Sỹ Vinh") để hiển thị trên màn chính.
- `LIMIT 1` → phòng vệ, dù lý thuyết 1 username chỉ có 1 nhân viên.

**Dùng ở đâu?** `MainActivity` hiển thị "Chào buổi sáng 👋 Đinh Sỹ Vinh".

---

## LoginActivity.java

### Constants

```java
public static final String PREFS_NAME   = "session";
public static final String KEY_USERNAME = "username";
public static final String KEY_ROLE     = "role";
```

**Tại sao `public static`?** Các Activity khác (SachActivity, MuonTraActivity...) cũng cần đọc session để kiểm tra phân quyền. Khai báo ở đây → import một chỗ, không hardcode chuỗi "session" khắp nơi.

**SharedPreferences là gì?** Là file XML nhỏ trên điện thoại (`/data/data/.../shared_prefs/session.xml`) lưu cặp key-value. Giống như "ghi nhớ" của app — tắt app bật lại vẫn còn.

---

### onCreate()

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    userDao = new UserDao(this);
    // bind views...
    btnLogin.setOnClickListener(v -> doLogin());

    // Auto-login nếu đã có session
    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    if (!prefs.getString(KEY_USERNAME, "").isEmpty()) {
        goToMain();
    }
}
```

**`super.onCreate(savedInstanceState)`:** Bắt buộc gọi cha trước — nếu bỏ sẽ crash ngay.

**`setContentView(R.layout.activity_login)`:** Nạp file XML layout thành màn hình thực.

**Auto-login:** Đọc prefs, nếu username không rỗng (đã từng login và chưa logout) → chuyển thẳng MainActivity, bỏ qua form đăng nhập.

---

### doLogin()

```java
private void doLogin() {
    String u = edtUsername.getText().toString().trim();
    String p = edtPassword.getText().toString();

    if (u.isEmpty() || p.isEmpty()) {
        Toast.makeText(this, "Nhập đủ thông tin", Toast.LENGTH_SHORT).show();
        return;
    }

    UserDao.UserInfo info = userDao.login(u, p);
    if (info == null) {
        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
        return;
    }

    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
            .putString(KEY_USERNAME, info.username)
            .putString(KEY_ROLE, info.role)
            .apply();

    goToMain();
}
```

**`.trim()` cho username:** Bỏ khoảng trắng thừa đầu/cuối (vd user gõ nhầm "admin ").

**Không `.trim()` password:** Password có thể có space cố ý.

**`apply()` thay vì `commit()`:** `apply()` ghi bất đồng bộ (không block màn hình), `commit()` ghi đồng bộ (block và trả về kết quả). Ở đây không cần biết kết quả ngay nên dùng `apply()`.

---

### goToMain()

```java
private void goToMain() {
    Intent i = new Intent(this, MainActivity.class);
    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(i);
    finish();
}
```

**2 flag quan trọng:**
- `FLAG_ACTIVITY_CLEAR_TASK`: Xóa toàn bộ back stack cũ.
- `FLAG_ACTIVITY_NEW_TASK`: Tạo task mới cho MainActivity.

**Tại sao cần?** Không có flag → user bấm Back từ MainActivity sẽ quay về màn Login → sai UX. Với flag → bấm Back thoát hẳn app.

**`finish()`:** Đóng LoginActivity ngay sau khi mở MainActivity.

---

### doLogout() — dùng ở mọi Activity

```java
private void doLogout() {
    getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
            .edit().clear().apply();
    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

**`.clear()`:** Xóa hết tất cả key trong file prefs → lần sau mở app, auto-login không kích hoạt.

---

## Phân quyền theo role

Pattern này dùng ở nhiều Activity:

```java
String role = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
        .getString(LoginActivity.KEY_ROLE, "");
if (!"admin".equals(role)) {
    Toast.makeText(this, "Bạn không có quyền", Toast.LENGTH_SHORT).show();
    return;
}
```

**Tại sao `"admin".equals(role)` chứ không phải `role.equals("admin")`?**

`role` có thể là `null` nếu prefs chưa có key → `null.equals(...)` ném `NullPointerException` crash app. `"admin".equals(null)` trả `false` an toàn. Đây gọi là **Yoda Condition** (đặt literal trước).

---

## Tài khoản test

| Username | Password | Role | Tên hiển thị |
|----------|----------|------|-------------|
| `admin`  | `123` | admin | Võ Thái Sơn |
| `nv1`    | `123` | nhanvien | Đinh Sỹ Vinh |
| `nv2`    | `123` | nhanvien | Nguyễn Trọng Hiếu |

---

## Câu hỏi thầy hay hỏi

**Q: SQL Injection là gì? App có bị không?**
> SQL Injection là tấn công bằng cách chèn code SQL vào ô nhập liệu. Ví dụ: username nhập `' OR '1'='1` → câu SQL trở thành `WHERE username='' OR '1'='1'` → luôn đúng → đăng nhập được dù không biết mật khẩu. App phòng tránh bằng **prepared statement** — dùng dấu `?` thay vì ghép chuỗi, Android tự escape các ký tự nguy hiểm.

**Q: SharedPreferences là gì? Dùng để làm gì?**
> SharedPreferences là một cơ chế lưu trữ key-value nhỏ dạng file XML trong internal storage của app. App dùng để lưu trạng thái đăng nhập (username, role) — giúp user không phải nhập lại mỗi lần mở app. Chỉ app đó mới đọc được (MODE_PRIVATE).

**Q: Tại sao không lưu password vào SharedPreferences?**
> Vì bảo mật — SharedPreferences là file XML có thể đọc được nếu điện thoại bị root. Chỉ cần lưu username và role (không nhạy cảm) là đủ để xác định ai đang đăng nhập. Password không cần thiết sau khi đã verify với DB.

**Q: FLAG_ACTIVITY_CLEAR_TASK làm gì?**
> Xóa toàn bộ stack các Activity đang mở. Khi đăng nhập xong → mở MainActivity với flag này → LoginActivity bị xóa khỏi stack → bấm Back không quay lại được màn login. Tương tự khi đăng xuất → mở LoginActivity với flag này → xóa hết mọi Activity trước đó.

**Q: Cursor là gì?**
> Cursor là "con trỏ" trỏ vào kết quả query SQL. Ban đầu con trỏ ở vị trí -1 (trước dòng đầu). `moveToFirst()` → di chuyển đến dòng 1. `moveToNext()` → dòng kế tiếp. `getInt(0)`, `getString(1)` → đọc giá trị cột tại vị trí hiện tại. Phải `close()` sau khi dùng để giải phóng tài nguyên — code dùng try-with-resources để tự động đóng.

**Q: Intent là gì?**
> Intent là "thông điệp" để Android biết cần làm gì — ở đây dùng để mở Activity khác. `new Intent(this, MainActivity.class)` = tạo intent chuyển từ màn hiện tại sang MainActivity. `putExtra()` để gửi kèm dữ liệu.
