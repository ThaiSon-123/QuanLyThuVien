# 07 — Quản lý Bạn đọc

> **Files chính:** `BanDocActivity.java`, `BanDocDetailActivity.java`, `BanDocAddActivity.java`, `db/BanDocDao.java`

---

## Giải thích đơn giản

Bạn đọc là người dùng thư viện — họ mượn sách nhưng không có tài khoản đăng nhập. Nhân viên quản lý danh sách bạn đọc: thêm mới, xem chi tiết kèm lịch sử mượn sách. **Admin** mới được sửa/xóa.

---

## Sơ đồ luồng

```
BanDocActivity (list + search)
  ├─ Stats: Tổng số / Đang mượn
  ├─ List bạn đọc (tìm kiếm không dấu)
  │       └─ Tap → BanDocDetailActivity
  │                   ├─ Thông tin cá nhân
  │                   ├─ Lịch sử mượn (danh sách phiếu)
  │                   └─ [Admin] Sửa / Xóa
  └─ FAB "Thêm bạn đọc" → BanDocAddActivity
```

---

## model/BanDoc.java

```java
public class BanDoc {
    public int    bdId;
    public String ten;
    public String sdt;
    public String diachi;
    public int    soPhieuDangMuon;  // join field — không có cột riêng
}
```

`soPhieuDangMuon` được tính khi query — không lưu trong DB (vì sẽ thay đổi liên tục, trigger không quản lý field này).

---

## db/BanDocDao.java

### listAll() — kèm số phiếu đang mượn

```java
public List<BanDoc> listAll() {
    String sql =
        "SELECT bd.bd_id, bd.ten, bd.sdt, bd.diachi, " +
        "       COUNT(pm.pm_id) AS so_phieu_dang_muon " +
        "FROM BanDoc bd " +
        "LEFT JOIN PhieuMuon pm ON pm.bd_id = bd.bd_id " +
        "   AND NOT EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) " +
        "GROUP BY bd.bd_id " +
        "ORDER BY bd.ten";
    // ...
}
```

**Giải thích điều kiện JOIN phức tạp:**

```sql
LEFT JOIN PhieuMuon pm ON pm.bd_id = bd.bd_id
   AND NOT EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)
```

- Ghép PhieuMuon của bạn đọc này.
- Nhưng **chỉ** ghép những PhieuMuon **chưa có** PhieuTra tương ứng (chưa trả).
- `COUNT(pm.pm_id)` → đếm số phiếu đang mượn thực sự (không tính đã trả).

**Nếu bỏ điều kiện NOT EXISTS:** COUNT sẽ đếm cả phiếu đã trả → số liệu sai.

---

### countStats() — 2 con số cho header

```java
public int[] countStats() {
    int total    = 0;
    int dangMuon = 0;
    String sql =
        "SELECT COUNT(DISTINCT bd.bd_id), " +
        "       COUNT(DISTINCT CASE WHEN pm.pm_id IS NOT NULL THEN bd.bd_id END) " +
        "FROM BanDoc bd " +
        "LEFT JOIN PhieuMuon pm ON pm.bd_id = bd.bd_id " +
        "   AND NOT EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)";
    try (Cursor c = ...) {
        if (c.moveToFirst()) {
            total    = c.getInt(0);
            dangMuon = c.getInt(1);
        }
    }
    return new int[]{total, dangMuon};
}
```

**2 cột trong 1 query:**
- `COUNT(DISTINCT bd.bd_id)` → tổng số bạn đọc.
- `COUNT(DISTINCT CASE WHEN pm.pm_id IS NOT NULL THEN bd.bd_id END)` → số bạn đọc đang mượn ít nhất 1 cuốn.

**`DISTINCT`:** Tránh đếm trùng — 1 bạn đọc mượn 3 cuốn sẽ có 3 dòng sau JOIN, `DISTINCT` đảm bảo chỉ đếm 1 lần.

---

### getMuonHistory() — lịch sử mượn của 1 bạn đọc

```java
public List<PhieuMuon> getMuonHistory(int bdId) {
    String sql =
        "SELECT pm.pm_id, pm.ngay_muon, pm.ngay_tra, " +
        "       EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) AS da_tra " +
        "FROM PhieuMuon pm " +
        "WHERE pm.bd_id = ? " +
        "ORDER BY pm.pm_id DESC";
    // ...
}
```

Dùng lại pattern `EXISTS` để determine trạng thái. Lọc `WHERE pm.bd_id = ?` → chỉ lấy phiếu của bạn đọc này.

---

### insert(), update(), delete()

```java
public long insert(BanDoc bd) {
    ContentValues cv = new ContentValues();
    cv.put("ten",    bd.ten);
    cv.put("sdt",    bd.sdt);
    cv.put("diachi", bd.diachi);
    return db.insert("BanDoc", null, cv);
}
```

Đơn giản — 3 field. Không có FK nên không lo ràng buộc. Trả về `bd_id` mới.

---

## BanDocActivity.java

### Tìm kiếm không dấu

```java
private static String norm(String s) {
    if (s == null) return "";
    String nfd = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD);
    return nfd.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
              .replace('đ', 'd').replace('Đ', 'd');
}

private void applyFilter(String keyword) {
    String kw = norm(keyword);
    filtered.clear();
    for (BanDoc b : allList) {
        if (kw.isEmpty()
                || norm(b.ten).contains(kw)
                || norm(b.sdt).contains(kw)
                || norm(b.diachi).contains(kw)) {
            filtered.add(b);
        }
    }
    adapter.submit(filtered);
    tvTongSo.setText(String.valueOf(filtered.size()));
}
```

