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
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhieuTraAddActivity extends AppCompatActivity {

    private PhieuMuonDao phieuMuonDao;
    private PhieuTraDao phieuTraDao;

    private PhieuMuonPickAdapter pickAdapter;
    private SachSelectedAdapter selectedAdapter;

    private LinearLayout sectionPick;
    private LinearLayout sectionSelected;
    private TextView tvMaPhieuMuon;
    private TextView tvBanDoc;
    private TextView tvPickEmpty;

    private PhieuMuon currentPm;

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

        showPickState();
        loadPhieuMuonChuaTra();
    }

    private void bindViews() {
        sectionPick = findViewById(R.id.sectionPick);
        sectionSelected = findViewById(R.id.sectionSelected);
        tvMaPhieuMuon = findViewById(R.id.tvMaPhieuMuon);
        tvBanDoc = findViewById(R.id.tvBanDoc);
        tvPickEmpty = findViewById(R.id.tvPickEmpty);
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
        showSelectedState();
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
            Toast.makeText(this, "Đã trả sách (PT-" + ptId + ")", Toast.LENGTH_SHORT).show();
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
