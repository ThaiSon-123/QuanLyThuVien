package com.example.quanlythuvien;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlythuvien.db.SachDao;
import com.example.quanlythuvien.db.TheLoaiDao;
import com.example.quanlythuvien.model.Sach;
import com.example.quanlythuvien.model.TheLoai;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class SachAddActivity extends AppCompatActivity {

    private static final int RED_STAR = 0xFFE04D4D;

    private EditText edtTen;
    private EditText edtTacGia;
    private EditText edtNxb;
    private EditText edtNamXb;
    private EditText edtSoLuong;
    private Spinner spinnerTheLoai;
    private ImageView ivCover;

    private SachDao sachDao;
    private TheLoaiDao theLoaiDao;
    private final List<TheLoai> theLoaiList = new ArrayList<>();

    private Uri selectedCoverUri;

    private final ActivityResultLauncher<String[]> imagePicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                    // Some providers don't support persistable grants — still usable this session.
                }
                selectedCoverUri = uri;
                ivCover.setPadding(0, 0, 0, 0);
                ivCover.setImageURI(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sach_add);

        sachDao = new SachDao(this);
        theLoaiDao = new TheLoaiDao(this);

        bindViews();
        setupLabels();
        loadTheLoai();
        setupActions();
        setupBottomNav();
    }

    private void bindViews() {
        edtTen = findViewById(R.id.edtTen);
        edtTacGia = findViewById(R.id.edtTacGia);
        edtNxb = findViewById(R.id.edtNxb);
        edtNamXb = findViewById(R.id.edtNamXb);
        edtSoLuong = findViewById(R.id.edtSoLuong);
        spinnerTheLoai = findViewById(R.id.spinnerTheLoai);
        ivCover = findViewById(R.id.ivCover);
    }

    private void setupLabels() {
        setLabelWithStar(R.id.lblTen, "Tên sách");
        setLabelWithStar(R.id.lblTheLoai, "Thể Loại");
        setLabelWithStar(R.id.lblTacGia, "Tác giả");
        setLabelWithStar(R.id.lblSoLuong, "Số Lượng");
        setLabelWithStar(R.id.lblNxb, "Nhà xuất bản");
        setLabelWithStar(R.id.lblNamXb, "Năm xuất bản");
    }

    private void setLabelWithStar(int id, String base) {
        TextView tv = findViewById(id);
        String full = base + " *";
        SpannableString sp = new SpannableString(full);
        sp.setSpan(new ForegroundColorSpan(RED_STAR),
                full.length() - 1, full.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(sp);
    }

    private void loadTheLoai() {
        theLoaiList.clear();
        theLoaiList.add(new TheLoai(0, "-- Chọn thể loại --"));
        theLoaiList.addAll(theLoaiDao.listAll());

        ArrayAdapter<TheLoai> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, theLoaiList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheLoai.setAdapter(adapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnConfirm).setOnClickListener(v -> onConfirm());
        ivCover.setOnClickListener(v -> pickImage());
    }

    private void pickImage() {
        try {
            imagePicker.launch(new String[]{"image/*"});
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được trình chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottomNav);
        nav.setSelectedItemId(R.id.nav_book);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                goHome();
                return false;
            } else if (id == R.id.nav_book) {
                finish();
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

    private void onConfirm() {
        String ten = edtTen.getText().toString().trim();
        String tacgia = edtTacGia.getText().toString().trim();
        String nxb = edtNxb.getText().toString().trim();
        String namXbStr = edtNamXb.getText().toString().trim();
        String soLuongStr = edtSoLuong.getText().toString().trim();

        if (TextUtils.isEmpty(ten)) {
            edtTen.setError("Vui lòng nhập tên sách");
            edtTen.requestFocus();
            return;
        }

        int tlId = 0;
        int pos = spinnerTheLoai.getSelectedItemPosition();
        if (pos >= 0 && pos < theLoaiList.size()) {
            tlId = theLoaiList.get(pos).tlId;
        }
        if (tlId <= 0) {
            Toast.makeText(this, "Vui lòng chọn thể loại", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(tacgia)) {
            edtTacGia.setError("Vui lòng nhập tác giả");
            edtTacGia.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(soLuongStr)) {
            edtSoLuong.setError("Vui lòng nhập số lượng");
            edtSoLuong.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(nxb)) {
            edtNxb.setError("Vui lòng nhập nhà xuất bản");
            edtNxb.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(namXbStr)) {
            edtNamXb.setError("Vui lòng nhập năm xuất bản");
            edtNamXb.requestFocus();
            return;
        }

        int soluong;
        int namxb;
        try {
            soluong = Integer.parseInt(soLuongStr);
            namxb = Integer.parseInt(namXbStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }
        if (soluong < 0) {
            edtSoLuong.setError("Số lượng phải >= 0");
            return;
        }

        Sach s = new Sach();
        s.ten = ten;
        s.tacgia = tacgia;
        s.nxb = nxb;
        s.namxb = namxb;
        s.soluong = soluong;
        s.tlId = tlId;
        s.coverUri = selectedCoverUri != null ? selectedCoverUri.toString() : null;

        long id = sachDao.insert(s);
        if (id > 0) {
            Toast.makeText(this, "Đã thêm sách", Toast.LENGTH_SHORT).show();
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