**Tìm theo 3 field:** Tên, số điện thoại, địa chỉ — tiện khi nhớ SĐT nhưng quên tên.

### Search Mode — toggle

```java
private void enterSearchMode() {
    searchMode = true;
    tvTitle.setVisibility(View.GONE);
    btnSearch.setVisibility(View.GONE);
    headerSearch.setVisibility(View.VISIBLE);
    edtSearch.requestFocus();
    showKeyboard(edtSearch);
}
```

Pattern giống SachActivity — 2 layout chồng nhau, toggle bằng VISIBLE/GONE.

### onBackPressed()

```java
@Override
public void onBackPressed() {
    if (searchMode) exitSearchMode();
    else super.onBackPressed();
}
```

Bấm Back khi đang search → thoát search mode (không thoát màn hình). Bấm Back khi bình thường → `super.onBackPressed()` → đóng Activity.

---

## BanDocDetailActivity.java

### loadData() — load thông tin + lịch sử

```java
private void loadData() {
    BanDoc bd = banDocDao.findById(bdId);
    if (bd == null) { finish(); return; }

    tvTen.setText(bd.ten);
    tvSdt.setText(bd.sdt != null ? bd.sdt : "Chưa có");
    tvDiachi.setText(bd.diachi != null ? bd.diachi : "Chưa có");

    // Load lịch sử mượn
    List<PhieuMuon> history = banDocDao.getMuonHistory(bdId);
    historyAdapter.submit(history);
    tvLichSu.setText("Lịch sử mượn (" + history.size() + ")");
}
```

**Null check:** `bd.sdt != null ? bd.sdt : "Chưa có"` — hiển thị "Chưa có" thay vì để trống hoặc crash.

### Tap vào phiếu mượn trong lịch sử

```java
historyAdapter.setOnItemClick(pm -> {
    startActivity(PhieuMuonDetailActivity.newIntent(this, pm.pmId));
});
```

Từ màn chi tiết bạn đọc → tap phiếu mượn → mở chi tiết phiếu mượn đó.

---

## BanDocAddActivity.java

```java
private void onConfirm() {
    String ten    = edtTen.getText().toString().trim();
    String sdt    = edtSdt.getText().toString().trim();
    String diachi = edtDiachi.getText().toString().trim();

    if (TextUtils.isEmpty(ten)) {
        edtTen.setError("Vui lòng nhập tên bạn đọc");
        return;
    }

    BanDoc bd = new BanDoc();
    bd.ten    = ten;
    bd.sdt    = sdt.isEmpty() ? null : sdt;
    bd.diachi = diachi.isEmpty() ? null : diachi;

    long id = banDocDao.insert(bd);
    if (id > 0) {
        Toast.makeText(this, "Đã thêm bạn đọc", Toast.LENGTH_SHORT).show();
        finish();
    }
}
```

**`setError()`:** Hiển thị thông báo lỗi inline ngay dưới EditText (không cần Toast riêng). Thuận tiện hơn Toast khi validate form.

**Null thay vì chuỗi rỗng:** `sdt.isEmpty() ? null : sdt` — lưu NULL vào DB thay vì chuỗi rỗng. Sau này query `WHERE sdt IS NULL` đơn giản hơn `WHERE sdt = ''`.

---

## Câu hỏi thầy hay hỏi

**Q: Bạn đọc khác Nhân viên ở điểm gì trong hệ thống?**
> Nhân viên có **tài khoản đăng nhập** — có bản ghi trong cả `Users` và `NhanVien`. Bạn đọc **không đăng nhập** — chỉ có bản ghi trong bảng `BanDoc`. Nhân viên là người dùng hệ thống, bạn đọc là khách hàng của thư viện.

**Q: Số "Đang mượn" trên màn bạn đọc được tính thế nào?**
> Query COUNT phiếu mượn chưa có phiếu trả: JOIN PhieuMuon với điều kiện `NOT EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)` → chỉ đếm phiếu mượn thực sự chưa trả.

**Q: Tại sao dùng NOT EXISTS thay vì WHERE trangthai = 'dangmuon'?**
> Cột `trangthai` trong PhieuMuon phải được update thủ công khi lập phiếu trả — nếu code quên update thì sai. `NOT EXISTS` truy vấn thẳng dữ liệu thực (có PhieuTra chưa) → luôn chính xác, không phụ thuộc vào cột `trangthai`.

**Q: TextWatcher là gì? Dùng để làm gì?**
> `TextWatcher` là interface lắng nghe sự thay đổi của EditText theo từng ký tự. `afterTextChanged()` được gọi mỗi khi nội dung thay đổi → app filter danh sách realtime khi user gõ. Tiện hơn nút "Tìm" — user không cần bấm nút, kết quả hiện ngay.

**Q: EditText.setError() là gì?**
> Phương thức hiển thị thông báo lỗi ngay bên dưới ô nhập liệu (có icon dấu chấm than đỏ). Thuận tiện cho validation form — user biết ngay ô nào bị lỗi mà không cần Toast riêng.

**Q: Tại sao lịch sử mượn của bạn đọc ORDER BY DESC?**
> `ORDER BY pm.pm_id DESC` — phiếu mượn gần nhất (id lớn nhất) hiện trước. Người dùng thường quan tâm đến lịch sử gần nhất — UX tốt hơn khi không phải cuộn xuống để xem phiếu mới.
