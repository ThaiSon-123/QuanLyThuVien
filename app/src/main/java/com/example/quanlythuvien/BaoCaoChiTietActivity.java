package com.example.quanlythuvien;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.db.BaoCaoDao;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BaoCaoChiTietActivity extends AppCompatActivity {

    public static final String EXTRA_TYPE = "type";
    public static final String EXTRA_TITLE = "title";

    /** Theo thể loại (mặc định). */
    public static final String TYPE_BY_THELOAI = "by_theloai";
    /** Top sách được mượn. */
    public static final String TYPE_BY_SACH = "by_sach";
    /** Top bạn đọc. */
    public static final String TYPE_BY_BANDOC = "by_bandoc";
    /** Tình hình trả: đã trả / chưa trả. */
    public static final String TYPE_TRA_STATUS = "tra_status";

    private BaoCaoDao baoCaoDao;
    private TextView tvHeaderTitle;
    private TextView tvSummaryTitle;
    private TextView tvMonth;
    private TextView tvTotal;
    private TextView tvEmpty;
    private Adapter adapter;

    private final Calendar selectedMonth = Calendar.getInstance();
    private String type = TYPE_BY_THELOAI;
    private String customTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baocao_chitiet);

        baoCaoDao = new BaoCaoDao(this);

        type = getIntent().getStringExtra(EXTRA_TYPE);
        if (type == null) type = TYPE_BY_THELOAI;
        customTitle = getIntent().getStringExtra(EXTRA_TITLE);

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle);
        tvMonth = findViewById(R.id.tvMonth);
        tvTotal = findViewById(R.id.tvTotal);
        tvEmpty = findViewById(R.id.tvEmpty);

        // Default tháng có data seed
        selectedMonth.set(2021, Calendar.MARCH, 1);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.monthRow).setOnClickListener(v -> showMonthPicker());

        RecyclerView rv = findViewById(R.id.rvTheLoai);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter();
        rv.setAdapter(adapter);

        applyTitles();
        refresh();
    }

    private void applyTitles() {
        if (!TextUtils.isEmpty(customTitle)) {
            tvHeaderTitle.setText(customTitle);
        }
        switch (type) {
            case TYPE_BY_SACH:
                tvSummaryTitle.setText("Top sách được mượn nhiều nhất");
                break;
            case TYPE_BY_BANDOC:
                tvSummaryTitle.setText("Top bạn đọc mượn nhiều nhất");
                break;
            case TYPE_TRA_STATUS:
                tvSummaryTitle.setText("Tình hình trả sách trong tháng");
                break;
            case TYPE_BY_THELOAI:
            default:
                tvSummaryTitle.setText("Thống kê tình hình mượn sách theo thể loại");
                break;
        }
    }

    private void refresh() {
        String yyyyMM = new SimpleDateFormat("yyyy-MM", Locale.getDefault())
                .format(selectedMonth.getTime());
        String label = new SimpleDateFormat("MM/yyyy", Locale.getDefault())
                .format(selectedMonth.getTime());
        tvMonth.setText(label);

        int total = baoCaoDao.totalLuotMuonInMonth(yyyyMM);
        tvTotal.setText("Tổng số lượt mượn : " + total);

        List<BaoCaoDao.TheLoaiStat> rows = loadRowsForType(yyyyMM);
        adapter.submit(rows, getRowSuffix());
        tvEmpty.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private List<BaoCaoDao.TheLoaiStat> loadRowsForType(String yyyyMM) {
        switch (type) {
            case TYPE_BY_SACH:
                return baoCaoDao.topSachInMonth(yyyyMM);
            case TYPE_BY_BANDOC:
                return baoCaoDao.topBanDocInMonth(yyyyMM);
            case TYPE_TRA_STATUS: {
                List<BaoCaoDao.TheLoaiStat> list = new ArrayList<>();
                BaoCaoDao.TheLoaiStat a = new BaoCaoDao.TheLoaiStat();
                a.ten = "Đã trả";
                a.luotMuon = baoCaoDao.countDaTraInMonth(yyyyMM);
                list.add(a);
                BaoCaoDao.TheLoaiStat b = new BaoCaoDao.TheLoaiStat();
                b.ten = "Chưa trả";
                b.luotMuon = baoCaoDao.countChuaTraInMonth(yyyyMM);
                list.add(b);
                return list;
            }
            case TYPE_BY_THELOAI:
            default:
                return baoCaoDao.luotMuonByTheLoaiInMonth(yyyyMM);
        }
    }

    private String getRowSuffix() {
        return TYPE_TRA_STATUS.equals(type) ? "phiếu" : "lượt mượn";
    }

    private void showMonthPicker() {
        DatePickerDialog d = new DatePickerDialog(this,
                (DatePicker view, int y, int m, int day) -> {
                    selectedMonth.set(y, m, 1);
                    refresh();
                },
                selectedMonth.get(Calendar.YEAR),
                selectedMonth.get(Calendar.MONTH),
                1);
        d.show();
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        private final List<BaoCaoDao.TheLoaiStat> data = new ArrayList<>();
        private String suffix = "lượt mượn";

        void submit(List<BaoCaoDao.TheLoaiStat> list, String suffix) {
            data.clear();
            if (list != null) data.addAll(list);
            this.suffix = suffix == null ? "lượt mượn" : suffix;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_thongke_theloai, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            BaoCaoDao.TheLoaiStat s = data.get(position);
            h.tvTen.setText(s.ten == null ? "" : s.ten);
            h.tvSoLuot.setText(String.valueOf(s.luotMuon));
            h.tvLabel.setText(s.luotMuon + " " + suffix);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvTen;
            TextView tvSoLuot;
            TextView tvLabel;

            VH(@NonNull View v) {
                super(v);
                tvTen = v.findViewById(R.id.tvTen);
                tvSoLuot = v.findViewById(R.id.tvSoLuot);
                tvLabel = v.findViewById(R.id.tvLuotMuonLabel);
            }
        }
    }
}
