# 09 — Báo cáo

> **Files chính:** `BaoCaoActivity.java`, `BaoCaoChiTietActivity.java`, `db/BaoCaoDao.java`

---

## Giải thích đơn giản

Chức năng Báo cáo tổng hợp 4 loại thống kê từ dữ liệu trong DB và hiển thị dưới dạng danh sách. Mỗi loại báo cáo có logic query SQL riêng, nhưng dùng chung 1 màn hình chi tiết (`BaoCaoChiTietActivity`) với adapter hiển thị kết quả.

---

## 4 Loại Báo cáo

| Loại | Nội dung | SQL chính |
|------|---------|-----------|
| `SACH_MUON_NHIEU` | Sách được mượn nhiều nhất | COUNT(ChiTietMuon) GROUP BY sach_id |
| `BAN_DOC_MUON_NHIEU` | Bạn đọc mượn nhiều nhất | COUNT(PhieuMuon) GROUP BY bd_id |
| `SACH_HET` | Sách đã hết trong kho | WHERE soluong <= 0 |
| `THONG_KE_THANG` | Thống kê theo tháng | GROUP BY strftime('%Y-%m', ngay_muon) |

---

## Sơ đồ luồng

```
BaoCaoActivity (4 card báo cáo)
  │
  ├─ Card 1: "Sách mượn nhiều nhất"
  ├─ Card 2: "Bạn đọc mượn nhiều nhất"
  ├─ Card 3: "Sách hết kho"
  └─ Card 4: "Thống kê theo tháng"
       │
       └─ Tap card → BaoCaoChiTietActivity(type = "SACH_MUON_NHIEU")
                         └─ BaoCaoDao.getReport(type) → List<Row>
                         └─ Adapter hiển thị kết quả
```

---

## db/BaoCaoDao.java

### Báo cáo 1: Sách mượn nhiều nhất

```java
public List<Object[]> sachMuonNhieu(int limit) {
    String sql =
        "SELECT s.ten, s.tacgia, COUNT(ctm.sach_id) AS so_lan_muon " +
        "FROM ChiTietMuon ctm " +
        "JOIN Sach s ON s.sach_id = ctm.sach_id " +
        "GROUP BY ctm.sach_id " +
        "ORDER BY so_lan_muon DESC " +
        "LIMIT ?";
    // ...
}
```

**Giải thích:**
- `COUNT(ctm.sach_id)` — đếm số lần xuất hiện trong ChiTietMuon = số lần được mượn.
- `GROUP BY ctm.sach_id` — gom theo từng cuốn sách.
- `ORDER BY so_lan_muon DESC` — sách mượn nhiều nhất lên đầu.
- `LIMIT ?` — chỉ lấy top N (vd top 10).

**Ví dụ kết quả:**
```
ten              | tacgia   | so_lan_muon
-----------------|----------|------------
Java cơ bản      | ABC      | 15
Dế Mèn phiêu... | Tô Hoài  | 12
```

---

### Báo cáo 2: Bạn đọc mượn nhiều nhất

```java
public List<Object[]> banDocMuonNhieu(int limit) {
    String sql =
        "SELECT bd.ten, bd.sdt, COUNT(pm.pm_id) AS so_phieu " +
        "FROM PhieuMuon pm " +
        "JOIN BanDoc bd ON bd.bd_id = pm.bd_id " +
        "GROUP BY pm.bd_id " +
        "ORDER BY so_phieu DESC " +
        "LIMIT ?";
    // ...
}
```

**Tương tự báo cáo 1** nhưng đếm phiếu mượn thay vì chi tiết mượn.

---

### Báo cáo 3: Sách hết kho

```java
public List<Sach> sachHetKho() {
    String sql =
        "SELECT s.sach_id, s.ten, s.tacgia, s.soluong " +
        "FROM Sach s " +
        "WHERE s.soluong <= 0 " +
        "ORDER BY s.ten";
    // ...
}
```

Đơn giản nhất — filter sluong <= 0.

---

### Báo cáo 4: Thống kê theo tháng

```java
public List<Object[]> thongKeTheo(String loai) {
    String dateExpr;
    if ("month".equals(loai)) {
        dateExpr = "strftime('%Y-%m', pm.ngay_muon)";
    } else if ("year".equals(loai)) {
        dateExpr = "strftime('%Y', pm.ngay_muon)";
    } else {
        dateExpr = "pm.ngay_muon";  // by day
    }

    String sql =
        "SELECT " + dateExpr + " AS period, " +
        "       COUNT(pm.pm_id) AS so_muon, " +
        "       COUNT(pt.pt_id) AS so_tra " +
        "FROM PhieuMuon pm " +
        "LEFT JOIN PhieuTra pt ON pt.pm_id = pm.pm_id " +
        "GROUP BY period " +
        "ORDER BY period DESC";
    // ...
}
```

**`strftime('%Y-%m', ngay_muon)`:** Hàm SQLite format chuỗi ngày. Với ngày `"2026-03-15"`:
- `'%Y-%m'` → `"2026-03"` (tháng)
- `'%Y'` → `"2026"` (năm)

