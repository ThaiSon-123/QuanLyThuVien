package com.example.quanlythuvien.model;

public class TheLoai {
    public int tlId;
    public String ten;
    public String iconUri;
    public int bookCount;

    public TheLoai() {
    }

    public TheLoai(int tlId, String ten) {
        this.tlId = tlId;
        this.ten = ten;
    }

    public TheLoai(int tlId, String ten, String iconUri) {
        this.tlId = tlId;
        this.ten = ten;
        this.iconUri = iconUri;
    }

    @Override
    public String toString() {
        return ten;
    }
}
