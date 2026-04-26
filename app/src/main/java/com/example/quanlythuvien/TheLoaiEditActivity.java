package com.example.quanlythuvien;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlythuvien.db.TheLoaiDao;
import com.example.quanlythuvien.model.TheLoai;

public class TheLoaiEditActivity extends AppCompatActivity {

    public static final String EXTRA_TL_ID = "tl_id";
    private static final int RED_STAR = 0xFFE04D4D;

    public static Intent newIntent(Context ctx, int tlId) {
        Intent i = new Intent(ctx, TheLoaiEditActivity.class);
        i.putExtra(EXTRA_TL_ID, tlId);
        return i;
    }

    private TheLoaiDao theLoaiDao;

    private int tlId;               // 0 => add mode
    private TheLoai current;
    private Uri selectedIconUri;
    private boolean iconChanged;

    private TextView tvTitle;
    private ImageView ivIcon;
    private EditText edtTen;
    private TextView lblTen;
    private TextView tvWarning;
    private Button btnDelete;
    private Button btnSubmit;

    private final ActivityResultLauncher<String[]> imagePicker =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    getContentResolver().takePersistableUriPermission(uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {
                }
                selectedIconUri = uri;
                iconChanged = true;
                ivIcon.setPadding(0, 0, 0, 0);
                ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivIcon.setImageURI(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theloai_edit);

        theLoaiDao = new TheLoaiDao(this);
        tlId = getIntent().getIntExtra(EXTRA_TL_ID, 0);

        bindViews();
        setupLabels();
        setupActions();
        setupMode();
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.tvTitle);
        ivIcon = findViewById(R.id.ivIcon);
        edtTen = findViewById(R.id.edtTen);
        lblTen = findViewById(R.id.lblTen);
        tvWarning = findViewById(R.id.tvWarning);
        btnDelete = findViewById(R.id.btnDelete);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupLabels() {
        String base = "Tên thể loại";
        String full = base + " *";
        SpannableString sp = new SpannableString(full);
        sp.setSpan(new ForegroundColorSpan(RED_STAR),
                full.length() - 1, full.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        lblTen.setText(sp);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        FrameLayout iconFrame = findViewById(R.id.iconFrame);
        iconFrame.setOnClickListener(v -> pickImage());
        ivIcon.setOnClickListener(v -> pickImage());

        btnSubmit.setOnClickListener(v -> onSubmit());
        btnDelete.setOnClickListener(v -> onDelete());
    }

    private void setupMode() {
        if (tlId > 0) {
            current = theLoaiDao.findById(tlId);
            if (current == null) {
                Toast.makeText(this, "Không tìm thấy thể loại", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            // Edit mode
            tvTitle.setText("Chỉnh sửa thể loại");
            btnSubmit.setText("Cập nhật");
            btnDelete.setVisibility(View.VISIBLE);
            tvWarning.setVisibility(View.VISIBLE);

            edtTen.setText(current.ten);
            edtTen.setSelection(current.ten == null ? 0 : current.ten.length());
            if (!TextUtils.isEmpty(current.iconUri)) {
                try {
                    ivIcon.setPadding(0, 0, 0, 0);
                    ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    ivIcon.setImageURI(Uri.parse(current.iconUri));
                } catch (Exception ignored) {
                }
            }
        } else {
            // Add mode
            tvTitle.setText("Thêm thể loại");
            btnSubmit.setText("Xác nhận");
            btnDelete.setVisibility(View.GONE);
            tvWarning.setVisibility(View.GONE);
        }
    }

    private void pickImage() {
        try {
            imagePicker.launch(new String[]{"image/*"});
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được trình chọn ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSubmit() {
        String ten = edtTen.getText().toString().trim();
        if (TextUtils.isEmpty(ten)) {
            edtTen.setError("Vui lòng nhập tên thể loại");
            edtTen.requestFocus();
            return;
        }

        if (tlId > 0) {
            current.ten = ten;
            if (iconChanged) {
                current.iconUri = selectedIconUri != null ? selectedIconUri.toString() : null;
            }
            int rows = theLoaiDao.update(current);
            if (rows > 0) {
                Toast.makeText(this, "Đã cập nhật thể loại", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
            }
        } else {
            TheLoai t = new TheLoai();
            t.ten = ten;
            t.iconUri = selectedIconUri != null ? selectedIconUri.toString() : null;
            long id = theLoaiDao.insert(t);
            if (id > 0) {
                Toast.makeText(this, "Đã thêm thể loại", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Thêm thất bại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onDelete() {
        if (tlId <= 0) return;
        int bookCount = theLoaiDao.countBooks(tlId);
        String msg = bookCount > 0
                ? "Thể loại này có " + bookCount + " cuốn sách. Sau khi xóa, các sách sẽ không còn thuộc thể loại nào. Tiếp tục?"
                : "Bạn có chắc chắn muốn xóa không?";
        com.example.quanlythuvien.util.ConfirmDialog.show(this, msg, "Xóa", this::doDelete);
    }

    private void doDelete() {
        int rows = theLoaiDao.delete(tlId);
        if (rows > 0) {
            Toast.makeText(this, "Đã xóa thể loại", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            // Close parent detail screen too by returning to the list
            Intent i = new Intent(this, SachActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(i);
            finish();
        } else {
            Toast.makeText(this, "Xóa thất bại", Toast.LENGTH_SHORT).show();
        }
    }
}
