package com.example.quanlythuvien;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.LienHeAdapter;
import com.example.quanlythuvien.db.BanDocDao;
import com.example.quanlythuvien.model.BanDoc;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class LienHeActivity extends AppCompatActivity {

    private BanDocDao banDocDao;
    private LienHeAdapter adapter;

    private EditText edtSearch;
    private TextView tvCount;
    private TextView tvEmpty;

    private List<BanDoc> allList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lienhe);

        banDocDao = new BanDocDao(this);

        bindViews();
        setupRecycler();
        setupActions();
        setupBottomNav();

        loadData();
    }

    private void bindViews() {
        edtSearch = findViewById(R.id.edtSearch);
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvLienHe);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LienHeAdapter(this::onCall);
        rv.setAdapter(adapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override
            public void afterTextChanged(Editable s) {
                applyFilter(s.toString());
            }
        });
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
                startActivity(new Intent(this, BanDocActivity.class));
                return false;
            } else if (id == R.id.nav_logout) {
                confirmLogout();
                return false;
            }
            return false;
        });
    }

    private void loadData() {
        allList = banDocDao.listAll();
        applyFilter(edtSearch.getText().toString());
    }

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
                    || norm(b.sdt).contains(kw)) {
                filtered.add(b);
            }
        }
        adapter.submit(filtered);
        tvCount.setText("Danh sách liên hệ (" + filtered.size() + "):");
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onCall(BanDoc bd) {
        if (TextUtils.isEmpty(bd.sdt)) {
            Toast.makeText(this, "Bạn đọc chưa có số điện thoại", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent dial = new Intent(Intent.ACTION_DIAL);
        dial.setData(Uri.parse("tel:" + bd.sdt));
        try {
            startActivity(dial);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được trình quay số", Toast.LENGTH_SHORT).show();
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
