# Hỏi - Đáp theo từng màn hình

> Thầy nhìn vào giao diện và hỏi — bạn trả lời. Tất cả câu trả lời đều dựa trên code thật.

---

## 🔐 Màn hình Đăng nhập

**Thầy hỏi: App có kiểm tra ô nhập liệu trống không? Kiểm tra như thế nào?**

> Có. Trong `doLogin()` của `LoginActivity.java`:
> ```java
> if (u.isEmpty() || p.isEmpty()) {
>     Toast.makeText(this, "Nhập đủ thông tin", Toast.LENGTH_SHORT).show();
>     return;
> }
> ```
> Dùng `getText().toString().trim()` để lấy text, rồi `.isEmpty()` kiểm tra rỗng. Dùng `||` nên chỉ cần 1 ô trống là báo lỗi ngay.

---

**Thầy hỏi: Sau khi đăng nhập thành công, app lưu thông tin gì? Lưu ở đâu?**

> Lưu vào `SharedPreferences` — file XML nhỏ trong bộ nhớ điện thoại. Cụ thể lưu 2 key:
> ```java
> getSharedPreferences("session", MODE_PRIVATE).edit()
>     .putString("username", info.username)
>     .putString("role",     info.role)     // "admin" hoặc "nhanvien"
>     .apply();
> ```
> Lần sau mở app, `onCreate()` đọc prefs — nếu username không rỗng thì tự chuyển thẳng vào MainActivity, không cần đăng nhập lại.

---

**Thầy hỏi: Nếu tài khoản bị vô hiệu hóa có đăng nhập được không?**

> Không. Câu SQL trong `UserDao.login()` có thêm điều kiện `AND status = 1`:
> ```sql
> WHERE username = ? AND password = ? AND status = 1
> ```
> Tài khoản bị vô hiệu hóa có `status = 0` → không khớp điều kiện → trả về null → app hiện "Sai tài khoản hoặc mật khẩu".

---

**Thầy hỏi: Nhấn Đăng nhập xong rồi bấm Back có quay về màn đăng nhập không?**

> Không. Sau khi đăng nhập, `goToMain()` dùng 2 flag:
> ```java
> intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
> ```
> `FLAG_ACTIVITY_CLEAR_TASK` xóa toàn bộ back stack → bấm Back từ MainActivity thoát hẳn app, không quay về màn đăng nhập.

---

## 🏠 Màn hình Trang chủ

**Thầy hỏi: Lời chào "Chào buổi sáng" thay đổi theo giờ như thế nào?**

> Dùng `Calendar.getInstance().get(Calendar.HOUR_OF_DAY)` lấy giờ hiện tại (0–23):
> ```java
> int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
> if      (hour < 12) hello = "Chào buổi sáng 👋";
> else if (hour < 18) hello = "Chào buổi chiều 👋";
> else                hello = "Chào buổi tối 👋";
> ```
> `HOUR_OF_DAY` là format 24h, khác `HOUR` (12h). Ví dụ: lúc 14:00 → `HOUR_OF_DAY = 14 >= 12` → "Chào buổi chiều".

---

**Thầy hỏi: Con số phiếu mượn, phiếu trả trên trang chủ lấy từ đâu? Cập nhật khi nào?**

> Lấy từ `StatDao`:
> ```java
> // StatDao.java
> public int countBorrow() {
>     // SELECT COUNT(*) FROM PhieuMuon
> }
> public int countReturn() {
>     // SELECT COUNT(*) FROM PhieuTra
> }
> ```
> Được cập nhật trong `onResume()` — mỗi khi user quay về màn chính (từ màn mượn trả, từ back...) thì tự refresh lại con số.

---

**Thầy hỏi: Nhân viên bình thường có vào được màn Nhân viên không?**

> Không. `openStaff()` trong `MainActivity` kiểm tra role:
> ```java
> private void openStaff() {
>     if (!ROLE_ADMIN.equals(currentRole)) {
>         Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
>         return;
>     }
>     startActivity(new Intent(this, NhanVienActivity.class));
> }
> ```
> Role được đọc từ SharedPreferences ngay khi `onCreate()`. Nhân viên bấm vào shortcut → hiện Toast → không mở Activity.

---

## 📚 Màn hình Thêm sách (`SachAddActivity`)

