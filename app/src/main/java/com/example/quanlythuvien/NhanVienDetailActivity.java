package com.example.quanlythuvien;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlythuvien.db.NhanVienDao;
import com.example.quanlythuvien.model.NhanVien;
import com.example.quanlythuvien.util.ConfirmDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NhanVienDetailActivity extends AppCompatActivity {

    public static final String EXTRA_NV_ID = "nv_id";

    public static Intent newIntent(Context ctx, int nvId) {
        Intent i = new Intent(ctx, NhanVienDetailActivity.class);
        i.putExtra(EXTRA_NV_ID, nvId);
        return i;
    }

    private NhanVienDao nhanVienDao;

    private TextView tvTen;
    private TextView tvMaNv;
    private TextView tvNgayVaoLam;
    private TextView tvChucVu;
    private TextView tvSdt;
    private TextView tvDiaChi;
    private TextView tvEmail;
    private TextView tvTrangThai;

    private int nvId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhanvien_detail);

        nvId = getIntent().getIntExtra(EXTRA_NV_ID, 0);
        if (nvId <= 0) {
            Toast.makeText(this, "Không tìm thấy nhân viên", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        nhanVienDao = new NhanVienDao(this);

        bindViews();
        setupActions();
        setupBottomNav();
    }

    private void bindViews() {
        tvTen = findViewById(R.id.tvTen);
        tvMaNv = findViewById(R.id.tvMaNv);
        tvNgayVaoLam = findViewById(R.id.tvNgayVaoLam);
        tvChucVu = findViewById(R.id.tvChucVu);
        tvSdt = findViewById(R.id.tvSdt);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        tvEmail = findViewById(R.id.tvEmail);
        tvTrangThai = findViewById(R.id.tvTrangThai);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEdit).setOnClickListener(v ->
                startActivity(NhanVienAddActivity.editIntent(this, nvId)));
        findViewById(R.id.btnDelete).setOnClickListener(v ->
                ConfirmDialog.showDelete(this, this::doDelete));
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

    private void doDelete() {
        int rows = nhanVienDao.delete(nvId);
        if (rows > 0) {
            Toast.makeText(this, "Đã xóa nhân viên", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadData() {
        NhanVien n = nhanVienDao.findById(nvId);
        if (n == null) {
            Toast.makeText(this, "Nhân viên không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvTen.setText(n.ten == null ? "" : n.ten);
        tvMaNv.setText(n.getMaNhanVien());
        tvNgayVaoLam.setText(formatDate(n.ngayVaoLam));
        tvChucVu.setText(TextUtils.isEmpty(n.chucvu) ? "Nhân viên" : n.chucvu);
        tvSdt.setText(TextUtils.isEmpty(n.sdt) ? "(chưa có)" : n.sdt);
        tvDiaChi.setText(TextUtils.isEmpty(n.diachi) ? "(chưa có)" : n.diachi);
        tvEmail.setText(TextUtils.isEmpty(n.email) ? "(chưa có)" : n.email);
        tvTrangThai.setText(n.getTrangthaiLabel());
    }

    private String formatDate(String iso) {
        if (TextUtils.isEmpty(iso)) return "(chưa có)";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(iso);
            if (d != null) {
                return new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(d);
            }
        } catch (Exception ignored) {
        }
        return iso;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nvId > 0) loadData();
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
