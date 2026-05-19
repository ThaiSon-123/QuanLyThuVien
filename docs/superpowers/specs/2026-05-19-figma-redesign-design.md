# Figma Redesign — Quản Lý Thư Viện (Material 3 Refresh)

**Ngày:** 2026-05-19
**Tác giả:** ThaiSon
**Trạng thái:** Đã duyệt — chuẩn bị implement

## Mục tiêu

Dựng lại toàn bộ giao diện app Android `QuanLyThuVien` (~25 màn hình) trong Figma theo Material 3 hiện đại ("M3 Refresh"). Đầu ra là một file Figma có:
- Design system đầy đủ (color, typography, shape, components)
- Tất cả màn hình production-ready 360×800 dp
- Sẵn sàng để dev triển khai vào layout XML Android

## Bối cảnh / Hiện trạng

App là hệ thống quản lý thư viện viết bằng Java + Android, dùng `Theme.Material3.DayNight.NoActionBar` nhưng các layout cũ chủ yếu dùng `ConstraintLayout` + `CardView` thủ công, không tận dụng component Material 3. Màu chính hiện tại `#4696EB`, accent cam (FAB). 25 activities chia theo các nhóm: Auth, Main, Catalog (Sách/Thể loại), Mượn-Trả, Bạn đọc, Nhân viên, Báo cáo, Liên hệ.

## Design direction

**Hướng A — Material 3 Refresh:** giữ nhận diện xanh nhưng nâng cấp tone, thay accent cam bằng amber, áp dụng đầy đủ M3 (component, shape, elevation surface-tint). Cân bằng giữa "modern" và "vẫn là app cũ".

## 1. Color tokens (Light)

| Token | Hex |
|---|---|
| Primary | `#1565C0` |
| On Primary | `#FFFFFF` |
| Primary Container | `#D6E4FF` |
| On Primary Container | `#001A41` |
| Secondary | `#5A5F71` |
| Secondary Container | `#DEE1F9` |
| Tertiary (Accent) | `#FFB300` |
| On Tertiary | `#3F2E00` |
| Error | `#BA1A1A` |
| Error Container | `#FFDAD6` |
| Surface | `#FDFCFF` |
| Surface Variant | `#E1E2EC` |
| Surface Container | `#F1F3F9` |
| Outline | `#74777F` |
| Outline Variant | `#C4C6D0` |

**Status (phiếu):**
- Còn hạn `#2E7D32` · Sắp hết `#EF6C00` · Quá hạn `#BA1A1A` · Đã trả `#546E7A`

## 2. Typography (Roboto / Roboto Flex)

| Style | Size / Weight |
|---|---|
| Display L | 57 / 400 |
| Headline L | 32 / 400 |
| Title L | 22 / 500 |
| Title M | 16 / 500 |
| Body L | 16 / 400 |
| Body M | 14 / 400 |
| Label L | 14 / 500 |
| Label M | 12 / 500 |

## 3. Shape & elevation

- Corner: XS 4, S 8, M 12, L 16, XL 28 (dp)
- Button pill: 100dp
- Card: 16dp
- Elevation surface-tint M3: Level 1 (cards), Level 2 (app bar khi scroll), Level 3 (FAB)

## 4. Components

- TopAppBar (Small / CenterAligned) — nền primary, text trắng
- BottomNavigationBar M3 — `surfaceContainer`, pill indicator
- Buttons: Filled / Tonal / Outlined / Text
- FAB Extended — tertiary container (amber)
- TextField Outlined M3 — label nổi, supporting text, error
- Card: Filled tonal / Elevated / Outlined
- ListItem M3 — leading + headline + supporting + trailing
- Chip: Assist / Filter / Input — trạng thái phiếu
- Dialog / BottomSheet / Snackbar / SearchBar M3

## 5. Iconography

**Bộ icon: Material Symbols Rounded** — Weight 400, Optical 24, Grade 0. Filled khi active, Outlined khi inactive.

| Chức năng | Material Symbol |
|---|---|
| Sách | `menu_book` |
| Thể loại | `category` |
| Bạn đọc | `groups` |
| Mượn trả | `swap_horiz` |
| Báo cáo | `analytics` |
| Nhân viên | `badge` |
| Liên hệ | `support_agent` |
| Tìm kiếm | `search` |
| Lọc | `tune` |
| Thêm | `add` |
| Menu / Back / Đóng | `menu` / `arrow_back` / `close` |
| Tài khoản | `account_circle` |
| Quên MK | `lock_reset` |
| Avatar | `person` |

**Status icons:** `schedule` (còn hạn) · `hourglass_bottom` (sắp hết) · `error` (quá hạn) · `check_circle` (đã trả)