**Thầy hỏi: Màn hình thêm sách làm sao để thêm được hình ảnh từ bên ngoài vào?**

> Dùng `ActivityResultLauncher` với contract `OpenDocument`:
> ```java
> private final ActivityResultLauncher<String[]> imagePicker =
>     registerForActivityResult(
>         new ActivityResultContracts.OpenDocument(), uri -> {
>             if (uri == null) return;
>             // Giữ quyền đọc URI sau khi app restart
>             getContentResolver().takePersistableUriPermission(uri,
>                     Intent.FLAG_GRANT_READ_URI_PERMISSION);
>             selectedCoverUri = uri;
>             ivCover.setImageURI(uri);  // hiển thị ảnh ngay
>         });
> ```
> Khi user tap vào ô ảnh:
> ```java
> ivCover.setOnClickListener(v -> imagePicker.launch(new String[]{"image/*"}));
> ```
> `launch(new String[]{"image/*"})` mở file picker hệ thống, chỉ hiện file ảnh. User chọn ảnh → callback nhận URI → hiển thị preview.

---

**Thầy hỏi: `takePersistableUriPermission` là gì? Có cần thiết không?**

> Rất cần. Khi user chọn ảnh, hệ thống cấp quyền đọc URI tạm thời. Nếu không gọi `takePersistableUriPermission`, quyền đó mất khi app bị tắt → lần sau mở app, ảnh bìa hiện lỗi "Permission denied". Gọi hàm này → Android lưu quyền vĩnh viễn cho URI đó → ảnh hiển thị được mọi lần.

---

**Thầy hỏi: Thể loại trong màn thêm sách hiển thị thế nào? Có hardcode không?**

> Không hardcode. Load động từ DB bằng `Spinner` + `ArrayAdapter`:
> ```java
> private void loadTheLoai() {
>     theLoaiList.add(new TheLoai(0, "-- Chọn thể loại --"));
>     theLoaiList.addAll(theLoaiDao.listAll());  // lấy từ DB
>
>     ArrayAdapter<TheLoai> adapter = new ArrayAdapter<>(this,
>             android.R.layout.simple_spinner_item, theLoaiList);
>     spinnerTheLoai.setAdapter(adapter);
> }
> ```
> `TheLoai.toString()` trả về `ten` → Spinner tự hiển thị tên thể loại. Thêm thể loại mới trong DB → Spinner tự có khi mở màn thêm sách.

---

**Thầy hỏi: Validate form thêm sách như thế nào? Kiểm tra những gì?**

> Kiểm tra theo thứ tự trong `onConfirm()`:
> 1. Tên sách không rỗng → `edtTen.setError("Vui lòng nhập tên sách")`
> 2. Thể loại được chọn (tlId > 0)
> 3. Tác giả không rỗng
> 4. Số lượng không rỗng
> 5. Nhà xuất bản không rỗng
> 6. Năm xuất bản không rỗng
> 7. Parse số nguyên hợp lệ → `Integer.parseInt()`, bọc `try-catch NumberFormatException`
> 8. Số lượng >= 0
>
> Dùng `setError()` hiển thị lỗi inline ngay dưới ô nhập + `requestFocus()` đặt con trỏ vào ô đó.

---

**Thầy hỏi: Dấu `*` màu đỏ cạnh label "Tên sách *" được tạo thế nào?**

> Dùng `SpannableString` để tô màu 1 phần text:
> ```java
> private void setLabelWithStar(int id, String base) {
>     String full = base + " *";
>     SpannableString sp = new SpannableString(full);
>     sp.setSpan(new ForegroundColorSpan(0xFFE04D4D),  // màu đỏ
>             full.length() - 1, full.length(),          // chỉ ký tự "*"
>             Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
>     ((TextView) findViewById(id)).setText(sp);
> }
> ```
> `ForegroundColorSpan` tô màu chữ từ index `(length-1)` đến `length` — tức chỉ ký tự `*` cuối cùng. Phần còn lại giữ màu mặc định.

---

## 📋 Màn hình Lập phiếu mượn (`PhieuMuonAddActivity`)

**Thầy hỏi: Màn hình lập phiếu mượn, làm sao để chọn bạn đọc?**

