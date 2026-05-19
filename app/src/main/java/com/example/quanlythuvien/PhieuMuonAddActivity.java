package com.example.quanlythuvien;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.SachPickAdapter;
import com.example.quanlythuvien.adapter.SachSelectedAdapter;
import com.example.quanlythuvien.db.BanDocDao;
import com.example.quanlythuvien.db.PhieuMuonDao;
import com.example.quanlythuvien.db.SachDao;
import com.example.quanlythuvien.model.BanDoc;
import com.example.quanlythuvien.model.ChiTietMuon;
import com.example.quanlythuvien.model.PhieuMuon;
import com.example.quanlythuvien.model.Sach;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhieuMuonAddActivity extends AppCompatActivity {

    private SachDao sachDao;
    private BanDocDao banDocDao;
    private PhieuMuonDao phieuMuonDao;

    private SachPickAdapter pickAdapter;
    private SachSelectedAdapter selectedAdapter;

    private TextView tvMaPhieu;
    private TextView tvBanDoc;
    private TextView tvHanTra;
    private TextView tvSachEmpty;
    private TextView lblSelected;

    private BanDoc selectedBanDoc;
    private Calendar selectedHanTra;

    private final java.util.LinkedHashMap<Integer, ChiTietMuon> selected = new java.util.LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phieumuon_add);

        sachDao = new SachDao(this);
        banDocDao = new BanDocDao(this);
        phieuMuonDao = new PhieuMuonDao(this);

        bindViews();
        setupRecyclers();
        setupActions();
        setupBottomNav();

        tvMaPhieu.setText("PM-" + phieuMuonDao.previewNextId());
        loadSachAvailable();
        renderSelected();
    }

    private void bindViews() {
        tvMaPhieu = findViewById(R.id.tvMaPhieu);
        tvBanDoc = findViewById(R.id.tvBanDoc);
        tvHanTra = findViewById(R.id.tvHanTra);
        tvSachEmpty = findViewById(R.id.tvSachEmpty);
        lblSelected = findViewById(R.id.lblSelected);
    }

    private void setupRecyclers() {
        RecyclerView rvPick = findViewById(R.id.rvSachPick);
        rvPick.setLayoutManager(new LinearLayoutManager(this));
        pickAdapter = new SachPickAdapter(this::onAddSach);
        rvPick.setAdapter(pickAdapter);

        RecyclerView rvSel = findViewById(R.id.rvSelected);
        rvSel.setLayoutManager(new LinearLayoutManager(this));
        selectedAdapter = new SachSelectedAdapter(SachSelectedAdapter.MODE_BORROW, this::onRemoveSach);
        rvSel.setAdapter(selectedAdapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.inputBanDoc).setOnClickListener(v -> showBanDocPicker());
        findViewById(R.id.inputHanTra).setOnClickListener(v -> showDatePicker());
        findViewById(R.id.btnConfirm).setOnClickListener(v -> onConfirm());
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

    private void loadSachAvailable() {
        List<Sach> all = sachDao.listAll();
        List<Sach> avail = new ArrayList<>();
        for (Sach s : all) {
            if (s.soluong > 0) avail.add(s);
        }
        pickAdapter.submit(avail);
        tvSachEmpty.setVisibility(avail.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showBanDocPicker() {
        List<BanDoc> list = banDocDao.listAll();
        if (list.isEmpty()) {
            Toast.makeText(this, "Chưa có bạn đọc", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[list.size()];
        for (int i = 0; i < list.size(); i++) names[i] = list.get(i).ten;

        new AlertDialog.Builder(this)
                .setTitle("Chọn bạn đọc")
                .setItems(names, (d, which) -> {
                    selectedBanDoc = list.get(which);
                    tvBanDoc.setText(selectedBanDoc.ten);
                    checkUnreturnedSlips(selectedBanDoc);
                })
                .show();
    }

    private void checkUnreturnedSlips(BanDoc banDoc) {
        List<PhieuMuon> chuaTra = phieuMuonDao.listChuaTraByBanDoc(banDoc.bdId);
        if (chuaTra.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append(banDoc.ten).append(" có ").append(chuaTra.size())
                .append(" phiếu mượn chưa trả:\n\n");
        for (PhieuMuon pm : chuaTra) {
            sb.append("• PM-").append(pm.pmId)
                    .append("  (mượn: ").append(pm.ngayMuon).append(")\n");
        }
        sb.append("\nBạn có muốn xem chi tiết?");

        new AlertDialog.Builder(this)
                .setTitle("Cảnh báo")
                .setMessage(sb.toString())
                .setPositiveButton("Xem phiếu mượn", (d, w) -> {
                    int firstPmId = chuaTra.get(0).pmId;
                    startActivity(PhieuMuonDetailActivity.newIntent(this, firstPmId));
                })
                .setNegativeButton("Tiếp tục lập phiếu", null)
                .show();
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        Calendar init = selectedHanTra != null ? selectedHanTra : now;
        new DatePickerDialog(this, (view, y, m, d) -> {
            selectedHanTra = Calendar.getInstance();
            selectedHanTra.set(y, m, d);
            tvHanTra.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(selectedHanTra.getTime()));
        }, init.get(Calendar.YEAR), init.get(Calendar.MONTH), init.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void onAddSach(Sach s) {
        ChiTietMuon ct = selected.get(s.sachId);
        if (ct == null) {
            ct = new ChiTietMuon(s.sachId, s.ten, 1);
            selected.put(s.sachId, ct);
        } else {
            ct.soluong++;
        }
        renderSelected();
    }

    private void onRemoveSach(ChiTietMuon ct) {
        selected.remove(ct.sachId);
        renderSelected();
    }

    private void renderSelected() {
        List<ChiTietMuon> list = new ArrayList<>(selected.values());
        selectedAdapter.submit(list);
        lblSelected.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void onConfirm() {
        if (selectedBanDoc == null) {
            Toast.makeText(this, "Vui lòng chọn bạn đọc", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedHanTra == null) {
            Toast.makeText(this, "Vui lòng chọn hạn trả", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selected.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 sách", Toast.LENGTH_SHORT).show();
            return;
        }

        PhieuMuon pm = new PhieuMuon();
        pm.bdId = selectedBanDoc.bdId;
        pm.ngayMuon = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        pm.ngayTra = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(selectedHanTra.getTime());

        long pmId = phieuMuonDao.insertWithDetails(pm,
                new ArrayList<>(selected.values()));
        if (pmId > 0) {
            Toast.makeText(this, "Đã lập phiếu mượn PM-" + pmId, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Lập phiếu thất bại", Toast.LENGTH_SHORT).show();
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
