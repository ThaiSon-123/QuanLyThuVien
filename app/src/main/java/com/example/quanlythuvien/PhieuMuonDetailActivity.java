package com.example.quanlythuvien;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.ChiTietMuonAdapter;
import com.example.quanlythuvien.db.PhieuMuonDao;
import com.example.quanlythuvien.db.PhieuTraDao;
import com.example.quanlythuvien.model.PhieuMuon;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PhieuMuonDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PM_ID = "pm_id";

    public static Intent newIntent(Context ctx, int pmId) {
        Intent i = new Intent(ctx, PhieuMuonDetailActivity.class);
        i.putExtra(EXTRA_PM_ID, pmId);
        return i;
    }

    private PhieuMuonDao phieuMuonDao;
    private PhieuTraDao phieuTraDao;
    private ChiTietMuonAdapter ctAdapter;

    private TextView tvMaPhieu;
    private TextView tvNgayTao;
    private TextView tvBanDoc;
    private TextView tvHanTra;
    private TextView btnTraPhieu;

    private int pmId;
    private int linkedPtId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phieumuon_detail);

        pmId = getIntent().getIntExtra(EXTRA_PM_ID, 0);
        if (pmId <= 0) {
            Toast.makeText(this, "Không tìm thấy phiếu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        phieuMuonDao = new PhieuMuonDao(this);
        phieuTraDao = new PhieuTraDao(this);

        bindViews();
        setupRecycler();
        setupActions();
        setupBottomNav();
        loadData();
    }

    private void bindViews() {
        tvMaPhieu = findViewById(R.id.tvMaPhieu);
        tvNgayTao = findViewById(R.id.tvNgayTao);
        tvBanDoc = findViewById(R.id.tvBanDoc);
        tvHanTra = findViewById(R.id.tvHanTra);
        btnTraPhieu = findViewById(R.id.btnTraPhieu);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvChiTiet);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ctAdapter = new ChiTietMuonAdapter();
        rv.setAdapter(ctAdapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnTraPhieu.setOnClickListener(v -> onTraPhieuClick());
    }

    private void onTraPhieuClick() {
        if (linkedPtId > 0) {
            // Đã trả — mở chi tiết phiếu trả
            startActivity(PhieuTraDetailActivity.newIntent(this, linkedPtId));
        } else {
            // Chưa trả — mở màn lập phiếu trả
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
                finish();
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
        PhieuMuon pm = phieuMuonDao.findById(pmId);
        if (pm == null) {
            Toast.makeText(this, "Phiếu không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvMaPhieu.setText(pm.getMaPhieu());
        tvNgayTao.setText("Ngày tạo: " + MuonTraActivity.formatDate(pm.ngayMuon));
        tvBanDoc.setText(pm.tenBanDoc == null ? "(Chưa có)" : pm.tenBanDoc);
        tvHanTra.setText(MuonTraActivity.formatDate(pm.ngayTra));
        ctAdapter.submit(pm.chiTiet);

        // Nút Lập phiếu trả / Xem phiếu trả
        linkedPtId = phieuTraDao.findIdByPmId(pmId);
        if (linkedPtId > 0) {
            btnTraPhieu.setText("Xem phiếu trả");
            btnTraPhieu.setTextColor(0xFF4696EB);
            btnTraPhieu.setBackgroundResource(R.drawable.bg_btn_outlined);
        } else {
            btnTraPhieu.setText("+ Lập phiếu trả");
            btnTraPhieu.setTextColor(0xFFFFFFFF);
            btnTraPhieu.setBackgroundResource(R.drawable.bg_btn_primary);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pmId > 0) loadData();
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