> Tap vào ô "Chọn bạn đọc" → `showBanDocPicker()`:
> ```java
> private void showBanDocPicker() {
>     List<BanDoc> list = banDocDao.listAll();
>     String[] names = new String[list.size()];
>     for (int i = 0; i < list.size(); i++) names[i] = list.get(i).ten;
>
>     new AlertDialog.Builder(this)
>             .setTitle("Chọn bạn đọc")
>             .setItems(names, (d, which) -> {
>                 selectedBanDoc = list.get(which);
>                 tvBanDoc.setText(selectedBanDoc.ten);
>             })
>             .show();
> }
> ```
> `setItems()` tạo dialog danh sách — tap vào tên nào → lấy `BanDoc` tại index đó → hiển thị tên lên TextView.

---

**Thầy hỏi: Chọn hạn trả dùng gì? Code thế nào?**

> Dùng `DatePickerDialog` — dialog lịch có sẵn của Android:
> ```java
> private void showDatePicker() {
>     Calendar now  = Calendar.getInstance();
>     Calendar init = selectedHanTra != null ? selectedHanTra : now;
>
>     new DatePickerDialog(this,
>         (view, year, month, day) -> {
>             selectedHanTra = Calendar.getInstance();
>             selectedHanTra.set(year, month, day);
>             // Hiển thị dạng dd/MM/yyyy
>             tvHanTra.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
>                     .format(selectedHanTra.getTime()));
>         },
>         init.get(Calendar.YEAR),
>         init.get(Calendar.MONTH),
>         init.get(Calendar.DAY_OF_MONTH))
>     .show();
> }
> ```
> `month` trong Calendar bắt đầu từ 0 (January=0, December=11) — `DatePickerDialog` tự xử lý điều này.

---

**Thầy hỏi: Danh sách sách để chọn có hiện hết tất cả sách không? Sách hết thì sao?**

> Chỉ hiện sách **còn hàng**. `loadSachAvailable()` filter trước:
> ```java
> private void loadSachAvailable() {
>     List<Sach> all = sachDao.listAll();
>     List<Sach> avail = new ArrayList<>();
>     for (Sach s : all) {
>         if (s.soluong > 0) avail.add(s);  // chỉ lấy sách còn
>     }
>     pickAdapter.submit(avail);
> }
> ```
> Sách `soluong = 0` không hiện trong danh sách → không thể chọn để mượn.

---

**Thầy hỏi: Tap vào sách thêm vào danh sách mượn — nếu tap nhiều lần thì sao?**

> Số lượng cộng dồn. Dùng `LinkedHashMap<sachId, ChiTietMuon>` để lưu:
> ```java
> private void onAddSach(Sach s) {
>     ChiTietMuon ct = selected.get(s.sachId);
>     if (ct == null) {
>         ct = new ChiTietMuon(s.sachId, s.ten, 1);  // lần đầu → tạo mới, qty=1
>         selected.put(s.sachId, ct);
>     } else {
>         ct.soluong++;   // đã có → cộng thêm 1
>     }
>     renderSelected();
> }
> ```
> `LinkedHashMap` giữ thứ tự thêm vào, key là `sachId` → tra nhanh O(1).

---

**Thầy hỏi: Lưu phiếu mượn — dữ liệu được lưu vào mấy bảng?**

> 2 bảng trong 1 lần gọi `insertWithDetails()`:
> ```java
> long pmId = phieuMuonDao.insertWithDetails(pm, new ArrayList<>(selected.values()));
> ```
> Bên trong DAO:
> 1. INSERT vào `PhieuMuon` → lấy `pm_id` mới
> 2. Vòng lặp INSERT từng dòng vào `ChiTietMuon(pm_id, sach_id, soluong)`
> 3. Mỗi INSERT ChiTietMuon → **trigger `trg_muon_sach` tự chạy** → trừ kho sách
>
> Tất cả trong 1 transaction để đảm bảo hoặc lưu hết hoặc hủy hết.

---

## 📦 Màn hình Lập phiếu trả (`PhieuTraAddActivity`)

**Thầy hỏi: Màn hình lập phiếu trả có gì khác so với lập phiếu mượn?**

> Khác hoàn toàn về quy trình:
> - Phiếu mượn: chọn bạn đọc → chọn sách → nhập số lượng → lưu.
> - Phiếu trả: **chọn phiếu mượn gốc** → sách tự động điền từ chi tiết phiếu mượn đó → chỉ cần xác nhận → lưu.
>
> Không cần chọn sách thủ công vì đã biết bạn đó mượn sách gì từ phiếu mượn.

