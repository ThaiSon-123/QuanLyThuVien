# Quan ly thu vien - Tong quan project

## 1. Tong quan

Project la ung dung Android quan ly thu vien, viet bang Java va su dung SQLite cuc bo. Ung dung ho tro quan ly sach, the loai, ban doc, nhan vien, phieu muon, phieu tra, bao cao va cau hinh he thong.

Package chinh: `com.example.quanlythuvien`

Co so du lieu chinh duoc tao trong `DatabaseHelper`, gom cac bang:

- `Users`: tai khoan dang nhap va role.
- `NhanVien`: thong tin nhan vien, lien ket voi `Users`.
- `BanDoc`: thong tin ban doc.
- `TheLoai`: the loai sach.
- `Sach`: thong tin sach va so luong con trong kho.
- `PhieuMuon`: phieu muon sach.
- `ChiTietMuon`: danh sach sach trong phieu muon.
- `PhieuTra`: phieu tra sach.
- `ChiTietTra`: danh sach sach duoc tra.
- `CauHinh`: cac tham so cau hinh he thong.

## 2. Role va quyen han

He thong hien co 2 role:

| Role | Ten hien thi | Mo ta |
| --- | --- | --- |
| `admin` | Quan tri vien / Quan ly | Co toan quyen quan ly du lieu va cau hinh he thong. |
| `nhanvien` | Nhan vien | Duoc thao tac cac nghiep vu thu vien hang ngay, bi han che cac chuc nang quan tri. |

### 2.1. Quyen cua admin

Admin co cac quyen:

- Dang nhap va xem man hinh tong quan.
- Quan ly sach: xem, tim kiem, loc, them, sua, xoa sach.
- Quan ly the loai: xem, them, sua, xoa the loai.
- Quan ly ban doc: xem, tim kiem, them va xem chi tiet ban doc.
- Quan ly nhan vien: xem danh sach, them, sua, xoa nhan vien.
- Tao phieu muon va phieu tra.
- Xem chi tiet phieu muon, phieu tra va thuc hien gia han neu hop le.
- Xem bao cao thong ke.
- Truy cap man cau hinh he thong.
- Cap nhat cac tham so: don gia phat, so ngay muon mac dinh, so sach toi da tren phieu, so lan gia han, so ngay gia han, nguong sach sap het, so ngay canh bao sap den han.

### 2.2. Quyen cua nhan vien

Nhan vien co cac quyen:

- Dang nhap va xem man hinh tong quan.
- Xem danh sach sach, tim kiem, loc sach, xem trang thai ton kho.
- Tao phieu muon va phieu tra.
- Xem danh sach ban doc, them ban doc va xem thong tin lien quan.
- Xem bao cao thong ke.
- Xem canh bao qua han, sap het han va sach sap het kho.
- Dang xuat tai khoan.

Nhan vien bi gioi han:

- Khong duoc truy cap man quan ly nhan vien.
- Khong duoc truy cap man cau hinh he thong.
- Khong duoc them sach hoac the loai.
- Khi bam vao sach, he thong hien thong bao `Ban khong co quyen chinh sua sach` thay vi mo man chinh sua.

## 3. Chuc nang chinh

### 3.1. Dang nhap va tai khoan

- Nguoi dung dang nhap bang username va password.
- Sau khi dang nhap, role duoc luu trong `SharedPreferences`.
- Menu va shortcut tren man hinh chinh se hien/an theo role hien tai.
- Co chuc nang dang xuat va quen mat khau.

Tai khoan seed mac dinh:

- `admin / 123`: role `admin`.
- `nv1 / 123`: role `nhanvien`.
- `nv2 / 123`: role `nhanvien`.

### 3.2. Man hinh tong quan

Man hinh tong quan hien thi:

- Tong so phieu muon.
- Tong so phieu tra.
- Canh bao phieu qua han.
- Canh bao phieu sap het han.
- Canh bao sach sap het kho.

Nguong canh bao duoc doc tu cau hinh:

- `near_due_days`: so ngay sap het han.
- `low_stock_threshold`: nguong sach sap het kho.

