package com.example.quanlythuvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.PhieuMuonPickAdapter;
import com.example.quanlythuvien.adapter.SachSelectedAdapter;
import com.example.quanlythuvien.db.PhieuMuonDao;
import com.example.quanlythuvien.db.PhieuTraDao;
import com.example.quanlythuvien.model.ChiTietMuon;
import com.example.quanlythuvien.model.PhieuMuon;
import com.example.quanlythuvien.util.FineCalculator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhieuTraAddActivity extends AppCompatActivity {

    public static final String EXTRA_PM_ID = "pm_id";

    public static android.content.Intent newIntent(android.content.Context ctx, int pmId) {
        android.content.Intent i = new android.content.Intent(ctx, PhieuTraAddActivity.class);
        i.putExtra(EXTRA_PM_ID, pmId);
        return i;
    }

    private PhieuMuonDao phieuMuonDao;
    private PhieuTraDao phieuTraDao;

    private PhieuMuonPickAdapter pickAdapter;
    private SachSelectedAdapter selectedAdapter;

    private LinearLayout sectionPick;
    private LinearLayout sectionSelected;
    private TextView tvMaPhieuMuon;
    private TextView tvBanDoc;
    private TextView tvPickEmpty;

    private LinearLayout cardFinePreview;
    private TextView tvFineHan;
    private TextView tvFineNgayTra;
    private TextView tvFineDays;
    private TextView tvFineRate;
    private TextView tvFineTotal;
    private TextView btnConfirm;

    private PhieuMuon currentPm;
    private double currentFine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phieutra_add);

        phieuMuonDao = new PhieuMuonDao(this);
        phieuTraDao = new PhieuTraDao(this);

        bindViews();
        setupRecyclers();
        setupActions();
        setupBottomNav();

        int preselectPmId = getIntent().getIntExtra(EXTRA_PM_ID, 0);
        if (preselectPmId > 0) {
            // Auto-fill từ phiếu mượn được truyền vào
            PhieuMuon p = new PhieuMuon();
            p.pmId = preselectPmId;
            onPickPhieuMuon(p);
            // Vẫn load list để khi đổi phiếu thì có sẵn
            loadPhieuMuonChuaTra();
        } else {
            showPickState();
            loadPhieuMuonChuaTra();
        }
    }

    private void bindViews() {
        sectionPick = findViewById(R.id.sectionPick);
        sectionSelected = findViewById(R.id.sectionSelected);
        tvMaPhieuMuon = findViewById(R.id.tvMaPhieuMuon);
        tvBanDoc = findViewById(R.id.tvBanDoc);
        tvPickEmpty = findViewById(R.id.tvPickEmpty);
        cardFinePreview = findViewById(R.id.cardFinePreview);
        tvFineHan = findViewById(R.id.tvFineHan);
        tvFineNgayTra = findViewById(R.id.tvFineNgayTra);
        tvFineDays = findViewById(R.id.tvFineDays);
        tvFineRate = findViewById(R.id.tvFineRate);
        tvFineTotal = findViewById(R.id.tvFineTotal);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupRecyclers() {
        RecyclerView rvPick = findViewById(R.id.rvPhieuMuon);
        rvPick.setLayoutManager(new LinearLayoutManager(this));
        pickAdapter = new PhieuMuonPickAdapter(this::onPickPhieuMuon);
        rvPick.setAdapter(pickAdapter);

        RecyclerView rvSel = findViewById(R.id.rvSachMuon);
        rvSel.setLayoutManager(new LinearLayoutManager(this));
        selectedAdapter = new SachSelectedAdapter(SachSelectedAdapter.MODE_RETURN, null);
        rvSel.setAdapter(selectedAdapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnDoiPhieu).setOnClickListener(v -> {
            currentPm = null;
            tvMaPhieuMuon.setText("");
            tvBanDoc.setText("");
            updateFinePreview();
            showPickState();
            loadPhieuMuonChuaTra();
        });
        findViewById(R.id.btnConfirm).setOnClickListener(v -> onConfirm());

        tvMaPhieuMuon.setOnClickListener(v -> {
            if (currentPm == null) {
                Toast.makeText(this, "Chọn 1 phiếu mượn ở danh sách bên dưới",
                        Toast.LENGTH_SHORT).show();
                ScrollView sv = findViewById(R.id.scrollContent);
                sv.post(() -> sv.smoothScrollTo(0, sectionPick.getTop()));
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

    private void loadPhieuMuonChuaTra() {
        List<PhieuMuon> list = phieuMuonDao.listChuaTra();
        pickAdapter.submit(list);
        tvPickEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void onPickPhieuMuon(PhieuMuon p) {
        currentPm = phieuMuonDao.findById(p.pmId);
        if (currentPm == null) {
            Toast.makeText(this, "Không tìm thấy phiếu", Toast.LENGTH_SHORT).show();
            return;
        }
        tvMaPhieuMuon.setText(currentPm.getMaPhieu());
        tvBanDoc.setText(currentPm.tenBanDoc == null ? "" : currentPm.tenBanDoc);
        selectedAdapter.submit(currentPm.chiTiet);
        updateFinePreview();
        showSelectedState();
    }

    private void updateFinePreview() {
        if (currentPm == null) {
            cardFinePreview.setVisibility(View.GONE);
            btnConfirm.setText("Xác nhận trả sách");
            currentFine = 0;
            return;
        }
        String today = FineCalculator.today();
        int days = FineCalculator.daysOverdue(currentPm.ngayTra, today);
        if (days > 0) {
            double finePerDay = FineCalculator.finePerDay(this);
            currentFine = FineCalculator.calcFine(currentPm.ngayTra, today,
                    finePerDay, totalBorrowedBooks(currentPm.chiTiet));
            cardFinePreview.setVisibility(View.VISIBLE);
            tvFineHan.setText(MuonTraActivity.formatDate(currentPm.ngayTra));
            tvFineNgayTra.setText(MuonTraActivity.formatDate(today));
            tvFineDays.setText(days + " ngày");
            tvFineRate.setText(FineCalculator.formatVnd(finePerDay) + "/ngày/quyển");
            tvFineTotal.setText(FineCalculator.formatVnd(currentFine));
            btnConfirm.setText("Xác nhận trả sách (" + FineCalculator.formatVnd(currentFine) + ")");
        } else {
            currentFine = 0;
            cardFinePreview.setVisibility(View.GONE);
            btnConfirm.setText("Xác nhận trả sách");
        }
    }

    private int totalBorrowedBooks(List<ChiTietMuon> details) {
        int total = 0;
        if (details == null) return total;
        for (ChiTietMuon ct : details) {
            total += Math.max(0, ct.soluong);
        }
        return total;
    }

    private void showPickState() {
        sectionPick.setVisibility(View.VISIBLE);
        sectionSelected.setVisibility(View.GONE);
    }

    private void showSelectedState() {
        sectionPick.setVisibility(View.GONE);
        sectionSelected.setVisibility(View.VISIBLE);
    }

    private void onConfirm() {
        if (currentPm == null) {
            Toast.makeText(this, "Vui lòng chọn phiếu mượn", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentPm.chiTiet == null || currentPm.chiTiet.isEmpty()) {
            Toast.makeText(this, "Phiếu không có sách để trả", Toast.LENGTH_SHORT).show();
            return;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date());
        long ptId = phieuTraDao.insertWithDetails(currentPm.pmId, today,
                new java.util.ArrayList<ChiTietMuon>(currentPm.chiTiet));
        if (ptId > 0) {
            String msg = currentFine > 0
                    ? "Đã trả sách (PT-" + ptId + ") · Phạt " + FineCalculator.formatVnd(currentFine)
                    : "Đã trả sách (PT-" + ptId + ")";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Trả sách thất bại", Toast.LENGTH_SHORT).show();
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
