package com.example.quanlythuvien;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;

import com.example.quanlythuvien.adapter.ChiTietMuonAdapter;
import com.example.quanlythuvien.db.PhieuTraDao;
import com.example.quanlythuvien.model.PhieuTra;
import com.example.quanlythuvien.util.FineCalculator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PhieuTraDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PT_ID = "pt_id";

    public static Intent newIntent(Context ctx, int ptId) {
        Intent i = new Intent(ctx, PhieuTraDetailActivity.class);
        i.putExtra(EXTRA_PT_ID, ptId);
        return i;
    }

    private PhieuTraDao phieuTraDao;
    private ChiTietMuonAdapter ctAdapter;

    private TextView tvMaPhieu;
    private TextView tvNgayTao;
    private TextView tvHanTra;
    private TextView tvTinhTrang;
    private TextView tvBanDoc;
    private TextView tvMaPhieuMuon;
    private LinearLayout cardTienPhat;
    private TextView tvTienPhatLabel;
    private TextView tvTienPhat;

    private int ptId;
    private int linkedPmId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phieutra_detail);

        ptId = getIntent().getIntExtra(EXTRA_PT_ID, 0);
        if (ptId <= 0) {
            Toast.makeText(this, "Không tìm thấy phiếu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
        tvHanTra = findViewById(R.id.tvHanTra);
        tvTinhTrang = findViewById(R.id.tvTinhTrang);
        tvBanDoc = findViewById(R.id.tvBanDoc);
        tvMaPhieuMuon = findViewById(R.id.tvMaPhieuMuon);
        cardTienPhat = findViewById(R.id.cardTienPhat);
        tvTienPhatLabel = findViewById(R.id.tvTienPhatLabel);
        tvTienPhat = findViewById(R.id.tvTienPhat);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvChiTiet);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ctAdapter = new ChiTietMuonAdapter();
        rv.setAdapter(ctAdapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.bannerLink).setOnClickListener(v -> {
            if (linkedPmId > 0) {
                startActivity(PhieuMuonDetailActivity.newIntent(this, linkedPmId));
            }
        });
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
        PhieuTra pt = phieuTraDao.findById(ptId);
        if (pt == null) {
            Toast.makeText(this, "Phiếu không tồn tại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        linkedPmId = pt.pmId;
        tvMaPhieu.setText(pt.getMaPhieu());
        tvNgayTao.setText("Ngày trả: " + MuonTraActivity.formatDate(pt.ngayTra));
        tvBanDoc.setText(pt.tenBanDoc == null ? "(Chưa có)" : pt.tenBanDoc);
        tvMaPhieuMuon.setText("PM - " + pt.pmId);
        ctAdapter.submit(pt.chiTiet);

        tvHanTra.setText("Hạn trả: " + MuonTraActivity.formatDate(pt.ngayHanTra));
        boolean treHan = pt.ngayTra != null && pt.ngayHanTra != null
                && pt.ngayTra.compareTo(pt.ngayHanTra) > 0;
        tvTinhTrang.setVisibility(View.VISIBLE);
        tvTinhTrang.setText(treHan ? "Quá hạn" : "Đúng hạn");
        GradientDrawable pill = new GradientDrawable();
        pill.setShape(GradientDrawable.RECTANGLE);
        pill.setCornerRadius(24f);
        pill.setColor(treHan ? 0xFFBA1A1A : 0xFF2F8A3E);
        tvTinhTrang.setBackground(pill);
        tvTinhTrang.setTextColor(0xFFFFFFFF);

        int days = FineCalculator.daysOverdue(pt.ngayHanTra, pt.ngayTra);
        if (days > 0) {
            cardTienPhat.setVisibility(View.VISIBLE);
            tvTienPhatLabel.setText("TIỀN PHẠT (TRỄ " + days + " NGÀY)");
            double tien = pt.tienphat > 0
                    ? pt.tienphat
                    : FineCalculator.calcFine(this, pt.ngayHanTra, pt.ngayTra,
                            totalBorrowedBooks(pt.chiTiet));
            tvTienPhat.setText(FineCalculator.formatVnd(tien));
        } else {
            cardTienPhat.setVisibility(View.GONE);
        }
    }

    private int totalBorrowedBooks(java.util.List<com.example.quanlythuvien.model.ChiTietMuon> details) {
        int total = 0;
        if (details == null) return total;
        for (com.example.quanlythuvien.model.ChiTietMuon ct : details) {
            total += Math.max(0, ct.soluong);
        }
        return total;
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
