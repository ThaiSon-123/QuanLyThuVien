package com.example.quanlythuvien.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.ChiTietMuon;

import java.util.ArrayList;
import java.util.List;

/**
 * Hiển thị các sách đã chọn vào phiếu.
 * - mode = MODE_BORROW (chi tiết phiếu mượn): hiển thị "xN" + nút xóa.
 * - mode = MODE_RETURN (sách đã mượn của phiếu trả): hiển thị "Số lượng N" màu xanh lá.
 */
public class SachSelectedAdapter extends RecyclerView.Adapter<SachSelectedAdapter.VH> {

    public static final int MODE_BORROW = 0;
    public static final int MODE_RETURN = 1;

    public interface OnRemove {
        void onRemove(ChiTietMuon ct);
    }

    private final int mode;
    private final List<ChiTietMuon> data = new ArrayList<>();
    private final OnRemove listener;

    public SachSelectedAdapter(int mode, OnRemove listener) {
        this.mode = mode;
        this.listener = listener;
    }

    public void submit(List<ChiTietMuon> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sach_selected, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ChiTietMuon ct = data.get(position);
        h.tvTen.setText(ct.tenSach == null ? "" : ct.tenSach);
        if (mode == MODE_RETURN) {
            h.tvSoLuong.setVisibility(View.VISIBLE);
            h.tvSoLuong.setText("Số lượng " + ct.soluong);
            h.tvQty.setVisibility(View.GONE);
            h.btnRemove.setVisibility(View.GONE);
        } else {
            h.tvSoLuong.setVisibility(View.GONE);
            h.tvQty.setVisibility(View.VISIBLE);
            h.tvQty.setText("x" + ct.soluong);
            h.btnRemove.setVisibility(listener != null ? View.VISIBLE : View.GONE);
            h.btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemove(ct);
            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTen;
        TextView tvSoLuong;
        TextView tvQty;
        ImageView btnRemove;

        VH(@NonNull View v) {
            super(v);
            tvTen = v.findViewById(R.id.tvTen);
            tvSoLuong = v.findViewById(R.id.tvSoLuong);
            tvQty = v.findViewById(R.id.tvQty);
            btnRemove = v.findViewById(R.id.btnRemove);
        }
    }
}
