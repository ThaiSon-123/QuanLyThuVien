package com.example.quanlythuvien.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.example.quanlythuvien.R;

public final class ConfirmDialog {

    private ConfirmDialog() {}

    public static void show(Context ctx, String message,
                            String confirmText, Runnable onConfirm) {
        View view = LayoutInflater.from(ctx).inflate(R.layout.dialog_confirm, null, false);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnConfirm = view.findViewById(R.id.btnConfirm);

        tvMessage.setText(message);
        if (confirmText != null) btnConfirm.setText(confirmText);

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setView(view)
                .setCancelable(true)
                .create();

        Window w = dialog.getWindow();
        if (w != null) w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            if (onConfirm != null) onConfirm.run();
        });
        dialog.show();
    }

    public static void showDelete(Context ctx, Runnable onConfirm) {
        show(ctx, "Bạn có chắc chắn muốn xóa không?", "Xóa", onConfirm);
    }
}
