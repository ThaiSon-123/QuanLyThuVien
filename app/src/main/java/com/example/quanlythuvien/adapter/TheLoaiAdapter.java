package com.example.quanlythuvien.adapter;

import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.TheLoai;

import java.util.ArrayList;
import java.util.List;

public class TheLoaiAdapter extends RecyclerView.Adapter<TheLoaiAdapter.VH> {

    public interface OnItemClick {
        void onClick(TheLoai tl);
    }

    private final List<TheLoai> data = new ArrayList<>();
    private final OnItemClick listener;

    public TheLoaiAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void submit(List<TheLoai> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theloai, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        TheLoai t = data.get(position);
        holder.tvTen.setText(t.ten);
        holder.tvCount.setText(t.bookCount + " cuốn sách");

        if (!TextUtils.isEmpty(t.iconUri)) {
            try {
                holder.ivIcon.setImageURI(Uri.parse(t.iconUri));
                holder.ivIcon.setPadding(0, 0, 0, 0);
                holder.ivIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } catch (Exception e) {
                fallback(holder);
            }
        } else {
            fallback(holder);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t);
        });
    }

    private void fallback(VH holder) {
        int pad = (int) (18 * holder.itemView.getResources().getDisplayMetrics().density);
        holder.ivIcon.setPadding(pad, pad, pad, pad);
        holder.ivIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        holder.ivIcon.setImageResource(R.drawable.ic_category_placeholder);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTen;
        TextView tvCount;

        VH(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTen = itemView.findViewById(R.id.tvTen);
            tvCount = itemView.findViewById(R.id.tvCount);
        }
    }
}
