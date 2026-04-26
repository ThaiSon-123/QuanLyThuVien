package com.example.quanlythuvien;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.adapter.SachAdapter;
import com.example.quanlythuvien.db.SachDao;
import com.example.quanlythuvien.db.TheLoaiDao;
import com.example.quanlythuvien.model.Sach;
import com.example.quanlythuvien.model.TheLoai;

import java.util.List;

public class TheLoaiDetailActivity extends AppCompatActivity {

    public static final String EXTRA_TL_ID = "tl_id";

    public static Intent newIntent(Context ctx, int tlId) {
        Intent i = new Intent(ctx, TheLoaiDetailActivity.class);
        i.putExtra(EXTRA_TL_ID, tlId);
        return i;
    }

    private TheLoaiDao theLoaiDao;
    private SachDao sachDao;
    private SachAdapter sachAdapter;

    private int tlId;
    private TheLoai current;

    private ImageView ivIcon;
    private TextView tvTen;
    private TextView tvCount;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theloai_detail);

        tlId = getIntent().getIntExtra(EXTRA_TL_ID, 0);
        if (tlId <= 0) {
            Toast.makeText(this, "Không tìm thấy thể loại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        theLoaiDao = new TheLoaiDao(this);
        sachDao = new SachDao(this);

        bindViews();
        setupRecycler();
        setupActions();
    }

    private void bindViews() {
        ivIcon = findViewById(R.id.ivIcon);
        tvTen = findViewById(R.id.tvTen);
        tvCount = findViewById(R.id.tvCount);
        tvEmpty = findViewById(R.id.tvEmpty);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvSach);
        rv.setLayoutManager(new LinearLayoutManager(this));
        sachAdapter = new SachAdapter(this::onSachClick);
        rv.setAdapter(sachAdapter);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEdit).setOnClickListener(v -> onEditClick());
    }

    private void onEditClick() {
        startActivity(TheLoaiEditActivity.newIntent(this, tlId));
    }

    private void onSachClick(Sach s) {
        startActivity(SachDetailActivity.newIntent(this, s.sachId));
    }

    private void loadData() {
        current = theLoaiDao.findById(tlId);
        if (current == null) {
            Toast.makeText(this, "Thể loại đã bị xóa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvTen.setText(current.ten);
        tvCount.setText(current.bookCount + " cuốn sách");

        if (!TextUtils.isEmpty(current.iconUri)) {
            try {
                ivIcon.setPadding(0, 0, 0, 0);
                ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivIcon.setImageURI(Uri.parse(current.iconUri));
            } catch (Exception e) {
                fallbackIcon();
            }
        } else {
            fallbackIcon();
        }

        List<Sach> list = sachDao.listByTheLoai(tlId);
        sachAdapter.submit(list);
        tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void fallbackIcon() {
        int pad = (int) (14 * getResources().getDisplayMetrics().density);
        ivIcon.setPadding(pad, pad, pad, pad);
        ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ivIcon.setImageResource(R.drawable.ic_category_placeholder);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
