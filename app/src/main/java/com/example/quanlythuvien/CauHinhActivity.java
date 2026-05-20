package com.example.quanlythuvien;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlythuvien.db.CauHinhDao;
import com.example.quanlythuvien.db.PhieuTraDao;

/** Cấu hình hệ thống — chỉ dành cho admin. */
public class CauHinhActivity extends AppCompatActivity {

    private CauHinhDao cauHinhDao;

    private EditText edtFinePerDay, edtBorrowDays, edtMaxBooks,
            edtMaxGiaHan, edtGiaHanDays, edtLowStock, edtNearDue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String role = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(LoginActivity.KEY_ROLE, "");
        if (!"admin".equals(role)) {
            Toast.makeText(this, "Bạn không có quyền truy cập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_cauhinh);
        cauHinhDao = CauHinhDao.getInstance(this);

        edtFinePerDay = findViewById(R.id.edtFinePerDay);
        edtBorrowDays = findViewById(R.id.edtBorrowDays);
        edtMaxBooks   = findViewById(R.id.edtMaxBooks);
        edtMaxGiaHan  = findViewById(R.id.edtMaxGiaHan);
        edtGiaHanDays = findViewById(R.id.edtGiaHanDays);
        edtLowStock   = findViewById(R.id.edtLowStock);
        edtNearDue    = findViewById(R.id.edtNearDue);

        loadCurrent();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> save());
        findViewById(R.id.btnReset).setOnClickListener(v -> confirmReset());
    }

    private void loadCurrent() {
        edtFinePerDay.setText(String.valueOf((long) cauHinhDao.finePerDay()));
        edtBorrowDays.setText(String.valueOf(cauHinhDao.defaultBorrowDays()));
        edtMaxBooks.setText(String.valueOf(cauHinhDao.maxBooksPerSlip()));
        edtMaxGiaHan.setText(String.valueOf(cauHinhDao.maxGiaHan()));
        edtGiaHanDays.setText(String.valueOf(cauHinhDao.giaHanDays()));
        edtLowStock.setText(String.valueOf(cauHinhDao.lowStockThreshold()));
        edtNearDue.setText(String.valueOf(cauHinhDao.nearDueDays()));
    }

    private void save() {
        Integer fine     = parseIntField(edtFinePerDay, "Đơn giá phạt", 0, 1_000_000);
        Integer borrow   = parseIntField(edtBorrowDays, "Số ngày mượn", 1, 365);
        Integer maxBooks = parseIntField(edtMaxBooks, "Hạn mức sách", 1, 100);
        Integer maxGiaH  = parseIntField(edtMaxGiaHan, "Số lần gia hạn", 0, 10);
        Integer giaHanD  = parseIntField(edtGiaHanDays, "Số ngày gia hạn", 1, 365);
        Integer lowStock = parseIntField(edtLowStock, "Cảnh báo hết kho", 0, 1000);
        Integer nearDue  = parseIntField(edtNearDue, "Cảnh báo sắp hạn", 0, 365);
        if (fine == null || borrow == null || maxBooks == null
                || maxGiaH == null || giaHanD == null || lowStock == null || nearDue == null) {
            return;
        }

        cauHinhDao.setInt(CauHinhDao.KEY_FINE_PER_DAY, fine);
        cauHinhDao.setInt(CauHinhDao.KEY_DEFAULT_BORROW_DAYS, borrow);
        cauHinhDao.setInt(CauHinhDao.KEY_MAX_BOOKS_PER_SLIP, maxBooks);
        cauHinhDao.setInt(CauHinhDao.KEY_MAX_GIA_HAN, maxGiaH);
        cauHinhDao.setInt(CauHinhDao.KEY_GIA_HAN_DAYS, giaHanD);
        cauHinhDao.setInt(CauHinhDao.KEY_LOW_STOCK_THRESHOLD, lowStock);
        cauHinhDao.setInt(CauHinhDao.KEY_NEAR_DUE_DAYS, nearDue);
        new PhieuTraDao(this).recalculateAllFines(fine);

        Toast.makeText(this, "Đã lưu cấu hình", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("Khôi phục mặc định")
                .setMessage("Đặt lại tất cả về giá trị mặc định?")
                .setPositiveButton("Khôi phục", (d, w) -> doReset())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void doReset() {
        cauHinhDao.setInt(CauHinhDao.KEY_FINE_PER_DAY, 500);
        cauHinhDao.setInt(CauHinhDao.KEY_DEFAULT_BORROW_DAYS, 7);
        cauHinhDao.setInt(CauHinhDao.KEY_MAX_BOOKS_PER_SLIP, 5);
        cauHinhDao.setInt(CauHinhDao.KEY_MAX_GIA_HAN, 2);
        cauHinhDao.setInt(CauHinhDao.KEY_GIA_HAN_DAYS, 7);
        cauHinhDao.setInt(CauHinhDao.KEY_LOW_STOCK_THRESHOLD, 2);
        cauHinhDao.setInt(CauHinhDao.KEY_NEAR_DUE_DAYS, 3);
        new PhieuTraDao(this).recalculateAllFines(500);
        loadCurrent();
        Toast.makeText(this, "Đã khôi phục mặc định", Toast.LENGTH_SHORT).show();
    }

    private Integer parseIntField(EditText edt, String label, int min, int max) {
        String s = edt.getText().toString().trim();
        if (TextUtils.isEmpty(s)) {
            Toast.makeText(this, label + " không được để trống", Toast.LENGTH_SHORT).show();
            return null;
        }
        try {
            int v = Integer.parseInt(s);
            if (v < min || v > max) {
                Toast.makeText(this, label + " phải từ " + min + " đến " + max,
                        Toast.LENGTH_SHORT).show();
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            Toast.makeText(this, label + " phải là số nguyên", Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}
