# 06 — Quản lý Mượn Trả

> **Files chính:** `MuonTraActivity.java`, `PhieuMuonDetailActivity.java`, `PhieuTraDetailActivity.java`, `PhieuMuonAddActivity.java`, `PhieuTraAddActivity.java`, `db/PhieuMuonDao.java`, `db/PhieuTraDao.java`

---

## Giải thích đơn giản

Mượn trả là chức năng trung tâm của thư viện. Quy trình:
1. Bạn đọc muốn mượn sách → nhân viên **lập Phiếu Mượn** (chọn bạn đọc + chọn sách + số lượng + hạn trả).
2. Khi bạn đọc trả sách → nhân viên **lập Phiếu Trả** (gắn với phiếu mượn ban đầu, chọn sách trả).
3. Kho sách được **trigger tự động cập nhật** — không cần code thủ công.

---

## Sơ đồ quan hệ bảng

```
BanDoc ─────→ PhieuMuon ─────→ ChiTietMuon ─────→ Sach
NhanVien ───↗              │
                           └──→ PhieuTra ──────→ ChiTietTra ─→ Sach
```

---

## Sơ đồ luồng màn hình

```
MuonTraActivity
  ├─ Tab "Phiếu Mượn"
  │       ├─ List phiếu mượn (lọc: Đang mượn / Đã trả / Tất cả)
  │       ├─ Tap → PhieuMuonDetailActivity
  │       │         ├─ Xem chi tiết sách mượn
  │       │         └─ Nút "Lập phiếu trả" → PhieuTraAddActivity
  │       └─ FAB "Lập phiếu mượn" → PhieuMuonAddActivity
  │
  └─ Tab "Phiếu Trả"
          ├─ List phiếu trả (pill Trễ hạn / Đúng hạn)
          └─ Tap → PhieuTraDetailActivity
```

---

## db/PhieuMuonDao.java

### listAll() — query phức tạp

```java
public List<PhieuMuon> listAll() {
    String sql =
        "SELECT pm.pm_id, pm.ngay_muon, pm.ngay_tra, pm.trangthai, " +
        "       bd.ten AS ten_bd, " +
        "       EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) AS da_tra " +
        "FROM PhieuMuon pm " +
        "LEFT JOIN BanDoc bd ON bd.bd_id = pm.bd_id " +
        "ORDER BY pm.pm_id DESC";
    // ...
}
```

**`EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id) AS da_tra`:**
- Subquery kiểm tra: phiếu mượn này **có** phiếu trả chưa?
- `EXISTS` trả về `1` (true) nếu có ít nhất 1 dòng khớp, `0` (false) nếu không có.
- Dùng cách này thay vì dựa vào cột `trangthai` (không đáng tin cậy) → derive trạng thái từ dữ liệu thực.

**Tại sao không dùng cột `trangthai` sẵn có?**
Cột `trangthai` phải được cập nhật thủ công → dễ quên, dễ sai. `EXISTS` query thẳng vào PhieuTra → luôn chính xác, không bao giờ out-of-sync.

---

### Phân tích trạng thái phiếu mượn

```java
// Trong loadData() của MuonTraActivity:
String status;
if (daTraFlag == 1) {
    status = "datra";
} else {
    // Kiểm tra có quá hạn không
    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(new Date());
    status = (p.ngayTra != null && today.compareTo(p.ngayTra) > 0)
             ? "quahan" : "dangmuon";
}
```

**3 trạng thái hiển thị:**
| Status | Màu pill | Điều kiện |
|--------|----------|-----------|
| `"datra"` | Xanh lá | PhieuTra tồn tại |
| `"quahan"` | Đỏ | Chưa trả + ngày hôm nay > hạn trả |
| `"dangmuon"` | Xanh dương | Chưa trả + còn trong hạn |

**So sánh ngày bằng chuỗi:** `today.compareTo(p.ngayTra) > 0` hoạt động đúng vì format `yyyy-MM-dd` đảm bảo sắp xếp lexicographic = sắp xếp thời gian.

---

## PhieuMuonAddActivity.java — Lập phiếu mượn

### Quy trình lập phiếu

