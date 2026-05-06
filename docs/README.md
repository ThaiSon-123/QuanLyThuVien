# Tài liệu chức năng — Quản Lý Thư Viện

Tài liệu giải thích toàn bộ code theo từng chức năng. Mỗi file có: **giải thích đơn giản → code chi tiết → câu hỏi thầy hay hỏi**.

---

## Danh sách file

| File | Chức năng chính |
|------|----------------|
| `01-Database.md` | SQLite: 9 bảng, 2 trigger, singleton, seed data |
| `02-Dang-nhap.md` | Đăng nhập, SharedPreferences, phân quyền |
| `03-Trang-chu.md` | Dashboard, thống kê, greeting theo giờ |
| `04-Sach.md` | CRUD sách, filter, tìm kiếm không dấu, RecyclerView |
| `05-The-loai.md` | CRUD thể loại, grid 2 cột, GROUP BY |
| `06-Muon-tra.md` | Phiếu mượn/trả, trigger kho, trễ hạn/đúng hạn |
| `07-Ban-doc.md` | Quản lý bạn đọc, lịch sử mượn |
| `08-Nhan-vien.md` | Nhân viên, transaction 2 bảng, vô hiệu hóa |
| `09-Bao-cao.md` | 4 loại báo cáo, COUNT/GROUP BY/strftime |
| `10-Lien-he.md` | Liên hệ, Intent gọi điện, tìm kiếm |

---

## Kiến trúc tổng thể

```
┌───────────────────── UI LAYER ─────────────────────┐
│  Activities (extends AppCompatActivity)             │
│   ├── LoginActivity, MainActivity                   │
│   ├── SachActivity, SachDetailActivity, SachAdd     │
│   ├── MuonTraActivity, PhieuMuon/Tra Detail + Add   │
│   ├── BanDocActivity, NhanVienActivity              │
│   ├── BaoCaoActivity, LienHeActivity                │
│                                                     │
│  Adapters (RecyclerView)                            │
│   SachAdapter, PhieuAdapter, BanDocAdapter...       │
│                                                     │
│  Layouts XML: activity_*.xml, item_*.xml            │
└────────────────────────────────────────────────────┘
                        ↓
┌───────────────────── DATA LAYER ───────────────────┐
│  Models (POJO): Sach, BanDoc, PhieuMuon...          │
│  DAOs: SachDao, BanDocDao, PhieuMuonDao...          │
│  DatabaseHelper (Singleton)                         │
│  SQLite: 9 bảng, 2 trigger                         │
└────────────────────────────────────────────────────┘
```

---

## Các pattern quan trọng dùng trong app

### 1. Singleton DatabaseHelper
```java
public static synchronized DatabaseHelper getInstance(Context context) {
    if (instance == null)
        instance = new DatabaseHelper(context.getApplicationContext());
    return instance;
}
```
Đảm bảo toàn app chỉ có 1 kết nối DB duy nhất → tránh xung đột.

### 2. DAO Pattern
Mỗi bảng có 1 DAO class. Activity **không bao giờ** gọi SQL trực tiếp — luôn qua DAO. Dễ test, dễ thay đổi SQL mà không ảnh hưởng UI.

### 3. ViewHolder Pattern (RecyclerView)
```java
static class VH extends RecyclerView.ViewHolder {
    TextView tvTen;
    VH(View v) { super(v); tvTen = v.findViewById(R.id.tvTen); }
}
```
`findViewById()` chỉ chạy 1 lần khi tạo item → cache reference → scroll mượt.

### 4. Static Factory Intent
```java
public static Intent newIntent(Context ctx, int id) {
    Intent i = new Intent(ctx, DetailActivity.class);
    i.putExtra(EXTRA_ID, id);
    return i;
}
```
Truyền dữ liệu giữa Activity an toàn, rõ ràng, không hardcode key.

### 5. onResume() Reload
Mọi list Activity gọi `loadData()` trong `onResume()` → quay về từ màn thêm/sửa thì list tự refresh.

### 6. Tìm kiếm không dấu (norm)
```java
private static String norm(String s) {
    String nfd = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD);
    return nfd.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
              .replace('đ', 'd');
}
```
Dùng ở SachActivity, BanDocActivity, MuonTraActivity, LienHeActivity.

### 7. GradientDrawable tạo pill màu sắc
```java
GradientDrawable pill = new GradientDrawable();
pill.setShape(GradientDrawable.RECTANGLE);
pill.setCornerRadius(24f);
pill.setColor(0xFFE04D4D); // đỏ
textView.setBackground(pill);
```
Tạo background bo góc bằng code, không cần thêm file XML.

### 8. Trigger SQLite tự động cập nhật kho
- `trg_muon_sach`: AFTER INSERT ON ChiTietMuon → trừ kho.
- `trg_tra_sach`: AFTER INSERT ON ChiTietTra → cộng kho lại.

---

## Câu hỏi thầy hay hỏi — Tổng quát

**Q: App dùng database gì? Lưu ở đâu?**
> SQLite — file `quanlythuvien.db` lưu tại `/data/data/com.example.quanlythuvien/databases/`. Không cần server, không cần internet. Android hỗ trợ sẵn.

**Q: App có bao nhiêu bảng?**
> 9 bảng: `Users`, `NhanVien`, `BanDoc`, `TheLoai`, `Sach`, `PhieuMuon`, `ChiTietMuon`, `PhieuTra`, `ChiTietTra`.

**Q: App có bao nhiêu trigger? Làm gì?**
> 2 trigger: `trg_muon_sach` (AFTER INSERT ON ChiTietMuon — trừ kho sách) và `trg_tra_sach` (AFTER INSERT ON ChiTietTra — cộng kho lại).

**Q: Phân quyền trong app như thế nào?**
> 2 role: `admin` và `nhanvien`. Role lưu trong SharedPreferences sau đăng nhập. Các chức năng nhạy cảm (thêm/sửa/xóa sách, quản lý nhân viên) kiểm tra role trước khi cho phép. Admin thấy tất cả, nhân viên chỉ xem + lập phiếu.

**Q: App xử lý tiếng Việt trong tìm kiếm thế nào?**
> Dùng `java.text.Normalizer` với chuẩn NFD để tách ký tự có dấu, sau đó xóa phần dấu, replace đ→d. Cả từ khóa và dữ liệu đều được normalize trước khi so sánh. SQLite LIKE không hỗ trợ tiếng Việt nên phải filter ở tầng Java.

**Q: Khi mượn sách, số lượng kho cập nhật thế nào?**
> Trigger `trg_muon_sach` tự động chạy khi INSERT vào `ChiTietMuon` — `UPDATE Sach SET soluong = soluong - NEW.soluong`. Không cần code Java gọi thêm. Khi trả — trigger `trg_tra_sach` cộng lại.

**Q: Sự khác biệt giữa admin và nhân viên là gì?**
> Admin: thêm/sửa/xóa sách, thể loại, bạn đọc; quản lý nhân viên; xem báo cáo. Nhân viên: lập phiếu mượn/trả; xem danh sách; không thể thêm/sửa/xóa sách.