Nhóm theo tháng → thống kê số mượn/trả từng tháng.

**Ví dụ kết quả:**
```
period   | so_muon | so_tra
---------|---------|-------
2026-05  | 8       | 5
2026-04  | 12      | 10
2026-03  | 6       | 6
```

---

## BaoCaoActivity.java

### Hiển thị 4 card báo cáo

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_baocao);

    // 4 card click
    findViewById(R.id.cardSachMuonNhieu)
            .setOnClickListener(v -> openReport("SACH_MUON_NHIEU"));
    findViewById(R.id.cardBanDocMuonNhieu)
            .setOnClickListener(v -> openReport("BAN_DOC_MUON_NHIEU"));
    findViewById(R.id.cardSachHet)
            .setOnClickListener(v -> openReport("SACH_HET"));
    findViewById(R.id.cardThongKeThang)
            .setOnClickListener(v -> openReport("THONG_KE_THANG"));
}

private void openReport(String type) {
    Intent i = new Intent(this, BaoCaoChiTietActivity.class);
    i.putExtra("report_type", type);
    startActivity(i);
}
```

**String constant làm "type":** Truyền qua Intent dưới dạng String. `BaoCaoChiTietActivity` đọc type → quyết định gọi DAO method nào và hiển thị thế nào.

---

## BaoCaoChiTietActivity.java

### Switch theo loại báo cáo

```java
private void loadReport() {
    String type = getIntent().getStringExtra("report_type");
    List<BaoCaoAdapter.Row> rows = new ArrayList<>();

    switch (type) {
        case "SACH_MUON_NHIEU":
            tvTitle.setText("Sách mượn nhiều nhất");
            List<Object[]> sm = baoCaoDao.sachMuonNhieu(20);
            for (Object[] r : sm) {
                rows.add(new BaoCaoAdapter.Row(
                    (String) r[0],           // tên sách
                    (String) r[1],           // tác giả
                    r[2] + " lần mượn"       // số lần
                ));
            }
            break;
        case "BAN_DOC_MUON_NHIEU":
            // ...
            break;
        case "SACH_HET":
            // ...
            break;
        case "THONG_KE_THANG":
            // ...
            break;
    }
    adapter.submit(rows);
    tvCount.setText(rows.size() + " kết quả");
}
```

**Pattern Strategy:** Dùng 1 Activity + 1 Adapter, switch loại báo cáo bằng String type → linh hoạt, thêm loại mới chỉ cần thêm case.

---

## BaoCaoAdapter.Row — Model hiển thị

```java
public static class Row {
    public String col1;   // cột chính (tên sách, tên bạn đọc, tháng...)
    public String col2;   // cột phụ (tác giả, sdt, số mượn...)
    public String badge;  // badge bên phải (số lần, số phiếu...)
}
```

Dùng 1 generic Row cho tất cả loại báo cáo — adapter không cần biết loại báo cáo, chỉ hiển thị 3 text.

---

## Câu hỏi thầy hay hỏi

**Q: App có bao nhiêu loại báo cáo? Mỗi loại lấy dữ liệu thế nào?**
> 4 loại: (1) Sách mượn nhiều nhất — COUNT(ChiTietMuon) GROUP BY sach_id ORDER BY count DESC. (2) Bạn đọc mượn nhiều nhất — COUNT(PhieuMuon) GROUP BY bd_id. (3) Sách hết kho — WHERE soluong <= 0. (4) Thống kê theo tháng — GROUP BY strftime('%Y-%m', ngay_muon) đếm số mượn/trả từng tháng.

**Q: strftime() trong SQLite là gì?**
> Hàm format chuỗi ngày của SQLite. `strftime('%Y-%m', '2026-03-15')` trả về `'2026-03'`. Dùng để GROUP BY tháng hoặc năm — thay vì so sánh ngày cụ thể, nhóm các ngày trong cùng tháng lại đếm chung. `%Y` = năm, `%m` = tháng, `%d` = ngày.

**Q: Tại sao dùng COUNT(*) thay vì đếm thủ công?**
> `COUNT()` là hàm tổng hợp (aggregate function) chạy trong DB engine — nhanh và hiệu quả hơn nhiều so với load hết dữ liệu về Java rồi đếm. DB có thể dùng index để đếm nhanh mà không cần quét hết bảng.

**Q: Sách hết kho query thế nào?**
> `SELECT ... FROM Sach WHERE soluong <= 0 ORDER BY ten`. Cột `soluong` được trigger tự động cập nhật mỗi khi mượn/trả → luôn phản ánh kho thực tế. Không cần join bảng phức tạp vì cột `soluong` đã sẵn trong bảng Sach.

**Q: Tại sao dùng chung 1 Activity cho 4 loại báo cáo?**
> Vì giao diện hiển thị tương tự nhau — đều là danh sách có tiêu đề, count, adapter. Truyền `report_type` qua Intent, Activity switch theo type để load data và hiển thị tiêu đề phù hợp. Thêm loại báo cáo mới chỉ cần thêm 1 case — không tạo Activity mới.
