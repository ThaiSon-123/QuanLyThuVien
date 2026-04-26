package com.example.quanlythuvien.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.BanDoc;

import java.util.ArrayList;
import java.util.List;

public class BanDocAdapter extends RecyclerView.Adapter<BanDocAdapter.VH> {

    public interface OnItemClick {
        void onClick(BanDoc bd);
    }

    private final List<BanDoc> data = new ArrayList<>();
    private final OnItemClick listener;

    public BanDocAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void submit(List<BanDoc> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bandoc, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BanDoc b = data.get(position);
        h.tvTen.setText(b.ten == null ? "" : b.ten);
        h.tvSdt.setText("SDT: " + (TextUtils.isEmpty(b.sdt) ? "(chưa có)" : b.sdt));
        h.tvSachGiu.setText(b.sachGiu + " SÁCH");
        h.tvDiaChi.setText(TextUtils.isEmpty(b.diachi) ? "—" : b.diachi);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(b);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTen;
        TextView tvSdt;
        TextView tvSachGiu;
        TextView tvDiaChi;

        VH(@NonNull View v) {
            super(v);
            tvTen = v.findViewById(R.id.tvTen);
            tvSdt = v.findViewById(R.id.tvSdt);
            tvSachGiu = v.findViewById(R.id.tvSachGiu);
            tvDiaChi = v.findViewById(R.id.tvDiaChi);
        }
    }
}