**Empty state illustrations:** line-art đơn giản primary color — kệ sách trống / tờ giấy + plus / kính lúp + ?.

**Brand mark:** `local_library` filled trong vòng tròn `primaryContainer` 96dp + tagline "Quản lý thư viện".

**Bottom nav (5 tab):** `home`, `menu_book`, `swap_horiz`, `groups`, `more_horiz`.

## 6. Danh sách màn hình (25)

| # | Group | Frame name | Layout file gốc |
|---|---|---|---|
| 1 | Auth | Login | `activity_login.xml` |
| 2 | Auth | ForgotPassword | `activity_forgot_password.xml` |
| 3 | Auth | ForgotPassword — Success | (state) |
| 4 | Main | Home | `activity_main.xml` |
| 5 | Main | Menu Drawer (overlay) | (overlay) |
| 6 | Catalog | SachList — Tab Tất cả | `activity_sach.xml` |
| 7 | Catalog | SachList — Tab Thể loại | `activity_sach.xml` |
| 8 | Catalog | SachDetail | `activity_sach_detail.xml` |
| 9 | Catalog | SachAdd | `activity_sach_add.xml` |
| 10 | Catalog | TheLoaiDetail | `activity_theloai_detail.xml` |
| 11 | Catalog | TheLoaiEdit | `activity_theloai_edit.xml` |
| 12 | Catalog | DialogFilterSach | `dialog_filter_sach.xml` |
| 13 | Mượn-Trả | MuonTraList — Tab Mượn | `activity_muontra.xml` |
| 14 | Mượn-Trả | MuonTraList — Tab Trả | `activity_muontra.xml` |
| 15 | Mượn-Trả | PhieuMuonDetail | `activity_phieumuon_detail.xml` |
| 16 | Mượn-Trả | PhieuMuonAdd | `activity_phieumuon_add.xml` |
| 17 | Mượn-Trả | PhieuTraDetail | `activity_phieutra_detail.xml` |
| 18 | Mượn-Trả | PhieuTraAdd | `activity_phieutra_add.xml` |
| 19 | Bạn đọc | BanDocList | `activity_bandoc.xml` |
| 20 | Bạn đọc | BanDocDetail | `activity_bandoc_detail.xml` |
| 21 | Bạn đọc | BanDocAdd | `activity_bandoc_add.xml` |
| 22 | Nhân viên | NhanVienList | `activity_nhanvien.xml` |
| 23 | Nhân viên | NhanVienDetail | `activity_nhanvien_detail.xml` |
| 24 | Nhân viên | NhanVienAdd | `activity_nhanvien_add.xml` |
| 25 | Khác | LienHe | `activity_lienhe.xml` |
| 26 | Khác | BaoCao | `activity_baocao.xml` |
| 27 | Khác | BaoCaoChiTiet | `activity_baocao_chitiet.xml` |

> Ghi chú: 1 layout có thể tách thành nhiều frame để mô tả các tab/state, nên tổng số frame >= 27.

## 7. Cấu trúc file Figma

Pages:
1. 🪧 Cover
2. 🎨 Foundations — Color, Typography, Shape, Spacing, Elevation, Iconography
3. 🧩 Components — tất cả components ở mục 4 (mỗi cái có variants)
4. 📱 Screens — Auth
5. 📱 Screens — Main
6. 📱 Screens — Catalog
7. 📱 Screens — Mượn/Trả
8. 📱 Screens — Bạn đọc & Nhân viên
9. 📱 Screens — Báo cáo & Khác
10. 🔄 Flows (prototype — optional, làm cuối)

Phone canvas: **360 × 800 dp**. Dùng Figma Variables cho tất cả token để dễ đổi theme dark sau.

## 8. Thứ tự thực hiện

1. **Tạo file Figma mới** + setup pages
2. **Foundations**: Variables (color, spacing, radius) → Text styles → Color styles
3. **Components**: app bar → bottom nav → buttons → text field → card → list item → chip → FAB → dialog
4. **Screens**: build từng nhóm — Auth → Main → Catalog → Mượn-Trả → Bạn đọc → Nhân viên → Báo cáo/Khác
5. **Cover** + checkpoint cuối

## Out of scope

- Dark theme (sẽ làm sau, đã reserve variable mode)
- Animation / motion spec chi tiết
- Code Connect mapping (sẽ làm khi triển khai code thực tế)
- Triển khai code XML mới (đây là pure design — không sửa code Android)

## Open questions / rủi ro

- Token consumption khi dựng 27+ frame trong 1 phiên — có thể cần chia phiên nếu vượt giới hạn.
- Material Symbols trong Figma: dùng plugin hoặc copy SVG từ fonts.google.com/icons.
