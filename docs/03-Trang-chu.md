# 03 — Trang chủ (MainActivity)

> **Files chính:** `MainActivity.java`, `db/StatDao.java`, `LibraryApplication.java`

---

## Giải thích đơn giản

Trang chủ là màn hình trung tâm sau khi đăng nhập. Nó làm 4 việc chính:
1. **Hiển thị lời chào** dựa theo giờ hiện tại và tên nhân viên đang đăng nhập.
2. **Hiển thị thống kê nhanh** — tổng số phiếu mượn và phiếu trả.
3. **Cung cấp phím tắt** — 6 card shortcut để vào nhanh các chức năng.
4. **Thanh điều hướng bên dưới** (Bottom Navigation) để di chuyển giữa các màn hình.

---

## Sơ đồ layout màn chính

```
┌─────────────────────────────────┐
│ [icon menu]  QUẢN LÝ THƯ VIỆN  │  ← Header
│ Chào buổi sáng 👋               │
│ Đinh Sỹ Vinh                   │
├──────────────┬──────────────────┤
│  📚 125      │  ✅  42          │  ← Stat cards (mượn / trả)
│  Phiếu mượn  │  Phiếu trả      │
├──────────────┴──────────────────┤
│  [Sách] [Bạn đọc] [Liên hệ]   │  ← Shortcut row 1
│  [Mượn trả] [Báo cáo] [NV]    │  ← Shortcut row 2
├─────────────────────────────────┤
│  📖 Truy cập gần đây           │  ← Recent section
├─────────────────────────────────┤
│ [Home] [Sách] [Mượn] [Đọc] [→] │  ← Bottom Nav
└─────────────────────────────────┘
```

---

## db/StatDao.java

### Mục đích
Chuyên đếm số liệu thống kê — tổng phiếu mượn và phiếu trả.

```java
public int countBorrow() { return countFromTable("PhieuMuon"); }
public int countReturn() { return countFromTable("PhieuTra"); }

private int countFromTable(String table) {
    SQLiteDatabase db = helper.getReadableDatabase();
    try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + table, null)) {
        if (c.moveToFirst()) return c.getInt(0);
        return 0;
    }
}
```

**Giải thích:**
- `SELECT COUNT(*)` → đếm tổng số dòng trong bảng. Luôn trả về **đúng 1 dòng 1 cột** → `c.getInt(0)` đọc thẳng.
- `countBorrow()` và `countReturn()` là 2 method public — code gọi rõ nghĩa hơn, tránh truyền chuỗi tên bảng sai.
- Ghép tên bảng trực tiếp vào SQL (`+ table`) ở đây an toàn vì `table` là hằng số trong code (dev truyền), **không phải** input từ user (mới gây SQL injection).

---

## MainActivity.java

### Constants và Fields

```java
private static final String ROLE_ADMIN = "admin";
private TextView tvBorrowCount, tvReturnCount;
private StatDao statDao;
private String currentRole;
```

**`currentRole`:** Cache role từ SharedPreferences vào biến local — tránh đọc prefs lặp đi lặp lại mỗi khi check phân quyền.

---

