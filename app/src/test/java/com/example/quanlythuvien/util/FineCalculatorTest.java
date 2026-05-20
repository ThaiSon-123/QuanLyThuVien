package com.example.quanlythuvien.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FineCalculatorTest {

    @Test
    public void calcFineUsesCustomFinePerDay() {
        double fine = FineCalculator.calcFine("2026-05-09", "2026-05-12", 750);

        assertEquals(2250, fine, 0.0);
    }
}
