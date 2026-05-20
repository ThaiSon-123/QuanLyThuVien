package com.example.quanlythuvien.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;

import java.util.ArrayList;
import java.util.List;

public class PhieuAdapter extends RecyclerView.Adapter<PhieuAdapter.VH> {

    public static class Row {
        public final int id;
        public final String ma;
        public final String tenBanDoc;
        public final String ngay;
        public final String traCho;

        public final String status;

        public Row(int id, String ma, String tenBanDoc, String ngay) {
            this(id, ma, tenBanDoc, ngay, null, null);
        }

        public Row(int id, String ma, String tenBanDoc, String ngay, String traCho) {
            this(id, ma, tenBanDoc, ngay, traCho, null);
        }

        public Row(int id, String ma, String tenBanDoc, String ngay, String traCho, String status) {
            this.id = id;
            this.ma = ma;
            this.tenBanDoc = tenBanDoc;
            this.ngay = ngay;
            this.traCho = traCho;
            this.status = status;
        }
    }

    public interface OnItemClick {
        void onClick(Row row);
    }

    private final List<Row> data = new ArrayList<>();
    private final OnItemClick listener;

    public PhieuAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void submit(List<Row> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_phieu, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Row r = data.get(position);
        holder.tvMaPhieu.setText(r.ma);
        holder.tvTenBanDoc.setText(r.tenBanDoc == null ? "" : r.tenBanDoc);
        holder.tvNgay.setText(r.ngay == null ? "" : r.ngay);

        if (r.traCho == null || r.traCho.isEmpty()) {
            holder.rowTraCho.setVisibility(View.GONE);
        } else {
            holder.rowTraCho.setVisibility(View.VISIBLE);
            holder.tvTraCho.setText("TRẢ CHO: " + r.traCho);
        }

        if (r.status == null) {
            holder.tvStatus.setVisibility(View.GONE);
        } else if ("datra".equals(r.status)) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Đã trả");
            holder.tvStatus.setTextColor(0xFF2F8A3E);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_datra);
        } else if ("dunghạn".equals(r.status)) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Đúng hạn");
            holder.tvStatus.setTextColor(0xFF2F8A3E);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_datra);
        } else if ("trehan".equals(r.status)) {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Quá hạn");
            holder.tvStatus.setTextColor(0xFFFFFFFF);
            GradientDrawable red = new GradientDrawable();
            red.setShape(GradientDrawable.RECTANGLE);
            red.setCornerRadius(24f);
            red.setColor(0xFFBA1A1A);
            holder.tvStatus.setBackground(red);
        } else {
            holder.tvStatus.setVisibility(View.VISIBLE);
            holder.tvStatus.setText("Chưa trả");
            holder.tvStatus.setTextColor(0xFFB9651A);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_chuatra);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMaPhieu;
        TextView tvTenBanDoc;
        TextView tvNgay;
        LinearLayout rowTraCho;
        TextView tvTraCho;
        TextView tvStatus;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMaPhieu = itemView.findViewById(R.id.tvMaPhieu);
            tvTenBanDoc = itemView.findViewById(R.id.tvTenBanDoc);
            tvNgay = itemView.findViewById(R.id.tvNgay);
            rowTraCho = itemView.findViewById(R.id.rowTraCho);
            tvTraCho = itemView.findViewById(R.id.tvTraCho);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