### onCreate()

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    statDao = new StatDao(this);
    currentRole = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
            .getString(LoginActivity.KEY_ROLE, "");

    tvBorrowCount = findViewById(R.id.tvBorrowCount);
    tvReturnCount = findViewById(R.id.tvReturnCount);

    setupTopBar();
    setupShortcuts();
    setupBottomNav();
}
```

Tách thành 3 method setup rõ ràng — code dễ đọc, dễ sửa từng phần.

---

### onResume() — Tự động refresh

```java
@Override
protected void onResume() {
    super.onResume();
    refreshStats();
}
```

**Tại sao cần `onResume()`?**

Khi user mở màn chính → vào Mượn Trả → tạo phiếu mượn mới → bấm Back về màn chính: số liệu phải cập nhật ngay. `onResume()` được gọi **mỗi khi Activity trở về foreground** — đúng thời điểm để refresh.

Nếu chỉ đặt trong `onCreate()` → số liệu chỉ load lần đầu, không cập nhật khi quay về.

---

### refreshStats()

```java
private void refreshStats() {
    tvBorrowCount.setText(String.valueOf(statDao.countBorrow()));
    tvReturnCount.setText(String.valueOf(statDao.countReturn()));
}
```

Query 2 con số từ DB và set text. `String.valueOf(int)` convert số nguyên thành chuỗi để set lên TextView.

---

### setupTopBar() — Lời chào theo giờ

```java
private void setupTopBar() {
    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    String hello;
    if      (hour < 12) hello = "Chào buổi sáng 👋";
    else if (hour < 18) hello = "Chào buổi chiều 👋";
    else                hello = "Chào buổi tối 👋";
    tvGreeting.setText(hello);

    String username    = prefs.getString(LoginActivity.KEY_USERNAME, "");
    String displayName = new UserDao(this).findStaffName(username);
    if (displayName == null || displayName.isEmpty()) displayName = username;
    tvUserName.setText(displayName);
}
```

**`Calendar.HOUR_OF_DAY`:** Trả về giờ theo format 24h (0–23). Giờ < 12 = buổi sáng, 12–17 = buổi chiều, ≥ 18 = buổi tối.

**`findStaffName(username)`:** Query DB lấy tên đầy đủ của nhân viên. Nếu null (user admin không có hồ sơ NhanVien) → dùng username thô làm fallback.

---

### setupShortcuts()

```java
private void setupShortcuts() {
    findViewById(R.id.statBorrow).setOnClickListener(v -> openBorrow());
    findViewById(R.id.statReturn).setOnClickListener(v -> openBorrow());
    findViewById(R.id.favBook).setOnClickListener(v -> openBook());
    findViewById(R.id.favReader).setOnClickListener(v -> openReader());
    // ...
}
```

Wire click listener cho từng card shortcut → gọi method `openXxx()`. Tách method nhỏ để:
1. Code rõ ràng.
2. Nếu cần thêm logic (animation, log...) chỉ sửa 1 chỗ.

---

### setupBottomNav()

```java
private void setupBottomNav() {
    BottomNavigationView nav = findViewById(R.id.bottomNav);
    nav.setSelectedItemId(R.id.nav_home);
    nav.setOnItemSelectedListener(item -> {
        int id = item.getItemId();
        if      (id == R.id.nav_home)   return true;        // đã ở home
        else if (id == R.id.nav_book)   { openBook();   return false; }
        else if (id == R.id.nav_borrow) { openBorrow(); return false; }
        else if (id == R.id.nav_reader) { openReader(); return false; }
        else if (id == R.id.nav_logout) { confirmLogout(); return false; }
        return false;
    });
}
```

**`return true` vs `return false`:**
- `true` → giữ item được highlight (đang ở trang này → highlight Home).
- `false` → không highlight (đang chuyển sang trang khác → không highlight nav item đó ở trang mới).

**`setSelectedItemId(R.id.nav_home)`:** Highlight icon "Trang chủ" khi vào màn này.

---

### openStaff() — Kiểm tra phân quyền

```java
private void openStaff() {
    if (!ROLE_ADMIN.equals(currentRole)) {
        toast("Bạn không có quyền truy cập");
        return;
    }
    startActivity(new Intent(this, NhanVienActivity.class));
}
```

Chỉ admin mới được vào màn Nhân viên. Nhân viên thường bấm vào shortcut → hiện Toast thông báo → không mở Activity.

---

### confirmLogout() + doLogout()

```java
private void confirmLogout() {
    new AlertDialog.Builder(this)
            .setTitle("Đăng xuất")
            .setMessage("Bạn có chắc muốn đăng xuất?")
            .setPositiveButton("Đăng xuất", (d, w) -> doLogout())
            .setNegativeButton("Hủy", null)
            .show();
}

