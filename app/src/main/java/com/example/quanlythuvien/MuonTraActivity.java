package com.example.quanlythuvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.PhieuAdapter;
import com.example.quanlythuvien.db.PhieuMuonDao;
import com.example.quanlythuvien.db.PhieuTraDao;
import com.example.quanlythuvien.model.PhieuMuon;
import com.example.quanlythuvien.model.PhieuTra;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MuonTraActivity extends AppCompatActivity {

    private static final int TAB_MUON = 0;
    private static final int TAB_TRA = 1;

    private PhieuMuonDao phieuMuonDao;
    private PhieuTraDao phieuTraDao;
    private PhieuAdapter adapter;

    private TextView tvCount;
    private TextView tvEmpty;
    private TextView tvDate;
    private TextView tvFabLabel;
    private TextView tvTabMuon;
    private TextView tvTabTra;
    private View indicatorMuon;
    private View indicatorTra;

    private int currentTab = TAB_MUON;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muontra);

        phieuMuonDao = new PhieuMuonDao(this);
        phieuTraDao = new PhieuTraDao(this);

        bindViews();
        setupRecycler();
        setupHeader();
        setupTabs();
        setupFab();
        setupBottomNav();

        // Hôm nay — chỉ hiển thị
        tvDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                .format(new Date()));

        selectTab(TAB_MUON);
    }

    private void bindViews() {
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvDate = findViewById(R.id.tvDate);
        tvFabLabel = findViewById(R.id.tvFabLabel);
        tvTabMuon = findViewById(R.id.tvTabMuon);
        tvTabTra = findViewById(R.id.tvTabTra);
        indicatorMuon = findViewById(R.id.indicatorMuon);
        indicatorTra = findViewById(R.id.indicatorTra);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvPhieu);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PhieuAdapter(this::onRowClick);
        rv.setAdapter(adapter);
    }

    private void setupHeader() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        ImageView btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(v ->
                Toast.makeText(this, "Tìm kiếm đang phát triển", Toast.LENGTH_SHORT).show());
    }

    private void setupTabs() {
        LinearLayout tabMuon = findViewById(R.id.tabMuon);
        LinearLayout tabTra = findViewById(R.id.tabTra);
        tabMuon.setOnClickListener(v -> selectTab(TAB_MUON));
        tabTra.setOnClickListener(v -> selectTab(TAB_TRA));
    }

    private void setupFab() {
        findViewById(R.id.fabAdd).setOnClickListener(v -> onAddClick());
    }

    private void onAddClick() {
        if (currentTab == TAB_MUON) {
            startActivity(new Intent(this, PhieuMuonAddActivity.class));
        } else {
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
                return true;
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

    private void selectTab(int tab) {
        currentTab = tab;
        boolean muon = tab == TAB_MUON;

        tvTabMuon.setTextColor(muon ? 0xFF4696EB : 0xFF747474);
        tvTabMuon.setTypeface(null, muon ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        tvTabTra.setTextColor(muon ? 0xFF747474 : 0xFF4696EB);
        tvTabTra.setTypeface(null, muon ? android.graphics.Typeface.NORMAL : android.graphics.Typeface.BOLD);
        indicatorMuon.setBackgroundResource(muon ? R.drawable.bg_tab_indicator : android.R.color.transparent);
        indicatorTra.setBackgroundResource(muon ? android.R.color.transparent : R.drawable.bg_tab_indicator);

        tvFabLabel.setText(muon ? "Lập phiếu mượn" : "Lập phiếu trả");

        loadData();
    }

    private void loadData() {
        List<PhieuAdapter.Row> rows = new ArrayList<>();
        if (currentTab == TAB_MUON) {
            List<PhieuMuon> list = phieuMuonDao.listAll();
            for (PhieuMuon p : list) {
                rows.add(new PhieuAdapter.Row(
                        p.pmId,
                        p.getMaPhieu(),
                        p.tenBanDoc,
                        formatDate(p.ngayMuon),
                        null,
                        p.trangthai));
            }
            tvCount.setText(list.size() + " Phiếu mượn");
        } else {
            List<PhieuTra> list = phieuTraDao.listAll();
            for (PhieuTra p : list) {
                rows.add(new PhieuAdapter.Row(
                        p.ptId,
                        p.getMaPhieu(),
                        p.tenBanDoc,
                        formatDate(p.ngayTra),
                        "PM-" + p.pmId));
            }
            tvCount.setText(list.size() + " Phiếu trả");
        }
        adapter.submit(rows);
        tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText(currentTab == TAB_MUON ? "Chưa có phiếu mượn" : "Chưa có phiếu trả");
    }

    private void onRowClick(PhieuAdapter.Row row) {
        if (currentTab == TAB_MUON) {
            startActivity(PhieuMuonDetailActivity.newIntent(this, row.id));
        } else {
            startActivity(PhieuTraDetailActivity.newIntent(this, row.id));
        }
    }

    /** yyyy-MM-dd → dd/MM/yyyy (robust với null hoặc format khác). */
    public static String formatDate(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(iso);
            if (d != null) {
                return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d);
            }
        } catch (Exception ignored) {
        }
        return iso;
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

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