```
1. Chọn Bạn đọc (Spinner hoặc Dialog tìm kiếm)
2. Chọn Ngày mượn (DatePicker hoặc mặc định hôm nay)
3. Chọn Hạn trả (DatePicker)
4. Thêm sách:
   │  └─ Dialog chọn sách → nhập số lượng → Add vào list
5. Bấm "Lưu":
   │  ├─ Validate (có bạn đọc? có sách? số lượng hợp lệ?)
   │  ├─ INSERT vào PhieuMuon → lấy pm_id mới
   │  ├─ INSERT từng dòng vào ChiTietMuon (kích trigger trừ kho)
   │  └─ finish()
```

### Insert phiếu mượn

```java
private void savePhieu() {
    // 1. Insert PhieuMuon
    PhieuMuon pm = new PhieuMuon();
    pm.bdId     = selectedBanDoc.bdId;
    pm.nvId     = currentNvId;
    pm.ngayMuon = selectedNgayMuon;  // "yyyy-MM-dd"
    pm.ngayTra  = selectedHanTra;    // "yyyy-MM-dd"
    long pmId = phieuMuonDao.insert(pm);

    // 2. Insert từng ChiTietMuon → trigger tự trừ kho
    for (ChiTietItem ct : chiTietList) {
        phieuMuonDao.insertChiTiet((int) pmId, ct.sachId, ct.soluong);
    }
    finish();
}
```

**Tại sao cần `pmId` từ bước 1?**
`pmId` được SQLite tự tạo (AUTOINCREMENT). Phải insert PhieuMuon trước → lấy id mới → dùng id đó khi insert ChiTietMuon (`pm_id` là khóa ngoại).

**`db.insert()` trả về gì?** Row id của dòng vừa insert (= pm_id mới). Trả `-1` nếu thất bại.

---

## PhieuTraAddActivity.java — Lập phiếu trả

### Khác phiếu mượn ở chỗ nào?

1. Phải chọn **Phiếu Mượn gốc** (PM nào cần trả).
2. Danh sách sách được **tự động điền** từ ChiTietMuon của PM đó.
3. User chỉ xác nhận số lượng trả.

```java
private void loadChiTietFromPM(int pmId) {
    List<ChiTietMuon> ctList = phieuMuonDao.getChiTiet(pmId);
    chiTietTraList.clear();
    for (ChiTietMuon ct : ctList) {
        chiTietTraList.add(new ChiTietTraItem(ct.sachId, ct.tenSach, ct.soluong));
    }
    adapter.submit(chiTietTraList);
}
```

### Insert phiếu trả

```java
private void savePhieuTra() {
    PhieuTra pt = new PhieuTra();
    pt.pmId    = selectedPmId;
    pt.ngayTra = selectedNgayTra;
    long ptId = phieuTraDao.insert(pt);

    for (ChiTietTraItem ct : chiTietTraList) {
        phieuTraDao.insertChiTiet((int) ptId, ct.sachId, ct.soluong);
        // → trigger tự cộng kho
    }
    finish();
}
```

---

## db/PhieuTraDao.java

### listAll() — Kèm hạn trả từ PhieuMuon

```java
String sql =
    "SELECT pt.pt_id, pt.pm_id, pt.ngay_tra, pt.tienphat, " +
    "       bd.ten AS ten_bd, " +
    "       pm.ngay_tra AS ngay_han_tra " +   // hạn trả gốc
    "FROM PhieuTra pt " +
    "JOIN PhieuMuon pm ON pm.pm_id = pt.pm_id " +
    "JOIN BanDoc bd ON bd.bd_id = pm.bd_id " +
    "ORDER BY pt.pt_id DESC";
```

**`pm.ngay_tra AS ngay_han_tra`:** Lấy hạn trả dự kiến từ PhieuMuon (đây là ngày user phải trả). `pt.ngay_tra` là ngày thực tế trả.

**Tại sao cần cả 2?** Để tính xem trả **đúng hạn** hay **trễ hạn**:
```java
// Trong MuonTraActivity và PhieuTraDetailActivity:
boolean treHan = pt.ngayTra.compareTo(pt.ngayHanTra) > 0;
// ngayTra = ngày thực tế trả
// ngayHanTra = hạn trả (lấy từ PhieuMuon.ngay_tra)
```

---

## PhieuTraDetailActivity.java — Pill trễ hạn

