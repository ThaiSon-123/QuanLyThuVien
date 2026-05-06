# 10 — Liên hệ

> **Files chính:** `LienHeActivity.java`

---

## Giải thích đơn giản

Màn Liên hệ hiển thị danh sách bạn đọc kèm **số điện thoại** để nhân viên có thể gọi trực tiếp. Tính năng đặc biệt: tap icon điện thoại → **mở ứng dụng gọi điện hệ thống** với số điện thoại được điền sẵn. Có tìm kiếm không dấu realtime.

---

## Sơ đồ luồng

```
LienHeActivity
  ├─ Load danh sách bạn đọc (từ BanDocDao)
  ├─ Tìm kiếm realtime (norm không dấu)
  │       └─ Filter theo tên / SĐT / địa chỉ
  │
  └─ Tap icon 📞 bên cạnh tên
          └─ Intent ACTION_DIAL với tel:SĐT
          └─ Mở app Dialer hệ thống (số đã điền sẵn)
```

---

## LienHeActivity.java

### onCreate() — Load dữ liệu

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lienhe);

    banDocDao = new BanDocDao(this);
    allList = banDocDao.listAll();

    setupRecycler();
    setupSearch();
    setupBottomNav();
    applyFilter("");
}
```

Load toàn bộ bạn đọc vào `allList` — không filter ban đầu. `applyFilter("")` với keyword rỗng hiển thị tất cả.

---

### RecyclerView + Adapter

```java
private void setupRecycler() {
    RecyclerView rv = findViewById(R.id.rvLienHe);
    rv.setLayoutManager(new LinearLayoutManager(this));
    adapter = new LienHeAdapter((bd, action) -> {
        if ("call".equals(action)) {
            dialPhone(bd.sdt);
        } else {
            // tap vào item → xem chi tiết (không implement)
        }
    });
    rv.setAdapter(adapter);
}
```

Adapter nhận callback 2 tham số: `(BanDoc, action)`. Action "call" → gọi điện.

---

### dialPhone() — Gọi điện hệ thống

```java
private void dialPhone(String sdt) {
    if (sdt == null || sdt.isEmpty()) {
        Toast.makeText(this, "Bạn đọc chưa có SĐT", Toast.LENGTH_SHORT).show();
        return;
    }
    Intent intent = new Intent(Intent.ACTION_DIAL);
    intent.setData(Uri.parse("tel:" + sdt));
    startActivity(intent);
}
```

**`Intent.ACTION_DIAL`:** Mở màn hình quay số hệ thống với số điện thoại điền sẵn. User vẫn phải bấm nút gọi — app không tự động gọi.

**Tại sao dùng `ACTION_DIAL` thay vì `ACTION_CALL`?**
- `ACTION_CALL`: Gọi ngay lập tức — cần permission `CALL_PHONE` trong AndroidManifest.
- `ACTION_DIAL`: Chỉ mở dialer, user tự quyết định gọi hay không — **không cần permission**.
- An toàn hơn về UX và permissions.

**`Uri.parse("tel:" + sdt)`:** Định dạng URI cho số điện thoại. Ví dụ: `tel:0912345678`.

---

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
    List<BanDoc> filtered = new ArrayList<>();
    for (BanDoc b : allList) {
        if (kw.isEmpty()
                || norm(b.ten).contains(kw)
                || norm(b.sdt).contains(kw)
                || norm(b.diachi).contains(kw)) {
            filtered.add(b);
        }
    }
    adapter.submit(filtered);
    tvCount.setText(filtered.size() + " liên hệ");
}
```

**Giống hoàn toàn với BanDocActivity** — tìm theo tên, SĐT, địa chỉ với normalize tiếng Việt.

---

### Search Mode

```java
// TextWatcher trong setupSearch():
edtSearch.addTextChangedListener(new TextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
        String kw = s.toString();
        btnClearSearch.setVisibility(kw.isEmpty() ? View.GONE : View.VISIBLE);
        applyFilter(kw);   // filter realtime
    }
    // beforeTextChanged, onTextChanged: để trống
});
```

Mỗi ký tự gõ → `afterTextChanged` → `applyFilter` → adapter cập nhật ngay.

---

