# Figma Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.
> **MANDATORY:** Before any `use_figma` call, load the `figma:figma-use` skill. Before `create_new_file`, load `figma-create-new-file` if it exists.

**Goal:** Dựng toàn bộ 27 màn hình app Quản Lý Thư Viện trong một file Figma mới theo design system Material 3 Refresh.

**Architecture:** Một file Figma duy nhất với 10 pages (Cover, Foundations, Components, 6 nhóm Screens, Flows). Foundations dùng Figma Variables để mọi token (color/spacing/radius) có thể đổi theme. Mỗi màn hình là một frame 360×800 dp, dựng từ components đã tạo ở page Components.

**Tech Stack:** Figma MCP (`use_figma`, `create_new_file`, `get_screenshot`), Material Symbols Rounded (lấy SVG từ fonts.google.com/icons hoặc plugin), Figma Plugin API trong từng `use_figma` call.

**Spec gốc:** `docs/superpowers/specs/2026-05-19-figma-redesign-design.md`

**Plan key (đã verify):** `team::1628985609645022300` (Sơn Võ Thái's team)

---

## Conventions xuyên suốt

- **Frame size phone:** 360 × 800 dp
- **Spacing scale (dp):** 4, 8, 12, 16, 20, 24, 32, 40, 48
- **Variable collection:** `LibraryTokens` với mode `Light` (chuẩn bị mode `Dark` cho sau)
- **Color variable naming:** `color/primary`, `color/onPrimary`, `color/primaryContainer`, ... khớp tên token M3
- **Text style naming:** `display/large`, `title/large`, `body/medium`, ...
- **Số dp = số px trong Figma** (1:1 không scale)
- **Verify mỗi task:** chụp `get_screenshot` frame vừa dựng, so sánh với mô tả trong task
- **Không commit gì lên Git** trong suốt quá trình dựng Figma (file Figma không nằm trong repo). Chỉ commit `plan.md` đã được cập nhật checkbox nếu cần.

---

### Task 1: Tạo Figma file mới + setup 10 pages

**Tools:** `create_new_file`, `use_figma`

- [ ] **Step 1: Load skill figma-create-new-file (nếu có), fallback figma-use**

Run: invoke `Skill` với name `figma:figma-create-new-file` — nếu trả về "không tìm thấy" thì load `figma:figma-use` thay thế.

- [ ] **Step 2: Tạo file Figma mới**

Gọi `create_new_file`:
```
fileName: "Quản Lý Thư Viện — Redesign (M3)"
planKey: "team::1628985609645022300"
editorType: "design"
```

Ghi lại `fileKey` và `url` trả về. Lưu fileKey để tất cả `use_figma` về sau.

- [ ] **Step 3: Tạo 10 pages**

Load skill `figma:figma-use`. Sau đó `use_figma` với code tạo pages (sau Page 1 mặc định, rename + add 9 page nữa):

```js
const titles = [
  "🪧 Cover",
  "🎨 Foundations",
  "🧩 Components",
  "📱 Screens — Auth",
  "📱 Screens — Main",
  "📱 Screens — Catalog",
  "📱 Screens — Mượn-Trả",
  "📱 Screens — Bạn đọc & Nhân viên",
  "📱 Screens — Báo cáo & Khác",
  "🔄 Flows"
];
figma.root.children[0].name = titles[0];
for (let i = 1; i < titles.length; i++) {
  const p = figma.createPage();
  p.name = titles[i];
}
```

- [ ] **Step 4: Verify**

Chụp `get_screenshot` page bất kỳ (vd "🎨 Foundations") để xác nhận file mở được. Update checkbox trong plan.md.

---

### Task 2: Foundations — Color Variables

**Page:** 🎨 Foundations
**Tools:** `use_figma`

- [ ] **Step 1: Tạo Variable Collection `LibraryTokens` với mode Light**

```js
const col = figma.variables.createVariableCollection("LibraryTokens");
// mode mặc định "Mode 1" → rename "Light"
col.renameMode(col.modes[0].modeId, "Light");
```

- [ ] **Step 2: Tạo 16 color variables (light mode)**

Mapping (đầy đủ từ spec mục 1):
```
color/primary               #1565C0
color/onPrimary             #FFFFFF
color/primaryContainer      #D6E4FF
color/onPrimaryContainer    #001A41
color/secondary             #5A5F71
color/secondaryContainer    #DEE1F9
color/tertiary              #FFB300
color/onTertiary            #3F2E00
color/error                 #BA1A1A
color/errorContainer        #FFDAD6
color/surface               #FDFCFF
color/surfaceVariant        #E1E2EC
color/surfaceContainer      #F1F3F9
color/outline               #74777F
color/outlineVariant        #C4C6D0
color/onSurface             #1A1C1E
```

Code mẫu một biến:
```js
const v = figma.variables.createVariable("color/primary", col, "COLOR");
v.setValueForMode(col.modes[0].modeId, { r: 0x15/255, g: 0x65/255, b: 0xC0/255, a: 1 });
```

Lặp cho 16 biến.

- [ ] **Step 3: Tạo status color variables**

```
color/status/conHan     #2E7D32
color/status/sapHet     #EF6C00
color/status/quaHan     #BA1A1A
color/status/daTra      #546E7A
```

- [ ] **Step 4: Vẽ "Color palette swatches" trên page Foundations**

Frame `Foundations / Colors` (W=920, H=auto, auto-layout vertical, padding 24, gap 24). Mỗi token một row với 64×64 swatch + tên + hex. Group thành sections: "Primary", "Secondary/Tertiary", "Surface", "Outline/Error", "Status".

- [ ] **Step 5: Verify** — Chụp screenshot frame `Colors`, xác nhận đủ 20 ô màu hiển thị đúng hex.

---

### Task 3: Foundations — Typography

**Page:** 🎨 Foundations
**Tools:** `use_figma`

- [ ] **Step 1: Load font Roboto** (load `loadFontAsync` cho Regular/Medium/Bold)

```js
await figma.loadFontAsync({ family: "Roboto", style: "Regular" });
await figma.loadFontAsync({ family: "Roboto", style: "Medium" });
await figma.loadFontAsync({ family: "Roboto", style: "Bold" });
```

- [ ] **Step 2: Tạo 8 text styles**

| Name | Family / Style | Size | Line height | Letter spacing |
|---|---|---|---|---|
| display/large | Roboto Regular | 57 | 64 | -0.25 |
| headline/large | Roboto Regular | 32 | 40 | 0 |
| title/large | Roboto Medium | 22 | 28 | 0 |
| title/medium | Roboto Medium | 16 | 24 | 0.15 |
| body/large | Roboto Regular | 16 | 24 | 0.5 |
| body/medium | Roboto Regular | 14 | 20 | 0.25 |
| label/large | Roboto Medium | 14 | 20 | 0.1 |
| label/medium | Roboto Medium | 12 | 16 | 0.5 |

```js
const s = figma.createTextStyle();
s.name = "title/large";
s.fontName = { family: "Roboto", style: "Medium" };
s.fontSize = 22;
s.lineHeight = { value: 28, unit: "PIXELS" };
s.letterSpacing = { value: 0, unit: "PIXELS" };
```

- [ ] **Step 3: Vẽ "Typography specimen" frame**

Frame `Foundations / Typography` — mỗi style một dòng "The quick brown fox" + tên style + size/weight.

- [ ] **Step 4: Verify screenshot**

---

### Task 4: Foundations — Shape, Spacing, Elevation

**Page:** 🎨 Foundations
**Tools:** `use_figma`

- [ ] **Step 1: Tạo number variables (shape & spacing)**

```
shape/corner/xs   4
shape/corner/sm   8
shape/corner/md   12
shape/corner/lg   16
shape/corner/xl   28
shape/corner/pill 100

spacing/0  0
spacing/1  4
spacing/2  8
spacing/3  12
spacing/4  16
spacing/5  20
spacing/6  24
spacing/8  32
spacing/10 40
spacing/12 48
```

- [ ] **Step 2: Vẽ shape demo frame**

Frame `Foundations / Shape` — 6 ô vuông 80×80 với corner radius tương ứng + label.

- [ ] **Step 3: Vẽ elevation demo frame**

Frame `Foundations / Elevation` — 3 card 120×80, mỗi card có effect drop-shadow M3:
- Level 1: y=1, blur=2, alpha=0.30 + y=1, blur=3, alpha=0.15
- Level 2: y=1, blur=2 + y=2, blur=6, alpha=0.15
- Level 3: y=1, blur=3 + y=4, blur=8, alpha=0.15

- [ ] **Step 4: Vẽ Iconography frame** (cách dùng Material Symbols Rounded)

Frame `Foundations / Icons` — vẽ 24 ô 32×32 chứa SVG các icon từ bảng mapping spec mục 5. Lấy SVG bằng cách inline path từ fonts.google.com/icons hoặc dán SVG string. Mỗi icon kèm label dưới.

- [ ] **Step 5: Verify** — screenshot toàn page Foundations.

---

### Task 5: Component — TopAppBar (Small + CenterAligned)

**Page:** 🧩 Components
**Tools:** `use_figma`

- [ ] **Step 1: Tạo component `TopAppBar/Small`**

Specs: W=360, H=56, auto-layout horizontal, padding L=4 R=4, gap=0, fill = `color/primary`.
- Leading icon button 48×48 (touch target), icon `arrow_back` 24, tint `color/onPrimary`
- Title text "Headline" — title/large, color `onPrimary`, padding-left 16
- Spacer flex
- Trailing icon button 48×48 (vd `search`)

- [ ] **Step 2: Component variants**

Properties: `Leading` (back / menu / none), `Trailing` (search / filter / none / search+filter), `Scrolled` (false/true → khi true thêm shadow Level 2)

- [ ] **Step 3: Tạo component `TopAppBar/CenterAligned`**

Như Small nhưng title căn giữa.

- [ ] **Step 4: Tạo component `TopAppBar/Search`** (state thay thế khi nhấn search)

Leading back + search field full width + trailing close.

- [ ] **Step 5: Verify** — screenshot ba components.

---

### Task 6: Component — Bottom Navigation Bar

**Page:** 🧩 Components

- [ ] **Step 1: Tạo `BottomNav/Item` (sub-component)**

W=72, H=64, auto-layout vertical center. Variants `state` = active / inactive.
- Active: pill indicator 56×32 nền `secondaryContainer` + icon filled `onSecondaryContainer` + label `onSurface` label/medium
- Inactive: chỉ icon outlined `onSurfaceVariant` + label `onSurfaceVariant`

- [ ] **Step 2: Tạo `BottomNav` container**

W=360, H=80, fill `surfaceContainer`, auto-layout horizontal, distribute 5 items đều.

- [ ] **Step 3: Tạo 5 instance items** (icons: home, menu_book, swap_horiz, groups, more_horiz) với label "Trang chủ", "Sách", "Mượn-Trả", "Bạn đọc", "Khác".

- [ ] **Step 4: Verify** — screenshot.

---

### Task 7: Components — Buttons + FAB

**Page:** 🧩 Components

- [ ] **Step 1: Tạo `Button` component**

H=40, auto-layout horizontal, padding 24, gap 8, radius pill (100).
Variants:
- `style`: filled (fill primary, text onPrimary) / tonal (fill secondaryContainer, text onSecondaryContainer) / outlined (border outline, text primary) / text (no bg, text primary)
- `state`: default / hover / pressed / disabled
- `leadingIcon`: yes/no

Label: label/large.

- [ ] **Step 2: Tạo `FAB/Extended`**

W=auto, H=56, padding L=16 R=20, gap 12, fill `tertiaryContainer` (#FFE0B3 hoặc derive từ #FFB300), radius 16, icon 24 (`add`), label "Thêm" title/medium. Elevation Level 3.

- [ ] **Step 3: Tạo `FAB/Regular`** (chỉ icon, 56×56, radius 16).

- [ ] **Step 4: Verify** screenshot tất cả button variants.

---

### Task 8: Component — TextField (Outlined M3)

**Page:** 🧩 Components

- [ ] **Step 1: Tạo `TextField/Outlined`**

W=320, H=56, border outline 1px radius 4 (corner xs), padding L=16 R=16.
- Label nổi (state empty: 16dp ở giữa color outline; state filled/focused: 12dp ở top-left, nền surface đè qua border, color primary khi focused)
- Input text body/large color onSurface
- Trailing icon optional (vd `visibility` cho password)
- Supporting text label/medium color onSurfaceVariant 4dp dưới field

Variants:
- `state`: enabled / focused / filled / error / disabled
- `trailingIcon`: none / visibility / clear
- `leadingIcon`: none / search / person

- [ ] **Step 2: Verify** screenshot 5 state.

---

### Task 9: Components — Card variants

**Page:** 🧩 Components

- [ ] **Step 1: `Card/Elevated`** — radius 16, fill surface, shadow Level 1, padding 16, auto-layout vertical gap 8.

- [ ] **Step 2: `Card/Filled`** — radius 16, fill surfaceContainer, no shadow.

- [ ] **Step 3: `Card/Outlined`** — radius 16, border 1 outlineVariant, no shadow.

- [ ] **Step 4: `StatCard`** (cho Home dashboard) — W=164, H=104, layout vertical. Slot: icon 32 + value title/large + label body/medium. Một stat dùng primary tone, một dùng tertiary tone.

- [ ] **Step 5: Verify** screenshot.

---

### Task 10: Components — ListItem, Chip, Dialog, Snackbar

**Page:** 🧩 Components

- [ ] **Step 1: `ListItem`** — W=360, H=72, padding 16. Slots: leading (avatar 40 / icon 24), headline (body/large), supporting (body/medium onSurfaceVariant), trailing (icon / chip / text).

Variants: `lines` 1/2/3, `leading` avatar/icon/none, `trailing` icon/text/chip/none.

- [ ] **Step 2: `Chip`** components

- `Chip/Assist` — radius 8, border outline, padding 16/8, label label/large
- `Chip/Filter` — như Assist nhưng có state selected (fill secondaryContainer + check icon)
- `Chip/Status` — pill, padding 12/4, label label/medium. Variants: conHan/sapHet/quaHan/daTra theo color/status/*

- [ ] **Step 3: `Dialog`** — W=312, padding 24, radius 28, fill surface. Slots: icon (optional, 24, primary), title title/large, body body/medium, actions row (text buttons end-aligned).

- [ ] **Step 4: `Snackbar`** — H=48, radius 4, fill inverseSurface (~#2F3033), text body/medium color #F1F0F4, action label label/large primary tint #A8C7FA.

- [ ] **Step 5: `BottomSheet`** handle bar — W=32 H=4 radius pill, dùng cho dialog filter.

- [ ] **Step 6: Verify** screenshot tất cả.

---

### Task 11: Screen — Login

**Page:** 📱 Screens — Auth
**Frame:** `Login` (360×800)

- [ ] **Step 1: Tạo frame và background**

Frame W=360 H=800, fill `surface`. Top half W=360 H=400 nền gradient: `primary` → mix với 10% white ở dưới (làm header bo cong dưới: dùng vector rect với corner BL=0 BR=0 + bottom edge cong bằng arc — hoặc đơn giản: rect 360×360 + ellipse mask bottom).

- [ ] **Step 2: Brand mark** — đặt giữa header, y=72: icon `local_library` filled 48 trong vòng tròn 96 nền `primaryContainer`. Dưới đó text "QUẢN LÝ THƯ VIỆN" title/large color onPrimary, tracking 0.04em.

- [ ] **Step 3: Card form**

Card elevated W=304 (margin 28 hai bên), radius 24, padding 28/32, y=420 (lệch xuống ~55% như layout cũ). Bên trong:
- 2 instance `TextField/Outlined` với leadingIcon `person` (label "Tên đăng nhập") và `lock` (label "Mật khẩu", trailingIcon `visibility`)
- Text "Quên mật khẩu?" align-end, body/medium color primary, có underline
- Button Filled full-width "Đăng nhập" — label/large

- [ ] **Step 4: Footer text** dưới card: "© 2026 Library Management" body/medium color onSurfaceVariant, căn giữa, margin-top 24.

- [ ] **Step 5: Verify** screenshot frame Login.

---

### Task 12: Screen — ForgotPassword + Success state

**Page:** 📱 Screens — Auth

- [ ] **Step 1: Frame `ForgotPassword`** (360×800)
- TopAppBar Small, leading back, title "Quên mật khẩu"
- Illustration `lock_reset` 96 trong vòng tròn 144 primaryContainer, y=120
- Headline "Khôi phục mật khẩu" headline/large center y=296
- Body "Nhập email của bạn để nhận liên kết đặt lại mật khẩu" body/large onSurfaceVariant center, padding-x 32
- TextField outlined leadingIcon `mail`, label "Email", margin-x 24
- Button filled "Gửi liên kết" full-width margin-x 24, margin-top 32

- [ ] **Step 2: Frame `ForgotPassword - Success`**

Cùng template nhưng thay illustration thành `check_circle` 96 trong vòng tròn 144 tertiaryContainer, headline "Đã gửi email", body "Kiểm tra hộp thư...", button "Quay lại đăng nhập".

- [ ] **Step 3: Verify** screenshot cả hai.

---

### Task 13: Screen — Home (Dashboard)

**Page:** 📱 Screens — Main
**Frame:** `Home` (360×800)

- [ ] **Step 1: TopAppBar** custom 56dp, nền primary
- Leading: icon `menu` 24, padding 16
- Center-vertical: vertical stack — "Xin chào 👋" label/medium opacity 80, "Nhân viên ABC" title/medium bold
- Trailing: avatar 32 (account_circle filled)

- [ ] **Step 2: Stats Card** (margin 16, radius 16, elevation 1, padding 16)

Auto-layout horizontal 2 columns + divider 1px outlineVariant ở giữa.
- Col 1: `swap_horiz` 24 primary + "Lượt mượn" label/medium + "128" headline/large primary
- Col 2: `assignment_return` 24 tertiary + "Lượt trả" label/medium + "94" headline/large tertiary

- [ ] **Step 3: Section "Truy cập nhanh"** title/medium margin-top 24 margin-x 16.

Grid 3×2 (gap 12), mỗi ô: vòng tròn 56 secondaryContainer chứa icon 28 onSecondaryContainer + label body/medium dưới. 6 ô: Sách (menu_book), Bạn đọc (groups), Liên hệ (support_agent), Mượn-Trả (swap_horiz), Báo cáo (analytics), Nhân viên (badge).

- [ ] **Step 4: Card "Gần đây"** elevated radius 16 margin-top 24 margin-x 16 padding 16.

Title "Gần đây" title/medium + 3 ListItem (Sách / Bạn đọc / Nhân viên) với mock data + chip status.

- [ ] **Step 5: BottomNav** instance ở đáy, item "Trang chủ" active.

- [ ] **Step 6: Verify** screenshot Home.

---

### Task 14: Screen — Menu Drawer (overlay)

**Page:** 📱 Screens — Main
**Frame:** `MenuDrawer` (360×800)

- [ ] **Step 1:** Scrim đen 32% phủ toàn màn.

- [ ] **Step 2:** Drawer W=280 H=800 nền surface, slide từ trái.

Content vertical:
- Header 160dp: nền primaryContainer, avatar 56 + tên "Võ Thái Sơn" title/medium + email body/medium
- 7 ListItem (no leading icon padding-left 24): Trang chủ (home), Sách (menu_book), Mượn-Trả (swap_horiz), Bạn đọc (groups), Nhân viên (badge), Báo cáo (analytics), Liên hệ (support_agent)
- Divider
- ListItem "Đăng xuất" icon `logout` color error

- [ ] **Step 3: Verify** screenshot.

---

### Task 15: Screen — SachList (2 tabs)

**Page:** 📱 Screens — Catalog
**Frames:** `SachList - Tab Tất cả`, `SachList - Tab Thể loại`

- [ ] **Step 1: Common template (Tab Tất cả)**

- TopAppBar Small: back + "Sách" + search + filter
- TabRow H=48: 2 tab "Tất cả" (active, primary, indicator pill 60×3 primary) / "Thể loại" (inactive onSurfaceVariant)
- Divider 1px
- Count row H=48 padding-x 16: "24 sản phẩm" body/medium + icon `tune` end
- RecyclerView area: 10 row item sách (vd 64dp height — image cover 48×64 + title body/large + author body/medium + chip status)
- FAB Extended "Thêm sách" bottom-end margin 16
- BottomNav "Sách" active

- [ ] **Step 2: Frame Tab Thể loại** — duplicate frame, đổi tab active sang "Thể loại". Thay list bằng grid 2 cột (160×120 mỗi ô): card filled primaryContainer, icon thể loại 32 + tên + số sách.

- [ ] **Step 3: Verify** screenshot cả 2 frame.

---

### Task 16: Screen — SachDetail + SachAdd

**Page:** 📱 Screens — Catalog
**Frames:** `SachDetail`, `SachAdd`

- [ ] **Step 1: SachDetail**

- TopAppBar Small: back + "Chi tiết sách" + trailing icon `edit`
- Hero section H=240 nền primaryContainer: book cover 120×160 center + shadow Level 1, title book + author dưới
- Section "Thông tin" — card filled padding 16: rows (Mã sách, ISBN, Thể loại, Năm XB, Số lượng, Còn lại) — 2 cột label body/medium / value body/large
- Section "Mô tả" — body/large 3 dòng + "Xem thêm"
- Section "Thống kê mượn" — 2 mini stat card (Lượt mượn, Đang mượn)
- Bottom: 2 button Tonal "Sửa" + Filled "Lập phiếu mượn" share-row padding 16

- [ ] **Step 2: SachAdd**

- TopAppBar Small: back + "Thêm sách" + trailing "Lưu" text button onPrimary
- Form scroll padding 16, vertical gap 16:
  - Image picker 120×160 placeholder + "Thêm ảnh bìa"
  - TextField "Tên sách *"
  - TextField "Tác giả"
  - TextField "ISBN"
  - Dropdown TextField "Thể loại" trailingIcon `arrow_drop_down`
  - 2 TextField hàng ngang "Năm XB" + "Số lượng"
  - TextField "Mô tả" multiline H=120

- [ ] **Step 3: Verify** screenshot.

---

### Task 17: Screen — TheLoaiDetail + TheLoaiEdit + DialogFilterSach

**Page:** 📱 Screens — Catalog
**Frames:** `TheLoaiDetail`, `TheLoaiEdit`, `DialogFilterSach`

- [ ] **Step 1: TheLoaiDetail**

- TopAppBar Small + "Tiểu thuyết" + trailing `edit`
- Hero card filled primaryContainer 120dp: icon `category` 64 + tên thể loại + "12 sách"
- ListItem section "Sách trong thể loại": 8 row.

- [ ] **Step 2: TheLoaiEdit**

- TopAppBar Small + "Sửa thể loại" + trailing "Lưu"
- Icon picker (grid 6×3 các icon Material Symbols để chọn) + TextField "Tên thể loại" + TextField "Mô tả".

- [ ] **Step 3: DialogFilterSach**

Bottom sheet H=auto, radius top 28, handle bar.
- Title "Bộ lọc" title/large
- Section "Thể loại": chip filter wrap (8 chip)
- Section "Trạng thái": chip filter (Còn, Hết, Sắp hết)
- Section "Sắp xếp": radio "Tên A→Z", "Mới nhất", "Lượt mượn"
- Footer 2 button: outlined "Đặt lại" + filled "Áp dụng".

- [ ] **Step 4: Verify** screenshot 3 frame.

---

### Task 18: Screen — MuonTraList (2 tabs)

**Page:** 📱 Screens — Mượn-Trả
**Frames:** `MuonTraList - Tab Mượn`, `MuonTraList - Tab Trả`

- [ ] **Step 1: Tab Mượn**

- TopAppBar: back + "Quản lý mượn trả" + search
- TabRow: "Phiếu Mượn" active / "Phiếu Trả"
- Count row: "16 Phiếu mượn" + filter icon
- List: card outlined 88dp mỗi phiếu — mã PM, tên bạn đọc, ngày mượn / ngày phải trả, chip status (conHan/sapHet/quaHan)
- FAB Extended "Lập phiếu mượn"
- BottomNav "Mượn-Trả" active.

- [ ] **Step 2: Tab Trả** — đổi tab active, list phiếu trả với chip "Đã trả" status.

- [ ] **Step 3: Verify** screenshot.

---

### Task 19: Screen — PhieuMuonDetail + PhieuMuonAdd

**Page:** 📱 Screens — Mượn-Trả

- [ ] **Step 1: PhieuMuonDetail**

- TopAppBar + "Phiếu mượn #PM001" + trailing `more_vert`
- Card Filled: status chip large + ngày mượn / ngày trả + bạn đọc avatar+tên + nhân viên
- Section "Sách đã mượn" — list 3 sách (cover 48×64 + title + số lượng)
- Bottom button Filled "Tạo phiếu trả" full-width margin 16

- [ ] **Step 2: PhieuMuonAdd**

- TopAppBar + "Lập phiếu mượn" + "Lưu"
- TextField search/dropdown "Bạn đọc" leadingIcon `person`
- TextField date "Ngày mượn" + "Ngày trả dự kiến" hai cột
- Section "Sách" — list các sách đã chọn (item_phieumuon_pick): cover + title + stepper số lượng
- Button outlined full-width "+ Thêm sách"

- [ ] **Step 3: Verify** screenshot.

---

### Task 20: Screen — PhieuTraDetail + PhieuTraAdd

**Page:** 📱 Screens — Mượn-Trả

- [ ] **Step 1: PhieuTraDetail** — như PhieuMuonDetail nhưng tiêu đề "Phiếu trả #PT001", status "Đã trả" (color daTra), thêm field "Tiền phạt" (nếu quá hạn).

- [ ] **Step 2: PhieuTraAdd**

- TopAppBar + "Lập phiếu trả" + "Lưu"
- TextField dropdown "Phiếu mượn liên quan"
- Auto-fill bạn đọc + ngày
- List sách với checkbox "Đã trả"
- TextField "Tình trạng sách" + "Tiền phạt" (auto-calc nếu trễ)

- [ ] **Step 3: Verify** screenshot.

---

### Task 21: Screen — BanDocList + BanDocDetail + BanDocAdd

**Page:** 📱 Screens — Bạn đọc & Nhân viên

- [ ] **Step 1: BanDocList**
- TopAppBar + "Bạn đọc" + search
- Count row "120 bạn đọc"
- List ListItem 72dp: avatar 40 + tên + mã thẻ body/medium + trailing chip "Đang mượn 2"
- FAB "Thêm bạn đọc"

- [ ] **Step 2: BanDocDetail**
- TopAppBar + "Chi tiết bạn đọc" + `edit`
- Hero 200dp: avatar 96 center + tên title/large + mã thẻ + status chip
- Info card: SĐT, Email, Địa chỉ, Ngày sinh
- Stats: lượt mượn / đang mượn / quá hạn
- Section "Lịch sử mượn" — list 5 phiếu gần nhất.

- [ ] **Step 3: BanDocAdd**
- TopAppBar + "Thêm bạn đọc" + "Lưu"
- Avatar picker 96 + camera icon
- TextField: Họ tên * / Mã thẻ / SĐT / Email / Địa chỉ / Ngày sinh (date picker) / Giới tính (radio).

- [ ] **Step 4: Verify** screenshot 3 frame.

---

### Task 22: Screen — NhanVienList + NhanVienDetail + NhanVienAdd

**Page:** 📱 Screens — Bạn đọc & Nhân viên

- [ ] **Step 1: NhanVienList** — như BanDocList nhưng icon `badge`, trailing chip role.

- [ ] **Step 2: NhanVienDetail** — như BanDocDetail nhưng thêm card "Quyền hạn" (chip list: Quản trị / Thủ thư / Xem báo cáo) và "Tài khoản đăng nhập" (username).

- [ ] **Step 3: NhanVienAdd** — form như BanDocAdd nhưng thêm field "Chức vụ" (dropdown), "Username" + "Mật khẩu".

- [ ] **Step 4: Verify** screenshot.

---

### Task 23: Screen — LienHe

**Page:** 📱 Screens — Báo cáo & Khác
**Frame:** `LienHe`

- [ ] **Step 1: Build**
- TopAppBar + "Liên hệ"
- Hero 160dp nền primaryContainer: icon `support_agent` 64 + "Chúng tôi có thể giúp gì?"
- Section "Thông tin liên hệ" — 4 ListItem: Hotline (phone), Email (mail), Địa chỉ (location_on), Website (language)
- Section "Gửi phản hồi" — card filled: TextField "Tiêu đề" + TextField multiline "Nội dung" + Button Filled "Gửi".

- [ ] **Step 2: Verify** screenshot.

---

### Task 24: Screen — BaoCao + BaoCaoChiTiet

**Page:** 📱 Screens — Báo cáo & Khác

- [ ] **Step 1: BaoCao**

- Header curved H=200 fill primary: back icon + "Báo cáo" title/large center, avatar 64 center + tên nhân viên dưới (theo layout gốc nhưng modernize: dùng surface tint thay shadow)
- Section title "XEM BÁO CÁO CHI TIẾT" label/medium uppercase
- List 5 ListItem báo cáo (item_baocao): icon + tên báo cáo + trailing arrow:
  - Báo cáo lượt mượn (swap_horiz)
  - Báo cáo lượt trả (assignment_return)
  - Báo cáo theo thể loại (category)
  - Báo cáo bạn đọc (groups)
  - Báo cáo sách quá hạn (error)

- [ ] **Step 2: BaoCaoChiTiet**

- TopAppBar + tên báo cáo + `share`
- Filter row: 2 chip "Tháng này" + "Tất cả thể loại" (chip filter có dropdown)
- Card chart elevated H=240 — placeholder chart (vẽ bar chart 6 cột màu primary)
- Stat row 3 mini stat
- List "Top theo thể loại" — item_thongke_theloai: rank + tên + progress bar + số lượng

- [ ] **Step 3: Verify** screenshot.

---

### Task 25: Cover page + final QA

**Page:** 🪧 Cover

- [ ] **Step 1: Tạo frame Cover** 1600×900

- Title "Quản Lý Thư Viện" display/large primary
- Subtitle "Material 3 Refresh — Mobile Redesign" headline/large secondary
- Mockup grid: paste 6 screenshot thumbnail (Login, Home, SachList, MuonTraList, BanDocDetail, BaoCao) — dùng `figma.exportAsync` trong code và `setFillAsync` hoặc đơn giản tạo frame instance link tới các screen frames đã có
- Footer: "Designed by Võ Thái Sơn · 2026-05-19 · v1.0"

- [ ] **Step 2: Final QA pass**

Chụp screenshot từng page (10 pages), so sánh:
- Tất cả màu dùng đúng variable từ collection LibraryTokens (không hardcode)
- Tất cả text dùng đúng text style
- Spacing nhất quán theo scale
- Component instance không bị detached

Fix mọi sai sót phát hiện.

- [ ] **Step 3: Trả về URL file Figma cho user**

In ra link `https://www.figma.com/file/<fileKey>` để user mở trực tiếp.

---

## Out-of-band tasks (chỉ làm nếu user yêu cầu)

- Dark mode: thêm mode `Dark` vào collection LibraryTokens + duplicate screen quan trọng để demo.
- Prototype flows: nối các frame bằng prototype arrow trên page 🔄 Flows.
- Code Connect: map components Figma ↔ Android layout (cần plugin `code-connect`).

## Lưu ý token consumption

Mỗi task gọi 1-3 `use_figma` lớn + 1 `get_screenshot`. Ước lượng toàn plan: 50-80 tool calls. Nếu phiên hiện tại không đủ ngân sách, dừng sau Task 10 (xong design system + components), cập nhật plan.md checkpoint, tiếp tục phiên sau với Task 11.
