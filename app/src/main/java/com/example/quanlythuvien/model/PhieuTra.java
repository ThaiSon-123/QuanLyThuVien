package com.example.quanlythuvien.model;

import java.util.ArrayList;
import java.util.List;

public class PhieuTra {
    public int ptId;
    public int pmId;
    public String ngayTra;  // yyyy-MM-dd
    public double tienphat;

    // Join fields (từ PhieuMuon)
    public String tenBanDoc;
    public String ngayMuon;

    public List<ChiTietMuon> chiTiet = new ArrayList<>();

    public String getMaPhieu() {
        return "PT-" + ptId;
    }
}
