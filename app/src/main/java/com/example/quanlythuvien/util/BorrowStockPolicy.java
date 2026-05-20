package com.example.quanlythuvien.util;

public class BorrowStockPolicy {

    private BorrowStockPolicy() {}

    public static boolean canAddOneMore(int selectedQuantity, int availableStock) {
        return availableStock > 0 && selectedQuantity < availableStock;
    }
}
