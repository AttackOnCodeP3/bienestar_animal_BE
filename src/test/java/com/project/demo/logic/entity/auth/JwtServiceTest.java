package com.project.demo.logic.entity.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void getTokenFromHeader_extractsBearerToken() {
        JwtService svc = new JwtService();
        assertEquals("abc.def.ghi", svc.getTokenFromHeader("Bearer abc.def.ghi"));
    }

    @Test
    void getTokenFromHeader_returnsNullWhenHeaderInvalid() {
        JwtService svc = new JwtService();
        assertNull(svc.getTokenFromHeader(null));
        assertNull(svc.getTokenFromHeader("Basic xyz"));
        assertNull(svc.getTokenFromHeader("Bearer"));
    }
}