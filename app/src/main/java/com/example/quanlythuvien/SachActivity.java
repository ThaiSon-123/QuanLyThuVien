package com.example.quanlythuvien;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.SachAdapter;
import com.example.quanlythuvien.adapter.TheLoaiAdapter;
import com.example.quanlythuvien.db.SachDao;
import com.example.quanlythuvien.db.TheLoaiDao;
import com.example.quanlythuvien.model.Sach;
import com.example.quanlythuvien.model.TheLoai;
import com.example.quanlythuvien.util.BookAccessPolicy;
import com.example.quanlythuvien.util.RoleHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SachActivity extends AppCompatActivity {

    private static final int TAB_ALL = 0;
    private static final int TAB_CATEGORY = 1;
    private static final String ROLE_ADMIN = "admin";

    public static final String EXTRA_FILTER = "filter";
    public static final String FILTER_LOW_STOCK = "low_stock";

    private boolean lowStockOnly = false;
    private static final String STATUS_ALL = "all";
    private static final String STATUS_CON = "con";
    private static final String STATUS_HET = "het";

    private SachDao sachDao;
    private TheLoaiDao theLoaiDao;
    private SachAdapter sachAdapter;
    private TheLoaiAdapter theLoaiAdapter;

    private RecyclerView rvSach;
    private RecyclerView rvTheLoai;

    private TextView tvTitle;
    private ImageView btnSearch;
    private LinearLayout headerSearch;
    private EditText edtSearch;
    private ImageView btnClearSearch;

    private TextView tvCount;
    private TextView tvEmpty;
    private TextView tvTabAll;
    private TextView tvTabCategory;
    private TextView tvFabLabel;
    private View indicatorAll;
    private View indicatorCategory;
    private ImageView btnFilter;

    private int currentTab = TAB_ALL;
    private boolean searchMode = false;
    private String currentKeyword = "";
    private String currentStatus = STATUS_ALL;
    private int currentTlId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sach);

        sachDao = new SachDao(this);
        theLoaiDao = new TheLoaiDao(this);

        bindViews();
        setupRecycler();
        setupHeader();
        setupSearchHeader();
        setupTabs();
        setupFab();
        setupBottomNav();

        String f = getIntent().getStringExtra(EXTRA_FILTER);
        if (FILTER_LOW_STOCK.equals(f)) {
            lowStockOnly = true;
            android.widget.Toast.makeText(this, "Lọc: sách sắp hết kho",
                    android.widget.Toast.LENGTH_SHORT).show();
        }

        selectTab(TAB_ALL);
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        btnSearch = findViewById(R.id.btnSearch);
        btnFilter = findViewById(R.id.btnFilter);
        headerSearch = findViewById(R.id.headerSearch);
        edtSearch = findViewById(R.id.edtSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);

        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTabAll = findViewById(R.id.tvTabAll);
        tvTabCategory = findViewById(R.id.tvTabCategory);
        indicatorAll = findViewById(R.id.indicatorAll);
        indicatorCategory = findViewById(R.id.indicatorCategory);
        tvFabLabel = findViewById(R.id.tvFabLabel);

        rvSach = findViewById(R.id.rvSach);
        rvTheLoai = findViewById(R.id.rvTheLoai);
    }

    private void setupRecycler() {
        rvSach.setLayoutManager(new LinearLayoutManager(this));
        sachAdapter = new SachAdapter(this::onSachClick);
        rvSach.setAdapter(sachAdapter);

        rvTheLoai.setLayoutManager(new GridLayoutManager(this, 2));
        theLoaiAdapter = new TheLoaiAdapter(this::onTheLoaiClick);
        rvTheLoai.setAdapter(theLoaiAdapter);
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
                if (currentTab == TAB_ALL) {
                    applyFilter();
                }
            }
        });
    }

    private void enterSearchMode() {
        searchMode = true;
        tvTitle.setVisibility(View.GONE);
        btnSearch.setVisibility(View.GONE);
        headerSearch.setVisibility(View.VISIBLE);
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
        if (currentTab == TAB_ALL) applyFilter();
    }

    private void showKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    private void setupTabs() {
        LinearLayout tabAll = findViewById(R.id.tabAll);
        LinearLayout tabCategory = findViewById(R.id.tabCategory);
        tabAll.setOnClickListener(v -> selectTab(TAB_ALL));
        tabCategory.setOnClickListener(v -> selectTab(TAB_CATEGORY));
    }

    private void setupFab() {
        android.view.View fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> onAddClick());
        if (!com.example.quanlythuvien.util.RoleHelper.isAdmin(this)) {
            fab.setVisibility(android.view.View.GONE);
        }
    }

    private void onAddClick() {
        String role = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(LoginActivity.KEY_ROLE, "");
        if (!ROLE_ADMIN.equals(role)) {
            Toast.makeText(this,
                    currentTab == TAB_ALL
                            ? "Bạn không có quyền thêm sách"
                            : "Bạn không có quyền thêm thể loại",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentTab == TAB_ALL) {
            startActivity(new Intent(this, SachAddActivity.class));
        } else {
            startActivity(new Intent(this, TheLoaiEditActivity.class));
        }
    }

    private void selectTab(int tab) {
        currentTab = tab;
        boolean all = tab == TAB_ALL;

        tvTabAll.setTextColor(all ? 0xFF4696EB : 0xFF747474);
        tvTabCategory.setTextColor(all ? 0xFF747474 : 0xFF4696EB);
        indicatorAll.setBackgroundResource(all ? R.drawable.bg_tab_indicator : android.R.color.transparent);
        indicatorCategory.setBackgroundResource(all ? android.R.color.transparent : R.drawable.bg_tab_indicator);


        rvSach.setVisibility(all ? View.VISIBLE : View.GONE);
        rvTheLoai.setVisibility(all ? View.GONE : View.VISIBLE);


        btnSearch.setVisibility(all && !searchMode ? View.VISIBLE : View.GONE);
        btnFilter.setVisibility(all ? View.VISIBLE : View.GONE);
        if (!all && searchMode) exitSearchMode();


        tvFabLabel.setText(all ? "Thêm sách" : "Thêm thể loại");

        if (all) {
            applyFilter();
        } else {
            loadCategoryView();
        }
    }

    private static String norm(String s) {
        if (s == null) return "";
        String nfd = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD);
        return nfd.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                  .replace('đ', 'd').replace('Đ', 'd');
    }

    private void applyFilter() {
        List<Sach> all = sachDao.filter(null, currentStatus, currentTlId);
        List<Sach> list;
        if (currentKeyword.trim().isEmpty()) {
            list = all;
        } else {
            String kw = norm(currentKeyword);
            list = new ArrayList<>();
            for (Sach s : all) {
                if (norm(s.ten).contains(kw) || norm(s.tacgia).contains(kw)) {
                    list.add(s);
                }
            }
        }
        if (lowStockOnly) {
            int threshold = com.example.quanlythuvien.db.CauHinhDao.getInstance(this).lowStockThreshold();
            List<Sach> lowList = new ArrayList<>();
            for (Sach s : list) if (s.soluong <= threshold) lowList.add(s);
            list = lowList;
        }
        sachAdapter.submit(list);
        tvCount.setText(list.size() + " sản phẩm");
        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText(hasActiveFilter() ? "Không có kết quả phù hợp" : "Chưa có sách nào");
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private boolean hasActiveFilter() {
        return !currentKeyword.isEmpty()
                || !STATUS_ALL.equals(currentStatus)
                || currentTlId > 0;
    }

    private void loadCategoryView() {
        List<TheLoai> list = theLoaiDao.listWithCount();
        theLoaiAdapter.submit(list);
        tvCount.setText(list.size() + " thể loại");
        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Chưa có thể loại nào");
        } else {
            tvEmpty.setVisibility(View.GONE);
        }
    }

    private void showFilterDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_filter_sach, null, false);
        RadioGroup rgStatus = view.findViewById(R.id.rgStatus);
        RadioGroup rgTheLoai = view.findViewById(R.id.rgTheLoai);


        int statusChecked = R.id.rbStatusAll;
        if (STATUS_CON.equals(currentStatus)) statusChecked = R.id.rbStatusCon;
        else if (STATUS_HET.equals(currentStatus)) statusChecked = R.id.rbStatusHet;
        rgStatus.check(statusChecked);


        List<TheLoai> theLoaiList = new ArrayList<>();
        theLoaiList.add(new TheLoai(0, "Tất cả"));
        theLoaiList.addAll(theLoaiDao.listAll());

        rgTheLoai.removeAllViews();
        for (int i = 0; i < theLoaiList.size(); i++) {
            TheLoai tl = theLoaiList.get(i);
            RadioButton rb = new RadioButton(this);
            rb.setId(View.generateViewId());
            rb.setText(tl.ten);
            rb.setTextSize(14f);
            rb.setTag(tl.tlId);
            if (tl.tlId == currentTlId) {
                rb.setChecked(true);
            }
            rgTheLoai.addView(rb);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Bộ lọc")
                .setView(view)
                .setPositiveButton("Áp dụng", null)
                .setNegativeButton("Đặt lại", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(b -> {
                int checkedId = rgStatus.getCheckedRadioButtonId();
                if (checkedId == R.id.rbStatusCon) currentStatus = STATUS_CON;
                else if (checkedId == R.id.rbStatusHet) currentStatus = STATUS_HET;
                else currentStatus = STATUS_ALL;

                int rgChecked = rgTheLoai.getCheckedRadioButtonId();
                currentTlId = 0;
                if (rgChecked != -1) {
                    RadioButton rb = rgTheLoai.findViewById(rgChecked);
                    if (rb != null && rb.getTag() instanceof Integer) {
                        currentTlId = (Integer) rb.getTag();
                    }
                }
                applyFilter();
                dialog.dismiss();
            });
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(b -> {
                currentStatus = STATUS_ALL;
                currentTlId = 0;
                applyFilter();
                dialog.dismiss();
            });
        });

        dialog.show();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_book);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent i = new Intent(this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
                return false;
            } else if (id == R.id.nav_book) {
                return true;
            } else if (id == R.id.nav_borrow) {
                startActivity(new Intent(this, MuonTraActivity.class));
                return false;
            } else if (id == R.id.nav_reader) {
                startActivity(new Intent(this, BanDocActivity.class));
                return false;
            } else if (id == R.id.nav_logout) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Đăng xuất")
                        .setMessage("Bạn có chắc muốn đăng xuất?")
                        .setPositiveButton("Đăng xuất", (d, w) -> {
                            getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE).edit().clear().apply();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
                return false;
            }
            return false;
        });
    }

    private void onSachClick(Sach sach) {
        if (!BookAccessPolicy.canOpenBookEditScreen(RoleHelper.currentRole(this))) {
            Toast.makeText(this, BookAccessPolicy.deniedMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(SachDetailActivity.newIntent(this, sach.sachId));
    }

    private void onTheLoaiClick(TheLoai tl) {
        startActivity(TheLoaiDetailActivity.newIntent(this, tl.tlId));
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
        if (currentTab == TAB_ALL) {
            applyFilter();
        } else {
            loadCategoryView();
        }
    }
}