## LienHeAdapter.java

### ViewHolder — Layout 1 item

```java
static class VH extends RecyclerView.ViewHolder {
    TextView  tvTen, tvSdt, tvDiachi;
    ImageView btnCall;

    VH(@NonNull View itemView) {
        super(itemView);
        tvTen    = itemView.findViewById(R.id.tvTen);
        tvSdt    = itemView.findViewById(R.id.tvSdt);
        tvDiachi = itemView.findViewById(R.id.tvDiachi);
        btnCall  = itemView.findViewById(R.id.btnCall);
    }
}
```

### onBindViewHolder()

```java
@Override
public void onBindViewHolder(@NonNull VH holder, int position) {
    BanDoc bd = data.get(position);
    holder.tvTen.setText(bd.ten);
    holder.tvSdt.setText(bd.sdt != null ? bd.sdt : "Chưa có SĐT");
    holder.tvDiachi.setText(bd.diachi != null ? bd.diachi : "");

    // Icon gọi điện
    if (bd.sdt != null && !bd.sdt.isEmpty()) {
        holder.btnCall.setVisibility(View.VISIBLE);
        holder.btnCall.setOnClickListener(v -> {
            if (listener != null) listener.onAction(bd, "call");
        });
    } else {
        holder.btnCall.setVisibility(View.GONE);  // ẩn nếu không có SĐT
    }
}
```

**Ẩn icon gọi khi không có SĐT:** Tránh user tap vào → app hiện Toast "Chưa có SĐT" mà không có hành động gì. Ẩn đi thì gọn hơn.

---

## Câu hỏi thầy hay hỏi

**Q: Màn Liên hệ lấy dữ liệu từ đâu? Có bảng Liên hệ riêng không?**
> Không có bảng riêng. Màn Liên hệ dùng lại dữ liệu từ bảng `BanDoc` — gọi `BanDocDao.listAll()`. Đây không phải chức năng quản lý mà là chức năng tiện ích — hiển thị danh sách bạn đọc + SĐT để nhân viên gọi điện nhanh.

**Q: Intent ACTION_DIAL là gì? Khác ACTION_CALL thế nào?**
> `ACTION_DIAL` mở màn hình quay số hệ thống với số điền sẵn — user vẫn phải bấm gọi. Không cần permission. `ACTION_CALL` gọi thẳng ngay lập tức — cần permission `CALL_PHONE` trong Manifest và user phải cấp runtime permission. App dùng `ACTION_DIAL` để đơn giản hơn và tôn trọng quyết định của người dùng.

**Q: Uri.parse("tel:...") nghĩa là gì?**
> URI (Uniform Resource Identifier) là chuẩn định danh tài nguyên. `tel:` là scheme cho số điện thoại — giống `http:` cho web, `mailto:` cho email. Android đọc scheme này → biết cần mở app điện thoại. `Uri.parse("tel:0912345678")` → tạo URI object từ chuỗi.

**Q: TextWatcher là gì? Tại sao cần interface?**
> `TextWatcher` là interface với 3 method: `beforeTextChanged`, `onTextChanged`, `afterTextChanged`. Implement interface này để lắng nghe mọi thay đổi trong EditText. `afterTextChanged` được gọi sau khi text thay đổi xong → lấy text mới, filter danh sách. Dùng interface vì Android không biết muốn làm gì sau khi text thay đổi — callback để developer tự quyết định.

**Q: Tại sao ẩn icon gọi điện khi không có SĐT?**
> UX tốt — tránh hiển thị nút không có tác dụng. Nếu hiện mà tap vào → Toast "Chưa có SĐT" → user confused. Ẩn đi rõ ràng hơn: icon gọi chỉ xuất hiện khi thực sự có thể gọi.

**Q: Tìm kiếm không dấu hoạt động thế nào?**
> Cả keyword và dữ liệu đều được chuẩn hóa bằng hàm `norm()`: lowercase → NFD decompose (tách dấu) → xóa dấu kết hợp → replace đ→d. So sánh chuỗi đã chuẩn hóa → gõ "nguyen van a" tìm được "Nguyễn Văn A". Không cần thư viện ngoài, chỉ dùng `java.text.Normalizer` có sẵn.
