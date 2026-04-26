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

public class PhieuMuonPickAdapter extends RecyclerView.Adapter<PhieuMuonPickAdapter.VH> {

    public interface OnPick {
        void onPick(PhieuMuon pm);
    }

    private final List<PhieuMuon> data = new ArrayList<>();
    private final OnPick listener;

    public PhieuMuonPickAdapter(OnPick listener) {
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
                .inflate(R.layout.item_phieumuon_pick, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PhieuMuon p = data.get(position);
        h.tvMaPhieu.setText(p.getMaPhieu());
        h.tvSubLine.setText("Ngày mượn " + MuonTraActivity.formatDate(p.ngayMuon)
                + " - Bạn đọc: " + (p.tenBanDoc == null ? "" : p.tenBanDoc));
        h.btnPick.setOnClickListener(v -> {
            if (listener != null) listener.onPick(p);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvMaPhieu;
        TextView tvSubLine;
        View btnPick;

        VH(@NonNull View v) {
            super(v);
            tvMaPhieu = v.findViewById(R.id.tvMaPhieu);
            tvSubLine = v.findViewById(R.id.tvSubLine);
            btnPick = v.findViewById(R.id.btnPick);
        }
    }
}
