package com.example.quanlythuvien.adapter;

import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanlythuvien.R;
import com.example.quanlythuvien.model.Sach;

import java.util.ArrayList;
import java.util.List;

public class SachAdapter extends RecyclerView.Adapter<SachAdapter.VH> {

    public interface OnItemClick {
        void onClick(Sach sach);
    }

    private final List<Sach> data = new ArrayList<>();
    private final OnItemClick listener;

    public SachAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void submit(List<Sach> list) {
        data.clear();
        if (list != null) {
            data.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sach, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Sach s = data.get(position);
        holder.tvTen.setText(s.ten);

        if (!TextUtils.isEmpty(s.coverUri)) {
            try {
                holder.ivCover.setImageURI(Uri.parse(s.coverUri));
            } catch (Exception e) {
                holder.ivCover.setImageResource(R.drawable.ic_book_placeholder);
            }
        } else {
            holder.ivCover.setImageResource(R.drawable.ic_book_placeholder);
        }

        String prefix = "Có thể mượn : ";
        String number = String.valueOf(s.soluong);
        SpannableString sp = new SpannableString(prefix + number);
        sp.setSpan(new ForegroundColorSpan(0xFF4696EB),
                prefix.length(), prefix.length() + number.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        holder.tvSoLuong.setText(sp);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(s);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTen;
        TextView tvSoLuong;

        VH(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTen = itemView.findViewById(R.id.tvTen);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
        }
    }
}
