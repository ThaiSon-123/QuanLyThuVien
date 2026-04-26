package com.example.quanlythuvien.model;

public class ChiTietMuon {
    public int id;
    public int pmId;
    public int sachId;
    public int soluong;

    // Join
    public String tenSach;

    public ChiTietMuon() {}

    public ChiTietMuon(int sachId, String tenSach, int soluong) {
        this.sachId = sachId;
        this.tenSach = tenSach;
        this.soluong = soluong;
    }
}