---

**Thầy hỏi: Làm sao app biết phiếu mượn nào chưa được trả?**

> `phieuMuonDao.listChuaTra()` dùng `NOT EXISTS`:
> ```sql
> SELECT pm.pm_id, pm.ngay_muon, pm.ngay_tra, bd.ten
> FROM PhieuMuon pm
> LEFT JOIN BanDoc bd ON bd.bd_id = pm.bd_id
> WHERE NOT EXISTS (
>     SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id
> )
> ORDER BY pm.pm_id DESC
> ```
> Chỉ lấy phiếu mượn **chưa có** phiếu trả nào tương ứng. Đây là danh sách hiển thị cho nhân viên chọn để lập phiếu trả.

---

**Thầy hỏi: Khi chọn xong phiếu mượn, màn hình thay đổi gì?**

> Ẩn section chọn phiếu, hiện section chi tiết sách:
> ```java
> private void onPickPhieuMuon(PhieuMuon p) {
>     currentPm = phieuMuonDao.findById(p.pmId);
>     tvMaPhieuMuon.setText(currentPm.getMaPhieu());
>     tvBanDoc.setText(currentPm.tenBanDoc);
>     selectedAdapter.submit(currentPm.chiTiet);  // fill sách từ phiếu mượn
>     showSelectedState();  // ẩn pick, hiện chi tiết
> }
>
> private void showSelectedState() {
>     sectionPick.setVisibility(View.GONE);
>     sectionSelected.setVisibility(View.VISIBLE);
> }
> ```
> Có nút "Đổi phiếu" để quay lại chọn phiếu khác.

---

## 📖 Màn hình Bạn đọc

**Thầy hỏi: 2 con số "Tổng số" và "Đang mượn" trên màn bạn đọc tính thế nào?**

> Từ `BanDocDao.countStats()` — 1 query trả về cả 2:
> ```sql
> SELECT COUNT(DISTINCT bd.bd_id),
>        COUNT(DISTINCT CASE WHEN pm.pm_id IS NOT NULL THEN bd.bd_id END)
> FROM BanDoc bd
> LEFT JOIN PhieuMuon pm ON pm.bd_id = bd.bd_id
>   AND NOT EXISTS(SELECT 1 FROM PhieuTra pt WHERE pt.pm_id = pm.pm_id)
> ```
> - Cột 1: `COUNT(DISTINCT bd.bd_id)` → tổng số bạn đọc.
> - Cột 2: đếm bạn đọc có ít nhất 1 phiếu mượn chưa trả.
> - `DISTINCT` tránh đếm trùng (1 bạn đọc 3 phiếu → vẫn đếm là 1).

---

**Thầy hỏi: Thêm bạn đọc cần nhập những gì? Trường nào bắt buộc?**

> Trong `BanDocAddActivity.onConfirm()`: chỉ **Tên** là bắt buộc:
> ```java
> if (TextUtils.isEmpty(ten)) {
>     edtTen.setError("Vui lòng nhập tên");
>     edtTen.requestFocus();
>     return;
> }
> ```
> SĐT và địa chỉ không bắt buộc — để trống cũng lưu được. Trong DB lưu null nếu để trống (`b.sdt = sdt` — nếu sdt rỗng, lưu chuỗi rỗng).

---

## 👥 Màn hình Nhân viên

**Thầy hỏi: Thêm nhân viên khác thêm bạn đọc ở chỗ nào?**

> Thêm nhân viên phải tạo **tài khoản đăng nhập** kèm theo. `NhanVienAddActivity` dùng email làm username:
> ```java
> // Cần thêm email + password khi thêm mới
> long id = nhanVienDao.insertWithUser(n, email, password);
> ```
> Bên trong `insertWithUser()` có **transaction** — insert cả `Users` lẫn `NhanVien` cùng lúc. Thêm bạn đọc chỉ insert 1 bảng `BanDoc`, không cần tài khoản.

---

**Thầy hỏi: Form thêm nhân viên và sửa nhân viên có khác nhau không?**

