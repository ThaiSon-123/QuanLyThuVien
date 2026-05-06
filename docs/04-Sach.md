# 04 — Quản lý Sách

> **Files chính:** `SachActivity.java`, `SachDetailActivity.java`, `SachAddActivity.java`, `db/SachDao.java`, `adapter/SachAdapter.java`

---

## Giải thích đơn giản

Chức năng Sách có 3 màn hình:
- **SachActivity** — danh sách sách, có tìm kiếm + lọc + 2 tab (Tất cả / Thể loại).
- **SachDetailActivity** — xem chi tiết 1 cuốn sách, admin có thể sửa/xóa.
- **SachAddActivity** — thêm sách mới (chỉ admin).

---

## Sơ đồ luồng

```
SachActivity (list)
  │
  ├─ Tap sách → SachDetailActivity
  │                 ├─ [Admin] Sửa → sachDao.update()
  │                 └─ [Admin] Xóa → sachDao.delete() → quay lại
  │
  └─ FAB "Thêm sách" → [Admin] SachAddActivity
                            └─ Lưu → sachDao.insert() → quay lại
```

---

## model/Sach.java

```java
public class Sach {
    public int    sachId;
    public String ten;
    public String tacgia;
    public String nxb;          // nhà xuất bản
    public int    namxb;        // năm xuất bản
    public int    soluong;      // số cuốn còn trong kho
    public String trangthai;    // "con" hoặc "het"
    public int    tlId;         // id thể loại
    public String tenTheLoai;   // tên thể loại (join từ DB, không có cột riêng)
    public String coverUri;     // URI ảnh bìa
}
```

**`tenTheLoai` là join field:** Không lưu trong bảng `Sach` — chỉ tồn tại khi query JOIN với bảng `TheLoai`. DAO tự set khi đọc dữ liệu.

---

## db/SachDao.java

### Hằng SELECT_COLS

```java
private static final String SELECT_COLS =
    "s.sach_id, s.ten, s.tacgia, s.nxb, s.namxb, s.soluong, s.trangthai, " +
    "s.tl_id, tl.ten, s.cover_uri";
```

**Tại sao tách ra?** Dùng lại ở nhiều query (`listAll`, `findById`, `filter`...). Nếu thêm cột mới chỉ sửa 1 chỗ này, không phải sửa từng query.

**Thứ tự index (dùng trong `readRow`):**

| Index | Cột | Kiểu |
|-------|-----|------|
| 0 | s.sach_id | int |
| 1 | s.ten | String |
| 2 | s.tacgia | String |
| 3 | s.nxb | String |
| 4 | s.namxb | int |
| 5 | s.soluong | int |
| 6 | s.trangthai | String |
| 7 | s.tl_id | int |
| 8 | tl.ten (tenTheLoai) | String |
| 9 | s.cover_uri | String |

---

### private query() — helper dùng chung

```java
private List<Sach> query(String where, String[] args) {
    List<Sach> list = new ArrayList<>();
    SQLiteDatabase db = helper.getReadableDatabase();
    String sql = "SELECT " + SELECT_COLS + " FROM Sach s "
               + "LEFT JOIN TheLoai tl ON s.tl_id = tl.tl_id";
    if (where != null) sql += " WHERE " + where;
    sql += " ORDER BY s.sach_id DESC";
    try (Cursor c = db.rawQuery(sql, args)) {
        while (c.moveToNext()) list.add(readRow(c));
    }
    return list;
}
```

**LEFT JOIN TheLoai:** Lấy tên thể loại cùng lúc. `LEFT JOIN` (thay vì `INNER JOIN`) để vẫn trả về sách **kể cả khi** sách chưa gán thể loại (`tl_id = NULL`).

**ORDER BY sach_id DESC:** Sách mới thêm (id lớn hơn) hiển thị trước.

**Pattern hay:** Thay vì viết 5 method query riêng, có 1 helper `query(where, args)` — các method public chỉ truyền khác nhau về `WHERE`.

---

### readRow() — đọc 1 dòng cursor

```java
private Sach readRow(Cursor c) {
    Sach s = new Sach();
    s.sachId     = c.getInt(0);
    s.ten        = c.getString(1);
    s.tacgia     = c.getString(2);
    s.nxb        = c.getString(3);
    s.namxb      = c.getInt(4);
    s.soluong    = c.getInt(5);
    s.trangthai  = c.getString(6);
    s.tlId       = c.getInt(7);
    s.tenTheLoai = c.getString(8);
    s.coverUri   = c.getString(9);
    return s;
}
```

Cô lập logic đọc Cursor → các method query không phải lo index cột.

