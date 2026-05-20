package com.example.quanlythuvien.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BookAccessPolicyTest {

    @Test
    public void adminCanOpenBookEditScreen() {
        assertTrue(BookAccessPolicy.canOpenBookEditScreen(RoleHelper.ROLE_ADMIN));
    }

    @Test
    public void employeeCannotOpenBookEditScreen() {
        assertFalse(BookAccessPolicy.canOpenBookEditScreen(RoleHelper.ROLE_NHAN_VIEN));
    }

    @Test
    public void deniedMessageMentionsBookEditingPermission() {
        assertEquals("Bạn không có quyền chỉnh sửa sách", BookAccessPolicy.deniedMessage());
    }
}