> Dùng chung 1 Activity (`NhanVienAddActivity`) — phân biệt bằng `nvId`:
> ```java
> nvId = getIntent().getIntExtra(EXTRA_NV_ID, 0);
>
> if (nvId > 0) {
>     // Mode sửa: điền sẵn thông tin, ẩn ô mật khẩu
>     tvTitle.setText("Sửa nhân viên");
>     btnConfirm.setText("Cập nhật");
>     lblPassword.setVisibility(View.GONE);
>     edtPassword.setVisibility(View.GONE);
> } else {
>     // Mode thêm: form trống, hiện ô mật khẩu
>     tvTitle.setText("Thêm nhân viên");
> }
> ```
> Khi sửa, ô mật khẩu bị ẩn — không cho đổi pass ở đây (cần chức năng riêng).

---

## 📞 Màn hình Liên hệ

**Thầy hỏi: Màn hình liên hệ làm sao để gọi điện? Dùng hàm gì?**

> Dùng `Intent.ACTION_DIAL` và `Uri.parse("tel:...")`:
> ```java
> private void onCall(BanDoc bd) {
>     if (TextUtils.isEmpty(bd.sdt)) {
>         Toast.makeText(this, "Bạn đọc chưa có số điện thoại", Toast.LENGTH_SHORT).show();
>         return;
>     }
>     Intent dial = new Intent(Intent.ACTION_DIAL);
>     dial.setData(Uri.parse("tel:" + bd.sdt));
>     startActivity(dial);
> }
> ```
> `ACTION_DIAL` mở ứng dụng quay số hệ thống với số được điền sẵn — user vẫn phải bấm nút Gọi. Khác `ACTION_CALL` (gọi thẳng, cần permission `CALL_PHONE`).

---

**Thầy hỏi: Tìm kiếm trên màn liên hệ hoạt động thế nào? Realtime không?**

> Realtime — mỗi ký tự gõ filter ngay. Dùng `TextWatcher`:
> ```java
> edtSearch.addTextChangedListener(new TextWatcher() {
>     @Override
>     public void afterTextChanged(Editable s) {
>         applyFilter(s.toString());  // gọi mỗi khi text thay đổi
>     }
>     // beforeTextChanged, onTextChanged: để trống (bắt buộc implement nhưng không dùng)
> });
> ```
> `applyFilter()` normalize không dấu, so sánh với tên và SĐT.

---

**Thầy hỏi: Dữ liệu màn liên hệ lấy từ đâu? Có bảng Liên hệ trong DB không?**

> Không có bảng riêng. Dùng lại `BanDocDao.listAll()`:
> ```java
> private void loadData() {
>     allList = banDocDao.listAll();  // đọc bảng BanDoc
>     applyFilter(edtSearch.getText().toString());
> }
> ```
> Liên hệ không phải chức năng quản lý mà là **chức năng tiện ích** — hiển thị danh sách bạn đọc kèm SĐT để nhân viên liên lạc nhanh mà không cần vào màn Bạn đọc.

---

## 📊 Màn hình Báo cáo

**Thầy hỏi: App có mấy loại báo cáo? Mỗi loại hiển thị gì?**

> 4 loại:
> 1. **Sách mượn nhiều nhất** — xếp hạng sách theo số lần được mượn.
> 2. **Bạn đọc mượn nhiều nhất** — xếp hạng bạn đọc theo số phiếu mượn.
> 3. **Sách hết kho** — danh sách sách có `soluong = 0`.
> 4. **Thống kê theo tháng** — số phiếu mượn và trả từng tháng.

---

**Thầy hỏi: Báo cáo "sách mượn nhiều nhất" tính thế nào?**

> Query `COUNT` trên `ChiTietMuon`, `GROUP BY` sach_id:
> ```sql
> SELECT s.ten, s.tacgia, COUNT(ctm.sach_id) AS so_lan_muon
> FROM ChiTietMuon ctm
> JOIN Sach s ON s.sach_id = ctm.sach_id
> GROUP BY ctm.sach_id
> ORDER BY so_lan_muon DESC
> LIMIT 20
> ```
> Đếm số lần mỗi cuốn sách xuất hiện trong `ChiTietMuon` → sắp xếp từ nhiều đến ít.

---

**Thầy hỏi: Thống kê theo tháng dùng hàm gì của SQLite?**

