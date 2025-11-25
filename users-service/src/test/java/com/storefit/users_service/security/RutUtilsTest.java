package com.storefit.users_service.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class RutUtilsTest {

    @Test
    void detectsDottedFormat() {
        assertTrue(RutUtils.isDottedFormat("12.345.678-9"));
        assertFalse(RutUtils.isDottedFormat("12345678-9"));
    }

    @Test
    void toCanonicalDottedAddsDots() {
        assertEquals("12.345.678-9", RutUtils.toCanonicalDotted("12345678-9"));
    }

    @Test
    void requireDottedThrowsOnInvalid() {
        assertThrows(ResponseStatusException.class, () -> RutUtils.requireDottedOrBadRequest("12345678-9"));
    }
}