---

### filter() — lọc nhiều điều kiện

```java
public List<Sach> filter(String keyword, String status, int tlId) {
    List<String> conds = new ArrayList<>();
    List<String> args  = new ArrayList<>();

    // Điều kiện 1: keyword (tên hoặc tác giả)
    if (keyword != null && !keyword.trim().isEmpty()) {
        String like = "%" + keyword.trim() + "%";
        conds.add("(s.ten LIKE ? OR s.tacgia LIKE ?)");
        args.add(like); args.add(like);
    }
    // Điều kiện 2: trạng thái tồn kho
    if ("con".equals(status))      conds.add("s.soluong > 0");
    else if ("het".equals(status)) conds.add("s.soluong <= 0");
    // Điều kiện 3: thể loại
    if (tlId > 0) {
        conds.add("s.tl_id = ?");
        args.add(String.valueOf(tlId));
    }

    // Ghép các điều kiện bằng AND
    String where = null;
    if (!conds.isEmpty()) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < conds.size(); i++) {
            if (i > 0) sb.append(" AND ");
            sb.append(conds.get(i));
        }
        where = sb.toString();
    }
    return query(where, args.toArray(new String[0]));
}
```

**Giải thích logic:**
- Mỗi điều kiện được thêm **độc lập** vào 2 list song song: `conds` chứa chuỗi SQL, `args` chứa giá trị tương ứng.
- Nếu không có điều kiện nào → `where = null` → query tất cả.
- `%keyword%` cho phép tìm chuỗi xuất hiện ở bất kỳ vị trí (đầu, giữa, cuối tên sách).

**Ví dụ:** Lọc sách "còn" thể loại id=2 có chữ "hóa":
```
WHERE (s.ten LIKE '%hóa%' OR s.tacgia LIKE '%hóa%') AND s.soluong > 0 AND s.tl_id = 2
```

**Lưu ý:** Hàm `filter()` nhận `keyword = null` và lọc tìm kiếm không dấu ở client-side (Java), không dùng SQL LIKE cho keyword — vì SQLite không hỗ trợ normalize Unicode tiếng Việt.

---

### insert() và update()

```java
public long insert(Sach s) {
    SQLiteDatabase db = helper.getWritableDatabase();
    ContentValues cv  = new ContentValues();
    cv.put("ten", s.ten);
    cv.put("tacgia", s.tacgia);
    cv.put("soluong", s.soluong);
    cv.put("trangthai", s.soluong > 0 ? "con" : "het");  // tự derive
    if (s.tlId > 0)      cv.put("tl_id", s.tlId);
    if (s.coverUri != null) cv.put("cover_uri", s.coverUri);
    return db.insert("Sach", null, cv);   // trả về sach_id mới
}

public int update(Sach s) {
    // tương tự insert nhưng dùng db.update() với WHERE sach_id = ?
    // putNull("tl_id") khi xóa thể loại (khác insert chỉ put khi có)
    return db.update("Sach", cv, "sach_id = ?",
            new String[]{String.valueOf(s.sachId)});
}
```

**`getWritableDatabase()`:** Cần khi ghi dữ liệu (insert/update/delete). Khác `getReadableDatabase()` chỉ đọc.

**`trangthai` derive từ `soluong`:** Đảm bảo 2 cột luôn đồng bộ — nếu soluong > 0 → "con", ngược lại → "het". Không cho phép nhập sai.

**`db.insert()` trả về:** row_id mới (bằng sach_id) nếu thành công, `-1` nếu thất bại.

---

## SachActivity.java

### Tìm kiếm không dấu — norm()

```java
private static String norm(String s) {
    if (s == null) return "";
    String nfd = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD);
    return nfd.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
              .replace('đ', 'd').replace('Đ', 'd');
}
```

**Giải thích từng bước với ví dụ "Đại Việt":**
1. `toLowerCase()` → "đại việt"
2. `Normalizer.normalize(..., NFD)` → tách "ạ" thành "a" + dấu nặng, "ê" + dấu sắc...
3. `replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")` → xóa hết dấu → "đai viet"
4. `.replace('đ', 'd')` → "dai viet"

**Kết quả:** Người dùng gõ "dai viet" hoặc "Đại Việt" đều tìm được sách "Đại Việt Sử Ký".

**Tại sao không dùng SQL LIKE?** SQLite không có hàm normalize Unicode tiếng Việt sẵn. Phải load hết data về Java rồi filter bằng `norm()`.

---

### applyFilter()

