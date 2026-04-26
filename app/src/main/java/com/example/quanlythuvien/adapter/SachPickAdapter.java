package com.example.quanlythuvien.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.Sach;

import java.util.ArrayList;
import java.util.List;

/** Danh sách sách có thể mượn — nút + để chọn. */
public class SachPickAdapter extends RecyclerView.Adapter<SachPickAdapter.VH> {

    public interface OnAdd {
        void onAdd(Sach sach);
    }

    private final List<Sach> data = new ArrayList<>();
    private final OnAdd listener;

    public SachPickAdapter(OnAdd listener) {
        this.listener = listener;
    }

    public void submit(List<Sach> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_sach_pick, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Sach s = data.get(position);
        h.tvTen.setText(s.ten);
        h.btnAdd.setOnClickListener(v -> {
            if (listener != null) listener.onAdd(s);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTen;
        View btnAdd;

        VH(@NonNull View v) {
            super(v);
            tvTen = v.findViewById(R.id.tvTen);
            btnAdd = v.findViewById(R.id.btnAdd);
        }
    }
}
