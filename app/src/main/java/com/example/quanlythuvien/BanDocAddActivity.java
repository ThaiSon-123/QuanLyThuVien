package com.example.quanlythuvien;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlythuvien.db.BanDocDao;
import com.example.quanlythuvien.model.BanDoc;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BanDocAddActivity extends AppCompatActivity {

    private BanDocDao banDocDao;
    private EditText edtTen;
    private EditText edtSdt;
    private EditText edtDiaChi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bandoc_add);

        banDocDao = new BanDocDao(this);

        bindViews();
        setupActions();
        setupBottomNav();
    }

    private void bindViews() {
        edtTen = findViewById(R.id.edtTen);
        edtSdt = findViewById(R.id.edtSdt);
        edtDiaChi = findViewById(R.id.edtDiaChi);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnConfirm).setOnClickListener(v -> onConfirm());
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
                finish();
                return false;
            } else if (id == R.id.nav_logout) {
                confirmLogout();
                return false;
            }
            return false;
        });
    }

    private void onConfirm() {
        String ten = edtTen.getText().toString().trim();
        String sdt = edtSdt.getText().toString().trim();
        String diachi = edtDiaChi.getText().toString().trim();

        if (TextUtils.isEmpty(ten)) {
            edtTen.setError("Vui lòng nhập tên");
            edtTen.requestFocus();
            return;
        }

        BanDoc b = new BanDoc();
        b.ten = ten;
        b.sdt = sdt;
        b.diachi = diachi;
        long id = banDocDao.insert(b);
        if (id > 0) {
            Toast.makeText(this, "Đã thêm bạn đọc", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
        }
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
