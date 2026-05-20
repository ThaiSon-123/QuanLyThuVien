package com.example.quanlythuvien.model;

public class NhanVien {
    public int nvId;
    public String ten;
    public String sdt;
    public String email;
    public String diachi;
    public String chucvu;
    public String ngayVaoLam;   // yyyy-MM-dd
    public String trangthai;    // "lamviec" | "nghi"
    public int userId;
    public String username;

    public String getMaNhanVien() {
        return String.format("NV - %03d", nvId);
    }

    public String getTrangthaiLabel() {
        return "nghi".equals(trangthai) ? "Đã nghỉ" : "Đang làm việc";
    }
}
