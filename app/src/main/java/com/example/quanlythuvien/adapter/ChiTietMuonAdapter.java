package com.example.quanlythuvien.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.ChiTietMuon;

import java.util.ArrayList;
import java.util.List;

public class ChiTietMuonAdapter extends RecyclerView.Adapter<ChiTietMuonAdapter.VH> {

    private final List<ChiTietMuon> data = new ArrayList<>();

    public void submit(List<ChiTietMuon> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chitiet_muon, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ChiTietMuon c = data.get(position);
        holder.tvTenSach.setText(c.tenSach == null ? "" : c.tenSach);
        holder.tvSoLuong.setText("x" + c.soluong);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTenSach;
        TextView tvSoLuong;

        VH(@NonNull View itemView) {
            super(itemView);
            tvTenSach = itemView.findViewById(R.id.tvTenSach);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
        }
    }
}