> `strftime()` — hàm format ngày của SQLite:
> ```sql
> SELECT strftime('%Y-%m', pm.ngay_muon) AS period,
>        COUNT(pm.pm_id) AS so_muon
> FROM PhieuMuon pm
> GROUP BY period
> ORDER BY period DESC
> ```
> `strftime('%Y-%m', '2026-03-15')` → `'2026-03'`. Nhóm tất cả ngày trong tháng 3/2026 thành 1 dòng → đếm số phiếu mượn của tháng đó.

---

## 🔄 Tổng quát — Thầy hỏi về kỹ thuật chung

**Thầy hỏi: Tại sao dùng `onResume()` để load data thay vì `onCreate()`?**

> `onCreate()` chỉ chạy **1 lần** khi Activity được tạo. `onResume()` chạy **mỗi khi** Activity trở về foreground — kể cả sau khi user sang màn khác rồi quay lại. Ví dụ: từ màn Sách vào Thêm sách → thêm xong → bấm Back → `onResume()` chạy → list sách cập nhật sách mới ngay. Nếu chỉ dùng `onCreate()` thì phải đóng/mở lại màn mới thấy sách mới.

---

**Thầy hỏi: `finish()` làm gì? Khi nào dùng?**

> `finish()` đóng Activity hiện tại, trả về Activity trước đó trong back stack. Dùng khi: user bấm Back (`btnBack.setOnClickListener(v -> finish())`), sau khi lưu thành công (`setResult(RESULT_OK); finish()`), sau đăng xuất... Khác `System.exit()` — `finish()` chỉ đóng 1 Activity, `System.exit()` tắt hẳn app.

---

**Thầy hỏi: `setResult(RESULT_OK)` trước `finish()` dùng để làm gì?**

> Gửi kết quả về Activity gọi nó. Activity trước có thể `startActivityForResult()` và kiểm tra kết quả:
> ```java
> @Override
> protected void onActivityResult(int requestCode, int resultCode, Intent data) {
>     if (resultCode == RESULT_OK) {
>         loadData();  // có dữ liệu mới → refresh
>     }
> }
> ```
> Nếu không set `RESULT_OK` → Activity trước không biết có dữ liệu mới → không refresh. App hiện dùng `onResume()` để refresh nên không phụ thuộc hoàn toàn vào `setResult`, nhưng vẫn set để chuẩn code.

---

**Thầy hỏi: Lambda `v -> finish()` nghĩa là gì?**

> Lambda là cách viết tắt của anonymous class trong Java 8+. `v -> finish()` tương đương:
> ```java
> new View.OnClickListener() {
>     @Override
>     public void onClick(View v) {
>         finish();
>     }
> }
> ```
> `v` là tham số (View được click), `finish()` là body. Code ngắn hơn nhiều, dễ đọc hơn.

---

**Thầy hỏi: `TextUtils.isEmpty()` khác `String.isEmpty()` ở chỗ nào?**

> `TextUtils.isEmpty(s)` kiểm tra cả `null` lẫn chuỗi rỗng `""`. `String.isEmpty()` chỉ kiểm tra rỗng — nếu `s == null` thì sẽ throw `NullPointerException`. Dùng `TextUtils.isEmpty()` an toàn hơn khi chuỗi có thể null (vd lấy từ DB).

---

**Thầy hỏi: Mã phiếu "PM-001" được tạo thế nào? Có lưu trong DB không?**

> Không lưu, tạo động khi hiển thị. Trong model `PhieuMuon.java`:
> ```java
> public String getMaPhieu() {
>     return "PM-" + String.format("%03d", pmId);
> }
> ```
> `%03d` format số thành 3 chữ số với leading zero: 1→"001", 12→"012", 123→"123". DB chỉ lưu số nguyên `pm_id` — tiết kiệm, không mất index, dễ query. Tương tự "PT-001" cho phiếu trả.

---

**Thầy hỏi: App có xử lý trường hợp xoay màn hình không?**

> Không xử lý đặc biệt — khi xoay màn hình, Activity bị destroy và create lại từ đầu (gọi lại `onCreate()`). Data từ DB được load lại. `selectedBanDoc`, `selectedHanTra` trong `PhieuMuonAddActivity` sẽ mất — giới hạn của app hiện tại. Production app cần dùng `ViewModel` + `onSaveInstanceState()` để giữ state.
