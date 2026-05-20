package com.example.quanlythuvien.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StaffAccountValidatorTest {

    @Test
    public void createRequiresUsernameEmailAndPassword() {
        assertFalse(StaffAccountValidator.isValidForCreate("", "nv@example.com", "123"));
        assertFalse(StaffAccountValidator.isValidForCreate("nv3", "", "123"));
        assertFalse(StaffAccountValidator.isValidForCreate("nv3", "nv@example.com", ""));

        assertTrue(StaffAccountValidator.isValidForCreate("nv3", "nv@example.com", "123"));
    }

    @Test
    public void updateRequiresUsernameAndEmailButAllowsBlankPassword() {
        assertFalse(StaffAccountValidator.isValidForUpdate("", "nv@example.com", ""));
        assertFalse(StaffAccountValidator.isValidForUpdate("nv3", "", ""));

        assertTrue(StaffAccountValidator.isValidForUpdate("nv3", "nv@example.com", ""));
        assertTrue(StaffAccountValidator.isValidForUpdate("nv3", "nv@example.com", "new-pass"));
    }

    @Test
    public void blankPasswordDoesNotRequestPasswordUpdate() {
        assertFalse(StaffAccountValidator.shouldUpdatePassword(""));
        assertFalse(StaffAccountValidator.shouldUpdatePassword("   "));
        assertTrue(StaffAccountValidator.shouldUpdatePassword("123456"));
    }
}
