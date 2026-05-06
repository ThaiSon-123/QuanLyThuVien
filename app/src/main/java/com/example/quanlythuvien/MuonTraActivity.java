package com.example.quanlythuvien;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.text.Normalizer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.PhieuAdapter;
import com.example.quanlythuvien.db.PhieuMuonDao;
import com.example.quanlythuvien.db.PhieuTraDao;
import com.example.quanlythuvien.model.PhieuMuon;
import com.example.quanlythuvien.model.PhieuTra;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MuonTraActivity extends AppCompatActivity {

    private static final int TAB_MUON = 0;
    private static final int TAB_TRA = 1;

    private PhieuMuonDao phieuMuonDao;
    private PhieuTraDao phieuTraDao;
    private PhieuAdapter adapter;

    private TextView tvCount;
    private TextView tvEmpty;
    private TextView tvFabLabel;
    private TextView tvTabMuon;
    private TextView tvTabTra;
    private View indicatorMuon;
    private View indicatorTra;

    // Search & Filter
    private TextView tvTitle;
    private ImageView btnSearch;
    private ImageView btnFilter;
    private LinearLayout headerSearch;
    private EditText edtSearch;
    private ImageView btnClearSearch;

    private static final String STATUS_ALL      = "all";
    private static final String STATUS_DANGMUON = "dangmuon";
    private static final String STATUS_DATRA    = "datra";

    private int currentTab = TAB_MUON;
    private boolean searchMode = false;
    private String currentKeyword = "";
    private String currentStatus = STATUS_ALL;
    private List<PhieuAdapter.Row> allRows = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muontra);

        phieuMuonDao = new PhieuMuonDao(this);
        phieuTraDao = new PhieuTraDao(this);

        bindViews();
        setupRecycler();
        setupHeader();
        setupSearchHeader();
        setupTabs();
        setupFab();
        setupBottomNav();

        selectTab(TAB_MUON);
    }

    private void bindViews() {
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvFabLabel = findViewById(R.id.tvFabLabel);
        tvTabMuon = findViewById(R.id.tvTabMuon);
        tvTabTra = findViewById(R.id.tvTabTra);
        indicatorMuon = findViewById(R.id.indicatorMuon);
        indicatorTra = findViewById(R.id.indicatorTra);
        tvTitle = findViewById(R.id.tvTitle);
        btnSearch = findViewById(R.id.btnSearch);
        btnFilter = findViewById(R.id.btnFilter);
        headerSearch = findViewById(R.id.headerSearch);
        edtSearch = findViewById(R.id.edtSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvPhieu);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PhieuAdapter(this::onRowClick);
        rv.setAdapter(adapter);
    }

    private void setupHeader() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> enterSearchMode());
        btnFilter.setOnClickListener(v -> showFilterDialog());
    }

    private void setupSearchHeader() {
        findViewById(R.id.btnSearchBack).setOnClickListener(v -> exitSearchMode());
        btnClearSearch.setOnClickListener(v -> edtSearch.setText(""));
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                String kw = s.toString();
                btnClearSearch.setVisibility(kw.isEmpty() ? View.GONE : View.VISIBLE);
                currentKeyword = kw;
                applyFilter();
            }
        });
    }

    private void enterSearchMode() {
        searchMode = true;
        tvTitle.setVisibility(View.GONE);
        btnSearch.setVisibility(View.GONE);
        headerSearch.setVisibility(View.VISIBLE);
        edtSearch.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(edtSearch, InputMethodManager.SHOW_IMPLICIT);
    }

    private void exitSearchMode() {
        searchMode = false;
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(edtSearch.getWindowToken(), 0);
        edtSearch.setText("");
        currentKeyword = "";
        headerSearch.setVisibility(View.GONE);
        tvTitle.setVisibility(View.VISIBLE);
        btnSearch.setVisibility(View.VISIBLE);
        applyFilter();
    }

    /** Chuẩn hóa: bỏ dấu tiếng Việt, lowercase. */
    private static String norm(String s) {
        if (s == null) return "";
        String nfd = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD);
        return nfd.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                  .replace('đ', 'd').replace('Đ', 'd');
    }

    private void applyFilter() {
        String kw = norm(currentKeyword);
        List<PhieuAdapter.Row> filtered = new ArrayList<>();
        for (PhieuAdapter.Row r : allRows) {
            // Lọc trạng thái (chỉ áp dụng cho tab Phiếu Mượn)
            if (currentTab == TAB_MUON && !STATUS_ALL.equals(currentStatus)) {
                if (!currentStatus.equals(r.status)) continue;
            }
            // Lọc keyword không dấu
            if (!kw.isEmpty() && !norm(r.ma).contains(kw) && !norm(r.tenBanDoc).contains(kw)) {
                continue;
            }
            filtered.add(r);
        }
        adapter.submit(filtered);
        String label = currentTab == TAB_MUON ? " Phiếu mượn" : " Phiếu trả";
        tvCount.setText(filtered.size() + label);
        boolean hasFilter = !kw.isEmpty() || (currentTab == TAB_MUON && !STATUS_ALL.equals(currentStatus));
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText(hasFilter ? "Không tìm thấy kết quả"
                : (currentTab == TAB_MUON ? "Chưa có phiếu mượn" : "Chưa có phiếu trả"));
        // Đổi màu icon filter khi đang có lọc trạng thái
        if (btnFilter != null) {
            boolean filterActive = currentTab == TAB_MUON && !STATUS_ALL.equals(currentStatus);
            btnFilter.setColorFilter(filterActive ? 0xFFFFD700 : 0xFFFFFFFF, PorterDuff.Mode.SRC_IN);
        }
    }

    private void showFilterDialog() {
        // Xây RadioGroup lọc trạng thái
        RadioGroup rg = new RadioGroup(this);
        rg.setOrientation(RadioGroup.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        rg.setPadding(padding, padding, padding, 0);

        String[][] options = {
                {STATUS_ALL,      "Tất cả"},
                {STATUS_DANGMUON, "Đang mượn"},
                {STATUS_DATRA,    "Đã trả"}
        };
        int checkedId = -1;
        int[] ids = new int[options.length];
        for (int i = 0; i < options.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());
            rb.setText(options[i][1]);
            rb.setTextSize(15f);
            rb.setPadding(8, 12, 8, 12);
            ids[i] = rb.getId();
            rg.addView(rb);
            if (options[i][0].equals(currentStatus)) checkedId = rb.getId();
        }
        if (checkedId != -1) rg.check(checkedId);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Lọc theo trạng thái")
                .setView(rg)
                .setPositiveButton("Áp dụng", null)
                .setNegativeButton("Đặt lại", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
                int sel = rg.getCheckedRadioButtonId();
                currentStatus = STATUS_ALL;
                for (int i = 0; i < options.length; i++) {
                    if (ids[i] == sel) { currentStatus = options[i][0]; break; }
                }
                applyFilter();
                dialog.dismiss();
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(b -> {
                currentStatus = STATUS_ALL;
                applyFilter();
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private void setupTabs() {
        LinearLayout tabMuon = findViewById(R.id.tabMuon);
        LinearLayout tabTra = findViewById(R.id.tabTra);
        tabMuon.setOnClickListener(v -> selectTab(TAB_MUON));
        tabTra.setOnClickListener(v -> selectTab(TAB_TRA));
    }

    private void setupFab() {
        findViewById(R.id.fabAdd).setOnClickListener(v -> onAddClick());
    }

    private void onAddClick() {
        if (currentTab == TAB_MUON) {
            startActivity(new Intent(this, PhieuMuonAddActivity.class));
        } else {
            startActivity(new Intent(this, PhieuTraAddActivity.class));
        }
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_borrow);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                goHome();
                return false;
            } else if (id == R.id.nav_book) {
                startActivity(new Intent(this, SachActivity.class));
                return false;
            } else if (id == R.id.nav_borrow) {
                return true;
            } else if (id == R.id.nav_reader) {
                startActivity(new Intent(this, BanDocActivity.class));
                return false;
            } else if (id == R.id.nav_logout) {
                confirmLogout();
                return false;
            }
            return false;
        });
    }

    private void selectTab(int tab) {
        currentTab = tab;
        boolean muon = tab == TAB_MUON;

        tvTabMuon.setTextColor(muon ? 0xFF4696EB : 0xFF747474);
        tvTabMuon.setTypeface(null, muon ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tvTabTra.setTextColor(muon ? 0xFF747474 : 0xFF4696EB);
        tvTabTra.setTypeface(null, muon ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);
        indicatorMuon.setBackgroundResource(muon ? R.drawable.bg_tab_indicator : android.R.color.transparent);
        indicatorTra.setBackgroundResource(muon ? android.R.color.transparent : R.drawable.bg_tab_indicator);

        tvFabLabel.setText(muon ? "Lập phiếu mượn" : "Lập phiếu trả");

        // Bộ lọc trạng thái chỉ có ý nghĩa với Phiếu Mượn
        btnFilter.setVisibility(muon ? View.VISIBLE : View.GONE);
        if (!muon) currentStatus = STATUS_ALL; // reset khi sang tab Trả

        loadData();
    }

    private void loadData() {
        allRows = new ArrayList<>();
        if (currentTab == TAB_MUON) {
            List<PhieuMuon> list = phieuMuonDao.listAll();
            for (PhieuMuon p : list) {
                allRows.add(new PhieuAdapter.Row(
                        p.pmId,
                        p.getMaPhieu(),
                        p.tenBanDoc,
                        formatDate(p.ngayMuon),
                        null,
                        p.trangthai));
            }
        } else {
            List<PhieuTra> list = phieuTraDao.listAll();
            for (PhieuTra p : list) {
                String tinhTrang = tinhTrangTra(p.ngayTra, p.ngayHanTra);
                allRows.add(new PhieuAdapter.Row(
                        p.ptId,
                        p.getMaPhieu(),
                        p.tenBanDoc,
                        formatDate(p.ngayTra),
                        "PM-" + p.pmId,
                        tinhTrang));
            }
        }
        applyFilter();
    }

    private void onRowClick(PhieuAdapter.Row row) {
        if (currentTab == TAB_MUON) {
            startActivity(PhieuMuonDetailActivity.newIntent(this, row.id));
        } else {
            startActivity(PhieuTraDetailActivity.newIntent(this, row.id));
        }
    }

    /**
     * So sánh ngày trả thực tế với hạn trả.
     * @return "dunghạn" nếu trả đúng/trước hạn, "trehan" nếu trễ.
     */
    private static String tinhTrangTra(String ngayTraThucTe, String ngayHanTra) {
        if (ngayTraThucTe == null || ngayHanTra == null) return "dunghạn";
        // So sánh chuỗi yyyy-MM-dd được rồi (ISO format so sánh đúng thứ tự)
        return ngayTraThucTe.compareTo(ngayHanTra) <= 0 ? "dunghạn" : "trehan";
    }

    /** yyyy-MM-dd → dd/MM/yyyy (robust với null hoặc format khác). */
    public static String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(iso);
            if (d != null) {
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
            }
        } catch (Exception ignored) {
        }
        return iso;
    }

    private void goHome() {
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (d, w) -> doLogout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doLogout() {
        getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (searchMode) {
            exitSearchMode();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