Khi bam vao canh bao, he thong mo man tuong ung voi bo loc phu hop.

### 3.3. Quan ly sach va the loai

Chuc nang sach:

- Xem danh sach sach.
- Tim kiem theo ten sach hoac tac gia.
- Loc theo trang thai con sach / het sach.
- Loc theo the loai.
- Loc sach sap het kho tu dashboard.
- Admin duoc them, sua, xoa sach.
- Nhan vien chi duoc xem danh sach va khong duoc vao man chinh sua sach.

Chuc nang the loai:

- Xem danh sach the loai.
- Xem chi tiet the loai.
- Admin duoc them, sua, xoa the loai.

### 3.4. Quan ly ban doc

Chuc nang ban doc:

- Xem danh sach ban doc.
- Tim kiem ban doc theo ten, so dien thoai hoac dia chi.
- Xem thong tin va lich su muon tra cua ban doc.
- Xem so ban doc dang muon sach.
- Admin va nhan vien deu duoc them ban doc.

### 3.5. Lap phieu muon

Chuc nang lap phieu muon:

- Chon ban doc muon sach.
- Ngay muon mac dinh la ngay hien tai.
- Han tra mac dinh bang ngay hien tai cong `default_borrow_days`.
- Nguoi lap phieu van co the chon lai han tra bang DatePicker.
- Chon sach can muon tu danh sach sach con hang.
- Danh sach chon sach hien so luong sach con.
- Khi them sach vao phieu, he thong thong bao `Da them sach ... vao phieu muon`.
- Mot phieu co the co nhieu dau sach va moi dau sach co so luong rieng.

### 3.6. Lap phieu tra

Chuc nang lap phieu tra:

- Chi chon duoc cac phieu muon chua tra.
- Hien thong tin ban doc, han tra va danh sach sach trong phieu muon.
- Tinh so ngay tre han dua tren han tra va ngay tra thuc te.
- Tinh tien phat theo don gia hien tai trong cau hinh.
- Sau khi lap phieu tra, trang thai phieu muon duoc xem la da tra.
- So luong sach tra duoc cong lai vao kho qua trigger SQLite.

### 3.7. Gia han phieu muon

Chuc nang gia han:

- Thuc hien tren chi tiet phieu muon.
- Chi cho gia han phieu chua tra.
- Khong cho gia han neu phieu da qua han.
- Khong cho gia han neu da dat so lan gia han toi da.
- Moi lan gia han cong them `gia_han_days` vao han tra hien tai.
- So lan gia han toi da doc tu `max_gia_han`.
- Han tra goc duoc luu rieng de doi chieu.

### 3.8. Quan ly nhan vien

Chuc nang nhan vien:

- Chi admin duoc truy cap.
- Xem danh sach nhan vien.
- Them, sua, xoa nhan vien.
- Khi them/sua nhan vien, truong chuc vu duoc chon tu danh sach role thay vi nhap tay.
- Chuc vu `Quan ly` duoc suy ra role `admin`.
- Chuc vu `Nhan vien` duoc suy ra role `nhanvien`.

### 3.9. Bao cao

Chuc nang bao cao:

- Xem thong ke muon tra.
- Xem thong ke theo thang.
- Xem thong ke theo the loai, sach, ban doc hoac trang thai thanh toan tuy theo man bao cao chi tiet.

### 3.10. Cau hinh he thong

Chi admin duoc truy cap man cau hinh.

Các tham so hien co:

| Key | Y nghia | Gia tri mac dinh |
| --- | --- | --- |
| `fine_per_day` | Don gia phat tre han tren 1 ngay / 1 quyen | `500` |
| `default_borrow_days` | So ngay muon mac dinh | `7` |
| `max_books_per_slip` | Tong so sach toi da tren 1 phieu muon | `5` |
| `max_gia_han` | So lan gia han toi da tren 1 phieu | `2` |
| `gia_han_days` | So ngay cong them moi lan gia han | `7` |
| `low_stock_threshold` | Nguong canh bao sach sap het kho | `2` |
| `near_due_days` | So ngay canh bao phieu sap het han | `3` |

Khi thay doi don gia phat, he thong tinh lai tien phat cua cac phieu tra da co theo cau hinh moi.

