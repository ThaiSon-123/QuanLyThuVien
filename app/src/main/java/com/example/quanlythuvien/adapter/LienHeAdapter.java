package com.example.quanlythuvien.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.BanDoc;

import java.util.ArrayList;
import java.util.List;

public class LienHeAdapter extends RecyclerView.Adapter<LienHeAdapter.VH> {

    public interface OnCall {
        void onCall(BanDoc bd);
    }

    private final List<BanDoc> data = new ArrayList<>();
    private final OnCall listener;

    public LienHeAdapter(OnCall listener) {
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
                .inflate(R.layout.item_lienhe, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        BanDoc b = data.get(position);
        h.tvTen.setText(b.ten == null ? "" : b.ten);
        h.tvSdt.setText(TextUtils.isEmpty(b.sdt) ? "(chưa có)" : b.sdt);
        h.btnCall.setEnabled(!TextUtils.isEmpty(b.sdt));
        h.btnCall.setAlpha(TextUtils.isEmpty(b.sdt) ? 0.4f : 1f);
        h.btnCall.setOnClickListener(v -> {
            if (listener != null && !TextUtils.isEmpty(b.sdt)) listener.onCall(b);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTen;
        TextView tvSdt;
        ImageView btnCall;

        VH(@NonNull View v) {
            super(v);
            tvTen = v.findViewById(R.id.tvTen);
            tvSdt = v.findViewById(R.id.tvSdt);
            btnCall = v.findViewById(R.id.btnCall);
        }
    }
}
