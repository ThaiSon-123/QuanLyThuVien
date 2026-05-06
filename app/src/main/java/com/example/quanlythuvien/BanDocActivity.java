package com.example.quanlythuvien;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.BanDocAdapter;
import com.example.quanlythuvien.db.BanDocDao;
import com.example.quanlythuvien.model.BanDoc;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class BanDocActivity extends AppCompatActivity {

    private BanDocDao banDocDao;
    private BanDocAdapter adapter;

    private TextView tvTongSo;
    private TextView tvDangMuon;
    private TextView tvEmpty;

    private TextView tvTitle;
    private ImageView btnSearch;
    private LinearLayout headerSearch;
    private EditText edtSearch;
    private ImageView btnClearSearch;

    private boolean searchMode = false;
    private String currentKeyword = "";
    private List<BanDoc> allList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bandoc);

        banDocDao = new BanDocDao(this);

        bindViews();
        setupRecycler();
        setupActions();
        setupSearchHeader();
        setupBottomNav();
    }

    private void bindViews() {
        tvTongSo = findViewById(R.id.tvTongSo);
        tvDangMuon = findViewById(R.id.tvDangMuon);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvTitle = findViewById(R.id.tvTitle);
        btnSearch = findViewById(R.id.btnSearch);
        headerSearch = findViewById(R.id.headerSearch);
        edtSearch = findViewById(R.id.edtSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvBanDoc);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BanDocAdapter(this::onItemClick);
        rv.setAdapter(adapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> enterSearchMode());
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, BanDocAddActivity.class)));
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

    private static String norm(String s) {
        if (s == null) return "";
        String nfd = Normalizer.normalize(s.toLowerCase().trim(), Normalizer.Form.NFD);
        return nfd.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                  .replace('đ', 'd').replace('Đ', 'd');
    }

    private void applyFilter() {
        String kw = norm(currentKeyword);
        List<BanDoc> filtered;
        if (kw.isEmpty()) {
            filtered = new ArrayList<>(allList);
        } else {
            filtered = new ArrayList<>();
            for (BanDoc b : allList) {
                if (norm(b.ten).contains(kw)
                        || norm(b.sdt).contains(kw)
                        || norm(b.diachi).contains(kw)) {
                    filtered.add(b);
                }
            }
        }
        adapter.submit(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText(kw.isEmpty() ? "Chưa có bạn đọc" : "Không tìm thấy kết quả");
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_reader);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                goHome();
                return false;
            } else if (id == R.id.nav_book) {
                startActivity(new Intent(this, SachActivity.class));
                return false;
            } else if (id == R.id.nav_borrow) {
                startActivity(new Intent(this, MuonTraActivity.class));
                return false;
            } else if (id == R.id.nav_reader) {
                return true;
            } else if (id == R.id.nav_logout) {
                confirmLogout();
                return false;
            }
            return false;
        });
    }

    private void onItemClick(BanDoc bd) {
        startActivity(BanDocDetailActivity.newIntent(this, bd.bdId));
    }

    private void loadData() {
        allList = banDocDao.listWithStats();
        tvTongSo.setText(String.valueOf(banDocDao.countTotal()));
        tvDangMuon.setText(String.valueOf(banDocDao.countDangMuon()));
        applyFilter();
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
}