## 4. Rang buoc nghiep vu

### 4.1. Rang buoc role

- Bang `Users` chi chap nhan role `admin` hoac `nhanvien`.
- Man nhan vien va man cau hinh chi mo cho admin.
- Nhan vien khong duoc sua sach; thao tac bam vao sach chi hien thong bao khong co quyen.
- Nut them sach va them the loai duoc an hoac chan neu user khong phai admin.
- Nut them ban doc duoc mo cho ca admin va nhan vien.

### 4.2. Rang buoc muon sach

- Ngay muon mac dinh la ngay hien tai.
- Han tra mac dinh = ngay hien tai + `default_borrow_days`.
- Tong so sach tren mot phieu khong duoc vuot `max_books_per_slip`.
- Chi cho chon sach co so luong ton kho lon hon 0.
- So luong muon cua tung dau sach khong duoc vuot so luong sach con trong kho.
- Khi them chi tiet muon, trigger `trg_muon_sach` tu dong tru so luong sach trong bang `Sach`.

### 4.3. Rang buoc tra sach va tien phat

- Chi lap phieu tra cho phieu muon chua tra.
- Tien phat chi phat sinh khi ngay tra thuc te lon hon han tra.
- So ngay tre = `max(0, ngay_tra_thuc_te - ngay_han_tra)`.
- Tien phat = so ngay tre x don gia phat x tong so luong sach trong phieu muon.
- Don gia phat duoc doc tu `CauHinh.fine_per_day`.
- Khi them chi tiet tra, trigger `trg_tra_sach` tu dong cong lai so luong sach vao bang `Sach`.

Vi du:

- Han tra: `2026-05-10`.
- Ngay tra thuc te: `2026-05-13`.
- So ngay tre: `3`.
- Tong so sach muon: `2`.
- Don gia phat: `1.000`.
- Tien phat: `3 x 2 x 1.000 = 6.000`.

### 4.4. Rang buoc gia han

- Khong duoc gia han phieu da tra.
- Khong duoc gia han phieu da qua han.
- Khong duoc gia han qua `max_gia_han` lan.
- Moi lan gia han cong them `gia_han_days` vao han tra hien tai.
- `ngay_tra_goc` duoc luu de biet han tra ban dau truoc khi gia han.

### 4.5. Rang buoc canh bao

- Phieu qua han: phieu chua tra va ngay hien tai lon hon han tra.
- Phieu sap het han: phieu chua tra co han tra nam trong khoang canh bao `near_due_days`.
- Sach sap het kho: sach co so luong con nho hon hoac bang `low_stock_threshold`.

### 4.6. Rang buoc du lieu va SQLite

- Cac bang nghiep vu co khoa ngoai lien ket voi nhau.
- `ChiTietMuon` lien ket `PhieuMuon` va `Sach`.
- `ChiTietTra` lien ket `PhieuTra` va `Sach`.
- `PhieuMuon` lien ket `BanDoc` va `NhanVien`.
- Khi nang version database, `onUpgrade` hien tai drop bang va tao lai du lieu seed.

## 5. Du lieu mau

Database seed san:

- Tai khoan admin va nhan vien.
- Danh sach nhan vien.
- Danh sach ban doc.
- The loai sach.
- Sach va so luong ton kho.
- Phieu muon, phieu tra mau trong thang 05/2026.
- Cau hinh mac dinh.

Du lieu phieu muon/tra mau dung de test cac tinh huong:

- Phieu da tra.
- Phieu chua tra.
- Phieu tre han.
- Phieu co tien phat.
- Phieu muon nhieu sach.
- Sach sap het kho.

## 6. Lenh kiem tra nhanh

Co the chay unit test bang lenh:

```powershell
.\gradlew.bat testDebugUnitTest
```

Mot so nhom test dang co:

- Tinh tien phat theo don gia cau hinh.
- Tinh tien phat theo so luong sach muon.
- Kiem tra quyen mo man sua sach.
- Kiem tra rule khong muon vuot so luong ton kho.
- Kiem tra danh sach role khi them/sua nhan vien.