```java
private void loadData() {
    PhieuTra pt = phieuTraDao.findById(ptId);

    tvMaPhieu.setText(pt.getMaPhieu());
    tvNgayTao.setText("Ngày trả: " + MuonTraActivity.formatDate(pt.ngayTra));
    tvHanTra.setText("Hạn trả: " + MuonTraActivity.formatDate(pt.ngayHanTra));

    boolean treHan = pt.ngayTra != null && pt.ngayHanTra != null
            && pt.ngayTra.compareTo(pt.ngayHanTra) > 0;

    tvTinhTrang.setVisibility(View.VISIBLE);
    tvTinhTrang.setText(treHan ? "Trễ hạn" : "Đúng hạn");

    GradientDrawable pill = new GradientDrawable();
    pill.setShape(GradientDrawable.RECTANGLE);
    pill.setCornerRadius(24f);
    pill.setColor(treHan ? 0xFFE04D4D : 0xFF2F8A3E); // đỏ hoặc xanh lá
    tvTinhTrang.setBackground(pill);
    tvTinhTrang.setTextColor(0xFFFFFFFF);
}
```

**`GradientDrawable` tạo động:** Thay vì tạo file XML drawable mới (user yêu cầu không thêm file), tạo shape background bằng code Java. `setCornerRadius(24f)` → bo góc tạo hình viên thuốc (pill shape).

---

## MuonTraActivity.java — Màn list chính

### Tab + Filter

```java
private static final int TAB_MUON = 0;
private static final int TAB_TRA  = 1;
private static final String STATUS_ALL     = "all";
private static final String STATUS_DANGMUON = "dangmuon";
private static final String STATUS_DATRA    = "datra";
```

**Tab Phiếu Mượn:** Có nút lọc (Tất cả / Đang mượn / Đã trả). Khi switch sang tab → reset filter về "Tất cả".

**Tab Phiếu Trả:** Hiển thị pill "Trễ hạn" (đỏ) / "Đúng hạn" (xanh). Không có filter riêng.

### showFilterDialog() — cho tab Mượn

```java
private void showFilterDialog() {
    String[] options = {"Tất cả", "Đang mượn", "Đã trả"};
    String[] values  = {STATUS_ALL, STATUS_DANGMUON, STATUS_DATRA};
    int currentIdx   = 0;
    if (STATUS_DANGMUON.equals(currentStatus)) currentIdx = 1;
    else if (STATUS_DATRA.equals(currentStatus)) currentIdx = 2;

    new AlertDialog.Builder(this)
            .setTitle("Lọc phiếu mượn")
            .setSingleChoiceItems(options, currentIdx, (dialog, which) -> {
                currentStatus = values[which];
            })
            .setPositiveButton("Áp dụng", (d, w) -> loadData())
            .setNegativeButton("Hủy", null)
            .show();
}
```

**`setSingleChoiceItems`:** Tạo dialog với radio button — user chỉ chọn được 1 option. Khác `setMultiChoiceItems` (checkbox nhiều lựa).

### loadData() — load list phiếu theo tab

```java
private void loadData() {
    if (currentTab == TAB_MUON) {
        List<PhieuMuon> allPm = phieuMuonDao.listAll();
        List<PhieuAdapter.Row> rows = new ArrayList<>();
        for (PhieuMuon p : allPm) {
            // Determine status
            String status;
            if (p.daTraFlag == 1) status = "datra";
            else status = isOverdue(p.ngayTra) ? "quahan" : "dangmuon";

            // Filter theo currentStatus
            if (!STATUS_ALL.equals(currentStatus) && !status.contains(currentStatus))
                continue;

            // Filter keyword (không dấu)
            if (!currentKeyword.isEmpty()) {
                String kw = norm(currentKeyword);
                if (!norm(p.maPhieu).contains(kw) && !norm(p.tenBanDoc).contains(kw))
                    continue;
            }
            rows.add(new PhieuAdapter.Row(p.pmId, p.getMaPhieu(),
                    p.tenBanDoc, p.ngayMuon, p.ngayTra, status, "muon"));
        }
        adapter.submit(rows);
        tvCount.setText(rows.size() + " Phiếu mượn");
    } else {
        // Tab TRA — tương tự, tính trễ hạn
    }
}
```

