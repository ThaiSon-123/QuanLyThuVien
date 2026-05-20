package com.example.quanlythuvien;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlythuvien.db.NhanVienDao;
import com.example.quanlythuvien.model.NhanVien;
import com.example.quanlythuvien.util.StaffRoleOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NhanVienAddActivity extends AppCompatActivity {

    public static final String EXTRA_NV_ID = "nv_id";

    public static Intent editIntent(Context ctx, int nvId) {
        Intent i = new Intent(ctx, NhanVienAddActivity.class);
        i.putExtra(EXTRA_NV_ID, nvId);
        return i;
    }

    private NhanVienDao nhanVienDao;

    private EditText edtTen;
    private EditText edtSdt;
    private EditText edtEmail;
    private Spinner spinnerChucVu;
    private EditText edtPassword;
    private EditText edtDiaChi;
    private TextView tvTitle;
    private TextView btnConfirm;
    private TextView lblPassword;

    private int nvId;
    private NhanVien current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nhanvien_add);

        nhanVienDao = new NhanVienDao(this);
        nvId = getIntent().getIntExtra(EXTRA_NV_ID, 0);

        bindViews();
        setupActions();
        setupBottomNav();
        setupMode();
    }

    private void bindViews() {
        edtTen = findViewById(R.id.edtTen);
        edtSdt = findViewById(R.id.edtSdt);
        edtEmail = findViewById(R.id.edtEmail);
        spinnerChucVu = findViewById(R.id.spinnerChucVu);
        edtPassword = findViewById(R.id.edtPassword);
        edtDiaChi = findViewById(R.id.edtDiaChi);
        tvTitle = findViewById(R.id.tvTitle);
        btnConfirm = findViewById(R.id.btnConfirm);
        lblPassword = findViewById(R.id.lblPassword);
        setupRoleSpinner();
    }

    private void setupRoleSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, StaffRoleOptions.labels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChucVu.setAdapter(adapter);
        spinnerChucVu.setSelection(StaffRoleOptions.indexOf(StaffRoleOptions.EMPLOYEE));
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> onConfirm());
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

    private void setupMode() {
        if (nvId > 0) {
            current = nhanVienDao.findById(nvId);
            if (current == null) {
                Toast.makeText(this, "Nhân viên không tồn tại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            tvTitle.setText("Sửa nhân viên");
            btnConfirm.setText("Cập nhật");

            edtTen.setText(current.ten);
            edtSdt.setText(current.sdt);
            edtEmail.setText(current.email);
            spinnerChucVu.setSelection(StaffRoleOptions.indexOf(current.chucvu));
            edtDiaChi.setText(current.diachi);

            lblPassword.setVisibility(View.GONE);
            edtPassword.setVisibility(View.GONE);
        } else {
            tvTitle.setText("Thêm nhân viên");
            btnConfirm.setText("Thêm nhân viên");
        }
    }

    private void onConfirm() {
        String ten = edtTen.getText().toString().trim();
        String sdt = edtSdt.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String chucvu = spinnerChucVu.getSelectedItem() == null
                ? StaffRoleOptions.EMPLOYEE
                : spinnerChucVu.getSelectedItem().toString();
        String password = edtPassword.getText().toString();
        String diachi = edtDiaChi.getText().toString().trim();

        if (TextUtils.isEmpty(ten)) {
            edtTen.setError("Vui lòng nhập họ tên");
            edtTen.requestFocus();
            return;
        }
        if (nvId > 0) {
            // Update
            current.ten = ten;
            current.sdt = sdt;
            current.email = email;
            current.chucvu = chucvu;
            current.diachi = diachi;
            int rows = nhanVienDao.update(current);
            if (rows > 0) {
                Toast.makeText(this, "Đã cập nhật", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Add
            if (TextUtils.isEmpty(email)) {
                edtEmail.setError("Vui lòng nhập email (làm tài khoản)");
                edtEmail.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password)) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                edtPassword.requestFocus();
                return;
            }

            NhanVien n = new NhanVien();
            n.ten = ten;
            n.sdt = sdt;
            n.email = email;
            n.diachi = diachi;
            n.chucvu = chucvu;
            n.ngayVaoLam = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());
            n.trangthai = "lamviec";

            long id = nhanVienDao.insertWithUser(n, email, password);
            if (id > 0) {
                Toast.makeText(this, "Đã thêm nhân viên", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Thêm thất bại (email có thể đã tồn tại)",
                        Toast.LENGTH_SHORT).show();
            }
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