```java
private void applyFilter() {
    // Luôn pass null cho keyword — filter keyword ở client-side
    List<Sach> all = sachDao.filter(null, currentStatus, currentTlId);
    List<Sach> list;
    if (currentKeyword.trim().isEmpty()) {
        list = all;
    } else {
        String kw = norm(currentKeyword);
        list = new ArrayList<>();
        for (Sach s : all) {
            if (norm(s.ten).contains(kw) || norm(s.tacgia).contains(kw))
                list.add(s);
        }
    }
    sachAdapter.submit(list);
    tvCount.setText(list.size() + " sản phẩm");
    tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
}
```

**Tại sao tách 2 bước?**
1. SQL filter: lọc status và thể loại (có index, nhanh).
2. Java filter: lọc keyword sau khi normalize (SQL không làm được).

---

### Search Mode — toggle header

```java
private void enterSearchMode() {
    searchMode = true;
    tvTitle.setVisibility(View.GONE);       // ẩn tiêu đề
    btnSearch.setVisibility(View.GONE);      // ẩn icon kính lúp
    headerSearch.setVisibility(View.VISIBLE); // hiện ô nhập tìm kiếm
    edtSearch.requestFocus();
    showKeyboard(edtSearch);
}

private void exitSearchMode() {
    searchMode = false;
    hideKeyboard(edtSearch);
    edtSearch.setText("");
    currentKeyword = "";
    headerSearch.setVisibility(View.GONE);
    tvTitle.setVisibility(View.VISIBLE);
    btnSearch.setVisibility(View.VISIBLE);
    applyFilter();
}
```

**Kỹ thuật:** 2 `LinearLayout` chồng lên nhau trong header — 1 cái là tiêu đề + icon search bình thường, 1 cái là ô nhập tìm kiếm. Toggle `VISIBLE/GONE` để chuyển đổi giữa 2 trạng thái. Không cần navigate sang Activity mới.

---

### showFilterDialog() — Dialog lọc

```java
private void showFilterDialog() {
    View view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_filter_sach, null, false);
    // Inflate XML dialog thành View
    
    RadioGroup rgStatus  = view.findViewById(R.id.rgStatus);
    RadioGroup rgTheLoai = view.findViewById(R.id.rgTheLoai);

    // Preselect trạng thái đang chọn
    // Build RadioButton thể loại động từ DB...

    AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton("Áp dụng", null)   // null để tự xử lý dismiss
            .setNegativeButton("Đặt lại", null)
            .create();

    dialog.setOnShowListener(d -> {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
            // Đọc state radio → cập nhật currentStatus, currentTlId
            applyFilter();
            dialog.dismiss();
        });
    });
    dialog.show();
}
```

**Tại sao `setPositiveButton(..., null)` rồi override sau?**

Mặc định AlertDialog tự dismiss khi bấm button. Nếu muốn **validate trước** (vd: báo lỗi nếu chưa chọn gì mà không đóng dialog) → phải override click listener sau khi dialog show. Dùng `null` listener trong builder → rồi set listener thật trong `setOnShowListener`.

**RadioButton thể loại build động:**

```java
for (TheLoai tl : theLoaiList) {
    RadioButton rb = new RadioButton(this);
    rb.setText(tl.ten);
    rb.setTag(tl.tlId);   // lưu tlId vào tag để đọc lại
    rgTheLoai.addView(rb);
}
```

Thể loại load từ DB → số lượng không cố định → phải tạo RadioButton bằng code (không thể hardcode trong XML). `setTag()` lưu tlId vào view, đọc lại bằng `getTag()`.

---

### Tab Tất cả / Thể loại

```java
private void selectTab(int tab) {
    currentTab = tab;
    boolean isAll = (tab == TAB_ALL);

    // Đổi màu text và indicator
    tvTabAll.setTextColor(isAll ? 0xFF4696EB : 0xFF747474);
    indicatorAll.setBackgroundResource(
        isAll ? R.drawable.bg_tab_indicator : android.R.color.transparent);

    if (isAll) applyFilter();        // tab Tất cả → list sách
    else       loadCategoryView();   // tab Thể loại → grid thể loại
}
```

**Kỹ thuật tab thủ công (không dùng TabLayout):** Dùng 2 LinearLayout làm tab, thay đổi màu chữ và hiện/ẩn đường gạch xanh dưới tab đang active.

---

## SachAdapter.java — RecyclerView Adapter

### Pattern ViewHolder

```java
static class VH extends RecyclerView.ViewHolder {
    ImageView ivCover;
    TextView  tvTen, tvSoLuong;

    VH(@NonNull View itemView) {
        super(itemView);
        ivCover   = itemView.findViewById(R.id.ivCover);
        tvTen     = itemView.findViewById(R.id.tvTen);
        tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
    }
}
```

