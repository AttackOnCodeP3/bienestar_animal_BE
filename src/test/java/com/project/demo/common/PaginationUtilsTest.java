package com.project.demo.common;

import com.project.demo.logic.entity.http.Meta;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class PaginationUtilsTest {

    @Test
    void buildPageable_translates1BasedTo0Based() {
        var pageable = PaginationUtils.buildPageable(1, 25);
        assertEquals(0, pageable.getPageNumber());
        assertEquals(25, pageable.getPageSize());
    }

    @Test
    void buildMeta_fillsNumbersAndUrl() {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/species");
        req.setServerName("localhost");
        req.setServerPort(80);

        Page<?> page = new PageImpl<>(java.util.List.of(1,2,3), PageRequest.of(0, 3), 3);

        Meta meta = PaginationUtils.buildMeta(req, page);

        assertEquals("GET", meta.getMethod());
        assertTrue(meta.getUrl().contains("/species"));
        assertEquals(1, meta.getPageNumber());
        assertEquals(3, meta.getPageSize());
        assertEquals(1, meta.getTotalPages());
        assertEquals(3, meta.getTotalElements());
    }
}
