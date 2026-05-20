package com.example.quanlythuvien.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BorrowStockPolicyTest {

    @Test
    public void allowsAddingWhenSelectedQuantityIsBelowAvailableStock() {
        assertTrue(BorrowStockPolicy.canAddOneMore(2, 3));
    }

    @Test
    public void rejectsAddingWhenSelectedQuantityEqualsAvailableStock() {
        assertFalse(BorrowStockPolicy.canAddOneMore(3, 3));
    }

    @Test
    public void rejectsAddingWhenNoStockIsAvailable() {
        assertFalse(BorrowStockPolicy.canAddOneMore(0, 0));
    }
}
