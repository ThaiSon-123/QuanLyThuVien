package com.example.quanlythuvien.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class StaffRoleOptionsTest {

    @Test
    public void roleOptionsAreFixed() {
        assertArrayEquals(new String[]{"Quản lý", "Nhân viên"}, StaffRoleOptions.labels());
    }

    @Test
    public void unknownRoleDefaultsToEmployee() {
        assertEquals(1, StaffRoleOptions.indexOf("Thủ thư"));
    }

    @Test
    public void managerRoleSelectsManagerIndex() {
        assertEquals(0, StaffRoleOptions.indexOf("Quản lý"));
    }
}
