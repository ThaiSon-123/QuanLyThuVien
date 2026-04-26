package com.example.quanlythuvien.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.MuonTraActivity;
import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.PhieuMuon;

import java.util.ArrayList;
import java.util.List;

public class LichSuMuonAdapter extends RecyclerView.Adapter<LichSuMuonAdapter.VH> {

    public interface OnItemClick {
        void onClick(PhieuMuon pm);
    }

    private final List<PhieuMuon> data = new ArrayList<>();
    private final OnItemClick listener;

    public LichSuMuonAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void submit(List<PhieuMuon> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lichsu_muon, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PhieuMuon p = data.get(position);
        h.tvMaPhieu.setText(p.getMaPhieu());
        h.tvSubLine.setText(MuonTraActivity.formatDate(p.ngayMuon) + " · " + p.soQuyen + " quyển");

        if ("datra".equals(p.trangthai)) {
            h.tvStatus.setVisibility(View.VISIBLE);
            h.tvStatus.setText("Đã trả");
            h.tvStatus.setTextColor(0xFF2F8A3E);
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_datra);
        } else if ("chuatra".equals(p.trangthai)) {
            h.tvStatus.setVisibility(View.VISIBLE);
            h.tvStatus.setText("Chưa trả");
            h.tvStatus.setTextColor(0xFFB9651A);
            h.tvStatus.setBackgroundResource(R.drawable.bg_status_chuatra);
        } else {
            h.tvStatus.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMaPhieu;
        TextView tvSubLine;
        TextView tvStatus;

        VH(@NonNull View v) {
            super(v);
            tvMaPhieu = v.findViewById(R.id.tvMaPhieu);
            tvSubLine = v.findViewById(R.id.tvSubLine);
            tvStatus = v.findViewById(R.id.tvStatus);
        }
    }
}
