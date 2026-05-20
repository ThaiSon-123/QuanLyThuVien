package com.example.quanlythuvien.util;

import android.content.Context;

import com.example.quanlythuvien.db.CauHinhDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** Tính tiền phạt khi trả sách trễ hạn. */
public class FineCalculator {

    /**
     * Đơn giá phạt mặc định (VND/ngày). Sử dụng khi không có Context để lấy CauHinhDao.
     * Khi có Context, ưu tiên gọi {@link #finePerDay(Context)}.
     */
    public static final double FINE_PER_DAY = 500;

    /** Đơn giá phạt từ cấu hình hệ thống (đọc qua CauHinhDao). */
    public static double finePerDay(Context ctx) {
        return CauHinhDao.getInstance(ctx).finePerDay();
    }

    private static final SimpleDateFormat ISO =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    /**
     * Số ngày trễ = max(0, ngayTraThucTe - ngayHanTra).
     * Trả về 0 nếu một trong hai ngày null/sai định dạng.
     */
    public static int daysOverdue(String ngayHanTra, String ngayTraThucTe) {
        if (ngayHanTra == null || ngayTraThucTe == null) return 0;
        try {
            Date han = ISO.parse(ngayHanTra);
            Date tra = ISO.parse(ngayTraThucTe);
            if (han == null || tra == null) return 0;
            long diff = tra.getTime() - han.getTime();
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            return days > 0 ? (int) days : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    /** Tổng tiền phạt = số ngày trễ × FINE_PER_DAY (mặc định). */
    public static double calcFine(String ngayHanTra, String ngayTraThucTe) {
        return daysOverdue(ngayHanTra, ngayTraThucTe) * FINE_PER_DAY;
    }

    /** Tổng tiền phạt theo đơn giá truyền vào. */
    public static double calcFine(String ngayHanTra, String ngayTraThucTe, double finePerDay) {
        return daysOverdue(ngayHanTra, ngayTraThucTe) * finePerDay;
    }

    /** Tổng tiền phạt theo đơn giá và số lượng sách bị trễ hạn. */
    public static double calcFine(String ngayHanTra, String ngayTraThucTe,
                                  double finePerDay, int bookQuantity) {
        return daysOverdue(ngayHanTra, ngayTraThucTe) * finePerDay * Math.max(0, bookQuantity);
    }

    /** Tổng tiền phạt theo đơn giá lấy từ cấu hình. */
    public static double calcFine(Context ctx, String ngayHanTra, String ngayTraThucTe) {
        return calcFine(ngayHanTra, ngayTraThucTe, finePerDay(ctx));
    }

    /** Tổng tiền phạt theo cấu hình và số lượng sách bị trễ hạn. */
    public static double calcFine(Context ctx, String ngayHanTra, String ngayTraThucTe,
                                  int bookQuantity) {
        return calcFine(ngayHanTra, ngayTraThucTe, finePerDay(ctx), bookQuantity);
    }

    /** Hôm nay theo định dạng yyyy-MM-dd. */
    public static String today() {
        return ISO.format(new Date());
    }

    /** Format số tiền VND có chấm phân cách: "2.500 đ". */
    public static String formatVnd(double amount) {
        long v = Math.round(amount);
        StringBuilder sb = new StringBuilder();
        String s = String.valueOf(v);
        int n = s.length();
        for (int i = 0; i < n; i++) {
            if (i > 0 && (n - i) % 3 == 0) sb.append('.');
            sb.append(s.charAt(i));
        }
        sb.append(" đ");
        return sb.toString();
    }
}
