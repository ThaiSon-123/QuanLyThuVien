package com.example.quanlythuvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.BanDocAdapter;
import com.example.quanlythuvien.db.BanDocDao;
import com.example.quanlythuvien.model.BanDoc;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class BanDocActivity extends AppCompatActivity {

    private BanDocDao banDocDao;
    private BanDocAdapter adapter;

    private TextView tvTongSo;
    private TextView tvDangMuon;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bandoc);

        banDocDao = new BanDocDao(this);

        bindViews();
        setupRecycler();
        setupActions();
        setupBottomNav();
    }

    private void bindViews() {
        tvTongSo = findViewById(R.id.tvTongSo);
        tvDangMuon = findViewById(R.id.tvDangMuon);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvBanDoc);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BanDocAdapter(this::onItemClick);
        rv.setAdapter(adapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSearch).setOnClickListener(v ->
                Toast.makeText(this, "Tìm kiếm đang phát triển", Toast.LENGTH_SHORT).show());
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, BanDocAddActivity.class)));
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
        List<BanDoc> list = banDocDao.listWithStats();
        adapter.submit(list);
        tvTongSo.setText(String.valueOf(banDocDao.countTotal()));
        tvDangMuon.setText(String.valueOf(banDocDao.countDangMuon()));
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
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
