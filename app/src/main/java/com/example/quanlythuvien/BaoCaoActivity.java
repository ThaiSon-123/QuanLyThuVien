package com.example.quanlythuvien;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.db.UserDao;

import java.util.ArrayList;
import java.util.List;

public class BaoCaoActivity extends AppCompatActivity {

    private static class ReportItem {
        final String title;
        final String subtitle;
        final String type;

        ReportItem(String t, String s, String type) {
            this.title = t;
            this.subtitle = s;
            this.type = type;
        }
    }

    private TextView tvUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baocao);

        tvUserName = findViewById(R.id.tvUserName);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadUserName();
        setupRecycler();
    }

    private void loadUserName() {
        String username = getSharedPreferences(LoginActivity.PREFS_NAME, MODE_PRIVATE)
                .getString(LoginActivity.KEY_USERNAME, "");
        String displayName = new UserDao(this).findStaffName(username);
        if (displayName == null || displayName.isEmpty()) displayName = username;
        tvUserName.setText(displayName == null ? "" : displayName);
    }

    private void setupRecycler() {
        RecyclerView rv = findViewById(R.id.rvBaoCao);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<ReportItem> items = new ArrayList<>();
        items.add(new ReportItem("Thống kê mượn theo thể loại",
                "Số lượt mượn theo từng thể loại",
                BaoCaoChiTietActivity.TYPE_BY_THELOAI));
        items.add(new ReportItem("Top sách được mượn",
                "Sách được mượn nhiều nhất trong tháng",
                BaoCaoChiTietActivity.TYPE_BY_SACH));
        items.add(new ReportItem("Top độc giả tích cực",
                "Bạn đọc mượn nhiều nhất trong tháng",
                BaoCaoChiTietActivity.TYPE_BY_BANDOC));
        items.add(new ReportItem("Tình hình trả sách",
                "Số phiếu đã trả / chưa trả trong tháng",
                BaoCaoChiTietActivity.TYPE_TRA_STATUS));

        rv.setAdapter(new Adapter(this, items));
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final BaoCaoActivity ctx;
        private final List<ReportItem> data;

        Adapter(BaoCaoActivity ctx, List<ReportItem> data) {
            this.ctx = ctx;
            this.data = data;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_baocao, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            ReportItem r = data.get(position);
            h.tvTitle.setText(r.title);
            h.tvSubtitle.setText(r.subtitle);
            h.itemView.setOnClickListener(v -> {
                Intent i = new Intent(ctx, BaoCaoChiTietActivity.class);
                i.putExtra(BaoCaoChiTietActivity.EXTRA_TYPE, r.type);
                i.putExtra(BaoCaoChiTietActivity.EXTRA_TITLE, r.title);
                ctx.startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTitle;
            TextView tvSubtitle;

            VH(@NonNull View v) {
                super(v);
                tvTitle = v.findViewById(R.id.tvTitle);
                tvSubtitle = v.findViewById(R.id.tvSubtitle);
            }
        }
    }
}
