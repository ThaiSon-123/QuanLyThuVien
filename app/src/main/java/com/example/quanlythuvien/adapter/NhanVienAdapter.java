package com.example.quanlythuvien.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.NhanVien;

import java.util.ArrayList;
import java.util.List;

public class NhanVienAdapter extends RecyclerView.Adapter<NhanVienAdapter.VH> {

    public interface OnItemClick {
        void onClick(NhanVien nv);
    }

    private final List<NhanVien> data = new ArrayList<>();
    private final OnItemClick listener;

    public NhanVienAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void submit(List<NhanVien> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nhanvien, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NhanVien n = data.get(position);
        h.tvTen.setText(n.ten == null ? "" : n.ten);
        h.tvSdt.setText("SDT: " + (TextUtils.isEmpty(n.sdt) ? "(chưa có)" : n.sdt));
        h.tvChucVu.setText(TextUtils.isEmpty(n.chucvu) ? "Nhân viên" : n.chucvu);
        h.tvDiaChi.setText(TextUtils.isEmpty(n.diachi) ? "—" : n.diachi);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(n);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTen;
        TextView tvSdt;
        TextView tvChucVu;
        TextView tvDiaChi;

        VH(@NonNull View v) {
            super(v);
            tvTen = v.findViewById(R.id.tvTen);
            tvSdt = v.findViewById(R.id.tvSdt);
            tvChucVu = v.findViewById(R.id.tvChucVu);
            tvDiaChi = v.findViewById(R.id.tvDiaChi);
        }
    }
}
