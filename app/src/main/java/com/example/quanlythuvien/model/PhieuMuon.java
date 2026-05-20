package com.example.quanlythuvien.model;

import java.util.ArrayList;
import java.util.List;

public class PhieuMuon {
    public int pmId;
    public int bdId;
    public int nvId;
    public String ngayMuon;     // yyyy-MM-dd
    public String ngayTra;      // yyyy-MM-dd (hạn trả hiện tại; có thể đã được gia hạn)
    public String ngayTraGoc;   // yyyy-MM-dd (hạn trả gốc khi lập phiếu, để audit)
    public int lanGiaHan;       // số lần đã gia hạn (max 2)
    public String trangthai;    // "dangmuon" | "datra"
    public int songaytre;
    public double tienphat;

    // Join fields
    public String tenBanDoc;
    public String tenNhanVien;
    /** Tổng số quyển trên phiếu (khi DAO query có aggregate). */
    public int soQuyen;

    public List<ChiTietMuon> chiTiet = new ArrayList<>();

    public String getMaPhieu() {
        return "PM-" + pmId;
    }
}