private void doLogout() {
    getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
            .edit().clear().apply();
    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

**AlertDialog:** Hộp thoại xác nhận — tránh user bấm nhầm đăng xuất mà không hay. `setNegativeButton("Hủy", null)` → tap Hủy chỉ đóng dialog, không làm gì thêm.

---

## LibraryApplication.java — Xử lý Status Bar

```java
public class LibraryApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(new SimpleLifecycleCallbacks() {
            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                applyHeaderInset(activity);
            }
        });
    }
}
```

**Vấn đề cần giải quyết:** Trên Android 10+, content có thể vẽ "phía sau" thanh status bar (trong suốt). Nếu không xử lý, chữ tiêu đề bị che khuất bởi đồng hồ, pin...

**Giải pháp:**
1. `registerActivityLifecycleCallbacks` → đăng ký callback chạy mỗi khi **bất kỳ** Activity nào vào trạng thái `resumed`.
2. `applyHeaderInset()` → tìm View có id `header` hoặc `topBar` → thêm paddingTop bằng đúng chiều cao status bar → đẩy nội dung header xuống tránh bị che.

**Tại sao đặt trong `Application` thay vì từng Activity?**
Nếu đặt trong từng Activity → phải copy code vào tất cả ~15 Activity → dễ quên, khó bảo trì. Đặt trong `Application.onCreate()` → đăng ký 1 lần, tự áp dụng cho tất cả Activity.

---

## Vòng đời Activity (Activity Lifecycle)

```
                    onCreate()
                       │
                    onStart()
                       │
                    onResume()  ←─────── onRestart()
                       │                    ↑
                  [App đang hiển thị]       │
                       │                    │
                    onPause()               │
                       │              [User quay lại]
                    onStop() ──────────────┘
                       │
                    onDestroy()
```

**App dùng:**
- `onCreate()` → setup ban đầu (bind views, init DAO).
- `onResume()` → refresh data (gọi mỗi khi quay về).

---

## Câu hỏi thầy hay hỏi

**Q: Trang chủ lấy số liệu phiếu mượn, phiếu trả ở đâu?**
> Từ class `StatDao` — có 2 method `countBorrow()` và `countReturn()`, mỗi method chạy `SELECT COUNT(*)` trên bảng `PhieuMuon` và `PhieuTra` trong SQLite. Số liệu được load lại mỗi khi `onResume()` chạy — tức là mỗi lần user quay về màn chính.

**Q: Tại sao lời chào thay đổi theo buổi sáng/chiều/tối?**
> Dùng `Calendar.getInstance().get(Calendar.HOUR_OF_DAY)` lấy giờ hiện tại (0–23). Giờ < 12 → "Chào buổi sáng", 12–17 → "Chào buổi chiều", ≥ 18 → "Chào buổi tối". Đây là cách hiển thị thân thiện với người dùng.

**Q: Tại sao cần onResume() để refresh? Sao không để trong onCreate()?**
> `onCreate()` chỉ chạy 1 lần khi Activity được tạo lần đầu. `onResume()` chạy **mỗi khi** Activity trở về foreground — kể cả sau khi user sang màn khác và quay lại. Ví dụ: vào Mượn Trả, tạo phiếu mới, bấm Back về trang chủ → `onResume()` chạy → số liệu được cập nhật ngay.

**Q: AlertDialog là gì? Tại sao cần confirm trước khi đăng xuất?**
> `AlertDialog` là hộp thoại xác nhận hiện lên giữa màn hình. Cần confirm để tránh trường hợp user bấm nhầm nút đăng xuất và mất phiên làm việc. Đây là UX tốt — các action không thể hoàn tác nên cần xác nhận.

**Q: Bottom Navigation là gì?**
> `BottomNavigationView` là thanh điều hướng ở cuối màn hình với các icon. Khi tap icon → `OnItemSelectedListener` được gọi → xử lý điều hướng sang Activity tương ứng. `setSelectedItemId()` để highlight icon của trang đang hiển thị.

**Q: LibraryApplication làm gì?**
> `Application` là class đặc biệt của Android, chạy trước mọi Activity, sống suốt vòng đời app. `LibraryApplication` extend `Application` để đăng ký một lifecycle callback toàn cục — tự động căn chỉnh padding của header mọi Activity để tránh bị che bởi status bar hệ thống.
