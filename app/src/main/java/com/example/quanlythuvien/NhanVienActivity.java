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

import com.example.quanlythuvien.adapter.NhanVienAdapter;
import com.example.quanlythuvien.db.NhanVienDao;
import com.example.quanlythuvien.model.NhanVien;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class NhanVienActivity extends AppCompatActivity {

    private NhanVienDao nhanVienDao;
    private NhanVienAdapter adapter;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhanvien);

        nhanVienDao = new NhanVienDao(this);
        tvEmpty = findViewById(R.id.tvEmpty);

        RecyclerView rv = findViewById(R.id.rvNhanVien);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NhanVienAdapter(this::onItemClick);
        rv.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSearch).setOnClickListener(v ->
                Toast.makeText(this, "Tìm kiếm đang phát triển", Toast.LENGTH_SHORT).show());
        findViewById(R.id.fabAdd).setOnClickListener(v ->
                startActivity(new Intent(this, NhanVienAddActivity.class)));

        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
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

    private void onItemClick(NhanVien nv) {
        startActivity(NhanVienDetailActivity.newIntent(this, nv.nvId));
    }

    private void loadData() {
        List<NhanVien> list = nhanVienDao.listAll();
        adapter.submit(list);
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
