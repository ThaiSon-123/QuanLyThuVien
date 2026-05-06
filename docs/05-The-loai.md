# 05 — Quản lý Thể loại

> **Files chính:** `TheLoaiDetailActivity.java`, `TheLoaiEditActivity.java`, `db/TheLoaiDao.java`, `adapter/TheLoaiAdapter.java`

---

## Giải thích đơn giản

Thể loại được truy cập qua tab "Thể loại" trong màn Sách. App hiển thị danh sách thể loại dạng **grid 2 cột** — mỗi ô hiển thị tên thể loại và số sách trong đó. Admin có thể thêm/sửa/xóa thể loại.

---

## Sơ đồ luồng

```
SachActivity (tab Thể loại)
  → Grid 2 cột thể loại
       │
       ├─ Tap 1 thể loại → TheLoaiDetailActivity
       │       → Hiển thị list sách của thể loại này
       │       → [Admin] Sửa tên → TheLoaiEditActivity
       │       → [Admin] Xóa thể loại
       │
       └─ FAB "Thêm thể loại" → [Admin] TheLoaiEditActivity (mode thêm mới)
```

---

## model/TheLoai.java

```java
public class TheLoai {
    public int    tlId;
    public String ten;

    public TheLoai() {}

    public TheLoai(int tlId, String ten) {
        this.tlId = tlId;
        this.ten  = ten;
    }

    @Override
    public String toString() { return ten; }
}
```

**2 constructor:**
- `TheLoai()` rỗng: dùng khi tự set từng field.
- `TheLoai(id, ten)`: tiện cho DAO khi đọc từ DB một dòng.

**`toString()` trả về tên:** Tiện cho `ArrayAdapter`/`Spinner` — khi hiển thị object trong danh sách, Android tự gọi `toString()`.

---

## db/TheLoaiDao.java

### listAll()

```java
public List<TheLoai> listAll() {
    List<TheLoai> list = new ArrayList<>();
    SQLiteDatabase db = helper.getReadableDatabase();
    try (Cursor c = db.rawQuery(
            "SELECT tl_id, ten FROM TheLoai ORDER BY ten", null)) {
        while (c.moveToNext()) {
            list.add(new TheLoai(c.getInt(0), c.getString(1)));
        }
    }
    return list;
}
```

`ORDER BY ten` — sắp xếp theo tên thể loại alphabet.

---

### listWithSachCount() — SQL phức tạp nhất

```java
String sql =
    "SELECT tl.tl_id, tl.ten, COUNT(s.sach_id) AS sach_count " +
    "FROM TheLoai tl " +
    "LEFT JOIN Sach s ON s.tl_id = tl.tl_id " +
    "GROUP BY tl.tl_id, tl.ten " +
    "ORDER BY tl.ten";
```

**Giải thích từng phần:**

| Phần SQL | Ý nghĩa |
|----------|---------|
| `LEFT JOIN Sach s` | Ghép bảng Sach vào. LEFT JOIN → giữ thể loại dù chưa có sách |
| `COUNT(s.sach_id)` | Đếm số sách của mỗi thể loại |
| `GROUP BY tl.tl_id` | Gom nhóm theo thể loại — mỗi thể loại 1 dòng kết quả |
| `ORDER BY tl.ten` | Sắp xếp theo tên |

**Ví dụ kết quả:**
```
tl_id | ten        | sach_count
------|------------|----------
1     | Công nghệ  | 5
2     | Hóa học    | 2
3     | Văn học    | 8
```

---

### insert(), update(), delete()

```java
public long insert(TheLoai tl) {
    ContentValues cv = new ContentValues();
    cv.put("ten", tl.ten);
    return db.insert("TheLoai", null, cv);  // trả về tl_id mới
}

public int update(TheLoai tl) {
    ContentValues cv = new ContentValues();
    cv.put("ten", tl.ten);
    return db.update("TheLoai", cv, "tl_id = ?",
            new String[]{String.valueOf(tl.tlId)});
}

public int delete(int tlId) {
    return db.delete("TheLoai", "tl_id = ?",
            new String[]{String.valueOf(tlId)});
}
```

**Lưu ý khi xóa:** Nếu còn sách đang dùng thể loại này → Foreign Key constraint ngăn DELETE → app catch exception và thông báo user phải chuyển hoặc xóa sách trước.

---

## TheLoaiAdapter.java — Grid 2 cột

Trong `SachActivity` khi chuyển sang tab Thể loại:

```java
GridLayoutManager glm = new GridLayoutManager(this, 2); // 2 cột
rvTheLoai.setLayoutManager(glm);
```

**`GridLayoutManager(context, 2)`:** Khác `LinearLayoutManager` (1 cột), hiển thị dạng lưới 2 cột.

---

## TheLoaiDetailActivity.java

```java
tlId = getIntent().getIntExtra(EXTRA_TL_ID, 0);
tl   = theLoaiDao.findById(tlId);
if (tl == null) { finish(); return; }

tvTen.setText(tl.ten);
List<Sach> sachList = sachDao.listByTheLoai(tlId);
sachAdapter.submit(sachList);
```

Nhận `tl_id` từ Intent → load chi tiết thể loại → load danh sách sách của thể loại đó.

---

## Câu hỏi thầy hay hỏi

**Q: Thể loại và Sách có quan hệ gì trong database?**
> Quan hệ **1 - nhiều (One-to-Many)**: 1 thể loại có nhiều sách, 1 sách chỉ thuộc 1 thể loại. Thể hiện bằng cột `tl_id` trong bảng `Sach` — đây là **khóa ngoại (Foreign Key)** tham chiếu `TheLoai.tl_id`.

**Q: GridLayoutManager khác LinearLayoutManager gì?**
> `LinearLayoutManager` hiển thị danh sách 1 cột dọc (hoặc ngang). `GridLayoutManager` hiển thị dạng lưới nhiều cột. App dùng `GridLayoutManager(this, 2)` → grid 2 cột cho thể loại.

**Q: GROUP BY trong SQL dùng để làm gì?**
> Gom các dòng có cùng giá trị vào 1 nhóm, thường dùng với hàm tổng hợp như `COUNT`, `SUM`, `AVG`. Ví dụ: `GROUP BY tl.tl_id` → gom tất cả sách cùng thể loại → `COUNT(s.sach_id)` đếm số sách từng thể loại → kết quả: mỗi thể loại 1 dòng kèm số sách.

**Q: Tại sao dùng LEFT JOIN để đếm sách, không dùng INNER JOIN?**
> INNER JOIN loại bỏ thể loại không có sách nào. LEFT JOIN giữ tất cả thể loại, thể loại không có sách thì `COUNT = 0`. App hiển thị tất cả thể loại kể cả mới tạo chưa có sách → phải LEFT JOIN.

**Q: Tại sao xóa thể loại có thể thất bại?**
> Vì **Foreign Key constraint** — SQLite kiểm tra: nếu còn sách đang có `tl_id` trỏ đến thể loại cần xóa → từ chối xóa để bảo vệ tính toàn vẹn dữ liệu. App xử lý: kiểm tra trước xem còn sách không, nếu có thì thông báo user cần chuyển/xóa sách trước.
