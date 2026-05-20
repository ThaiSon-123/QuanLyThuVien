package com.example.quanlythuvien;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.ChiTietMuonAdapter;
import com.example.quanlythuvien.db.CauHinhDao;
import com.example.quanlythuvien.db.PhieuMuonDao;
import com.example.quanlythuvien.db.PhieuTraDao;
import com.example.quanlythuvien.model.PhieuMuon;
import com.example.quanlythuvien.util.FineCalculator;
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
    private CauHinhDao cauHinhDao;
    private ChiTietMuonAdapter ctAdapter;

    private TextView tvMaPhieu;
    private TextView tvNgayTao;
    private TextView tvBanDoc;
    private TextView tvHanTra;
    private TextView tvBannerLabel;
    private LinearLayout banner;
    private TextView btnTraPhieu;
    private TextView btnGiaHan;
    private TextView tvGiaHanInfo;

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
        cauHinhDao = CauHinhDao.getInstance(this);

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
        tvBannerLabel = findViewById(R.id.tvBannerLabel);
        banner = findViewById(R.id.banner);
        btnTraPhieu = findViewById(R.id.btnTraPhieu);
        btnGiaHan = findViewById(R.id.btnGiaHan);
        tvGiaHanInfo = findViewById(R.id.tvGiaHanInfo);
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
        btnGiaHan.setOnClickListener(v -> onGiaHanClick());
    }

    private void onGiaHanClick() {
        PhieuMuon pm = phieuMuonDao.findById(pmId);
        if (pm == null) return;
        int maxGiaHan = cauHinhDao.maxGiaHan();
        int giaHanDays = cauHinhDao.giaHanDays();
        new AlertDialog.Builder(this)
                .setTitle("Gia hạn phiếu mượn")
                .setMessage("Hạn trả hiện tại: " + MuonTraActivity.formatDate(pm.ngayTra)
                        + "\nSau gia hạn: +" + giaHanDays + " ngày"
                        + "\n\nĐã gia hạn: " + pm.lanGiaHan + "/" + maxGiaHan + " lần"
                        + "\n\nXác nhận gia hạn?")
                .setPositiveButton("Gia hạn", (d, w) -> doGiaHan())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doGiaHan() {
        int rc = phieuMuonDao.giaHan(pmId);
        int maxGiaHan = cauHinhDao.maxGiaHan();
        int giaHanDays = cauHinhDao.giaHanDays();
        String msg;
        switch (rc) {
            case PhieuMuonDao.GIAHAN_OK:
                msg = "Đã gia hạn thêm " + giaHanDays + " ngày";
                break;
            case PhieuMuonDao.GIAHAN_ERR_DATRA:
                msg = "Phiếu đã trả, không thể gia hạn";
                break;
            case PhieuMuonDao.GIAHAN_ERR_QUAHAN:
                msg = "Phiếu đã quá hạn, không thể gia hạn";
                break;
            case PhieuMuonDao.GIAHAN_ERR_MAX_REACHED:
                msg = "Đã gia hạn tối đa " + maxGiaHan + " lần";
                break;
            default:
                msg = "Gia hạn thất bại";
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        loadData();
    }

    private void onTraPhieuClick() {
        if (linkedPtId > 0) {
            // Đã trả — mở chi tiết phiếu trả
            startActivity(PhieuTraDetailActivity.newIntent(this, linkedPtId));
        } else {
            // Chưa trả — mở màn lập phiếu trả với pmId được pre-fill
            startActivity(PhieuTraAddActivity.newIntent(this, pmId));
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
        int maxGiaHan = cauHinhDao.maxGiaHan();


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

        // Banner: nếu phiếu CHƯA trả và đã quá hạn → đỏ "QUÁ HẠN N NGÀY"
        int daysOverdue = (linkedPtId == 0)
                ? FineCalculator.daysOverdue(pm.ngayTra, FineCalculator.today())
                : 0;
        if (daysOverdue > 0) {
            banner.setBackgroundResource(R.drawable.bg_hantra_banner_red);
            tvBannerLabel.setText("QUÁ HẠN " + daysOverdue + " NGÀY");
        } else {
            banner.setBackgroundResource(R.drawable.bg_hantra_banner);
            tvBannerLabel.setText("HẠN TRẢ SÁCH");
        }

        // Gia hạn: chỉ hiện cho phiếu chưa trả
        if (linkedPtId == 0) {
            // Hiện thông tin số lần gia hạn nếu đã từng gia hạn
            if (pm.lanGiaHan > 0) {
                tvGiaHanInfo.setVisibility(android.view.View.VISIBLE);
                tvGiaHanInfo.setText("Đã gia hạn " + pm.lanGiaHan + "/" + maxGiaHan
                        + " lần · Hạn gốc: " + MuonTraActivity.formatDate(pm.ngayTraGoc));
            } else {
                tvGiaHanInfo.setVisibility(android.view.View.GONE);
            }

            // Nút gia hạn: ẩn nếu đã quá hạn HOẶC đã max
            boolean canGiaHan = daysOverdue == 0 && pm.lanGiaHan < maxGiaHan;
            btnGiaHan.setVisibility(canGiaHan ? android.view.View.VISIBLE : android.view.View.GONE);
        } else {
            tvGiaHanInfo.setVisibility(android.view.View.GONE);
            btnGiaHan.setVisibility(android.view.View.GONE);
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
