package com.example.quanlythuvien.model;

public class BanDoc {
    public int bdId;
    public String ten;
    public String sdt;
    public String diachi;

    /** Stat đính kèm — chỉ set khi dùng listWithStats(). */
    public int sachGiu;

    public String getMaBanDoc() {
        return String.format("BD - %03d", bdId);
    }

    public BanDoc() {}

    public BanDoc(int bdId, String ten) {
        this.bdId = bdId;
        this.ten = ten;
    }

    @Override
    public String toString() {
        return ten;
    }
}