---

## adapter/PhieuAdapter.java

### Row — đối tượng truyền vào adapter

```java
public static class Row {
    public int    id;
    public String ma;           // "PM-001" hoặc "PT-001"
    public String tenBanDoc;
    public String ngayMuon;
    public String ngayTra;      // hạn trả (PM) hoặc ngày trả thực (PT)
    public String status;       // "dangmuon", "datra", "quahan", "dunghạn", "trehan"
    public String type;         // "muon" hoặc "tra"
}
```

### onBindViewHolder() — pill màu sắc

```java
if ("datra".equals(r.status)) {
    holder.tvStatus.setText("Đã trả");
    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_datra); // xanh lá
} else if ("quahan".equals(r.status)) {
    holder.tvStatus.setText("Quá hạn");
    // GradientDrawable đỏ
} else if ("dangmuon".equals(r.status)) {
    holder.tvStatus.setText("Đang mượn");
    // Xanh dương
} else if ("trehan".equals(r.status)) {
    holder.tvStatus.setText("Trễ hạn");
    GradientDrawable red = new GradientDrawable();
    red.setColor(0xFFE04D4D); red.setCornerRadius(24f);
    holder.tvStatus.setBackground(red);
}
```

---

## formatDate() — Định dạng hiển thị ngày

```java
public static String formatDate(String isoDate) {
    if (isoDate == null || isoDate.isEmpty()) return "—";
    try {
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat disp = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return disp.format(sdf.parse(isoDate));
    } catch (ParseException e) {
        return isoDate;
    }
}
```

Convert từ format DB `"2026-03-15"` → format hiển thị `"15/03/2026"`.

---

## Câu hỏi thầy hay hỏi

**Q: Khi mượn sách, kho sách cập nhật thế nào?**
> Khi INSERT dòng vào `ChiTietMuon`, trigger `trg_muon_sach` tự động chạy — UPDATE bảng Sach: `soluong = soluong - NEW.soluong`. Nếu `soluong <= 0` thì `trangthai = 'het'`. Không cần code Java gọi thêm gì — trigger lo tất cả.

**Q: Làm sao biết phiếu mượn đã được trả chưa?**
> Dùng subquery `EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)`. Nếu có dòng trong PhieuTra với pm_id tương ứng → trả về 1 (đã trả). Không dùng cột `trangthai` của PhieuMuon vì có thể không được cập nhật đúng.

**Q: Phiếu mượn và phiếu trả liên kết với nhau thế nào?**
> Bảng `PhieuTra` có cột `pm_id` là Foreign Key trỏ đến `PhieuMuon.pm_id`. Một phiếu mượn có thể có 0 hoặc 1 phiếu trả tương ứng. Khi lập phiếu trả, nhân viên phải chọn phiếu mượn gốc → `pm_id` được lưu vào PhieuTra.

**Q: Trễ hạn được xác định thế nào?**
> So sánh chuỗi ISO date: `ngayTraThucTe.compareTo(ngayHanTra) > 0`. Ví dụ: hạn trả `"2026-03-10"`, ngày thực tế trả `"2026-03-15"` → `"2026-03-15" > "2026-03-10"` → trễ hạn. So sánh chuỗi theo thứ tự từ điển hoạt động đúng với format `yyyy-MM-dd`.

**Q: Tại sao mã phiếu hiển thị "PM-001" nhưng trong DB chỉ lưu số nguyên?**
> `pm_id` trong DB là số nguyên (INTEGER PRIMARY KEY AUTOINCREMENT). Chuỗi "PM-001" được tạo bởi method `getMaPhieu()` trong model: `return "PM-" + String.format("%03d", pmId)` — format số thành 3 chữ số với leading zero. Không tốn thêm cột, không khó tìm kiếm.

**Q: Phiếu mượn có mấy trạng thái?**
> 3 trạng thái: **Đang mượn** (chưa trả, còn trong hạn), **Quá hạn** (chưa trả, đã qua hạn trả), **Đã trả** (có phiếu trả tương ứng). Phiếu trả có 2 trạng thái: **Đúng hạn** và **Trễ hạn** dựa trên so sánh ngày trả thực tế với hạn trả gốc.