**ViewHolder pattern:** `findViewById()` chậm (duyệt cây View). ViewHolder cache 3 reference → chỉ `findViewById` 1 lần khi tạo item, các lần scroll chỉ set text/image → app mượt.

### onBindViewHolder() — SpannableString

```java
String prefix = "Có thể mượn : ";
String number = String.valueOf(s.soluong);
SpannableString sp = new SpannableString(prefix + number);
sp.setSpan(new ForegroundColorSpan(0xFF4696EB),
        prefix.length(), prefix.length() + number.length(),
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
holder.tvSoLuong.setText(sp);
```

**SpannableString:** Cho phép tô màu/style một phần của text. Ở đây: "Có thể mượn : " màu đen, phần số "5" màu xanh. Không cần 2 TextView riêng.

**Load ảnh bìa:**
```java
if (!TextUtils.isEmpty(s.coverUri)) {
    try {
        holder.ivCover.setImageURI(Uri.parse(s.coverUri));
    } catch (Exception e) {
        holder.ivCover.setImageResource(R.drawable.ic_book_placeholder);
    }
} else {
    holder.ivCover.setImageResource(R.drawable.ic_book_placeholder);
}
```

Try-catch tránh crash nếu URI hỏng (file bị xóa, SD card unmount...). Fallback về ảnh placeholder.

---

## SachDetailActivity — Image Picker

```java
private final ActivityResultLauncher<String[]> imagePicker =
    registerForActivityResult(
        new ActivityResultContracts.OpenDocument(), uri -> {
            if (uri == null) return;
            getContentResolver().takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            selectedCoverUri = uri;
            ivThumbCover.setImageURI(uri);
        });
```

**`OpenDocument` contract:** Mở file picker hệ thống, filter ảnh.

**`takePersistableUriPermission()`:** Quan trọng! Khi user chọn ảnh, app được cấp permission đọc URI đó. Nhưng permission này mất khi app restart. `takePersistableUriPermission()` xin Android giữ permission đó **vĩnh viễn** — lần sau mở app vẫn đọc được ảnh.

---

## Câu hỏi thầy hay hỏi

**Q: RecyclerView khác ListView ở điểm gì?**
> RecyclerView tái sử dụng (recycle) các View đã cuộn khỏi màn hình thay vì tạo mới — tiết kiệm RAM và mượt hơn. Bắt buộc dùng ViewHolder pattern. ListView không bắt buộc ViewHolder, ít tùy chỉnh hơn. Android hiện khuyến nghị dùng RecyclerView.

**Q: Adapter là gì? Tại sao cần Adapter?**
> Adapter là "cầu nối" giữa data (List<Sach>) và UI (RecyclerView). RecyclerView không biết data của app là gì — Adapter dạy nó: "có bao nhiêu item (`getItemCount`), mỗi item hiển thị thế nào (`onBindViewHolder`), tạo View item như thế nào (`onCreateViewHolder`)". Design Pattern: Adapter Pattern.

**Q: Tìm kiếm không dấu hoạt động thế nào?**
> Dùng `java.text.Normalizer` với chuẩn NFD (Canonical Decomposition) — tách ký tự có dấu thành ký tự gốc + dấu riêng. Sau đó xóa dấu bằng regex. Ký tự "đ/Đ" không decompose được nên replace thủ công. Cả keyword và tên sách đều được normalize trước khi so sánh → gõ "sach ngu van" tìm được "Sách Ngữ Văn".

**Q: Tại sao không lưu tên thể loại trực tiếp trong bảng Sach?**
> Nguyên tắc chuẩn hóa DB (Normalization) — tránh lưu dữ liệu trùng lặp. Nếu lưu tên thể loại trong Sach: khi đổi tên thể loại phải UPDATE hàng trăm sách → dễ inconsistency. Dùng `tl_id` FK rồi JOIN → chỉ cần sửa 1 dòng trong bảng TheLoai.

**Q: ContentValues là gì?**
> Map đặc biệt của Android để truyền dữ liệu vào `db.insert()` và `db.update()`. `cv.put("ten", s.ten)` → Android tự xử lý kiểu dữ liệu và escape an toàn. An toàn hơn ghép chuỗi SQL thủ công.

**Q: Tại sao dùng LEFT JOIN thay vì INNER JOIN?**
> INNER JOIN chỉ trả về sách **có** thể loại. LEFT JOIN trả về **tất cả** sách, kể cả sách chưa gán thể loại (tl_id = NULL). Trong trường hợp này dùng LEFT JOIN để không bỏ sót sách nào trong danh sách.
