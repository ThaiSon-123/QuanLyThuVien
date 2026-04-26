package com.example.quanlythuvien;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.LichSuMuonAdapter;
import com.example.quanlythuvien.db.BanDocDao;
import com.example.quanlythuvien.db.PhieuMuonDao;
import com.example.quanlythuvien.model.BanDoc;
import com.example.quanlythuvien.model.PhieuMuon;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class BanDocDetailActivity extends AppCompatActivity {

    public static final String EXTRA_BD_ID = "bd_id";

    public static Intent newIntent(Context ctx, int bdId) {
        Intent i = new Intent(ctx, BanDocDetailActivity.class);
        i.putExtra(EXTRA_BD_ID, bdId);
        return i;
    }

    private BanDocDao banDocDao;
    private PhieuMuonDao phieuMuonDao;
    private LichSuMuonAdapter adapter;

    private TextView tvTen;
    private TextView tvMaBd;
    private TextView tvSachGiu;
    private TextView tvLanMuon;
    private TextView tvSdt;
    private TextView tvDiaChi;
    private TextView tvLichSuEmpty;

    private int bdId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bandoc_detail);

        bdId = getIntent().getIntExtra(EXTRA_BD_ID, 0);
        if (bdId <= 0) {
            Toast.makeText(this, "Không tìm thấy bạn đọc", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        banDocDao = new BanDocDao(this);
        phieuMuonDao = new PhieuMuonDao(this);

        bindViews();
        setupRecycler();
        setupActions();
        setupBottomNav();
    }

    private void bindViews() {
        tvTen = findViewById(R.id.tvTen);
        tvMaBd = findViewById(R.id.tvMaBd);
        tvSachGiu = findViewById(R.id.tvSachGiu);
        tvLanMuon = findViewById(R.id.tvLanMuon);
        tvSdt = findViewById(R.id.tvSdt);
        tvDiaChi = findViewById(R.id.tvDiaChi);
        tvLichSuEmpty = findViewById(R.id.tvLichSuEmpty);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvLichSu);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LichSuMuonAdapter(this::onPhieuClick);
        rv.setAdapter(adapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
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

    private void onPhieuClick(PhieuMuon p) {
        startActivity(PhieuMuonDetailActivity.newIntent(this, p.pmId));
    }

    private void loadData() {
        BanDoc b = banDocDao.findById(bdId);
        if (b == null) {
            Toast.makeText(this, "Bạn đọc không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvTen.setText(b.ten == null ? "" : b.ten);
        tvMaBd.setText(b.getMaBanDoc());
        tvSdt.setText(TextUtils.isEmpty(b.sdt) ? "(chưa có)" : b.sdt);
        tvDiaChi.setText(TextUtils.isEmpty(b.diachi) ? "(chưa có)" : b.diachi);

        tvSachGiu.setText(String.valueOf(banDocDao.countSachGiu(bdId)));
        tvLanMuon.setText(String.valueOf(banDocDao.countLanMuon(bdId)));

        List<PhieuMuon> ls = phieuMuonDao.listByBanDoc(bdId);
        adapter.submit(ls);
        tvLichSuEmpty.setVisibility(ls.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bdId > 0) loadData();
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
